package com.example.daryo.shareappsamp;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.daryo.shareappsamp.utils.PermissionsAndroid;

public class MainActivity extends AppCompatActivity {

    private static final int CHOOSE_FILE_CODE = 1;

    public static final String TAG = "P2P";

    private Button btnSend, btnReceive, btnDisconnect;

    public boolean isConnected = false;
    public boolean isReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSend = findViewById(R.id.btn_send);
        btnReceive = findViewById(R.id.btn_receive);
        btnDisconnect = findViewById(R.id.btn_disconnect);



        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "Send cliked");
                checkExternalStoragePermission();
                isReceiver = false;
                isConnected = false;
            }
        });

        btnReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "Received cliked");
                isReceiver = true;
                isConnected = false;
                Intent i = new Intent(MainActivity.this, ReceiveActivity.class);
                startActivity(i);
//                discoverPeers();
            }
        });

//        btnDisconnect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.e(TAG, "Disconnect cliked");
//                manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
//
//                    @Override
//                    public void onFailure(int reasonCode) {
//                        Log.e(TAG, "Disconnect failed. Reason :" + reasonCode);
//
//                    }
//
//                    @Override
//                    public void onSuccess() {
//                        Log.e(TAG, "Disconnect success");
//                    }
//
//                });
//            }
//        });

    }

    private void checkExternalStoragePermission() {
        boolean isExternalStorage = PermissionsAndroid.getInstance().checkWriteExternalStoragePermission(this);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP && !isExternalStorage) {
            PermissionsAndroid.getInstance().requestForWriteExternalStoragePermission(this);
        } else {
            Intent i = new Intent(this, FileExplorerActivity.class);
            startActivityForResult(i, CHOOSE_FILE_CODE);
        }

    }


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == CHOOSE_FILE_CODE && data != null) {
////            Log.e("TAG result", data.getSerializableExtra("paths").toString());
//            chosenFiles = (ArrayList<Item>) data.getSerializableExtra("paths");
//
////            for (Item i : chosenFiles) {
////                Log.e("TAG result", i.getFileName());
////            }
//
//            discoverPeers();
//        }
//    }












}
