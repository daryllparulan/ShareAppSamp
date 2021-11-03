package com.example.daryo.shareappsamp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.example.daryo.shareappsamp.interfaces.FileReceivedCallback;
import com.example.daryo.shareappsamp.wifip2p.FileServerAsyncTask;
import com.example.daryo.shareappsamp.wifip2p.WiFiDirectReceiver;

import java.util.concurrent.ThreadPoolExecutor;

public class ReceiveActivity extends AppCompatActivity implements FileReceivedCallback, WifiP2pManager.ConnectionInfoListener {

    private TextView tvStatus;

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);

        tvStatus = findViewById(R.id.tvStatus);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new WiFiDirectReceiver(manager, channel, this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        //receive();


    }

    private void receive(){
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
                tvStatus.setText("Error discovering peers. Error code:" + reasonCode);
            }
        });
    }

    @Override
    public void onFileReceived(final String message, boolean isSuccess) {
        Log.e(MainActivity.TAG, "file receive : " + message + isSuccess);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvStatus.setText(message);
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

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        Log.e(MainActivity.TAG, "info" + info.toString());
        if(info.groupFormed && info.isGroupOwner){
            //receiver
            //FileServerAsyncTask asyncTask = new FileServerAsyncTask(this);
            //asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"");
            Intent i = new Intent(this, ConnectedActivity.class);
            startActivity(i);
            finish();
        }
    }

    public void isWifiDirectEnabled(boolean isEnabled){
        if(!isEnabled){
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Error");
            alertDialog.setMessage("Please Enable Wifi or Wifi Direct");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
            alertDialog.show();
        }else {
            receive();
        }
    }

}
