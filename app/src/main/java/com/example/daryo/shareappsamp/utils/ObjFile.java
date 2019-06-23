package com.example.daryo.shareappsamp.utils;

import java.io.Serializable;

public class ObjFile implements Serializable {

    private long fileLength;
    private String fileName;

    public ObjFile(long fileLength, String fileName){
        this.fileLength = fileLength;
        this.fileName = fileName;
    }

    public long length(){
        return fileLength;
    }

    public String getName(){
        return fileName;
    }

}
