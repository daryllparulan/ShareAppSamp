package com.example.daryo.shareappsamp.wifip2p;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.example.daryo.shareappsamp.MainActivity;
import com.example.daryo.shareappsamp.SendActivity;
import com.example.daryo.shareappsamp.interfaces.FileReceivedCallback;
import com.example.daryo.shareappsamp.utils.Item;
import com.example.daryo.shareappsamp.utils.ObjFile;
import com.example.daryo.shareappsamp.utils.Util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class SendFileAsyncTask extends AsyncTask<String, String, String> {
    private Context context;
    private FileReceivedCallback mCallback;
    private ArrayList<Item> chosenFiles = new ArrayList<Item>();

    public SendFileAsyncTask(Context context, FileReceivedCallback mCallback, ArrayList<Item> chosenFiles) {
        this.context = context;
        this.mCallback = mCallback;
        this.chosenFiles = chosenFiles;
    }

    @Override
    protected String doInBackground(String... params) {
        int port = 8888;
        Socket socket = new Socket();

        try {
            /**
             * Create a client socket with the host,
             * port, and timeout information.
             */
            socket.bind(null);
            socket.connect((new InetSocketAddress(params[0], port)), 10000);

            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            DataOutputStream dos = new DataOutputStream(bos);

            oos.writeObject(chosenFiles);

            for(int i = 0; i < chosenFiles.size(); i++){
                long fileLength = chosenFiles.get(i).getFileSize();
                String fileName = chosenFiles.get(i).getFileName();

                //check if directory
                if(chosenFiles.get(i).isDir()){
                    //file is directory
                    File file = new File(fileName);

                    Util.sendDirectoryOrFile(file, bos, dos);
                    //send 2 as directory files all send
                    dos.writeByte(2);
                    dos.writeUTF("end");

                }else {
                    //single file
                    FileInputStream fis = new FileInputStream(chosenFiles.get(i).getAsFile());
                    BufferedInputStream bis = new BufferedInputStream(fis);

                    int len = 0;
                    long fileSize = 0;
                    byte buf[] = new byte[4096];

                    while ((len = bis.read(buf)) != -1) {
                        bos.write(buf, 0, len);
                        bos.flush();
                        fileSize += len;
                        //count for progress bar

                        Log.e("STAT:",  fileName + " " + (int) ((fileSize * 100) / fileLength) + "%");
                        mCallback.onFileReceived(fileName + " " + (int) ((fileSize * 100) / fileLength) + "%", true);
                    }


                    bis.close();

                }

            }

            oos.close();
            dos.close();

            return "success";
        } catch (FileNotFoundException e) {
            //catch logic
            Log.e(MainActivity.TAG, "send " + e.toString());
            return null;
        } catch (IOException e) {
            //catch logic
            Log.e(MainActivity.TAG, "send " + e.toString());
            return null;
        }

        /**
         * Clean up any open sockets when done
         * transferring or if an exception occurred.
         */
        finally {
            if (socket != null) {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        //catch logic
                    }
                }
            }
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            mCallback.onFileReceived("success", true);
        }else {
            mCallback.onFileReceived("failed", false);
        }
    }
}
