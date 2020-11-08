package com.elsner.VideoConverter.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.elsner.VideoConverter.Pojo.VideoModel;
import com.elsner.VideoConverter.R;
import com.elsner.VideoConverter.fragments.GalleryFragment;

import java.io.File;
import java.util.ArrayList;

public class GalleryVideoAdapter extends RecyclerView.Adapter<GalleryVideoAdapter.CellViewHolder> {

    public final String TAG = "GalleryVideoAdapter";
    private ArrayList<VideoModel> indexList;
    private GalleryFragment fragment;
    private Context context;

    public GalleryVideoAdapter(GalleryFragment fragment, ArrayList<VideoModel> indexList) {
        this.indexList = indexList;
        this.fragment = fragment;
        this.context = fragment.getContext();
    }

    @NonNull
    @Override
    public CellViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.adapter_gallery_image, parent, false);
        return new CellViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CellViewHolder holder, final int position) {

        final VideoModel videoModel = indexList.get(position);

        Glide.with(fragment)
                .asBitmap()
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .thumbnail(0.5f)
                .load(videoModel.getStr_thumb())
                .into(holder.image);

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                File file = new File(videoModel.getStr_path());

                if (file.exists() && file.length() > 0) {

                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file), "video/*");

                    context.startActivity(Intent.createChooser(intent, "Choose player..."));
                }
            }
        });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.remove(position, "mp4");
            }
        });

        holder.btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.share(position, "mp4");
            }
        });

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return indexList.size();
    }

    public static class CellViewHolder extends RecyclerView.ViewHolder {

        ImageView image, btnDelete, btnShare;

        public CellViewHolder(View convertView) {
            super(convertView);

            image = (ImageView) convertView.findViewById(R.id.back_image);
            btnDelete = (ImageView) convertView.findViewById(R.id.btn_delete);
            btnShare = (ImageView) convertView.findViewById(R.id.btn_share);
        }
    }
}
