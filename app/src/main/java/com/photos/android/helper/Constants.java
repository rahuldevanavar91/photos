package com.photos.android.helper;

/**
 * Created by Rahul D on 7/10/18.
 */
public class Constants {
    public static final String CALLBACK_URL = "https://elfsight.com/service/generate-instagram-access-token/";
    public static final String TOKEN_URL = "https://api.instagram.com/oauth/access_token";
    public static final String API_URL = "https://api.instagram.com/v1/";

    public static final String CLIENT_ID = "aff39e1cf8574ff3be1edf2716ad1764";
    public static final String CLIENT_SECRET = "a77dc9d9a93b4e1c9bd5499750c80442";

    public static final String AUTH_URL = "https://api.instagram.com/oauth/authorize/"
            + "?client_id="
            + CLIENT_ID
            + "&redirect_uri="
            + CALLBACK_URL
            + "&response_type=code&display=touch&scope=basic+public_content+follower_list+comments+relationships+likes";

}
