package com.elsner.VideoConverter.fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.elsner.VideoConverter.Adapters.RecyclerImageAdapter;
import com.elsner.VideoConverter.MainActivity;
import com.elsner.VideoConverter.R;
import com.elsner.VideoConverter.Utils.Constants;
import com.elsner.VideoConverter.Utils.GifMakeService;

import java.io.File;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ImagesGifFragment extends Fragment {

    public final String TAG = "ImagesGifFragment";
    public Activity activity;
    ArrayList<String> imagePaths = new ArrayList<>();
    ProgressDialog mlDialog;
    int nProgress;
    int total = 0, current = 0;
    private FloatingActionButton fabDone;
    private RecyclerView rclImages;
    private ImageView ivImage, ivPreview;
    private RecyclerImageAdapter customImageAdapter;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String log = intent.getStringExtra(GifMakeService.EXTRA_LOG);

            if (log != "")
                Log.d(TAG, "onReceive: " + log);

            total = intent.getIntExtra(GifMakeService.EXTRA_TOTAL, 0);
            current = intent.getIntExtra(GifMakeService.EXTRA_CURRENT, 0) + 1;

            Log.d(TAG, "onReceive: " + total + " : " + current);
            if (total != 0) {
                nProgress = (current * 100) / total;

                Log.d(TAG, "onReceive: per: " + nProgress);
                mlDialog.setProgress(nProgress);
            }

            boolean success = intent.getBooleanExtra(GifMakeService.EXTRA_SUCCESS, false);

            if (success) {

                if (null != mlDialog && mlDialog.isShowing())
                    mlDialog.dismiss();

                finishedVideoConvet();
            }
        }
    };

    public ImagesGifFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) activity).setTitle(R.string.images_to_gif);
        ((MainActivity) activity).setDrawerState(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_images_gif, container, false);

        activity = getActivity();

        LocalBroadcastManager.getInstance(activity).registerReceiver(mReceiver, new IntentFilter(GifMakeService.ACTION_MAKE_GIF_FROM_IMAGES));

        if (getArguments() != null) {
            imagePaths = getArguments().getStringArrayList("images");
        }

        initView(v);

        updateImageView(0);

        customImageAdapter = new RecyclerImageAdapter(this, imagePaths);
        rclImages.setAdapter(customImageAdapter);

        return v;
    }

    public void updateImageView(int position) {
        Glide.with(this)
                .load(new File(imagePaths.get(position)))
                .into(ivImage);
    }

    public void finishedVideoConvet() {

        int nSaveNumber = Constants.getIntValue(activity, Constants.TOTAL_COUNT_GIF);

        String strVideoOldName = Constants.GIF_FOLDER_PATH + String.format("/%s.gif", Constants.MY_GIF);
        String strVideoNewName = Constants.GIF_FOLDER_PATH + String.format("/%s%d.gif", Constants.MY_GIF, nSaveNumber);
        File file = new File(strVideoOldName);

        if (file.exists())
            file.renameTo(new File(strVideoNewName));

        Constants.putIntValue(activity, Constants.SAVE_GIF + nSaveNumber, 1);
        nSaveNumber++;
        Constants.putIntValue(activity, Constants.TOTAL_COUNT_GIF, nSaveNumber);

//        relativeImagesToGif.setVisibility(View.GONE);
//        ivPreview.setVisibility(View.VISIBLE);
//        Glide.with(this).asGif().load(strVideoNewName).into(ivPreview);

        activity.onBackPressed();
        Constants.SELECTED = Constants.GALLERY_FRAGMENT;
        ((MainActivity) activity).loadFragment(Constants.TYPE_GIF);
    }

    private void initView(View view) {

        fabDone = (FloatingActionButton) view.findViewById(R.id.fabDone);
        rclImages = view.findViewById(R.id.rclImages);
        rclImages.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        rclImages.setItemAnimator(new DefaultItemAnimator());
        ivImage = (ImageView) view.findViewById(R.id.ivImage);
        ivPreview = view.findViewById(R.id.ivPreview);

        ivPreview.setVisibility(View.GONE);

        fabDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nProgress = 0;
                mlDialog = new ProgressDialog(activity);
                mlDialog.setCancelable(false);
                mlDialog.setCanceledOnTouchOutside(false);
                mlDialog.setTitle("Processing Images to GIF");
                mlDialog.setMessage(getString(R.string.please_wait));
                mlDialog.setMax(100);
                mlDialog.setIndeterminate(false);
                mlDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mlDialog.show();

                String strGifName = Constants.GIF_FOLDER_PATH + String.format("/%s.gif", Constants.MY_GIF);
                File file = new File(strGifName);

                GifMakeService.startMakingImagesToGif(activity, imagePaths, file.getAbsolutePath());
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mReceiver != null)
            LocalBroadcastManager.getInstance(activity).unregisterReceiver(mReceiver);
    }
}
