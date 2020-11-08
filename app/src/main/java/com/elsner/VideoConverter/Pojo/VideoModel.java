package com.elsner.VideoConverter.Pojo;


import android.graphics.Bitmap;

public class VideoModel {

    int index;
    String str_path;
    Bitmap str_thumb;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getStr_path() {
        return str_path;
    }

    public void setStr_path(String str_path) {
        this.str_path = str_path;
    }

    public Bitmap getStr_thumb() {
        return str_thumb;
    }

    public void setStr_thumb(Bitmap str_thumb) {
        this.str_thumb = str_thumb;
    }
}