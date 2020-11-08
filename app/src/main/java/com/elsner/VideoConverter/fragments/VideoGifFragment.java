package com.elsner.VideoConverter.fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.elsner.VideoConverter.MainActivity;
import com.elsner.VideoConverter.R;
import com.elsner.VideoConverter.Utils.Constants;
import com.elsner.VideoConverter.Utils.GifMakeService;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoGifFragment extends Fragment {

    public final String TAG = "VideoGifFragment";
    public Activity activity;
    public int m_nAngle = 0;
    RelativeLayout relativeVideoGif;
    ImageView ivPreview;
    public int m_nSeekStart = 0, startTime = 0;
    public int m_nSeekEnd = 0, endTime = 0;
    int MAX_SEEK = 100;
    LinearLayout m_bottomBar;
    Button m_btnPlay;
    FloatingActionButton fabExpand, fabCollapse, fabDone;
    TextView m_txtPlayTime;
    TextView m_txtTotalTime;
    int m_nDuration = 0;
    CrystalRangeSeekbar m_rangeSeekbar;
    VideoView m_video;
    boolean m_bShowVideoController;
    boolean isFirstTime = true;
    boolean m_running = false;
    boolean m_runningReverse = false;
    ProgressDialog mlDialog;
    int nProgress;
    int total = 0, current = 0;
    String videoPath = "";
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
    public VideoGifFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) activity).setTitle(R.string.video_to_gif);
        ((MainActivity) activity).setDrawerState(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_video_gif, container, false);

        activity = getActivity();

        LocalBroadcastManager.getInstance(activity)
                .registerReceiver(mReceiver, new IntentFilter(GifMakeService.ACTION_MAKE_GIF_FROM_VIDEO));

        if (getArguments() != null) {
            videoPath = getArguments().getString("video");
        }

        init(v);

        return v;
    }

    public void init(View view) {

        m_bottomBar = (LinearLayout) view.findViewById(R.id.bottom_bar);
        m_btnPlay = (Button) view.findViewById(R.id.tutorialplay);
        fabExpand = (FloatingActionButton) view.findViewById(R.id.fabExpand);
        fabCollapse = (FloatingActionButton) view.findViewById(R.id.fabCollapse);
        fabDone = (FloatingActionButton) view.findViewById(R.id.fabDone);
        m_txtPlayTime = (TextView) view.findViewById(R.id.txt_playtime);
        m_txtTotalTime = (TextView) view.findViewById(R.id.txt_totaltime);
        m_rangeSeekbar = (CrystalRangeSeekbar) view.findViewById(R.id.rangeSeekBar);
        m_video = (VideoView) view.findViewById(R.id.videoView);
        relativeVideoGif = view.findViewById(R.id.relativeVideoGif);
        ivPreview = view.findViewById(R.id.ivPreview);

        ivPreview.setVisibility(View.GONE);

        m_btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playVideo();
                m_video.seekTo(m_nDuration * m_nSeekStart / MAX_SEEK);
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

                    if (Build.VERSION.SDK_INT >= 17) {
                        MediaMetadataRetriever m = new MediaMetadataRetriever();
                        m.setDataSource(videoPath);
                        Bitmap thumbnail = m.getFrameAtTime();
                        String s = m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION).trim();
                        m_nAngle = Integer.parseInt(s);
                        Log.e("Rotation", s);
                    }

                    getFrames(videoPath);
                }
            }
        });

        m_rangeSeekbar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {

                if (m_runningReverse)
                    return;

                int nMin = minValue.intValue();
                int nMax = maxValue.intValue();

                if (m_nSeekStart != nMin) {
                    startTime = m_nDuration * nMin / MAX_SEEK;
                    Log.d(TAG, "setRangeChangeListener: start: " + startTime);
                    m_video.seekTo(startTime);
                    m_txtPlayTime.setText(Constants.getFomattedTime((int) (m_nDuration * nMin / MAX_SEEK)));
                } else if (m_nSeekEnd != nMax) {
                    endTime = m_nDuration * nMax / MAX_SEEK;
                    Log.d(TAG, "setRangeChangeListener: end: " + endTime);
                    m_video.seekTo(endTime);
                    m_txtTotalTime.setText(Constants.getFomattedTime((int) (m_nDuration * nMax / MAX_SEEK)));
                }

                m_nSeekStart = nMin;
                m_nSeekEnd = nMax;
            }
        });

        m_nSeekStart = 0;
        m_nSeekEnd = MAX_SEEK;

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

    void prepareVideo()
    {
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
                                    if (m_video.isPlaying() && m_video.getCurrentPosition() >= m_nDuration * m_nSeekEnd / MAX_SEEK)
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

    void completeVideo() {
        m_video.pause();
        m_btnPlay.setBackgroundResource(R.drawable.ic_play);
        m_bottomBar.setVisibility(View.VISIBLE);
        m_bShowVideoController = true;
    }

    void getFrames(String strVidePaht) {
        File videoFile = new File(strVidePaht);
        if (!videoFile.exists())
            return;

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(activity, Uri.fromFile(videoFile));
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(time);
        if (timeInMillisec < 1000)
            return;

        nProgress = 0;
        mlDialog = new ProgressDialog(activity);
        mlDialog.setCancelable(false);
        mlDialog.setCanceledOnTouchOutside(false);
        mlDialog.setTitle("Processing Video to GIF");
        mlDialog.setMessage(getString(R.string.please_wait));
        mlDialog.setMax(100);
        mlDialog.setIndeterminate(false);
        mlDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mlDialog.show();

        String strGifName = Constants.GIF_FOLDER_PATH + String.format("/%s.gif", Constants.MY_GIF);
        File file = new File(strGifName);
        int duration = (int) (timeInMillisec / 1000);

        if (endTime == 0) endTime = m_nDuration;
        Log.d(TAG, "onCreate: path: " + videoPath + " start: " + startTime + " end: " + endTime + " dur: " + duration);
        GifMakeService.startMakingVideoToGif(activity, videoPath, file.getAbsolutePath(), startTime, endTime, 300);
    }

    public Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
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

//        relativeVideoGif.setVisibility(View.GONE);
//        ivPreview.setVisibility(View.VISIBLE);
//
//        Glide.with(this).asGif().load(strVideoNewName).into(ivPreview);

        activity.onBackPressed();
        Constants.SELECTED = Constants.GALLERY_FRAGMENT;
        ((MainActivity) activity).loadFragment(Constants.TYPE_GIF);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mReceiver != null)
            LocalBroadcastManager.getInstance(activity).unregisterReceiver(mReceiver);
    }
}