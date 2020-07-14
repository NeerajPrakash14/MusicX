package com.neeraj.musicX.activities;


import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import androidx.core.app.NavUtils;

import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.neeraj.musicX.R;
import com.neeraj.musicX.nowplaying.Timber5;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity implements Preference.OnPreferenceChangeListener {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private RewardedVideoAd mRewardedVideoAd;

    private com.facebook.ads.AdView adView;
    private com.facebook.ads.InterstitialAd interstitialAd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.preference);

        faninitialize();
        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        //bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_units_key)));
        bindPreferenceSummaryToValuesInt(findPreference(getString(R.string.pref_colors_key)));
        bindPreferenceSummaryToValuesBoolean(findPreference(getString(R.string.pref_switch_key)));
        bindPreferenceSummaryToValuesBoolean(findPreference(getString(R.string.pref_switch_wave_key)));
        MainActivity.audioWidget.hide();

        admobinitialize();
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
        if (interstitialAd.isAdLoaded()) {
            interstitialAd.show();
        }

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
            //Toast.makeText(SettingsActivity.this, "The Ad wasn't loaded yet.", Toast.LENGTH_SHORT).show();

        }

    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || Timber5.GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    private void bindPreferenceSummaryToValuesInt(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getInt(preference.getKey(), 0));
    }

    private void bindPreferenceSummaryToValuesBoolean(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getBoolean(preference.getKey(), true));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            //preference.setSummary(stringValue);
        }
        return true;
    }

}
