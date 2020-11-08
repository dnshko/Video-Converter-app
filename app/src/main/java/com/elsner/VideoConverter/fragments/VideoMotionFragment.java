package com.elsner.VideoConverter.fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.elsner.VideoConverter.MainActivity;
import com.elsner.VideoConverter.R;
import com.elsner.VideoConverter.Utils.Constants;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoMotionFragment extends Fragment {


    private static final String TAG = "VideoMotionFragment";
    String videoPath;
    TextView tvTemp;
    VideoView m_video;
    FloatingActionButton fabDone;
    Activity activity;
    AppCompatSpinner dropdown, spinnerFormat;

    private RadioGroup groupMotion;
    private RadioButton radio_slow, radio_fast;
    ArrayAdapter<String> adapter;
    String selectSpeed = "";
    String[] items;
    ProgressDialog mlDialog;
    String[] formats = new String[]{"ultrafast", "veryfast", "faster", "fast", "medium", "slow", "slower", "veryslow"};
    FFmpeg ffmpeg;

    public VideoMotionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_motion, container, false);
        // Inflate the layout for this fragment
        if (getArguments() != null) {
            videoPath = getArguments().getString("video");
        }
        activity = getActivity();
        initView(rootView);
        setSpinnerFormat();
        prepareVideo();
        fabDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectSpeed.isEmpty()) {
                    if (items.length > 0)
                        selectSpeed = items[0];
                }
                processVideo();

            }
        });


        groupMotion.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if (checkedId == R.id.radio_slow) {
                    chnageValueSpinner(false);
                } else if (checkedId == R.id.radio_fast) {
                    chnageValueSpinner(true);
                }
            }

        });

        return rootView;
    }

    private void setSpinnerFormat() {

        ArrayAdapter<String> aa = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, formats);
        spinnerFormat.setAdapter(aa);
        spinnerFormat.setSelected(true);
        spinnerFormat.setSelection(0);
    }


    private void initView(View rootView) {
        m_video = rootView.findViewById(R.id.videoView);
        fabDone = rootView.findViewById(R.id.fabDone);
        groupMotion = rootView.findViewById(R.id.groupMotion);
        radio_slow = rootView.findViewById(R.id.radio_slow);
        radio_fast = rootView.findViewById(R.id.radio_fast);
        dropdown = rootView.findViewById(R.id.spinner1);
        spinnerFormat = rootView.findViewById(R.id.spinnerFormat);
        radio_slow.setChecked(true);
        chnageValueSpinner(false);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectSpeed = items[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void chnageValueSpinner(boolean isFast) {
        if (isFast) {
            items = new String[]{"0.25", "0.50", "0.75"};

        } else {
            items = new String[]{"1.25", "1.50", "1.75", "2.0"};
        }
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
    }


    void prepareVideo() {
        m_video.setVideoPath(videoPath);
        m_video.start();
    }

    private void processVideo() {
        final String format = "mp4";
        String strVideoPath = Constants.VIDEO_FOLDER_PATH + String.format("/%s.%s", Constants.MY_VIDEO, format);
        String preset = spinnerFormat.getSelectedItem().toString();
        final String cmd[] = new String[]{
                "-y",
                "-i", // input
                videoPath,
                "-filter_complex",
                "[0:v]setpts=" + selectSpeed + "*PTS[v];[0:a]atempo=2.0[a]",
                "-map",
                "[v]",
                "-map",
                "[a]",
                "-b:v",
                "2097k",
                "-r", //Framerate
                "60",
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

                    Toast.makeText(getActivity(), "failed to Make motionVideo", Toast.LENGTH_SHORT).show();


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
    public void onResume() {
        super.onResume();
        prepareVideo();
    }
}
