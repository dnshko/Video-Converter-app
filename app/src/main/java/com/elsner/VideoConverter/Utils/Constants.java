package com.elsner.VideoConverter.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.util.ArrayList;

public class Constants {

    // Fragments
    public final static int MAIN_FRAGMENT = 0;
    public final static int IMAGES_GIF_FRAGMENT = 1;
    public final static int VIDEO_GIF_FRAGMENT = 2;
    public final static int CAPTURE_IMAGE_FRAGMENT = 3;
    public final static int VIDEO_AUDIO_FRAGMENT = 4;
    public final static int VIDEO_CUTTER_FRAGMENT = 5;
    public final static int GALLERY_FRAGMENT = 6;
    public final static int VIDEO_MOTION = 7;
    public final static int VIDEO_WATERMARK = 8;
    public final static int VIDEO_FILTER = 9;

    public final static String
            TYPE_GIF = "gif",
            TYPE_IMAGE = "image",
            TYPE_AUDIO = "audio",
            TYPE_MOTION = "motion",
            TYPE_WATERMARK = "waterMark",
            TYPE_VIDEO = "video",
            TYPE_FILTER="filter";

    // intent request codes
    public final static int REQUEST_TAKE_GALLERY_VIDEO = 101;
    public final static int REQUEST_TAKE_MULTI_IMAGES = 102;
    public final static int REQUEST_GALLERY_MULTI_IMAGES = 103;

    private static String MY_PREFS_NAME = "VideoConverter";
    public static final String KEY_BUNDLE_LIST = "BUNDLE_LIST";
    public static final String KEY_PARAMS = "PARAMS";

    //location path(User can change)
    private static final String FOLDER_PATH = Environment.getExternalStorageDirectory().getPath() + "/VideoConverter/";
    public static final String GIF_FOLDER_PATH = FOLDER_PATH + "GIF";
    public static final String IMAGE_FOLDER_PATH = FOLDER_PATH + "Image";
    public static final String VIDEO_FOLDER_PATH = FOLDER_PATH + "Video";
    public static final String AUDIO_FOLDER_PATH = FOLDER_PATH + "Audio";

    //User can not change
    public final static String MY_GIF = "mygif";
    public final static String TOTAL_COUNT_GIF = "TotalCount_gif";
    public final static String SAVE_GIF = "save_gif";
    public final static String MY_IMAGE = "myimage";
    public final static String TOTAL_COUNT_IMAGE = "TotalCount_image";
    public final static String SAVE_IMAGE = "save_image";
    public final static String MY_AUDIO = "myaudio";
    public final static String TOTAL_COUNT_AUDIO = "TotalCount_audio";
    public final static String SAVE_AUDIO = "save_audio";
    public final static String MY_VIDEO = "myvideo";
    public final static String TOTAL_COUNT_VIDEO = "TotalCount_video";
    public final static String SAVE_VIDEO = "save_video";
    public static int SELECTED = MAIN_FRAGMENT;

    // Storage Permissions
    public static final int REQUEST_EXTERNAL_STORAGE = 1;
    public static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    // Camera Permissions
    public static final int REQUEST_CAMERA = 2;
    public static String[] PERMISSIONS_CAMERA = {
            Manifest.permission.CAMERA
    };

    public static int AD_COUNT = 1;

    public static void initWorkSpace() {
        File gifFolder = new File(Constants.GIF_FOLDER_PATH);
        if (!gifFolder.exists()) {
            gifFolder.mkdirs();
        }
        File imageFolder = new File(Constants.IMAGE_FOLDER_PATH);
        if (!imageFolder.exists()) {
            imageFolder.mkdirs();
        }
        File audioFolder = new File(Constants.AUDIO_FOLDER_PATH);
        if (!audioFolder.exists()) {
            audioFolder.mkdirs();
        }
        File videoFolder = new File(Constants.VIDEO_FOLDER_PATH);
        if (!videoFolder.exists()) {
            videoFolder.mkdirs();
        }
    }

    public static void putIntValue(Activity act, String strKey, int nValue) {
        SharedPreferences.Editor editor = act.getSharedPreferences(MY_PREFS_NAME, act.MODE_PRIVATE).edit();
        editor.putInt(strKey, nValue);
        editor.apply();
    }

    public static void putStringValue(Activity act, String strKey, String strValue) {
        SharedPreferences.Editor editor = act.getSharedPreferences(MY_PREFS_NAME, act.MODE_PRIVATE).edit();
        editor.putString(strKey, strValue);
        editor.apply();
    }

    public static int getIntValue(Activity act, String strKey) {
        SharedPreferences prefs = act.getSharedPreferences(MY_PREFS_NAME, act.MODE_PRIVATE);
        int nValue = prefs.getInt(strKey, 0);
        return nValue;
    }

    public static String getStringValue(Activity act, String strKey) {
        SharedPreferences prefs = act.getSharedPreferences(MY_PREFS_NAME, act.MODE_PRIVATE);
        String strValue = prefs.getString(strKey, "");
        return strValue;
    }

