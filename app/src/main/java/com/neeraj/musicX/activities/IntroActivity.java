package com.neeraj.musicX.activities;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.facebook.ads.AudienceNetworkAds;
import com.neeraj.musicX.R;
import com.neeraj.musicX.utils.PrefManager;

/**
 * Created by rohan on 13-02-2017.
 */
public class IntroActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private static final String TAG = IntroActivity.class.getSimpleName();
    private static final int OVERLAY_PERMISSION_REQ_CODE = 100;

    private com.facebook.ads.AdView adView;
    private com.facebook.ads.InterstitialAd interstitialAd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        faninitialize();

        PrefManager prefManager = new PrefManager(this);
        if (!prefManager.isFirstTimeLaunch()) {
            prefManager.setFirstTimeLaunch(false);
            startActivity(new Intent(this, MainActivity.class));
            return;
            //finish();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
        }

        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(this,
                new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, new PermissionsResultAction() {

                    @Override
                    public void onGranted() {
                        Intent i = new Intent(IntroActivity.this, MainActivity.class);
                        startActivity(i);
                    }

                    @Override
                    public void onDenied(String permission) {
                        Toast.makeText(IntroActivity.this,
                                "Sorry, we need the Storage Permission to do that",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        //requestPermissions();
    }

    private void faninitialize() {

        AudienceNetworkAds.initialize(this);

        //adView = new AdView(Converter_MainActivity.this, "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID", AdSize.BANNER_HEIGHT_50);
        /*adView = new com.facebook.ads.AdView(this, "253012682465759_253661875734173", AdSize.BANNER_HEIGHT_50);
        LinearLayout adContainer = (LinearLayout) findViewById(R.id.banner_container);
        adContainer.addView(adView);
        adView.loadAd();*/

        interstitialAd = new com.facebook.ads.InterstitialAd(this, "2985095734899724_2985097014899596");
        interstitialAd.loadAd();
        if(interstitialAd.isAdLoaded()) {
            interstitialAd.show();
        }

    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "Activity-onRequestPermissionsResult() PermissionsManager.notifyPermissionsChange()");
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
    }


}
