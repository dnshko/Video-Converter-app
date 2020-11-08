package com.elsner.VideoConverter.fragments;


import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.crystal.crystalrangeseekbar.interfaces.OnSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar;
import com.elsner.VideoConverter.MainActivity;
import com.elsner.VideoConverter.R;
import com.elsner.VideoConverter.Utils.Constants;

import java.io.File;
import java.io.FileOutputStream;

/**
 * A simple {@link Fragment} subclass.
 */
public class CaptureImageFragment extends Fragment {

    public final String TAG = "CaptureImageFragment";
    public Activity activity;
    public int m_nAngle = 0;
    public int mSeekValue = 0, time = 0;
    ImageView ivPreview;
    RelativeLayout relativeCaptureImage;
    int MAX_SEEK = 100;
    LinearLayout m_bottomBar;
    FloatingActionButton fabExpand, fabCollapse, fabDone;
    Button m_btnPlay;
    TextView m_txtPlayTime;
    TextView m_txtTotalTime;
    int m_nDuration = 0;
    CrystalSeekbar mSeekBar;
    VideoView m_video;
    boolean m_bShowVideoController;
    boolean isFirstTime = true;
    boolean m_running = false;
    boolean m_runningReverse = false;
    String videoPath = "";

    public CaptureImageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) activity).setTitle(R.string.capture_image);
        ((MainActivity) activity).setDrawerState(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_capture_image, container, false);

        activity = getActivity();

        if (getArguments() != null) {
            videoPath = getArguments().getString("video");
        }

        init(v);

        return v;
    }

    public void init(View view) {

        fabExpand = (FloatingActionButton) view.findViewById(R.id.fabExpand);
        fabCollapse = (FloatingActionButton) view.findViewById(R.id.fabCollapse);
        fabDone = (FloatingActionButton) view.findViewById(R.id.fabDone);
        m_bottomBar = (LinearLayout) view.findViewById(R.id.bottom_bar);
        m_btnPlay = (Button) view.findViewById(R.id.tutorialplay);
        m_txtPlayTime = (TextView) view.findViewById(R.id.txt_playtime);
        mSeekBar = (CrystalSeekbar) view.findViewById(R.id.seekBar);
        m_txtTotalTime = (TextView) view.findViewById(R.id.txt_totaltime);
        m_video = (VideoView) view.findViewById(R.id.videoView);
        ivPreview = view.findViewById(R.id.ivPreview);
        relativeCaptureImage = view.findViewById(R.id.relativeCaptureImage);

        ivPreview.setVisibility(View.GONE);

        m_btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playVideo();
                m_video.seekTo(m_nDuration * mSeekValue / MAX_SEEK);
            }
        });

        fabExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_bottomBar.setVisibility(View.GONE);
                m_bShowVideoController = false;
                fabCollapse.setVisibility(View.VISIBLE);
            }
        });

        fabCollapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_bottomBar.setVisibility(View.VISIBLE);
                m_bShowVideoController = true;
                fabCollapse.setVisibility(View.GONE);
            }
        });

        fabDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!videoPath.equals("")) {

                    completeVideo();
                    m_nAngle = 0;
                    m_runningReverse = true;

//                    m_btnBack.setEnabled(false);
//                    fabDone.setEnabled(false);
//                    m_btnPlay.setEnabled(false);
//                    fabExpand.setEnabled(false);

                    MediaMetadataRetriever m = new MediaMetadataRetriever();
                    m.setDataSource(videoPath);
                    Bitmap thumbnail = m.getFrameAtTime(time * 1000);

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        String s = m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION).trim();
                        m_nAngle = Integer.parseInt(s);
                        Log.e("Rotation", s);
                    }

                    String strJpgName = Constants.IMAGE_FOLDER_PATH + String.format("/%s.jpg", Constants.MY_IMAGE);
                    File file = new File(strJpgName);

                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        thumbnail.compress(Bitmap.CompressFormat.JPEG, 85, fileOutputStream);
                        fileOutputStream.flush();
                        fileOutputStream.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    finishedCaptureImage();
                }
            }
        });

        mSeekBar.setOnSeekbarChangeListener(new OnSeekbarChangeListener() {
            @Override
            public void valueChanged(Number value) {

                if (m_runningReverse)
                    return;

                int val = value.intValue();

                if (mSeekValue != val) {
                    time = m_nDuration * val / MAX_SEEK;
                    Log.d(TAG, "setRangeChangeListener: time: " + time);
                    m_video.seekTo(time);
                    m_txtPlayTime.setText(Constants.getFomattedTime((int) (m_nDuration * val / MAX_SEEK)));
                }

                mSeekValue = val;
            }
        });

        mSeekValue = 0;

        prepareVideo();
    }

    void playVideo() {
        if (m_video.isPlaying()) {
            m_video.pause();
            m_btnPlay.setBackgroundResource(R.drawable.ic_play);
        } else {
            m_video.start();
            m_btnPlay.setBackgroundResource(R.drawable.ic_pause);
        }
    }

    void prepareVideo() {
        if (m_video.isPlaying())
            m_video.seekTo(0);

        m_video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                m_running = true;
                final int duration = m_video.getDuration();
                m_nDuration = mp.getDuration();
                m_txtTotalTime.setText(Constants.getFomattedTime(duration));
                new Thread(new Runnable() {
                    public void run() {
                        do {
                            m_txtPlayTime.post(new Runnable() {
                                public void run() {
                                    //duration -
                                    if (m_video.isPlaying() && m_video.getCurrentPosition() >= m_nDuration)
                                        completeVideo();
                                    if (isFirstTime) {
                                        m_video.seekTo(0);
                                        isFirstTime = false;
                                    }
                                }
                            });
                            try {
                                Thread.sleep(30);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (!m_running) break;
                        }
                        while (m_video.getCurrentPosition() < duration);
                    }
                }).start();
            }
        });

        m_video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                completeVideo();
            }
        });

        m_video.setVideoPath(videoPath);
        m_txtTotalTime.setText(Constants.getFomattedTime(m_video.getDuration()));

    }

    public void finishedCaptureImage() {

        int nSaveNumber = Constants.getIntValue(activity, Constants.TOTAL_COUNT_IMAGE);

        String strImageOldName = Constants.IMAGE_FOLDER_PATH + String.format("/%s.jpg", Constants.MY_IMAGE);
        String strImageNewName = Constants.IMAGE_FOLDER_PATH + String.format("/%s%d.jpg", Constants.MY_IMAGE, nSaveNumber);
        File file = new File(strImageOldName);

        if (file.exists())
            file.renameTo(new File(strImageNewName));

        Constants.putIntValue(activity, Constants.SAVE_IMAGE + nSaveNumber, 1);
        nSaveNumber++;
        Constants.putIntValue(activity, Constants.TOTAL_COUNT_IMAGE, nSaveNumber);

//        relativeCaptureImage.setVisibility(View.GONE);
//        ivPreview.setVisibility(View.VISIBLE);
//        Glide.with(this).asBitmap().load(strImageNewName).into(ivPreview);

        activity.onBackPressed();
        Constants.SELECTED = Constants.GALLERY_FRAGMENT;
        ((MainActivity) activity).loadFragment(Constants.TYPE_IMAGE);
    }

    void completeVideo() {
        m_video.pause();
        m_btnPlay.setBackgroundResource(R.drawable.ic_play);
        m_bottomBar.setVisibility(View.VISIBLE);
        m_bShowVideoController = true;
    }

}
