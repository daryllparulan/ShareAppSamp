package com.example.daryo.shareappsamp.utils;


import java.io.File;
import java.io.Serializable;

public class Item implements Serializable{
    private String fileName;
    private boolean isDir, isSelected;
    private long fileSize;
    private File file;

    public Item(String file, boolean isDir, boolean isSelected, long fileSize) {
        this.fileName = file;
        this.isDir = isDir;
        this.isSelected = isSelected;
        this.fileSize = fileSize;
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isDir() {
        return isDir;
    }

    public void setIsDir(boolean dir) {
        isDir = dir;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean selected) {
        isSelected = selected;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public File getAsFile(){
        if(file == null){
            file = new File(fileName);
        }
        return file;
    }

}

