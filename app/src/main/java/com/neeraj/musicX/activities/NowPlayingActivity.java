package com.neeraj.musicX.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.neeraj.musicX.R;
import com.neeraj.musicX.nowplaying.Timber1;
import com.neeraj.musicX.nowplaying.Timber2;
import com.neeraj.musicX.nowplaying.Timber3;
import com.neeraj.musicX.nowplaying.Timber4;
import com.neeraj.musicX.nowplaying.Timber5;
import com.neeraj.musicX.utils.Constants;

/**
 * Created by rohan on 01/01/16.
 */
public class NowPlayingActivity extends BaseActivity {

    private Toolbar mToolbar;

    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private RewardedVideoAd mRewardedVideoAd;

    private com.facebook.ads.AdView adView;
    private com.facebook.ads.InterstitialAd interstitialAd;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nowplaying);
        mToolbar = (Toolbar) findViewById(R.id.app_bar);

        admobinitialize();
        faninitialize();
//        StatusBarUtil.setTranslucent(this);

        //setSupportActionBar(mToolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);

        /*android.support.v7.app.ActionBar aBar = getSupportActionBar();
        aBar.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));*/

/*        ATE.config(this, null)
                .statusBarColor(Color.TRANSPARENT)
                .coloredNavigationBar(true)
                .navigationBarColor(Color.TRANSPARENT)
                .toolbarColor(Color.TRANSPARENT)
                .apply(this);*/

        SharedPreferences prefs = getSharedPreferences(Constants.FRAGMENT_ID, Context.MODE_PRIVATE);
        String fragmentID = prefs.getString(Constants.NOWPLAYING_FRAGMENT_ID, Constants.TIMBER3);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String unitType = sharedPrefs.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_timber4));

        Fragment fragment = getFragmentForNowplayingID(unitType);
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment).commit();

    }


    private void admobinitialize() {

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        /*mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);*/

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-1397331690199170/2802211670");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

       /* mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideAd();
        mRewardedVideoAd.show();*/

        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            //Toast.makeText(Converter_MainActivity.this, "The Ad wasn't loaded yet.", Toast.LENGTH_SHORT).show();
        }

    }

    public Fragment getFragmentForNowplayingID(String fragmentID) {
        switch (fragmentID) {
            case Constants.TIMBER1:
                return new Timber1();
            case Constants.TIMBER2:
                return new Timber2();
            case Constants.TIMBER3:
                return new Timber3();
            case Constants.TIMBER4:
                return new Timber4();
            case Constants.TIMBER5:
                return new Timber5();
            default:
                return new Timber4();
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        /*if (PreferencesUtility.getInstance(this).didNowplayingThemeChanged()) {
            PreferencesUtility.getInstance(this).setNowPlayingThemeChanged(false);
            recreate();
        }*/
    }

    /*@Override
    public int getLightToolbarMode(Toolbar toolbar) {
        return Config.LIGHT_TOOLBAR_ON;
    }

    @Override
    public int getToolbarColor(Toolbar toolbar) {
        return Color.TRANSPARENT;
    }*/

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

    @Override
    protected void onStart() {
        super.onStart();

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-1397331690199170/2802211670");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

       /* mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideAd();
        mRewardedVideoAd.show();*/

        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            //Toast.makeText(NowPlayingActivity.this, "The Ad wasn't loaded yet.", Toast.LENGTH_SHORT).show();
        }
    }
}
