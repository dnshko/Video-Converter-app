package com.elsner.VideoConverter.Adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.elsner.VideoConverter.R;
import com.elsner.VideoConverter.Utils.Constants;
import com.elsner.VideoConverter.fragments.GalleryFragment;

import java.io.File;
import java.util.ArrayList;

public class GalleryImageAdapter extends RecyclerView.Adapter<GalleryImageAdapter.CellViewHolder> {

    public final String TAG = "GalleryImageAdapter";
    private ArrayList<Integer> indexList;
    private GalleryFragment fragment;
    private Context context;
    private String type;

    public GalleryImageAdapter(GalleryFragment fragment, ArrayList<Integer> indexList, String type) {
        this.indexList = indexList;
        this.fragment = fragment;
        this.context = fragment.getContext();
        this.type = type;
    }

    @NonNull
    @Override
    public CellViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.adapter_gallery_image, parent, false);
        return new CellViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CellViewHolder holder, final int position) {

        final int index = indexList.get(position);

        if (type.equals("gif")) {

            String strThumb = Constants.GIF_FOLDER_PATH + String.format("/%s%d.gif", Constants.MY_GIF, index);
            File file = new File(strThumb);
            if (file.exists())
                Glide.with(fragment)
                        .asBitmap()
                        .thumbnail(0.5f)
                        .load(file)
                        .into(holder.image);

        } else if (type.equals("jpg")){

            String strThumb = Constants.IMAGE_FOLDER_PATH + String.format("/%s%d.jpg", Constants.MY_IMAGE, index);
            File file = new File(strThumb);
            if (file.exists())
                Glide.with(fragment)
                        .asBitmap()
                        .thumbnail(0.5f)
                        .load(file)
                        .into(holder.image);

        }

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.view(index, type);
            }
        });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.remove(position, type);
            }
        });

        holder.btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.share(position, type);
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
