package com.example.daryo.shareappsamp.interfaces;

public interface WifiDirectCallbacks {
    void WIFI_P2P_STATE_CHANGED_ACTION(boolean isEnabled);
    void WIFI_P2P_PEERS_CHANGED_ACTION();
    void WIFI_P2P_CONNECTION_CHANGED_ACTION(boolean isConnected);
    void WIFI_P2P_THIS_DEVICE_CHANGED_ACTION();
}
