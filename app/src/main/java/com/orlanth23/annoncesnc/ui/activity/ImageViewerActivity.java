package com.orlanth23.annoncesnc.ui.activity;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.ui.glide.GlideApp;
import com.orlanth23.annoncesnc.utility.Constants;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;


public class ImageViewerActivity extends AppCompatActivity {

    public static final String BUNDLE_KEY_URI = "URI";

    private final Handler mHideHandler = new Handler();
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
            mHideHandler.removeCallbacks(mShowPart2Runnable);
        }
    };
    @BindView(R.id.imageViewer)
    ImageViewTouch imageViewer;
    private String P_FILE_URI_TEMP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        ButterKnife.bind(this);
        Bundle bundle;

        if (savedInstanceState != null) {
            bundle = savedInstanceState;
        } else {
            bundle = getIntent().getExtras();
        }

        if (bundle != null) {
            P_FILE_URI_TEMP = bundle.getString(BUNDLE_KEY_URI);
            if (P_FILE_URI_TEMP != null)
                if (P_FILE_URI_TEMP.contains(Constants.PROTOCOL_HTTP) || P_FILE_URI_TEMP.contains(Constants.PROTOCOL_HTTPS)) {
                    GlideApp.with(this).load(P_FILE_URI_TEMP).placeholder(R.drawable.progress_refresh).error(R.drawable.ic_camera_black).into(imageViewer);
                } else {
                    Uri uri = Uri.parse(P_FILE_URI_TEMP);
                    GlideApp.with(this).load(new File(String.valueOf(uri))).placeholder(R.drawable.progress_refresh).error(R.drawable.ic_camera_black).into(imageViewer);
                }
        }
        imageViewer.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_KEY_URI, P_FILE_URI_TEMP);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide(100);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getFragmentManager().popBackStackImmediate();
    }
}
