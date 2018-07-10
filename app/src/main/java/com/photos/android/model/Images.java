package com.photos.android.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Rahul D on 7/6/18.
 */
public class Images {
    private int height;
    String id;
    private String source;

    private int width;
    private int likesCount;

    private boolean setUserHasLiked;

    @SerializedName("standard_resolution")
    private StandardResolution standedResulation;

    public int getHeight() {
        return height;
    }

    public String getSource() {
        return source;
    }

    public int getWidth() {
        return width;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public StandardResolution getStandedResulation() {
        return standedResulation;
    }

    public boolean isUserHasLiked() {
        return setUserHasLiked;
    }

    public void setSetUserHasLiked(boolean setUserHasLiked) {
        this.setUserHasLiked = setUserHasLiked;
    }
}
