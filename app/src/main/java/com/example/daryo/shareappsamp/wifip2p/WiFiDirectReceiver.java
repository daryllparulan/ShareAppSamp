package com.example.daryo.shareappsamp.wifip2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import com.example.daryo.shareappsamp.MainActivity;
import com.example.daryo.shareappsamp.ReceiveActivity;

public class WiFiDirectReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private ReceiveActivity mActivity;

    public WiFiDirectReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                              ReceiveActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            Log.e(MainActivity.TAG, "WIFI_P2P_STATE_CHANGED_ACTION");
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
                Log.e(MainActivity.TAG, "Wifi P2P is enabled");
                mActivity.isWifiDirectEnabled(true);
            } else {
                // Wi-Fi P2P is not enabled
                Log.e(MainActivity.TAG, "Wi-Fi P2P is not enabled");
                mActivity.isWifiDirectEnabled(false);
            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers


        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            Log.e(MainActivity.TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION");

            //Here is where the request for connection info should happen
            //As sometimes the pairing can happen but the devices
            //might still be negotiating group owner for example

            //Here is where the request for connection info should happen
            //As sometimes the pairing can happen but the devices
            //might still be negotiating group owner for example


            if (mManager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            Log.e(MainActivity.TAG, "networkInfo: " + networkInfo.toString());

            if (networkInfo.isConnected()) {

                // we are connected with the other device, request connection
                // info to find group owner IP

                mManager.requestConnectionInfo(mChannel, mActivity);
            } else {
                // It's a disconnect
                Log.e(MainActivity.TAG, "It's a disconnect");
            }


        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
            Log.e(MainActivity.TAG, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");
        }
    }


}
