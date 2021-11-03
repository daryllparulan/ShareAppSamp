package com.example.daryo.shareappsamp.wifip2p;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.example.daryo.shareappsamp.MainActivity;
import com.example.daryo.shareappsamp.interfaces.FileReceivedCallback;
import com.example.daryo.shareappsamp.utils.Item;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class FileServerAsyncTask extends AsyncTask<String, String, String> {

    private FileReceivedCallback mCallback;


    public FileServerAsyncTask(FileReceivedCallback mCallback) {
        this.mCallback = mCallback;

    }


    @Override
    protected String doInBackground(String... params) {
        try {

            /**
             * Create a server socket and wait for client connections. This
             * call blocks until a connection is accepted from a client
             */

            ServerSocket serverSocket = new ServerSocket(8888);
            Socket client = serverSocket.accept();

            File dirs = new File(Environment.getExternalStorageDirectory() + "/P2PSample");

            if (!dirs.exists())
                dirs.mkdirs();

            BufferedInputStream bis = new BufferedInputStream(client.getInputStream());
            ObjectInputStream ois = new ObjectInputStream(bis);
            DataInputStream dis = new DataInputStream(bis);


            Object o2 = ois.readObject();

            ArrayList<Item> filesToReceive = new ArrayList<Item>();

            filesToReceive.addAll((ArrayList<Item>) o2);

            int filesToReceiveCount = filesToReceive.size();

            //loop to all files
            for(int i = 0; i < filesToReceiveCount; i++) {
                long fileLength = filesToReceive.get(i).getFileSize();
                String fileName = filesToReceive.get(i).getFileName();

                int nameIndex = fileName.lastIndexOf('/');
                if (nameIndex > 0) {
                    fileName = fileName.substring(nameIndex + 1);
                }

                //check if directory
                if(filesToReceive.get(i).isDir()){
                    //file is directory
                    receiveDirectoryOrFile(bis, dis, "");

                }else {
                    //single file

                    File singleFile = new File(Environment.getExternalStorageDirectory() + "/P2PSample/" + fileName);

                    FileOutputStream fos = new FileOutputStream(singleFile);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);

                    int len = 0;
                    long fileSize = 0;
                    int readLength = fileLength > 4096 ? 4096 : (int)fileLength;
                    byte buf[] = new byte[readLength];

                    while ((len = bis.read(buf)) != -1 && fileSize < fileLength) {
                        bos.write(buf, 0, len);
                        bos.flush();
                        fileSize += len;
                        if((fileSize + readLength) >= fileLength){
                            buf = new byte[(int)(fileLength-fileSize)];
                        }

                        // to update
                        Log.e("STAT:",  fileName + " " + (int) ((fileSize * 100) / fileLength) + "%");
                        mCallback.onFileReceived(fileName + " " + (int) ((fileSize * 100) / fileLength) + "%", true);
                    }

                    bos.close();

                }

            }

            ois.close();

            //no need to close dos as oos.close will close the bis
            //dis.close();

            serverSocket.close();

            return "success";

        } catch (IOException e) {
            Log.e(MainActivity.TAG, e.toString());
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Start activity that can handle the JPEG image
     */
    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            mCallback.onFileReceived(result, true);
        }
    }

    private void copyFile(InputStream in, OutputStream out){

        try {
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            // Ensure that the InputStreams are closed even if there's an exception.
            try {
                if ( out != null ) {
                    out.close();
                }

                // If you want to close the "in" InputStream yourself then remove this
                // from here but ensure that you close it yourself eventually.
                in.close();
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }

    private void receiveDirectoryOrFile(BufferedInputStream bis,
                                              DataInputStream dis,
                                              String prevFileName) throws IOException{

        int isDir = dis.readByte();
        String fileName = dis.readUTF();

        if(isDir == 1){
            //directory
            File dirs = new File(Environment.getExternalStorageDirectory()
                    + "/P2PSample/"
                    + prevFileName
                    + "/" + fileName);

            if(!dirs.exists()){
                dirs.mkdir();
            }

            receiveDirectoryOrFile(bis, dis, prevFileName + "/" + fileName);

        }else if (isDir == 0) {
            //file

            long fileLength = dis.readLong();

            File file = new File(Environment.getExternalStorageDirectory()
                    + "/P2PSample/"
                    + prevFileName
                    + "/" + fileName);

            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            int len;
            long fileSize = 0;
            int readLength = fileLength > 4096 ? 4096 : (int)fileLength;
            byte buf[] = new byte[readLength];

            while ((len = bis.read(buf)) != -1 && fileSize < fileLength) {
                bos.write(buf, 0, len);
                bos.flush();
                fileSize += len;
                if((fileSize + readLength) >= fileLength){
                    buf = new byte[(int)(fileLength-fileSize)];
                }

                // to update
                Log.e("STAT:",  fileName + " " + (int) ((fileSize * 100) / fileLength) + "%");
                mCallback.onFileReceived(fileName + " " + (int) ((fileSize * 100) / fileLength) + "%", true);
            }

            bos.close();

            receiveDirectoryOrFile(bis, dis, prevFileName);

        }

    }

}