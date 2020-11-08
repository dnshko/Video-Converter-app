package com.elsner.VideoConverter.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.elsner.VideoConverter.MainActivity;
import com.elsner.VideoConverter.R;
import com.elsner.VideoConverter.Utils.Constants;
import com.elsner.VideoConverter.Utils.PathUtil;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.vlk.multimager.activities.GalleryActivity;
import com.vlk.multimager.activities.MultiCameraActivity;
import com.vlk.multimager.utils.Image;
import com.vlk.multimager.utils.Params;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    public MainFragment() {
        // Required empty public constructor
    }

    private final String TAG = "MainFragment";
    Activity activity;
    AdView bannerAdView;
    boolean isAdLoaded;
    CardView cardVideoToGIF, cardImagesToGIF, cardCaptureImage, cardVideoToAudio, cardVideoCutter, cardGallery, cardVideoSlowMotion, cardWaterMark,cardFilter;
    LinearLayout linearRow2;
    InterstitialAd mInterstitialAd;

    private String SELECTED_TYPE = Constants.TYPE_GIF;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bannerAdView != null) {
            bannerAdView.resume();
        }
        ((MainActivity) activity).setTitle("");
        ((MainActivity) activity).setDrawerState(true);

    }

    @Override
    public void onPause() {
        if (bannerAdView != null) {
            bannerAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (bannerAdView != null) {
            bannerAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        cardVideoToGIF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(cardVideoToGIF);
                SELECTED_TYPE = Constants.TYPE_GIF;
            }
        });

        cardImagesToGIF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(cardImagesToGIF);
                SELECTED_TYPE = Constants.TYPE_GIF;
            }
        });

        cardCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getVideoFromGallery();
                SELECTED_TYPE = Constants.TYPE_IMAGE;
            }
        });

        cardVideoToAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getVideoFromGallery();
                SELECTED_TYPE = Constants.TYPE_AUDIO;
            }
        });

        cardVideoCutter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getVideoFromGallery();
                SELECTED_TYPE = Constants.TYPE_VIDEO;
            }
        });


        cardGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Constants.AD_COUNT >= 3) {
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                        Constants.AD_COUNT = 1;
                    }
                } else {
                    Constants.AD_COUNT++;
                }

                Constants.SELECTED = Constants.GALLERY_FRAGMENT;
                ((MainActivity) activity).loadFragment("");
            }
        });

        cardVideoSlowMotion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showPopupMenu(cardVideoSlowMotion);
                SELECTED_TYPE = Constants.TYPE_MOTION;
            }
        });

        cardWaterMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(cardWaterMark);
                SELECTED_TYPE = Constants.TYPE_WATERMARK;
            }
        });

        cardFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(cardFilter);
                SELECTED_TYPE=Constants.TYPE_FILTER;
            }
        });

    }

    private void initViews(View view) {

        activity = getActivity();
        linearRow2 = view.findViewById(R.id.linearRow2);
        cardVideoToGIF = view.findViewById(R.id.cardVideoToGIF);
        cardImagesToGIF = view.findViewById(R.id.cardImagesToGIF);
        cardCaptureImage = view.findViewById(R.id.cardCaptureImage);
        cardVideoToAudio = view.findViewById(R.id.cardVideoToAudio);
        cardVideoCutter = view.findViewById(R.id.cardVideoCutter);
        cardGallery = view.findViewById(R.id.cardGallery);
        cardVideoSlowMotion = view.findViewById(R.id.cardVideoSlowMotion);
        cardWaterMark = view.findViewById(R.id.cardWatermark);
        cardFilter=view.findViewById(R.id.cardFilter);

        bannerAdView = view.findViewById(R.id.bannerAdView);
        mInterstitialAd = new InterstitialAd(activity);
         //set the ad unit ID
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_id));

        loadBannerAd();
        loadInterstitialAd();
    }

    private void loadBannerAd() {

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                // Check the LogCat to get your test device ID
                .addTestDevice("5F338128595663F734E36512D2DF7D2F")
                .build();

        bannerAdView.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                isAdLoaded = true;
                bannerAdView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdClosed() {
                isAdLoaded = false;
                bannerAdView.setVisibility(View.VISIBLE);
                Log.d(TAG, "onAdClosed: ");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                isAdLoaded = false;
                bannerAdView.setVisibility(View.GONE);
                Log.d(TAG, "onAdFailedToLoad: errorCode: " + errorCode);
            }

            @Override
            public void onAdLeftApplication() {
                Log.d(TAG, "onAdLeftApplication: ");
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }
        });

        bannerAdView.loadAd(adRequest);
    }

    private void loadInterstitialAd() {

        AdRequest adRequest1 = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                // Check the LogCat to get your test device ID
                .addTestDevice("5F338128595663F734E36512D2DF7D2F")
                .build();

        mInterstitialAd.setAdListener(new AdListener() {

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                loadInterstitialAd();
                Log.d(TAG, "onAdClosed: ");
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                Log.d(TAG, "onAdFailedToLoad: ");
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                Log.d(TAG, "onAdOpened: ");
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                Log.d(TAG, "onAdLeftApplication: ");
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.d(TAG, "onAdLoaded: ");
            }
        });

        // Load ads into Interstitial Ads
        mInterstitialAd.loadAd(adRequest1);
    }

    private void showPopupMenu(final CardView card) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
        bottomSheetDialog.setContentView(R.layout.dialog_chooser);
        CardView ivCamera = bottomSheetDialog.findViewById(R.id.cardCamera);
        CardView ivGallery = bottomSheetDialog.findViewById(R.id.cardGallery);

        ivCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)) {
                    Constants.verifyCameraPermissions(activity);
                } else {
                    openCamera(card);
                }
                bottomSheetDialog.dismiss();
            }
        });

        ivGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Constants.verifyStoragePermissions(activity);
                } else {
                    openGallery(card);
                }
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.show();
    }

    private void openGallery(CardView card) {

        if (card.getId() == R.id.cardVideoToGIF || card.getId() == R.id.cardVideoSlowMotion || card.getId() == R.id.cardWatermark || card.getId()==R.id.cardFilter) {
            getVideoFromGallery();
        } else {
            getImagesFromGallery();
        }

    }

    private void openCamera(CardView card) {


        if (card.getId() == R.id.cardVideoToGIF || card.getId() == R.id.cardVideoSlowMotion || card.getId() == R.id.cardWatermark || card.getId()==R.id.cardFilter) {
            getVideoFromCamera();
        } else {
            getImagesFromCamera();
        }

    }

    private void getVideoFromCamera() {

        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
        startActivityForResult(takeVideoIntent, Constants.REQUEST_TAKE_GALLERY_VIDEO);
    }

    private void getImagesFromCamera() {

        Intent intent = new Intent(activity, MultiCameraActivity.class);
        Params params = new Params();
        params.setCaptureLimit(10);
//        params.setToolbarColor(selectedColor);
//        params.setActionButtonColor(selectedColor);
//        params.setButtonTextColor(selectedColor);
        intent.putExtra(Constants.KEY_PARAMS, params);
        startActivityForResult(intent, Constants.REQUEST_TAKE_MULTI_IMAGES);
    }

    private void getVideoFromGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), Constants.REQUEST_TAKE_GALLERY_VIDEO);
    }

    private void getImagesFromGallery() {

        Intent intent = new Intent(activity, GalleryActivity.class);
        Params params = new Params();
        params.setCaptureLimit(10);
        params.setPickerLimit(10);
//        params.setToolbarColor(selectedColor);
//        params.setActionButtonColor(selectedColor);
//        params.setButtonTextColor(selectedColor);
        intent.putExtra(Constants.KEY_PARAMS, params);
        startActivityForResult(intent, Constants.REQUEST_GALLERY_MULTI_IMAGES);

    }



    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            switch (requestCode) {
                case Constants.REQUEST_TAKE_GALLERY_VIDEO:
                    Uri selectedImageUri = data.getData();

                    // MEDIA GALLERY
                    String videoPath = PathUtil.getPath(activity, selectedImageUri);

                    MediaPlayer mp = MediaPlayer.create(activity, Uri.parse(videoPath));
                    int duration = mp.getDuration() / 1000;
                    mp.release();

                    if (videoPath != null) {

                        if (duration > 3600) {
                            Toast.makeText(activity, "Video length exceeds limit", Toast.LENGTH_SHORT).show();
                        } else {

                            Bundle bundle = new Bundle();
                            bundle.putString("video", videoPath);

                            switch (SELECTED_TYPE) {
                                case Constants.TYPE_GIF:
                                    Constants.SELECTED = Constants.VIDEO_GIF_FRAGMENT;
                                    break;
                                case Constants.TYPE_IMAGE:
                                    Constants.SELECTED = Constants.CAPTURE_IMAGE_FRAGMENT;
                                    break;
                                case Constants.TYPE_AUDIO:
                                    Constants.SELECTED = Constants.VIDEO_AUDIO_FRAGMENT;
                                    break;
                                case Constants.TYPE_VIDEO:
                                    Constants.SELECTED = Constants.VIDEO_CUTTER_FRAGMENT;
                                    break;
                                case Constants.TYPE_MOTION:
                                    Constants.SELECTED = Constants.VIDEO_MOTION;
                                    break;
                                case Constants.TYPE_WATERMARK:
                                    Constants.SELECTED = Constants.VIDEO_WATERMARK;
                                    break;
                                case Constants.TYPE_FILTER:
                                    Constants.SELECTED=Constants.VIDEO_FILTER;
                                    break;
                            }

                            ((MainActivity) activity).loadFragment(bundle);
                        }
                    }
                    break;
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
        if (!imagePaths.isEmpty()) {
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("images", imagePaths);

            Constants.SELECTED = Constants.IMAGES_GIF_FRAGMENT;
            ((MainActivity) activity).loadFragment(bundle);
        }
    }
}
