package com.elsner.VideoConverter.fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.cardview.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.elsner.VideoConverter.MainActivity;
import com.elsner.VideoConverter.R;
import com.elsner.VideoConverter.Utils.Constants;
import com.elsner.VideoConverter.Utils.TimeEditText;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import mobi.upod.timedurationpicker.TimeDurationPicker;
import mobi.upod.timedurationpicker.TimeDurationPickerDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoCutterFragment extends Fragment {

    public final String TAG = "VideoCutterFragment";
    public Activity activity;
    public int m_nAngle = 0;
    public int m_nSeekStart = 0, startTime = 0;
    public int m_nSeekEnd = 0, endTime = 0;
    RelativeLayout relativeVideoGif;
    ImageView ivPreview;
    CardView video_bar;
    LinearLayout m_bottomBar;
    Button m_btnPlay;
    FloatingActionButton fabExpand, fabCollapse, fabDone;
    TextView m_txtPlayTime;
    TextView m_txtTotalTime;
    int m_nDuration = 0;
    float gap = 0;
    CrystalRangeSeekbar m_rangeSeekbar;
    VideoView m_video;
    boolean m_bShowVideoController;
    boolean isFirstTime = true;
    boolean m_running = false;
    boolean m_runningReverse = false;
    ProgressDialog mlDialog;
    int nProgress;
    String videoPath = "";
    AppCompatSpinner spinnerFormat, spinnerQuality;
    String[] formats = new String[]{"ultrafast", "veryfast", "faster", "fast", "medium", "slow", "slower", "veryslow"};
    String[] qualitys = new String[]{"320x160", "480x270", "640x360", "800x450", "960x540", "1280x720", "1920x1080"};
    FFmpeg ffmpeg;
    TimeEditText edtMinutes;

    public VideoCutterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.setTitle(R.string.video_cutter);
        ((MainActivity) activity).setDrawerState(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_video_cutter, container, false);

        activity = getActivity();

        if (getArguments() != null) {
            videoPath = getArguments().getString("video");
        }

        init(v);

        return v;
    }

    public void init(View view) {

        video_bar = view.findViewById(R.id.video_bar);
        m_bottomBar = view.findViewById(R.id.bottom_bar);
        m_btnPlay = view.findViewById(R.id.tutorialplay);
        fabExpand = view.findViewById(R.id.fabExpand);
        fabCollapse = view.findViewById(R.id.fabCollapse);
        fabDone = view.findViewById(R.id.fabDone);
        m_txtPlayTime = view.findViewById(R.id.txt_playtime);
        m_txtTotalTime = view.findViewById(R.id.txt_totaltime);
        m_rangeSeekbar = view.findViewById(R.id.rangeSeekBar);
        m_video = view.findViewById(R.id.videoView);
        relativeVideoGif = view.findViewById(R.id.relativeVideoGif);
        ivPreview = view.findViewById(R.id.ivPreview);
        spinnerFormat = view.findViewById(R.id.spinnerFormat);
        spinnerQuality = view.findViewById(R.id.spinnerQuality);
        edtMinutes = view.findViewById(R.id.edtMinutes);

        setSpinnerFormat();
        setSpinnerQuality();

        ivPreview.setVisibility(View.GONE);

        m_btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playVideo();
                m_video.seekTo(m_nSeekStart);
            }
        });

        m_txtPlayTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TimeDurationPickerDialog dialog = new TimeDurationPickerDialog(activity, new TimeDurationPickerDialog.OnDurationSetListener() {
                    @Override
                    public void onDurationSet(TimeDurationPicker view, long duration) {

                        Log.d(TAG, "onDurationSet: "+duration);
                        if (duration >= m_nDuration) {
                            Toast.makeText(activity, "Please select duration within of video length", Toast.LENGTH_SHORT).show();
                        } else if (endTime != 0 && duration >= endTime) {
                            Toast.makeText(activity, "Please select duration within of video length", Toast.LENGTH_SHORT).show();
                        } else {
                            m_rangeSeekbar.setMinStartValue(duration);
                            m_rangeSeekbar.setMaxStartValue(endTime == 0 ? m_nDuration : endTime);
                            m_rangeSeekbar.apply();
                        }
                    }
                },0, TimeDurationPicker.MM_SS);

                dialog.show();

            }
        });

        m_txtTotalTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TimeDurationPickerDialog dialog = new TimeDurationPickerDialog(activity, new TimeDurationPickerDialog.OnDurationSetListener() {
                    @Override
                    public void onDurationSet(TimeDurationPicker view, long duration) {

                        Log.d(TAG, "onDurationSet: "+duration);
                        if (duration >= m_nDuration || duration <= startTime) {
                            Toast.makeText(activity, "Please select duration within of video length", Toast.LENGTH_SHORT).show();
                        } else {
                            m_rangeSeekbar.setMinStartValue(startTime);
                            m_rangeSeekbar.setMaxStartValue(duration);
                            m_rangeSeekbar.apply();
                        }
                    }
                },0, TimeDurationPicker.MM_SS);

                dialog.show();

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

                    if (Build.VERSION.SDK_INT >= 17) {
                        MediaMetadataRetriever m = new MediaMetadataRetriever();
                        m.setDataSource(videoPath);
                        Bitmap thumbnail = m.getFrameAtTime();
                        String s = m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION).trim();
                        m_nAngle = Integer.parseInt(s);
                        Log.e("Rotation", s);
                    }

                    if (endTime == 0) endTime = m_nDuration;
                    int start = startTime / 1000;
                    int total = (endTime - startTime) / 1000;

                    convertVideo(start, total);
                }
            }
        });

        edtMinutes.setOnActionDone(new TimeEditText.OnActionDone() {
            @Override
            public void getActionDone() {

                String time = edtMinutes.getText().toString();
                int minutes = Integer.parseInt(time.split(":")[0]);
                int seconds = Integer.parseInt(time.split(":")[1]);

                int totalms = (minutes * 60 * 1000) + (seconds * 1000);

                if (totalms > m_nDuration || totalms == 0) {
                    Toast.makeText(activity, "please enter duration gap within of video length", Toast.LENGTH_SHORT).show();
                    edtMinutes.setHour(0);
                    edtMinutes.setMinutes(0);
                    gap = 0;
                    m_rangeSeekbar.setFixGap(gap);
                } else {
                    gap = totalms;
                    Log.d(TAG, "getActionDone: " + gap);
                    m_rangeSeekbar.setFixGap(gap).apply();
                }
            }
        });

        MediaPlayer mp = MediaPlayer.create(activity, Uri.parse(videoPath));
        int duration = mp.getDuration();
        mp.release();
        m_nDuration = duration;
        m_nSeekStart = 0;
        m_nSeekEnd = m_nDuration;
        Log.d(TAG, "init: seekEnd: "+m_nSeekEnd);
        m_rangeSeekbar.setMinValue(m_nSeekStart);
        m_rangeSeekbar.setMaxValue(m_nSeekEnd);
        m_rangeSeekbar.setMinStartValue(m_nSeekStart);
        m_rangeSeekbar.setMaxStartValue(m_nSeekEnd);
        m_rangeSeekbar.apply();

        m_rangeSeekbar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {

                if (m_runningReverse)
                    return;

                int nMin = minValue.intValue();
                int nMax = maxValue.intValue();

                if (m_nSeekStart != nMin) {

//                    float value1 = m_nDuration * nMin / MAX_SEEK;
//                    float value2 = value1 / 1000;
//                    startTime = Math.round(value2) * 1000;
                    startTime = nMin;
                    Log.d(TAG, "setRangeChangeListener: start: " + startTime);
                    m_video.seekTo(startTime);
                    m_txtPlayTime.setText(Constants.getFomattedTime(startTime));

                } else if (m_nSeekEnd != nMax) {

//                    float value1 = m_nDuration * nMax / MAX_SEEK;
//                    float value2 = value1 / 1000;
//                    endTime = Math.round(value2) * 1000;
                    endTime = nMax;
                    Log.d(TAG, "setRangeChangeListener: end: " + endTime);
                    m_video.seekTo(endTime);
                    m_txtTotalTime.setText(Constants.getFomattedTime(endTime));
                }

                m_nSeekStart = nMin;
                m_nSeekEnd = nMax;
            }
        });

        ffmpeg = FFmpeg.getInstance(activity);
        prepareVideo();
    }

    private void setSpinnerFormat() {

        ArrayAdapter<String> aa = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, formats);
        spinnerFormat.setAdapter(aa);
        spinnerFormat.setSelected(true);
        spinnerFormat.setSelection(0);
    }

    private void setSpinnerQuality() {

        ArrayAdapter<String> aa = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, qualitys);
        spinnerQuality.setAdapter(aa);
        spinnerQuality.setSelected(true);
        spinnerQuality.setSelection(2);
    }

    void convertVideo(int start, final int total) {

        final String format = "mp4";
        String preset = spinnerFormat.getSelectedItem().toString();
        String resolution = spinnerQuality.getSelectedItem().toString();

        String strVideoPath = Constants.VIDEO_FOLDER_PATH + String.format("/%s.%s", Constants.MY_VIDEO, format);
        Log.d(TAG, "ss: " + start + " t: " + total + " f: " + format + " ab: " + resolution);

        try {
            // to execute "ffmpeg -version" command you just need to pass "-version"
            // ffmpeg -i video.mp4 -f mp3 -ab 192000 -vn music.mp3

            mlDialog = new ProgressDialog(activity);
            mlDialog.setCanceledOnTouchOutside(false);
            mlDialog.setCancelable(false);
            mlDialog.setTitle("Processing Video");
            mlDialog.setMessage(getString(R.string.please_wait));
            mlDialog.setMax(100);
            mlDialog.setIndeterminate(false);
            mlDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

            final String cmd[] = new String[]{
                    "-i", // input path
                    videoPath,
                    "-ss", // start time
                    String.valueOf(start),
                    "-t", // duration
                    String.valueOf(total),
//                    "-f", // output format
//                    format,
//                    "-b:v",
//                    "1200",
                    "-s",
                    resolution,
//                    "-c:a",
//                    "copy",
//                    "-vcodec",
//                    "copy",
                    "-preset",
                    preset,
                    strVideoPath // output path
            };

            ffmpeg.execute(cmd, new FFmpegExecuteResponseHandler() {

                @Override
                public void onStart() {
                    mlDialog.show();
                    Log.d(TAG, "onStart: "+cmd);
                }

                @Override
                public void onProgress(String message) {
                    Log.d(TAG, "onProgress: "+message);

                    String after = message.trim().replaceAll("[ ]{2,}", "");

                    if (message.contains("frame"))
                    {
                        String[] arr = after.split(" ");
                        for (int i = 0; i < arr.length; i++) {
                            Log.d(TAG, "onProgress: "+arr[i]+"\n");
                            String[] keyValue = arr[i].split("=");
                            if (keyValue[0].equals("time"))
                            {
                                String time = keyValue[1];

                                int seconds;
                                int hours;
                                int minutes;
                                int totalSeconds = 1;
                                SimpleDateFormat fmt = new SimpleDateFormat("hh:mm:ss.SS", Locale.getDefault());

                                try {
                                    Date date = fmt.parse(time);
                                    seconds = date.getSeconds();
                                    hours = date.getHours();
                                    minutes = date.getMinutes();
                                    totalSeconds = (hours * 60 * 60) + (minutes * 60) + (seconds);
                                }
                                catch(ParseException pe) {
                                    pe.printStackTrace();
                                }

                                Log.d(TAG, "onReceive: sec: " + totalSeconds);
                                nProgress = (totalSeconds * 100) / total;
                                Log.d(TAG, "onReceive: per: " + nProgress);
                                mlDialog.setProgress(nProgress);
                            }
                            if (keyValue[0].equals("size")){

                                String size = keyValue[1];
                                mlDialog.setMessage(size);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(String message) {
                    if (mlDialog != null && mlDialog.isShowing())
                        mlDialog.cancel();
                    Toast.makeText(activity, "failed", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onFailure: "+message);
                    if (ffmpeg.isFFmpegCommandRunning()) {
                        ffmpeg.killRunningProcesses();
                    }
                }

                @Override
                public void onSuccess(String message) {
                    Log.d(TAG, "onSuccess: "+message);
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
            // Handle if FFmpeg is already running
            e.printStackTrace();
            if (ffmpeg.isFFmpegCommandRunning()) {
                ffmpeg.killRunningProcesses();
            }
        }

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
                                    if (m_video.isPlaying() && m_video.getCurrentPosition() >= m_nSeekEnd)
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
}