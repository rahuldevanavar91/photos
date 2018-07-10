package com.photos.android.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.photos.android.R;
import com.photos.android.model.Images;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Rahul D on 7/7/18.
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private final Context mContext;
    private ArrayList<Images> mArrayList;
    private OnItemClickListener mClickListener;

    public ImageAdapter(Context context, ArrayList<Images> images, OnItemClickListener clickListener) {
        mArrayList = images;
        mContext = context;
        mClickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.image_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Images item = mArrayList.get(holder.getAdapterPosition());
        Picasso.with(mContext)
                .load(item.getSource())
                .placeholder(R.color.app_grey)
                .into(holder.imageView);
        if (item.getLikesCount() > 0) {
            holder.likeButton.setText(" " + item.getLikesCount() + " Likes");
        } else {
            holder.likeButton.setText("Like");
        }
        holder.likeButton.setTag(holder.getAdapterPosition());
    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

    public void updateData(ArrayList<Images> imagesList) {
        mArrayList = imagesList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView likeButton;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            likeButton = itemView.findViewById(R.id.like_button);
            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = (int) v.getTag();
                    if (!mArrayList.get(pos).isUserHasLiked()) {
                        mClickListener.onItemCLick(mArrayList.get((int) v.getTag()), pos);
                    }
                }
            });
        }
    }


    public interface OnItemClickListener {
        void onItemCLick(Images images, int position);
    }
}
