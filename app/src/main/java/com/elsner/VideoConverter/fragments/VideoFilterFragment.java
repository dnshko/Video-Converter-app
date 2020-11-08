package com.elsner.VideoConverter.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.elsner.VideoConverter.MainActivity;
import com.elsner.VideoConverter.Pojo.FilterModel;
import com.elsner.VideoConverter.R;
import com.elsner.VideoConverter.Utils.Constants;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoFilterFragment extends Fragment {

    private static final String TAG = "VideoFilterFragment";
    String videoPath;
    VideoView filter_video;
    Button buttonFilter;
    Activity activity;
    FFmpeg ffmpeg;
    ProgressDialog mlDialog;
    RecyclerView recyclerViewFilter;
    ImageButton imageButtonPause, imageButtonPlay;

    FilterAdapter filterAdapter;
    FilterModel filterModel;
    List<FilterModel> filterModelList;

    int vLength;

    boolean applyCount = false;

    public final String format = "mp4";

    int newColor = 0;

    public String strVideoPath = Constants.VIDEO_FOLDER_PATH + String.format("/%s.%s", Constants.MY_VIDEO, format);

    public VideoFilterFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_video_filter, container, false);

        if (getArguments() != null) {
            videoPath = getArguments().getString("video");
        }

        activity = getActivity();
        initView(rootView);

        prepareVideo();

        buttonFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (applyCount == true) {
                    finishedVideoConvet(format);
                    applyCount = false;
                    /*String cmd[]=new String[]{
                            //full black
                            //"-y","-i",videoPath,"-vf","eq=contrast=2.0:brightness=-0.0:saturation=0.0",strVideoPath

                            //red face
                            //"-y","-i",videoPath,"-vf","eq=contrast=1.0:brightness=-0.60:saturation=2.0",strVideoPath

                            //"-y","-i",videoPath,"-vf","curves=preset=lighter",strVideoPath

                            //Sharpen
                            //"-y","-i",videoPath,"-vf","fftfilt=dc_Y=0:weight_Y='1+squish(1-(Y+X)/100)'",strVideoPath

                            //histogram
                            //"-y","-i",videoPath,"-vf","histogram",strVideoPath
                            //"-y","-i",videoPath,"-vf","histogram","level_height=50","scale_height=20","levels_mode=linear",strVideoPath

                            //loop
                            //"-y","-i",videoPath,"-filter_complex","loop=loop=2:size=10:start=0",strVideoPath
                            //"-y","-stream_loop","3","-i",videoPath,"-c","copy",strVideoPath
                            //"-y","loop=loop=3:size=1:start=0","-i",videoPath,"-c","copy",strVideoPath

                            //smooth
                            // "-y","-i",videoPath,"smooth=3|0|1.0|1.0",strVideoPath

                            //"-y","-i",videoPath,"-filter:v","tblend","-r","120",strVideoPath
                            //"-y","-i",videoPath,"-filter:v","minterpolate='fps=120'","-r","120",strVideoPath

                            //"-y","-i",videoPath,"-vf","palettegen",strVideoPath

                            //pixellate effect
                            //"-y","-i",videoPath,"-vf","frei0r=pixeliz0r=0.02:0.02",strVideoPath


                            // wave
                            //"-y","-i",videoPath,"separatefields,select=eq(mod(n,4),0)+eq(mod(n,4),3),weave","-preset","ultrafast",strVideoPath

                            //ripple
                            //"-y","-i",videoPath,"-f","lavfi","-i","nullsrc=s=hd720,lutrgb=128:128:128","-f","lavfi","-i","nullsrc=s=hd720,geq='r=128+30*sin(2*PI*X/400+T):g=128+30*sin(2*PI*X/400+T):b=128+30*sin(2*PI*X/400+T)'","-lavfi","[0][1][2]displace",strVideoPath

                            //draw box
                            //"-y","-i",videoPath,"drawbox=10:20:200:60:red@0.5","-preset","ultrafast",strVideoPath
                            //"-y","-i",videoPath,"-vf","drawbox=x=10:y=0:w=200:h=60:color=red@0.5","-preset","ultrafast",strVideoPath

                            //"-y","-i",videoPath,"lut2='ifnot(x-y,0,pow(2,bdx)-1):ifnot(x-y,0,pow(2,bdx)-1):ifnot(x-y,0,pow(2,bdx)-1)'","-preset","ultrafast",strVideoPath


                            //add noise
                            //"-y","-i",videoPath,"-vf","noise=alls=20:allf=t+u","-preset","ultrafast",strVideoPath

                            //"-y","-i",videoPath,"-vf","eq=gamma_g=3.1:gamma_b=2.1","-preset","ultrafast",strVideoPath

                            //life pattern
                            //"-y","-i",videoPath,"life=f=pattern:s=300x300","-preset","ultrafast",strVideoPath


                    };*/
                } else {
                    Toast.makeText(getActivity(), "Please select a filter to apply", Toast.LENGTH_LONG).show();
                }
                // processVideo(cmd);
            }
        });

        imageButtonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageButtonPlay.setVisibility(View.VISIBLE);
                imageButtonPause.setVisibility(View.INVISIBLE);
                filter_video.pause();
                vLength = filter_video.getCurrentPosition();
            }
        });

        imageButtonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageButtonPlay.setVisibility(View.INVISIBLE);
                imageButtonPause.setVisibility(View.VISIBLE);
                filter_video.seekTo(vLength);
                filter_video.start();
            }
        });

        return rootView;
    }

    private void initView(View rootView) {
        filter_video = rootView.findViewById(R.id.videoViewFilter);
        //filter_video.setClickable(false);
        filter_video.setOnClickListener(null);
//        filter_video.setEnabled(false);
        buttonFilter = rootView.findViewById(R.id.btnFilter);
        //   buttonFilter.setVisibility(View.INVISIBLE);
        recyclerViewFilter = rootView.findViewById(R.id.rcViewFilter);
        filterModelList = new ArrayList<>();
        recyclerViewFilter.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        filterAdapter = new FilterAdapter(filterModelList);
        recyclerViewFilter.setAdapter(filterAdapter);
        imageButtonPause = rootView.findViewById(R.id.imgBtnPause);
        imageButtonPlay = rootView.findViewById(R.id.imgBtnPlay);
        displayFilter();

        filter_video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (applyCount == true) {
                    preViewVideo();
                } else {
                    prepareVideo();
                }
            }
        });

        getAlphaColor();
    }

    private void displayFilter() {
        filterModel = new FilterModel(R.drawable.bandw, "Black and White");
        filterModelList.add(filterModel);
    /*    filterModel=new FilterModel(R.drawable.mirror,"Mirror Effect");
        filterModelList.add(filterModel);*/
        filterModelList.add(new FilterModel(R.drawable.contrast, "Bright"));
        filterModelList.add(new FilterModel(R.drawable.technicolor, "Technicolor"));
        filterModelList.add(new FilterModel(R.color.negateLuminance, "Negate luminance"));
        filterModelList.add(new FilterModel(R.color.goldenHour, "Golden Hour"));
        filterModelList.add(new FilterModel(R.color.paleRed, "Pale Red"));
        filterModelList.add(new FilterModel(R.color.lanTern, "Lantern"));
        filterModelList.add(new FilterModel(R.color.jungle, "Jungle"));
        filterModelList.add(new FilterModel(R.color.starryNight, "Starry Night"));
        filterModelList.add(new FilterModel(R.drawable.fire, "Burning"));
    }

    int getAlphaColor() {
        int alpha = Math.round(Color.alpha(R.color.starryNight) * 0.5f);
        int r = Color.red(R.color.starryNight);
        int g = Color.green(R.color.starryNight);
        int b = Color.blue(R.color.starryNight);
        newColor = Color.argb(alpha, r, g, b);
        return newColor;
    }

    void prepareVideo() {
        filter_video.setVideoPath(videoPath);
        filter_video.start();
    }


    void preViewVideo() {
        filter_video.setVideoPath(strVideoPath);
        filter_video.start();
    }

    private void processVideo(String cmd[]) {
        //buttonFilter.setVisibility(View.INVISIBLE);
        filter_video.stopPlayback();

        // final String cmd[] = new String[]{
/*                "-i",
                videoPath,
                "convolution= 0 -1 0 -1 5 -1 0 -1 0:0 -1 0 -1 5 -1 0 -1 0:0 -1 0 -1 5 -1 0 -1 0:0 -1 0 -1 5 -1 0 -1 0",
                "-filter_complex",
                strVideoPath*/

        /*                "-y","-i",videoPath,"-filter_complex","[1:v]colorkey=0x008000\\:0.3\\:0.2[ckout];[0:v][ckout]overlay[out]' -map '[out]","-t","10",strVideoPath*/

           /*     "-i",
                videoPath,
                "convolution=\"0 -1 0 -1 5 -1 0 -1 0:0 -1 0 -1 5 -1 0 -1 0:0 -1 0 -1 5 -1 0 -1 0:0 -1 0 -1 5 -1 0 -1 0\"",
                strVideoPath*/

         /*       "ffmpeg",
                "i",
                videoPath,
                "convolution=0 -1 0 -1 5 -1 0 -1 0:0 -1 0 -1 5 -1 0 -1 0:0 -1 0 -1 5 -1 0 -1 0:0 -1 0 -1 5 -1 0 -1 0:enable='gt(mod(t,60),57)',edgedetect=enable='gt(mod(t,60),57)',negate",
                "-c:a",
                "copy",
                strVideoPath*/

/*                "-i", // input
                videoPath,
                "colorbalance=rs=.3",
                strVideoPath*/

/*                "-i", // input
                videoPath,
                "-f",
                "lavfi",
                "-i",
                "color=red:s=1280x720",
                "-filter_complex",
                "blend=shortest=1:all_mode=overlay:all_opacity=0.7",
                strVideoPath*/

        //vectorScope
        //"-y","-i",videoPath,"-vf","format=yuva444p9","vectorscope",strVideoPath

        // gray-scale effect [672 frames]
        //"-y","-i",videoPath,"-vf","hue=s=0",strVideoPath

        //mirror effect [upto 2850 frames]
        //"-y","-i",videoPath,"-filter_complex","split [left][tmp]; [tmp] hflip[right]; [left][right] hstack",strVideoPath

        //Grren colour
        //"-y","-i",videoPath,"-f","lavfi","-i","color=color=green:s=320x240","-t","3600","-r","60",strVideoPath

        //negate   NO EFFECT
        // "-y","-i",videoPath,"-vf","lutrgb=\'r=20:g=15:b=10\'",strVideoPath

        //Make video output darker:
        //"-y","-i",videoPath,"colorlevels","rimin=0.058:gimin=0.058:bimin=0.058","-filter_complex",strVideoPath


        // };
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
                }

                @Override
                public void onFailure(String message) {
                    Log.d(TAG, "onFailure() called with: message = [" + message + "]");
                    if (mlDialog.isShowing())
                        mlDialog.dismiss();

                    Toast.makeText(getActivity(), "failed to Make filterVideo", Toast.LENGTH_SHORT).show();
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
                    // finishedVideoConvet(format);
                    preViewVideo();
                    buttonFilter.setVisibility(View.VISIBLE);
                    Log.d(TAG, "onFinish: ");
                    if (ffmpeg.isFFmpegCommandRunning()) {
                        ffmpeg.killRunningProcesses();
                    }
                }
            });

        } catch (Exception e) {
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
        applyCount = false;
    }

    public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.MyHolder> {

        List<FilterModel> filterModelList;

        public FilterAdapter(List<FilterModel> filterModelList) {
            this.filterModelList = filterModelList;
        }

        @Override
        public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.filter_layout, parent, false);
            return new MyHolder(view);
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public void onBindViewHolder(MyHolder holder, final int position) {
            String name = filterModelList.get(position).filterName;
            if (name.equalsIgnoreCase("Bright") || name.equalsIgnoreCase("Technicolor")
                    || name.equalsIgnoreCase("Burning")) {
                holder.imageViewFilter.setBackgroundColor(R.color.white);
            }
            holder.imageViewFilter.setImageResource(filterModelList.get(position).filterImgId);
            holder.textViewFilter.setText(filterModelList.get(position).filterName);


            holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    applyCount = true;
                    if (position == 0) {
                        prepareVideo();
                        // for gray-scale effect
                        final String cmd[] = new String[]{
                                //[672 frames]
                                "-y", "-i", videoPath, "-vf", "hue=s=0", "-preset", "ultrafast", strVideoPath
                                //or also we can use following command
                                //"-y","-i",videoPath,"-vf","eq=contrast=2.0:brightness=-0.0:saturation=0.0",strVideoPath

                        };
                        processVideo(cmd);
                    } else if (position == 1) {
                        prepareVideo();
                        String cmd[] = new String[]{
                                //Grren colour
                                //"-y","-i",videoPath,"-f","lavfi","-i","color=color=green:s=320x240","-t","3600","-r","60",strVideoPath

                                //"-y","-i",videoPath,"-filter_complex","[0:v]eq=contrast=1:brightness=0:saturation=1:gamma=1:gamma_r=1:gamma_g=1:gamma_b=1:gamma_weight=1[outv]",strVideoPath

                                // Bright video
                                "-y", "-i", videoPath, "-vf", "eq=contrast=1.5:brightness=-0.05:saturation=0.75", "-preset", "ultrafast", strVideoPath
                        };
                        processVideo(cmd);
                    } else if (position == 2) {
                        prepareVideo();
                        String cmd[] = new String[]{
                                //Techni color effect
                                "-y", "-i", videoPath, "-vf", "lutyuv=u='(val-maxval/2)*2+maxval/2':v='(val-maxval/2)*2+maxval/2'", "-preset", "ultrafast", strVideoPath

                                //flip horizontally
                                // "-y","-i",videoPath,"-vf","hflip","-preset","ultrafast",strVideoPath

                                //vertical flip
                                //"-y","-i",videoPath,"-vf","vflip","-preset","ultrafast",strVideoPath
                        };
                        processVideo(cmd);
                    } else if (position == 3) {
                        prepareVideo();
                        String cmd[] = new String[]{
                                //Negate luminance
                                "-y", "-i", videoPath, "-vf", "lutyuv=y=negval'", "-preset", "ultrafast", strVideoPath
                        };
                        processVideo(cmd);
                    } else if (position == 4) {
                        prepareVideo();
                        String cmd[] = new String[]{
                                //Golden Hour
                                "-y", "-i", videoPath, "-vf", "eq=gamma_r=5.0:gamma_g=5.1:gamma_b=2.1", "-preset", "ultrafast", strVideoPath
                        };
                        processVideo(cmd);
                    } else if (position == 5) {
                        prepareVideo();
                        String cmd[] = new String[]{
                                //Pale red
                                "-y", "-i", videoPath, "-vf", "eq=gamma_r=3.0:gamma_b=3.1", "-preset", "ultrafast", strVideoPath
                        };
                        processVideo(cmd);
                    } else if (position == 6) {
                        prepareVideo();
                        String cmd[] = new String[]{
                                //Lantern effect
                                "-y", "-i", videoPath, "-vf", "eq=gamma_r=6.0:gamma_g=3.1:gamma_b=2.1", "-preset", "ultrafast", strVideoPath
                        };
                        processVideo(cmd);
                    } else if (position == 7) {
                        prepareVideo();
                        String cmd[] = new String[]{
                                //Jungle Effect
                                "-y", "-i", videoPath, "-vf", "eq=gamma_g=2.0:gamma_b=2.1", "-preset", "ultrafast", strVideoPath
                        };
                        processVideo(cmd);
                    } else if (position == 8) {
                        prepareVideo();
                        String cmd[] = new String[]{
                                //Starry Night
                                "-y", "-i", videoPath, "-vf", "eq=gamma_r=1.0:gamma_b=3.1", "-preset", "ultrafast", strVideoPath
                        };
                        processVideo(cmd);
                    } else if (position == 9) {
                        prepareVideo();
                        String cmd[] = new String[]{
                                //Burning effect
                                "-y", "-i", videoPath, "-vf", "lutyuv=y=2*val", "-preset", "ultrafast", strVideoPath

                                //rotate
                                //"-y","-i",videoPath,"-vf","rotate=45*PI/180","-preset","ultrafast",strVideoPath
                        };
                        processVideo(cmd);
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return filterModelList.size();
        }

        public class MyHolder extends RecyclerView.ViewHolder {

            ImageView imageViewFilter;
            TextView textViewFilter;
            LinearLayout linearLayout;


            public MyHolder(View itemView) {
                super(itemView);
                imageViewFilter = itemView.findViewById(R.id.imageViewFilter);
                textViewFilter = itemView.findViewById(R.id.textViewFilter);
                linearLayout = itemView.findViewById(R.id.llFilter);
            }
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        prepareVideo();
        activity.setTitle(R.string.video_filter);
        ((MainActivity) activity).setDrawerState(false);
    }
}
