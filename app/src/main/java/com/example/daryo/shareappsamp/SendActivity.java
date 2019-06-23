package com.example.daryo.shareappsamp;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.daryo.shareappsamp.adapters.PeerAdapter;
import com.example.daryo.shareappsamp.interfaces.FileReceivedCallback;
import com.example.daryo.shareappsamp.interfaces.ItemClickListener;
import com.example.daryo.shareappsamp.utils.Item;
import com.example.daryo.shareappsamp.wifip2p.FileServerAsyncTask;
import com.example.daryo.shareappsamp.wifip2p.SendFileAsyncTask;
import com.example.daryo.shareappsamp.wifip2p.WiFiDirectBroadcastReceiver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class SendActivity extends AppCompatActivity implements WifiP2pManager.PeerListListener, ItemClickListener, WifiP2pManager.ConnectionInfoListener, FileReceivedCallback {

    private TextView tvStatus;
    private RecyclerView rvPeerList;
    private ProgressBar pbLoading;

    private PeerAdapter peerAdapter;

    //wifip2p
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();

    private ArrayList<Item> chosenFiles = new ArrayList<Item>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        chosenFiles = (ArrayList<Item>) getIntent().getSerializableExtra("paths");

        tvStatus = findViewById(R.id.tvStatus);
        rvPeerList = findViewById(R.id.rvPeerList);
        pbLoading = findViewById(R.id.progressBar);

        //needed this for notifyDataSetChanged to work
        rvPeerList.setLayoutManager(new LinearLayoutManager(this));

        peerAdapter = new PeerAdapter(this, peers);
        peerAdapter.setClickListener(this);
        rvPeerList.setAdapter(peerAdapter);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        discoverPeers();

    }

    private void discoverPeers(){

        //can put loading here or visible loading to get peers
        Log.e(MainActivity.TAG, "Start discovering peers");

        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.e(MainActivity.TAG, "discoverPeers success");
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.e(MainActivity.TAG, "discoverPeers failed reason:" + reasonCode);
                pbLoading.setVisibility(View.GONE);
                tvStatus.setText("Error discovering peers. Error code:" + reasonCode);
            }
        });
    }


    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    /*
     *success geting peer from WIFI_P2P_PEERS_CHANGED_ACTION
     */
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        Log.e(MainActivity.TAG, "onPeersAvailable called");
        Log.e(MainActivity.TAG, "getDottedDecimalIP " + getDottedDecimalIP(getLocalIPAddress()));
        peers.clear();

        peers.addAll(peerList.getDeviceList());

        if (peers.size() == 0) {
            tvStatus.setText("No devices found...");
            Log.e(MainActivity.TAG, "No devices found");
        }else {
            Log.e(MainActivity.TAG, "found: " + peers.size() + " devices");

            peerAdapter.notifyDataSetChanged();
            pbLoading.setVisibility(View.GONE);
            tvStatus.setText("Found: " + peers.size() + " devices");
        }

    }

    @Override
    public void onItemClick(final int position) {
        //obtain a peer from the WifiP2pDeviceList
        WifiP2pConfig config = new WifiP2pConfig();
        config.groupOwnerIntent = 0;
        config.deviceAddress = peers.get(position).deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //success logic
                Log.e(MainActivity.TAG, "success connecting to " + peers.get(position).deviceName);
                Log.e(MainActivity.TAG, "getDottedDecimalIP " + getDottedDecimalIP(getLocalIPAddress()));

            }

            @Override
            public void onFailure(int reason) {
                //failure logic
                Log.e(MainActivity.TAG, "failed connecting to "  + peers.get(position).deviceName);

            }
        });
    }

    private byte[] getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
                            return inetAddress.getAddress();
                        }
                        //return inetAddress.getHostAddress().toString(); // Galaxy Nexus returns IPv6
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(MainActivity.TAG, "getLocalIPAddress() err: ", ex);
        } catch (NullPointerException ex) {
            Log.e(MainActivity.TAG, "getLocalIPAddress() err: ", ex);
        }
        return null;
    }

    private String getDottedDecimalIP(byte[] ipAddr) {
        //convert to dotted decimal notation:
        String ipAddrStr = "";
        if(ipAddr == null)
            return "Null ip addr maybe not connected";
        for (int i=0; i<ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i]&0xFF;
        }
        return ipAddrStr;
    }


    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        Log.e(MainActivity.TAG, "info" + info.toString());
        if (info.groupFormed && !info.isGroupOwner && info.groupOwnerAddress.toString() != null){
            //sender
            String host = info.groupOwnerAddress.toString().replace("/", "");
            SendFileAsyncTask sendFileAsyncTask = new SendFileAsyncTask(this, this, chosenFiles);
            sendFileAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, host);

        }
    }

    @Override
    public void onFileReceived(final String message, boolean isSuccess) {
        Log.e(MainActivity.TAG, "Sending " + message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvStatus.setText(message);
            }
        });
    }

}
