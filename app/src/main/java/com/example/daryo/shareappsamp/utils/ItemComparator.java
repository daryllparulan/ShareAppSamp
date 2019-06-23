package com.example.daryo.shareappsamp.utils;

import java.util.Comparator;

public class ItemComparator implements Comparator<Item> {

    //can add constructor to get what sorting

    @Override
    public int compare(Item o1, Item o2) {
        if(o1.isDir() && !o2.isDir()){
            //negative means palit or o2>o1
            return -1;
        }else if(!o1.isDir() && o2.isDir()){
            //positive means retain or o1>o2
            return 1;
        }else {
            return o1.getFileName().toLowerCase().compareTo(o2.getFileName().toLowerCase());
        }
    }
}
