package com.photos.android.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Rahul D on 7/7/18.
 */
public class Summery {

    @SerializedName("can_like")
    private String canLike;

    @SerializedName("has_liked")
    private String hasLiked;

    @SerializedName("total_count")
    private String totalCount;

    public String getTotalCount() {
        return totalCount;
    }

    public String getHasLiked() {
        return hasLiked;
    }

    public String getCanLike() {
        return canLike;
    }
}
