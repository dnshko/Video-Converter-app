package com.elsner.VideoConverter.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elsner.VideoConverter.R;
import com.elsner.VideoConverter.Utils.Constants;
import com.elsner.VideoConverter.fragments.GalleryFragment;

import java.io.File;
import java.util.ArrayList;

public class GalleryAudioAdapter extends RecyclerView.Adapter<GalleryAudioAdapter.CellViewHolder> {

    public final String TAG = "GalleryAudioAdapter";
    private ArrayList<Integer> indexList;
    private GalleryFragment fragment;
    private Context context;

    public GalleryAudioAdapter(GalleryFragment fragment, ArrayList<Integer> indexList) {
        this.indexList = indexList;
        this.fragment = fragment;
        this.context = fragment.getContext();
    }

    @NonNull
    @Override
    public CellViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.adapter_gallery_audio, parent, false);
        return new CellViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CellViewHolder holder, final int position) {

        final int index = indexList.get(position);

        String pathWav = Constants.AUDIO_FOLDER_PATH + String.format("/%s%d.wav", Constants.MY_AUDIO, index);
        String pathMp3 = Constants.AUDIO_FOLDER_PATH + String.format("/%s%d.mp3", Constants.MY_AUDIO, index);

        final File fileWav = new File(pathWav);
        final File fileMp3 = new File(pathMp3);

        Log.d(TAG, "onBindViewHolder: wavlength: " + fileWav.length() + " mp3length: " + fileMp3.length());

        if (fileWav.length() > 0) {

            String name = fileWav.getName();
            holder.txtAudio.setText(name);

        } else if (fileMp3.length() > 0) {

            String name = fileMp3.getName();
            holder.txtAudio.setText(name);

        } else {

            Constants.removeAudio(fragment.getActivity(), position);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                if (fileWav.length() > 0)
                    intent.setDataAndType(Uri.fromFile(fileWav), "audio/*");
                else
                    intent.setDataAndType(Uri.fromFile(fileMp3), "audio/*");
                context.startActivity(Intent.createChooser(intent, "Choose player..."));
            }
        });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.remove(position, "audio");
            }
        });

        holder.btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileWav.length() > 0)
                    fragment.share(position, "wav");
                else
                    fragment.share(position, "mp3");
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

        RelativeLayout relativeAudio;
        TextView txtAudio;
        ImageView btnDelete, btnShare;

        public CellViewHolder(View convertView) {
            super(convertView);

            relativeAudio = convertView.findViewById(R.id.relativeAudio);
            txtAudio = (TextView) convertView.findViewById(R.id.txtAudio);
            btnDelete = (ImageView) convertView.findViewById(R.id.btn_delete);
            btnShare = (ImageView) convertView.findViewById(R.id.btn_share);
        }
    }
}
