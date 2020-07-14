
package com.neeraj.musicX.activities;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.ads.AudienceNetworkAds;
import com.neeraj.musicX.R;
import com.neeraj.musicX.adapters.SongsListAdapter;
import com.neeraj.musicX.dataloaders.LastAddedLoader;
import com.neeraj.musicX.dataloaders.PlaylistSongLoader;
import com.neeraj.musicX.dataloaders.SongLoader;
import com.neeraj.musicX.dataloaders.TopTracksLoader;
import com.neeraj.musicX.listeners.SimplelTransitionListener;
import com.neeraj.musicX.models.Song;
import com.neeraj.musicX.utils.Constants;
import com.neeraj.musicX.utils.PreferencesUtility;
import com.neeraj.musicX.utils.TimberUtils;
import com.neeraj.musicX.widgets.DividerItemDecoration;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.HashMap;
import java.util.List;

public class PlaylistDetailActivity extends AppCompatActivity {

    String action;
    long playlistID;
    HashMap<String, Runnable> playlistsMap = new HashMap<>();
    Runnable playlistLastAdded = new Runnable() {
        public void run() {
            new loadLastAdded().execute("");
        }
    };
    Runnable playlistRecents = new Runnable() {
        @Override
        public void run() {
            new loadRecentlyPlayed().execute("");

        }
    };
    Runnable playlistToptracks = new Runnable() {
        @Override
        public void run() {
            new loadTopTracks().execute("");
        }
    };
    Runnable playlistUsercreated = new Runnable() {
        @Override
        public void run() {
            new loadUserCreatedPlaylist().execute("");

        }
    };
    private AppCompatActivity mContext = PlaylistDetailActivity.this;
    private SongsListAdapter mAdapter;
    private RecyclerView recyclerView;
    private ImageView blurFrame;
    private TextView playlistname;
    private View foreground;

    private com.facebook.ads.AdView adView;
    private com.facebook.ads.InterstitialAd interstitialAd;

    @TargetApi(21)
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);

        action = getIntent().getAction();

        faninitialize();

        playlistsMap.put(Constants.NAVIGATE_PLAYLIST_LASTADDED, playlistLastAdded);
        playlistsMap.put(Constants.NAVIGATE_PLAYLIST_RECENT, playlistRecents);
        playlistsMap.put(Constants.NAVIGATE_PLAYLIST_TOPTRACKS, playlistToptracks);
        playlistsMap.put(Constants.NAVIGATE_PLAYLIST_USERCREATED, playlistUsercreated);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        blurFrame = (ImageView) findViewById(R.id.blurFrame);
        playlistname = (TextView) findViewById(R.id.name);
        foreground = findViewById(R.id.foreground);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setAlbumart();

        if (TimberUtils.isLollipop() && PreferencesUtility.getInstance(this).getAnimations()) {
            getWindow().getEnterTransition().addListener(new EnterTransitionListener());
        } else {
            setUpSongs();
        }

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

    private void setAlbumart() {
        playlistname.setText(getIntent().getExtras().getString(Constants.PLAYLIST_NAME));
        foreground.setBackgroundColor(getIntent().getExtras().getInt(Constants.PLAYLIST_FOREGROUND_COLOR));
        loadBitmap(TimberUtils.getAlbumArtUri(getIntent().getExtras().getLong(Constants.ALBUM_ID)).toString());
    }

    private void setUpSongs() {
        Runnable navigation = playlistsMap.get(action);
        if (navigation != null) {
            navigation.run();
        } else {
            Log.d("PlaylistDetail", "no action specified");
        }
    }

    private void loadBitmap(String uri) {
        ImageLoader.getInstance().displayImage(uri, blurFrame,
                new DisplayImageOptions.Builder().cacheInMemory(true)
                        .showImageOnFail(R.drawable.ic_dribble)
                        .resetViewBeforeLoading(true)
                        .build());
    }

    private void setRecyclerViewAapter() {
        recyclerView.setAdapter(mAdapter);
        if (TimberUtils.isLollipop() && PreferencesUtility.getInstance(mContext).getAnimations()) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST, R.drawable.item_divider_white));
                }
            }, 250);
        } else
            recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST, R.drawable.item_divider_white));
    }

    /*@StyleRes
    @Override
    public int getActivityTheme() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false) ? R.style.AppTheme : R.style.AppTheme;

    }*/

    private class loadLastAdded extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            List<Song> lastadded = LastAddedLoader.getLastAddedSongs(mContext);
            mAdapter = new SongsListAdapter(mContext, lastadded, true);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            setRecyclerViewAapter();
        }

        @Override
        protected void onPreExecute() {
        }
    }

    private class loadRecentlyPlayed extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            TopTracksLoader loader = new TopTracksLoader(mContext, TopTracksLoader.QueryType.RecentSongs);
            List<Song> recentsongs = SongLoader.getSongsForCursor(TopTracksLoader.getCursor());
            mAdapter = new SongsListAdapter(mContext, recentsongs, true);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            setRecyclerViewAapter();

        }

        @Override
        protected void onPreExecute() {
        }
    }

    private class loadTopTracks extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            TopTracksLoader loader = new TopTracksLoader(mContext, TopTracksLoader.QueryType.TopTracks);
            List<Song> toptracks = SongLoader.getSongsForCursor(TopTracksLoader.getCursor());
            mAdapter = new SongsListAdapter(mContext, toptracks, true);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            setRecyclerViewAapter();
        }

        @Override
        protected void onPreExecute() {
        }
    }

    private class loadUserCreatedPlaylist extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            playlistID = getIntent().getExtras().getLong(Constants.PLAYLIST_ID);
            List<Song> playlistsongs = PlaylistSongLoader.getSongsInPlaylist(mContext, playlistID);
            mAdapter = new SongsListAdapter(mContext, playlistsongs, true);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            setRecyclerViewAapter();
        }

        @Override
        protected void onPreExecute() {
        }
    }

    private class EnterTransitionListener extends SimplelTransitionListener {

        @TargetApi(21)
        public void onTransitionEnd(Transition paramTransition) {
            setUpSongs();
        }

        public void onTransitionStart(Transition paramTransition) {
        }

    }


}
