package com.photos.android.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.LoggingBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;
import com.photos.android.R;
import com.photos.android.adapter.ImageAdapter;
import com.photos.android.helper.ApiClient;
import com.photos.android.helper.ApiInterface;
import com.photos.android.helper.Constants;
import com.photos.android.helper.InstagramDialog;
import com.photos.android.model.Data;
import com.photos.android.model.DataRepponse;
import com.photos.android.model.Images;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements ImageAdapter.OnItemClickListener, View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final int PICK_IMAGE_REQUEST = 200;
    private CallbackManager mCallbackManager;
    private GraphRequest request;
    private ArrayList<Images> mImagesList;
    private RecyclerView mImageRecyler;
    private ImageAdapter mImageAdapter;
    private ProgressBar mProgressBar;
    private LoginButton mFBLoginButton;
    private Button mInstaLoginButton;
    private boolean mIsLoggedInWithFB;
    private View mUploadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageRecyler = findViewById(R.id.main_recycler);
        mProgressBar = findViewById(R.id.progress_bar);
        mUploadButton = findViewById(R.id.upload_button);
        mUploadButton.setOnClickListener(this);
        mInstaLoginButton = findViewById(R.id.sign_in_button);
        mInstaLoginButton.setOnClickListener(this);
        mFBLoginButton = findViewById(R.id.fb_login_button);

        mImageRecyler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        FirebaseApp.initializeApp(getApplicationContext());
        FacebookSdk.setIsDebugEnabled(true);
        FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        mCallbackManager = CallbackManager.Factory.create();
        if (!isLogIn()) {
            mFBLoginButton.setVisibility(View.VISIBLE);
            mInstaLoginButton.setVisibility(View.VISIBLE);
            mUploadButton.setVisibility(View.GONE);
            setUpFBLogIn();
        } else {
            mFBLoginButton.setVisibility(View.GONE);
            mInstaLoginButton.setVisibility(View.GONE);
        }
    }

    private boolean isLogIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        String instaToken = getSharedPreferences("SHARED", Context.MODE_PRIVATE).getString("ACCESS_TOKEN", null);
        if (accessToken != null && !accessToken.isExpired()) {
            getUserPhotos();
            return true;
        } else if (instaToken != null) {
            getAllPhotosInsta(instaToken);
            return true;
        }
        return false;
    }


    private void setUpFBLogIn() {
        mFBLoginButton.setReadPermissions(Arrays.asList("user_photos"));

        mFBLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                mIsLoggedInWithFB = true;
                mFBLoginButton.setVisibility(View.GONE);
                mInstaLoginButton.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                getUserPhotos();
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(getBaseContext(), "Authentication failed", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (mIsLoggedInWithFB) {
                    uploadPhotoToFB(data);
                } else {
                    createInstagramIntent(data.getData());
                }
            }
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadPhotoToFB(Intent data) {
        Uri uri = data.getData();
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            String path = "me/feed";
            AccessToken at = AccessToken.getCurrentAccessToken();
            Bundle parameters = new Bundle();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            bitmap.recycle();
            parameters.putByteArray("picture", byteArray);

            HttpMethod method = HttpMethod.POST;
            GraphRequest.Callback cb = new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse graphResponse) {
                    Toast.makeText(getApplicationContext(), "photo posted successfully on ur wall", Toast.LENGTH_LONG).show();
                }
            };

            GraphRequest request = new GraphRequest(at, path, parameters, method, cb);
            request.setParameters(parameters);
            request.executeAsync();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getUserPhotos() {
        request = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/posts",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        mProgressBar.setVisibility(View.GONE);
                        GraphRequest nextRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
                        if (nextRequest != null) {
                            nextRequest.setCallback(request.getCallback());
                            nextRequest.executeAsync();
                        }
                        if (response.getError() == null) {
                            prepareTheImageList(response);
                        }
                    }
                }
        );
        Bundle parameters = new Bundle();
        parameters.putString("fields", "source,full_picture,likes.summary(1)");
        parameters.putString("limit", "10");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void prepareTheImageList(GraphResponse response) {
        DataRepponse dataRepponse = new Gson().fromJson(response.getRawResponse(), DataRepponse.class);
        for (Data data : dataRepponse.getData()) {
            Images images = new Images();
            images.setSource(data.getFull_picture());
            images.setId(data.getId());
            if (data.getLikes() != null && data.getLikes().getSummary() != null) {
                images.setLikesCount(Integer.parseInt(data.getLikes().getSummary().getTotalCount()));
            }
            if (mImagesList == null) {
                mImagesList = new ArrayList<>();
            }
            mImagesList.add(images);
        }
        setAdapter();
    }

    private void setAdapter() {
        if (mImageAdapter == null) {
            mImageAdapter = new ImageAdapter(getApplicationContext(), mImagesList, MainActivity.this);
            mImageRecyler.setAdapter(mImageAdapter);
            mImageRecyler.setVisibility(View.VISIBLE);
            mUploadButton.setVisibility(View.VISIBLE);
        } else {
            mImageAdapter.updateData(mImagesList);
        }
    }

    @Override
    public void onItemCLick(Images images, int position) {
        likeImage(images, position);
    }

    private void likeImage(Images images, final int position) {

        new GraphRequest(AccessToken.getCurrentAccessToken(),
                "/" + images.getId() + "/likes",
                null,
                HttpMethod.POST,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        int likeCount = mImagesList.get(position).getLikesCount() + 1;
                        mImagesList.get(position).setLikesCount(likeCount);
                        mImageAdapter.updateData(mImagesList);
                    }
                }).executeAsync();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.upload_button:
                openGallary();
                break;
            case R.id.sign_in_button:
                setUpInstagram();
                break;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_options_menus, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            mFBLoginButton.setVisibility(View.VISIBLE);
            mInstaLoginButton.setVisibility(View.VISIBLE);
            mUploadButton.setVisibility(View.GONE);
            mImageRecyler.setVisibility(View.GONE);
            mImagesList = null;
            if (mIsLoggedInWithFB) {
                LoginManager.getInstance().logOut();
            } else {
                getSharedPreferences("SHARED", Context.MODE_PRIVATE).
                        edit().
                        putString("ACCESS_TOKEN", null).
                        apply();
            }
        }
        return true;
    }

    private void openGallary() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void setUpInstagram() {
        InstagramDialog.OAuthDialogListener listener = new InstagramDialog.OAuthDialogListener() {
            @Override
            public void onComplete(String code) {
                SharedPreferences sharedPref = getSharedPreferences("SHARED", Context.MODE_PRIVATE);
                sharedPref.edit().putString("ACCESS_TOKEN", code).apply();
                getAllPhotosInsta(code);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getApplicationContext(), "Auth failed", Toast.LENGTH_LONG).show();
            }
        };
        InstagramDialog mDialog = new InstagramDialog(this, Constants.AUTH_URL, listener);
        mDialog.show();
    }

    private void getAllPhotosInsta(String code) {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        mProgressBar.setVisibility(View.VISIBLE);
        Call<DataRepponse> call = apiService.getMovieDetails(code);
        call.enqueue(new Callback<DataRepponse>() {
            @Override
            public void onResponse(Call<DataRepponse> call, Response<DataRepponse> response) {
                if (response.body() != null) {
                    for (Data data : response.body().getData()) {
                        Images images = new Images();
                        images.setSource(data.getImages().getStandedResulation().getUrl());
                        images.setId(data.getId());
                        images.setLikesCount(data.getLikes().getCount());
                        images.setSetUserHasLiked(data.isUserHasLiked());
                        if (mImagesList == null) {
                            mImagesList = new ArrayList<>();
                        }
                        mImagesList.add(images);
                    }
                }
                setAdapter();
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<DataRepponse> call, Throwable t) {

                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createInstagramIntent(Uri mediaPath) {

        // Create the new Intent using the 'Send' action.
        Intent share = new Intent(Intent.ACTION_SEND);

        // Set the MIME type
        share.setType("image/*");


        // Add the URI to the Intent.
        share.putExtra(Intent.EXTRA_STREAM, mediaPath);

        // Broadcast the Intent.
        startActivity(Intent.createChooser(share, "Share to"));
    }

}