package com.photos.android.model;

/**
 * Created by Rahul D on 7/6/18.
 */
public class Data {
    private String id;

    private String full_picture;

    private Likes likes;
    private Images images;

    private boolean user_has_liked;

    public Likes getLikes() {
        return likes;
    }

    public String getFull_picture() {
        return full_picture;
    }

    public String getId() {
        return id;
    }

    public Images getImages() {
        return images;
    }

    public boolean isUserHasLiked() {
        return user_has_liked;
    }
}
