package com.example.daryo.shareappsamp;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.daryo.shareappsamp.interfaces.FileReceivedCallback;
import com.example.daryo.shareappsamp.wifip2p.FileServerAsyncTask;

public class ConnectedActivity extends AppCompatActivity implements FileReceivedCallback{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FileServerAsyncTask asyncTask = new FileServerAsyncTask(this);
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"");
    }

    @Override
    public void onFileReceived(final String message, final boolean isSuccess) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(MainActivity.TAG, message);
            }
        });
    }
}
