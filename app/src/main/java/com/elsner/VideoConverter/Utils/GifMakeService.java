package com.elsner.VideoConverter.Utils;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.util.ArrayList;

public class GifMakeService extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_MAKE_GIF_FROM_VIDEO = "com.elsner.VideoConverter.action.MAKE_GIF_FROM_VIDEO";
    public static final String ACTION_MAKE_GIF_FROM_IMAGES = "com.elsner.VideoConverter.action.MAKE_GIF_FROM_IMAGES";
    public static final String EXTRA_FILE = "file",
            EXTRA_SUCCESS = "success",
            EXTRA_TOTAL = "total",
            EXTRA_CURRENT = "current",
            EXTRA_LOG = "log";

    private static final String EXTRA_FROM_FILE = "com.elsner.VideoConverter.extra.FROM_FILE";
    private static final String EXTRA_TO_FILE = "com.elsner.VideoConverter.extra.TO_FILE";
    private static final String EXTRA_FROM_POSITION = "com.elsner.VideoConverter.extra.FROM_POSITION";
    private static final String EXTRA_TO_POSITION = "com.elsner.VideoConverter.extra.TO_POSITION";
    private static final String EXTRA_PERIOD = "com.elsner.VideoConverter.extra.PERIOD";

    public GifMakeService() {
        super("GifMakeService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startMakingVideoToGif(Context context, String fromFile, String toFile, int fromPosition, int toPosition, int period) {
        Intent intent = new Intent(context, GifMakeService.class);
        intent.setAction(ACTION_MAKE_GIF_FROM_VIDEO);
        intent.putExtra(EXTRA_FROM_FILE, fromFile);
        intent.putExtra(EXTRA_TO_FILE, toFile);
        intent.putExtra(EXTRA_FROM_POSITION, fromPosition);
        intent.putExtra(EXTRA_TO_POSITION, toPosition);
        intent.putExtra(EXTRA_PERIOD, period);
        context.startService(intent);
    }

    public static void startMakingImagesToGif(Context context, ArrayList<String> fromFiles, String toFile) {
        Intent intent = new Intent(context, GifMakeService.class);
        intent.setAction(ACTION_MAKE_GIF_FROM_IMAGES);
        intent.putStringArrayListExtra(EXTRA_FROM_FILE, fromFiles);
        intent.putExtra(EXTRA_TO_FILE, toFile);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            if (ACTION_MAKE_GIF_FROM_VIDEO.equals(action)) {
                final String fromFile = intent.getStringExtra(EXTRA_FROM_FILE);
                final String toFile = intent.getStringExtra(EXTRA_TO_FILE);
                final int fromPosition = intent.getIntExtra(EXTRA_FROM_POSITION, 0);
                final int toPosition = intent.getIntExtra(EXTRA_TO_POSITION, 0);
                final int period = intent.getIntExtra(EXTRA_PERIOD, 200);
                handleTaskVideoToGif(fromFile, toFile, fromPosition, toPosition, period);

            } else if (ACTION_MAKE_GIF_FROM_IMAGES.equals(action)) {
                final ArrayList<String> fromFiles = intent.getStringArrayListExtra(EXTRA_FROM_FILE);
                final String toFile = intent.getStringExtra(EXTRA_TO_FILE);
                handleTaskImagesToGif(fromFiles, toFile);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleTaskVideoToGif(String fromFile, String toFile, int fromPosition, int toPosition, int period) {
        GifMaker maker = new GifMaker(2);
        maker.setOnGifListener(new GifMaker.OnGifListener() {
            @Override
            public void onMake(int current, int total) {
                LocalBroadcastManager.getInstance(GifMakeService.this).sendBroadcast(new Intent(ACTION_MAKE_GIF_FROM_VIDEO)
                        .putExtra(EXTRA_TOTAL, total)
                        .putExtra(EXTRA_CURRENT, current)
                        .putExtra(EXTRA_LOG, ""));
            }
        });
        long startAt = System.currentTimeMillis();
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_MAKE_GIF_FROM_VIDEO)
                .putExtra(EXTRA_LOG, "start making at " + startAt));
        final boolean success = maker.makeGifFromVideo(this, Uri.parse(fromFile), fromPosition, toPosition, period, toFile);
        long now = System.currentTimeMillis();
        LocalBroadcastManager.getInstance(GifMakeService.this).sendBroadcast(new Intent(ACTION_MAKE_GIF_FROM_VIDEO)
                .putExtra(EXTRA_LOG, "Done! " +
                        (success ? " success " : " failed ") +
                        " cost time=" + ((now - startAt) / 1000) + " seconds " +
                        " \nsave at=" + toFile)
                .putExtra(EXTRA_FILE, toFile)
                .putExtra(EXTRA_SUCCESS, true));
    }

    private void handleTaskImagesToGif(ArrayList<String> fromFiles, String toFile) {
        GifMaker maker = new GifMaker(2);
        maker.setOnGifListener(new GifMaker.OnGifListener() {
            @Override
            public void onMake(int current, int total) {
                LocalBroadcastManager.getInstance(GifMakeService.this).sendBroadcast(new Intent(ACTION_MAKE_GIF_FROM_IMAGES)
                        .putExtra(EXTRA_TOTAL, total)
                        .putExtra(EXTRA_CURRENT, current)
                        .putExtra(EXTRA_LOG, ""));
            }
        });
        long startAt = System.currentTimeMillis();
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_MAKE_GIF_FROM_IMAGES)
                .putExtra(EXTRA_LOG, "start making at " + startAt));
        boolean success = false;
        try {
            success = maker.makeGifFromPath(fromFiles, toFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        long now = System.currentTimeMillis();
        LocalBroadcastManager.getInstance(GifMakeService.this).sendBroadcast(new Intent(ACTION_MAKE_GIF_FROM_IMAGES)
                .putExtra(EXTRA_LOG, "Done! " +
                        (success ? " success " : " failed ") +
                        " cost time=" + ((now - startAt) / 1000) + " seconds " +
                        " \nsave at=" + toFile)
                .putExtra(EXTRA_FILE, toFile)
                .putExtra(EXTRA_SUCCESS, true));
    }
}
