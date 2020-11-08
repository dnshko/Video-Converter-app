package com.elsner.VideoConverter;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.elsner.VideoConverter.Utils.Constants;
import com.elsner.VideoConverter.fragments.CaptureImageFragment;
import com.elsner.VideoConverter.fragments.GalleryFragment;
import com.elsner.VideoConverter.fragments.ImagesGifFragment;
import com.elsner.VideoConverter.fragments.MainFragment;
import com.elsner.VideoConverter.fragments.VideoFilterFragment;
import com.elsner.VideoConverter.fragments.VideoMotionFragment;
import com.elsner.VideoConverter.fragments.VideoCutterFragment;
import com.elsner.VideoConverter.fragments.VideoGifFragment;
import com.elsner.VideoConverter.fragments.VideoAudioFragment;
import com.elsner.VideoConverter.fragments.VideoWaterMarkFragment;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    boolean doubleBackToExitPressedOnce = false;
    FragmentManager manager;
    FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "CPU_ABI : " + Build.CPU_ABI);
        Log.i(TAG, "CPU_ABI2 : " + Build.CPU_ABI2);
        Log.i(TAG, "OS.ARCH : " + System.getProperty("os.arch"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.i(TAG, "SUPPORTED_ABIS : " + Arrays.toString(Build.SUPPORTED_ABIS));
            Log.i(TAG, "SUPPORTED_32_BIT_ABIS : " + Arrays.toString(Build.SUPPORTED_32_BIT_ABIS));
            Log.i(TAG, "SUPPORTED_64_BIT_ABIS : " + Arrays.toString(Build.SUPPORTED_64_BIT_ABIS));
        }

        // ask storage runtime permission
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Constants.verifyStoragePermissions(this);
        }

        //create directory for the tutorial
        Constants.initWorkSpace();

        manager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            Constants.SELECTED = Constants.MAIN_FRAGMENT;
            loadFragment("");
        }

        manager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment fragment = manager.findFragmentById(R.id.content_frame);
                fragment.onResume();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.REQUEST_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.initWorkSpace();
            }
        }
    }

    // manage fragment replacement
    public void loadFragment(String tag) {

        transaction = manager.beginTransaction();

        switch (Constants.SELECTED) {

            case Constants.MAIN_FRAGMENT:
                MainFragment mainFragment=new MainFragment();
                transaction.replace(R.id.content_frame, new MainFragment());
                Log.e(TAG, "loading Main fragment");
                transaction.commit();
                break;

            case Constants.GALLERY_FRAGMENT:
                transaction.add(R.id.content_frame, new GalleryFragment(), tag);
                transaction.addToBackStack(null);
                Log.e(TAG, "loading Gallery fragment");
                transaction.commit();
                break;

            default:
                break;

        }
    }

    // manage fragment replacement with Tags and backstack
    // and pass bundle data to fragments
    public void loadFragment(Bundle bundle) {

        transaction = manager.beginTransaction();

        switch (Constants.SELECTED) {

            case Constants.VIDEO_GIF_FRAGMENT:
                VideoGifFragment videoGifFragment = new VideoGifFragment();
                videoGifFragment.setArguments(bundle);
                transaction.add(R.id.content_frame, videoGifFragment);
                transaction.addToBackStack(null);
                Log.e(TAG, "loading VideoGif fragment");
                transaction.commit();
                break;

            case Constants.IMAGES_GIF_FRAGMENT:
                ImagesGifFragment imagesGifFragment = new ImagesGifFragment();
                imagesGifFragment.setArguments(bundle);
                transaction.add(R.id.content_frame, imagesGifFragment);
                transaction.addToBackStack(null);
                Log.e(TAG, "loading ImagesGif fragment");
                transaction.commit();
                break;

            case Constants.CAPTURE_IMAGE_FRAGMENT:
                CaptureImageFragment captureImageFragment = new CaptureImageFragment();
                captureImageFragment.setArguments(bundle);
                transaction.add(R.id.content_frame, captureImageFragment);
                transaction.addToBackStack(null);
                Log.e(TAG, "loading CaptureImage fragment");
                transaction.commit();
                break;

            case Constants.VIDEO_AUDIO_FRAGMENT:
                VideoAudioFragment videoAudioFragment = new VideoAudioFragment();
                videoAudioFragment.setArguments(bundle);
                transaction.add(R.id.content_frame, videoAudioFragment);
                transaction.addToBackStack(null);
                Log.e(TAG, "loading VideoAudio fragment");
                transaction.commit();
                break;

            case Constants.VIDEO_CUTTER_FRAGMENT:
                VideoCutterFragment videoCutterFragment = new VideoCutterFragment();
                videoCutterFragment.setArguments(bundle);
                transaction.add(R.id.content_frame, videoCutterFragment);
                transaction.addToBackStack(null);
                Log.e(TAG, "loading VideoCutter fragment");
                transaction.commit();
                break;

            case Constants.VIDEO_MOTION:
                VideoMotionFragment videoslowMotion = new VideoMotionFragment();
                videoslowMotion.setArguments(bundle);
                transaction.add(R.id.content_frame, videoslowMotion);
                transaction.addToBackStack(null);
                Log.e(TAG, "loading VideoCutter fragment");
                transaction.commit();
                break;
            case Constants.VIDEO_WATERMARK:
                VideoWaterMarkFragment videoWaterMarkFragment = new VideoWaterMarkFragment();
                videoWaterMarkFragment.setArguments(bundle);
                transaction.add(R.id.content_frame, videoWaterMarkFragment);
                transaction.addToBackStack(null);
                Log.e(TAG, "loading VideoCutter fragment");
                transaction.commit();
                break;
                case Constants.VIDEO_FILTER:
                    VideoFilterFragment videoFilterFragment=new VideoFilterFragment();
                    videoFilterFragment.setArguments(bundle);
                    transaction.replace(R.id.content_frame,videoFilterFragment);
                    transaction.addToBackStack(null);
                    Log.e(TAG, "loading VideoFilter fragment");
                    transaction.commit();
                    break;

            default:
                break;

        }
    }

    // set enable/disable state of drawer menu
    public void setDrawerState(boolean isEnabled) {

        if (isEnabled) {

            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setHomeButtonEnabled(false);
            }

        } else {

            if (getSupportActionBar() != null) {
                getSupportActionBar().show();
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (manager.getBackStackEntryCount() > 0) {
            manager.popBackStack();
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }


}