    public static ArrayList<Integer> getGifList(Activity act) {
        ArrayList<Integer> m_VideoList = new ArrayList<Integer>();
        int nCount = Constants.getIntValue(act, TOTAL_COUNT_GIF);
        for (int i = 0; i < nCount; i++) {
            int nIndex = Constants.getIntValue(act, SAVE_GIF + i);
            if (nIndex > 0) {
                m_VideoList.add(i);
            }
        }

        return m_VideoList;
    }

    public static ArrayList<Integer> getImageList(Activity act) {
        ArrayList<Integer> m_ImageList = new ArrayList<Integer>();
        int nCount = Constants.getIntValue(act, TOTAL_COUNT_IMAGE);
        for (int i = 0; i < nCount; i++) {
            int nIndex = Constants.getIntValue(act, SAVE_IMAGE + i);
            if (nIndex > 0) {
                m_ImageList.add(i);
            }
        }

        return m_ImageList;
    }

    public static ArrayList<Integer> getAudioList(Activity act) {
        ArrayList<Integer> mAudioList = new ArrayList<Integer>();
        int nCount = Constants.getIntValue(act, TOTAL_COUNT_AUDIO);
        for (int i = 0; i < nCount; i++) {
            int nIndex = Constants.getIntValue(act, SAVE_AUDIO + i);
            if (nIndex > 0) {
                mAudioList.add(i);
            }
        }

        return mAudioList;
    }

    public static ArrayList<Integer> getVideoList(Activity act) {
        ArrayList<Integer> mVideoList = new ArrayList<Integer>();
        int nCount = Constants.getIntValue(act, TOTAL_COUNT_VIDEO);
        for (int i = 0; i < nCount; i++) {
            int nIndex = Constants.getIntValue(act, SAVE_VIDEO + i);
            if (nIndex > 0) {
                mVideoList.add(i);
            }
        }

        return mVideoList;
    }

    public static void removeGif(Activity act, int nIndex) {
        Constants.putIntValue(act, SAVE_GIF + nIndex, 0);

        String strVideoNewName = GIF_FOLDER_PATH + String.format("/%s%d.gif", MY_GIF, nIndex);
        String strThumb = GIF_FOLDER_PATH + String.format("/thumb%d.jpg", nIndex);
        File removeFile = new File(strVideoNewName);
        if (removeFile.exists())
            removeFile.delete();

        removeFile = new File(strThumb);
        if (removeFile.exists())
            removeFile.delete();

    }

    public static void removeImage(Activity act, int nIndex) {
        Constants.putIntValue(act, SAVE_IMAGE + nIndex, 0);

        String strImageNewName = IMAGE_FOLDER_PATH + String.format("/%s%d.jpg", MY_IMAGE, nIndex);
        String strThumb = IMAGE_FOLDER_PATH + String.format("/thumb%d.jpg", nIndex);
        File removeFile = new File(strImageNewName);
        if (removeFile.exists())
            removeFile.delete();

        removeFile = new File(strThumb);
        if (removeFile.exists())
            removeFile.delete();

    }

    public static void removeAudio(Activity act, int nIndex) {
        Constants.putIntValue(act, SAVE_AUDIO + nIndex, 0);

        String strMp3Path = AUDIO_FOLDER_PATH + String.format("/%s%d.mp3", MY_AUDIO, nIndex);
        File removeMp3File = new File(strMp3Path);
        if (removeMp3File.exists())
            removeMp3File.delete();

        String strWavPath = AUDIO_FOLDER_PATH + String.format("/%s%d.wav", MY_AUDIO, nIndex);
        File removeWavFile = new File(strWavPath);
        if (removeWavFile.exists())
            removeWavFile.delete();

    }

    public static void removeVideo(Activity act, int nIndex) {
        Constants.putIntValue(act, SAVE_VIDEO + nIndex, 0);

        String strMp4Path = VIDEO_FOLDER_PATH + String.format("/%s%d.mp4", MY_VIDEO, nIndex);
        File removeMp4File = new File(strMp4Path);
        if (removeMp4File.exists())
            removeMp4File.delete();
    }

    // prompts for allowing storage permission
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission1 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission1 != PackageManager.PERMISSION_GRANTED && permission2 != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    // prompts for allowing storage permission
    public static void verifyCameraPermissions(Activity activity) {
        // Check if we have write permission
        int permission1 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);

        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_CAMERA,
                    REQUEST_CAMERA
            );
        }
    }

    public static String getFomattedTime(int nDuration) {
        int duration = nDuration / 1000;
        int hours = duration / 3600;
        int minutes = (duration / 60) - (hours * 60);
        int seconds = duration - (hours * 3600) - (minutes * 60);
        return String.format("%02d:%02d", minutes, seconds);
    }

    private boolean isARM() {

        String arch = System.getProperty("os.arch");
        String arc = arch.substring(0, 3).toUpperCase();

        return !arc.equals("MIP") && !arc.equals("X86");
    }
}
