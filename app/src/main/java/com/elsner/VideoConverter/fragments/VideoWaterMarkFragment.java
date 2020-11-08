package com.elsner.VideoConverter.fragments;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.cardview.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.elsner.VideoConverter.MainActivity;
import com.elsner.VideoConverter.R;
import com.elsner.VideoConverter.Utils.Constants;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.vlk.multimager.activities.GalleryActivity;
import com.vlk.multimager.activities.MultiCameraActivity;
import com.vlk.multimager.utils.Image;
import com.vlk.multimager.utils.Params;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoWaterMarkFragment extends Fragment implements View.OnClickListener {


    private static final String TAG = "videoWaterMark";
    VideoView videoView;
    String videoPath = "";
    ProgressDialog mlDialog;
    ImageView img_play, img_pause, img_preview, image_framePreview;
    TextView txt_convert, txt_showLocation, txt_imagePreview;
    RelativeLayout relative_preview;
    String picturepPath = "";
    String overLayString = "";
    BottomSheetDialog dialog;
    int videoHeight, videoWidth;
    Bitmap bitmap = null;
    AppCompatSpinner spinnerFormat;
    String[] formats = new String[]{"ultrafast", "veryfast", "faster", "fast", "medium", "slow", "slower", "veryslow"};
    Activity activity;
    FFmpeg ffmpeg;
    public VideoWaterMarkFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_video_water_mark, container, false);
        videoView = rootView.findViewById(R.id.videoView);
        img_play = rootView.findViewById(R.id.img_play);
        img_pause = rootView.findViewById(R.id.img_pause);
        img_preview = rootView.findViewById(R.id.img_preview);
        txt_convert = rootView.findViewById(R.id.txt_convert);
        txt_showLocation = rootView.findViewById(R.id.txt_showLocation);
        relative_preview = rootView.findViewById(R.id.relative_preview);
        image_framePreview = rootView.findViewById(R.id.image_framePreview);
        txt_imagePreview = rootView.findViewById(R.id.txt_imagePreview);
        spinnerFormat = rootView.findViewById(R.id.spinnerFormat);
        if (getArguments() != null) {
            videoPath = getArguments().getString("video");
        }
        activity=getActivity();
        Glide.with(getActivity())
                .load(videoPath) // or URI/path
                .into(img_preview); //imageview to set thumbnail to

        img_preview.setVisibility(View.VISIBLE);


        try {
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(videoPath);
            mp.prepare();
            videoWidth = mp.getVideoWidth();
            videoHeight = mp.getVideoHeight();
        } catch (IOException e) {
            e.printStackTrace();
        }

        videoView.setVisibility(View.GONE);
        videoView.setVideoPath(videoPath);

        Log.d(TAG, "onCreateView: " + videoHeight + " " + videoWidth);

        txt_convert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!picturepPath.isEmpty() && !overLayString.isEmpty())
                    processVideo();
                else
                    Toast.makeText(getActivity(), "Please Select Image and Positiob", Toast.LENGTH_SHORT);
            }
        });
        img_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayVideo();
            }
        });


        relative_preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openCameraDialog();
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                img_pause.setVisibility(View.GONE);
                img_play.setVisibility(View.VISIBLE);
                img_preview.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.GONE);
            }
        });

        img_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (videoView.isPlaying()) {
                    videoView.pause();
                    img_pause.setVisibility(View.INVISIBLE);
                    img_play.setVisibility(View.VISIBLE);
                }
            }
        });

        txt_showLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        setSpinnerFormat();
        return rootView;
    }

    private void setSpinnerFormat() {

        ArrayAdapter<String> aa = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, formats);
        spinnerFormat.setAdapter(aa);
        spinnerFormat.setSelected(true);
        spinnerFormat.setSelection(0);
    }

    void openCameraDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
        bottomSheetDialog.setContentView(R.layout.dialog_chooser);
        CardView ivCamera = bottomSheetDialog.findViewById(R.id.cardCamera);
        CardView ivGallery = bottomSheetDialog.findViewById(R.id.cardGallery);

        ivCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)) {
                    Constants.verifyCameraPermissions(getActivity());
                } else {
                    openCamera(true);
                }
                bottomSheetDialog.dismiss();
            }
        });

        ivGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Constants.verifyStoragePermissions(getActivity());
                } else {
                    openCamera(false);
                }
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.show();
    }


    void openCamera(boolean isCamera) {
        if (isCamera)
            getImagesFromCamera();
        else
            getImagesFromGallery();
    }

    private void getImagesFromCamera() {

        Intent intent = new Intent(getActivity(), MultiCameraActivity.class);
        Params params = new Params();
        params.setCaptureLimit(1);
//        params.setToolbarColor(selectedColor);
//        params.setActionButtonColor(selectedColor);
//        params.setButtonTextColor(selectedColor);
        intent.putExtra(Constants.KEY_PARAMS, params);
        startActivityForResult(intent, Constants.REQUEST_TAKE_MULTI_IMAGES);
    }

    private void getImagesFromGallery() {

        Intent intent = new Intent(getActivity(), GalleryActivity.class);
        Params params = new Params();
        params.setCaptureLimit(1);
        params.setPickerLimit(1);
        intent.putExtra(Constants.KEY_PARAMS, params);
        startActivityForResult(intent, Constants.REQUEST_GALLERY_MULTI_IMAGES);

    }

    void openDialog() {
        TextView txt_tple, txt_tprt, txt_bmlt, txt_bmrt;
        dialog = new BottomSheetDialog(getActivity());
        dialog.setContentView(R.layout.dialog_position);
        txt_tple = dialog.findViewById(R.id.txt_tple);
        txt_tprt = dialog.findViewById(R.id.txt_tprt);
        txt_bmlt = dialog.findViewById(R.id.txt_bmlt);
        txt_bmrt = dialog.findViewById(R.id.txt_bmrt);


        txt_tple.setOnClickListener(this);
        txt_tprt.setOnClickListener(this);
        txt_bmlt.setOnClickListener(this);
        txt_bmrt.setOnClickListener(this);

        dialog.setCancelable(true);
        dialog.show();
    }

    void PlayVideo() {

        videoView.start();
        videoView.setVisibility(View.VISIBLE);
        img_preview.setVisibility(View.GONE);
        img_play.setVisibility(View.GONE);
        img_pause.setVisibility(View.VISIBLE);
    }


    @SuppressLint("CheckResult")
    private void processVideo() {


        final String format = "mp4";
        String strVideoPath = Constants.VIDEO_FOLDER_PATH + String.format("/%s.%s", Constants.MY_VIDEO, format);

        String preset = spinnerFormat.getSelectedItem().toString();
        int width = (int) (videoWidth * 0.15);
        int height = (int) (videoHeight * 0.15);
        Bitmap out = Bitmap.createScaledBitmap(bitmap, width, height, true);

        File file = new File(Constants.IMAGE_FOLDER_PATH, "resize.png");
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(file);
            out.compress(Bitmap.CompressFormat.PNG, 70, fOut);
            fOut.flush();
            fOut.close();
            out.recycle();
        } catch (Exception e) {
        }

        Log.d(TAG, "processVideo: " + file.getAbsolutePath());

        final String cmd[] = new String[]{

                "-i",
                videoPath,
                "-i", file.getAbsolutePath(),
                "-filter_complex",
                overLayString,
//                "-filter_complex", overLayString,
                "-preset",
                preset,
                strVideoPath
        };
        ffmpeg = FFmpeg.getInstance(getActivity());
        try {
            ffmpeg.execute(cmd, new FFmpegExecuteResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    Log.d(TAG, "onSuccess() called with: message = [" + message + "]");
                    if (mlDialog.isShowing())
                        mlDialog.dismiss();

                }

                @Override
                public void onProgress(String message) {
                    Log.d(TAG, "onProgress() called with: message = [" + message + "]");

//                    Toast.makeText(getActivity(), "Success: " + message, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String message) {
                    Log.d(TAG, "onFailure() called with: message = [" + message + "]");
                    if (mlDialog.isShowing())
                        mlDialog.dismiss();

                    Toast.makeText(getActivity(), "failed to Make waterMarkVideo", Toast.LENGTH_SHORT).show();


                }

                @Override
                public void onStart() {
                    Log.d(TAG, "onStart() called");

                    mlDialog = new ProgressDialog(getActivity());
                    mlDialog.setCancelable(false);
                    mlDialog.setCanceledOnTouchOutside(false);
                    mlDialog.setTitle("Processing Video");
                    mlDialog.setMessage(getString(R.string.please_wait));
                    mlDialog.show();
                }

                @Override
                public void onFinish() {
                    if (mlDialog != null && mlDialog.isShowing())
                        mlDialog.cancel();
                    finishedVideoConvet(format);
                    Log.d(TAG, "onFinish: ");
                    if (ffmpeg.isFFmpegCommandRunning()) {
                        ffmpeg.killRunningProcesses();
                    }
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
            if (ffmpeg.isFFmpegCommandRunning()) {
                ffmpeg.killRunningProcesses();
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constants.REQUEST_TAKE_MULTI_IMAGES:
                    ArrayList<Image> imagesList1 = data.getParcelableArrayListExtra(Constants.KEY_BUNDLE_LIST);
                    getImagePaths(imagesList1);
                    break;
                case Constants.REQUEST_GALLERY_MULTI_IMAGES:
                    ArrayList<Image> imagesList2 = data.getParcelableArrayListExtra(Constants.KEY_BUNDLE_LIST);
                    getImagePaths(imagesList2);
                    break;
            }
        }
    }

    private void getImagePaths(ArrayList<Image> imagesList) {
        ArrayList<String> imagePaths = new ArrayList<>();
        for (int i = 0; i < imagesList.size(); i++) {
            imagePaths.add(imagesList.get(i).imagePath);
        }
        if (imagePaths.size() != 0)
            picturepPath = imagePaths.get(0);

        if (picturepPath.isEmpty()) {
            txt_imagePreview.setVisibility(View.VISIBLE);
            image_framePreview.setVisibility(View.GONE);
        } else {
            txt_imagePreview.setVisibility(View.GONE);
            image_framePreview.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .asBitmap()
                    .load(picturepPath)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            bitmap = resource;
                            image_framePreview.setImageBitmap(resource);
                        }
                    });


        }

    }

    public void finishedVideoConvet(String format) {

        int nSaveNumber = Constants.getIntValue(activity, Constants.TOTAL_COUNT_VIDEO);

        String strVideoOldName = Constants.VIDEO_FOLDER_PATH + String.format("/%s.%s", Constants.MY_VIDEO, format);
        String strVideoNewName = Constants.VIDEO_FOLDER_PATH + String.format("/%s%d.%s", Constants.MY_VIDEO, nSaveNumber, format);
        File file = new File(strVideoOldName);

        if (file.exists())
            file.renameTo(new File(strVideoNewName));

        Constants.putIntValue(activity, Constants.SAVE_VIDEO + nSaveNumber, 1);
        nSaveNumber++;
        Constants.putIntValue(activity, Constants.TOTAL_COUNT_VIDEO, nSaveNumber);

        activity.onBackPressed();
        Constants.SELECTED = Constants.GALLERY_FRAGMENT;
        ((MainActivity) activity).loadFragment(Constants.TYPE_VIDEO);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_tple:
                overLayString = "overlay=10:10";
                txt_showLocation.setText(R.string.top_left);
                break;
            case R.id.txt_tprt:
                overLayString = "overlay=main_w-overlay_w-10";
                txt_showLocation.setText(R.string.top_right);
                break;
            case R.id.txt_bmlt:
                overLayString = "overlay=10:main_h-overlay_h-10";
                txt_showLocation.setText(R.string.bottom_left);
                break;
            case R.id.txt_bmrt:
                overLayString = "overlay=main_w-overlay_w-10:main_h-overlay_h-10";
                txt_showLocation.setText(R.string.bottom_right);
                break;

        }

        dialog.dismiss();
    }
}
