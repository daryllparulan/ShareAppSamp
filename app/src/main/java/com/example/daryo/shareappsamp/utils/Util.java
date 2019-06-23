package com.example.daryo.shareappsamp.utils;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class Util {

    public static long getFileSize(final File file) {
        if (file == null || !file.exists())
            return 0;
        if (!file.isDirectory())
            return file.length();
        final List<File> dirs = new LinkedList<>();
        dirs.add(file);
        long result = 0;
        while (!dirs.isEmpty()) {
            final File dir = dirs.remove(0);
            if (!dir.exists())
                continue;
            final File[] listFiles = dir.listFiles();
            if (listFiles == null || listFiles.length == 0)
                continue;
            for (final File child : listFiles) {
                result += child.length();
                if (child.isDirectory())
                    dirs.add(child);
            }
        }
        return result;
    }

    public static void copyDirectoryOneLocationToAnotherLocation(
            File sourceLocation, File targetLocation) throws IOException{

        if(sourceLocation.isDirectory()){
            if(!targetLocation.exists()){
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < sourceLocation.listFiles().length; i++){
                copyDirectoryOneLocationToAnotherLocation(new File(sourceLocation, children[i]),
                new File(targetLocation, children[i]));
            }

        }else {

            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            //Copy the bits from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0){
                out.write(buf, 0 , len);
            }
            in.close();
            out.close();

        }


    }

    public static void sendDirectoryOrFile(File sourceLocation,
                                           BufferedOutputStream bos,
                                           DataOutputStream dos) throws IOException{

        if(sourceLocation.isDirectory()){
            //do send folder path for receiver to create
            dos.writeByte(1);
            dos.writeUTF(sourceLocation.getName());

            String[] children = sourceLocation.list();
            for (int i = 0; i < sourceLocation.listFiles().length; i++){
                sendDirectoryOrFile(new File(sourceLocation, children[i]), bos, dos);
            }

        }else {
            dos.writeByte(0);

            long fileLength = sourceLocation.length();
            String fileName = sourceLocation.getName();

            dos.writeUTF(fileName);
            dos.writeLong(fileLength);

            FileInputStream fis = new FileInputStream(sourceLocation);
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
            }


            bis.close();

        }
    }

    public static void receiveDirectoryOrFile(BufferedInputStream bis,
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
            }

            bos.close();

            receiveDirectoryOrFile(bis, dis, prevFileName);

        }

    }


}
