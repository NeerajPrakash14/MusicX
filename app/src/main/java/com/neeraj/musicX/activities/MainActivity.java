
package com.neeraj.musicX.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cleveroad.audiowidget.AudioWidget;
import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.neeraj.musicX.MusicPlayer;
import com.neeraj.musicX.R;
import com.neeraj.musicX.adapters.BaseQueueAdapter;
import com.neeraj.musicX.fragments.AlbumDetailFragment;
import com.neeraj.musicX.fragments.ArtistDetailFragment;
import com.neeraj.musicX.fragments.MainFragment;
import com.neeraj.musicX.fragments.PlaylistFragment;
import com.neeraj.musicX.fragments.QueueFragment;
import com.neeraj.musicX.fragments.SlideUpNowPlayingFragment;
import com.neeraj.musicX.permissions.Nammu;
import com.neeraj.musicX.permissions.PermissionCallback;
import com.neeraj.musicX.utils.Constants;
import com.neeraj.musicX.utils.Helpers;
import com.neeraj.musicX.utils.NavigationUtils;
import com.neeraj.musicX.utils.TimberUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends BaseActivity {

    private static final String EXTRA_FILE_URIS = "EXTRA_FILE_URIS";
    private static final String EXTRA_SELECT_TRACK = "EXTRA_SELECT_TRACK";
    private static final long UPDATE_INTERVAL = 1000;
    private static final String KEY_POSITION_X = "position_x";
    private static final String KEY_POSITION_Y = "position_y";
    private static final int TIME_INTERVAL = 2000;
    static final String YES_ACTION = "Sone Actions";

    private static final int OVERLAY_PERMISSION_REQ_CODE = 1;
    private static final int REQUEST_CODE = 1;
    private static final String TAG = MainActivity.class.getName();
    private boolean shouldOpenFragment;
    private static MainActivity sMainActivity;
    private boolean paused;
    private long mBackPressed;
    com.neeraj.musicX.slidinguppanel.SlidingUpPanelLayout panelLayout;
    NavigationView navigationView;
    TextView songtitle, songartist;
    ImageView albumart;
    String action;
    boolean floatWidget;
    SharedPreferences sharedPrefs;
    public static AudioWidget audioWidget;
    private Timer timer;
    private MediaPlayer mediaPlayer;
    private SharedPreferences preferences;
    Map<String, Runnable> navigationMap = new HashMap<String, Runnable>();
    Handler navDrawerRunnable = new Handler();
    Runnable runnable;

    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private RewardedVideoAd mRewardedVideoAd;

    private com.facebook.ads.AdView adView;
    private com.facebook.ads.InterstitialAd interstitialAd;

    Runnable navigateLibrary = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_library).setChecked(true);
            Fragment fragment = new MainFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment).commitAllowingStateLoss();

        }
    };
    Runnable navigateNowplaying = new Runnable() {
        public void run() {
            navigateLibrary.run();
            startActivity(new Intent(MainActivity.this, NowPlayingActivity.class));
        }
    };
    final PermissionCallback permissionReadstorageCallback = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            loadEverything();
        }

        @Override
        public void permissionRefused() {
            finish();
        }
    };
    Runnable navigatePlaylist = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_playlists).setChecked(true);
            Fragment fragment = new PlaylistFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.hide(getSupportFragmentManager().findFragmentById(R.id.fragment_container));
            transaction.replace(R.id.fragment_container, fragment).commit();

        }
    };
    Runnable navigateQueue = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_queue).setChecked(true);
            Fragment fragment = new QueueFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.hide(getSupportFragmentManager().findFragmentById(R.id.fragment_container));
            transaction.replace(R.id.fragment_container, fragment).commit();

        }
    };
    Runnable navigateAlbum = new Runnable() {
        public void run() {
            long albumID = getIntent().getExtras().getLong(Constants.ALBUM_ID);
            Fragment fragment = AlbumDetailFragment.newInstance(albumID, false, null);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment).commit();
        }
    };
    Runnable navigateArtist = new Runnable() {
        public void run() {
            long artistID = getIntent().getExtras().getLong(Constants.ARTIST_ID);
            Fragment fragment = ArtistDetailFragment.newInstance(artistID, false, null);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment).commit();
        }
    };
    private DrawerLayout mDrawerLayout;
    private boolean isDarkTheme;

    public static MainActivity getInstance() {
        return sMainActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        admobinitialize();
        faninitialize();
        sMainActivity = this;
        action = getIntent().getAction();

        isDarkTheme = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false);

        setTheme(R.style.ThemeBaseDark);
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.do_not_move, R.anim.do_not_move);
        setContentView(R.layout.activity_main);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (savedInstanceState == null) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.MODIFY_AUDIO_SETTINGS)) {
                AlertDialog.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            requestPermissions();
                        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                            //permissionsNotGranted();
                        }
                    }
                };
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.title_permissions))
                        .setMessage(Html.fromHtml(getString(R.string.message_permissions)))
                        .setPositiveButton(getString(R.string.btn_next), onClickListener)
                        .setNegativeButton(getString(R.string.btn_cancel), onClickListener)
                        .show();
            } else {
                requestPermissions();
            }
        }

        navigationMap.put(Constants.NAVIGATE_LIBRARY, navigateLibrary);
        navigationMap.put(Constants.NAVIGATE_PLAYLIST, navigatePlaylist);
        navigationMap.put(Constants.NAVIGATE_QUEUE, navigateQueue);
        navigationMap.put(Constants.NAVIGATE_NOWPLAYING, navigateNowplaying);
        navigationMap.put(Constants.NAVIGATE_ALBUM, navigateAlbum);
        navigationMap.put(Constants.NAVIGATE_ARTIST, navigateArtist);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        panelLayout = (com.neeraj.musicX.slidinguppanel.SlidingUpPanelLayout) findViewById(R.id.sliding_layout);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View header = navigationView.inflateHeaderView(R.layout.nav_header);

        albumart = (ImageView) header.findViewById(R.id.album_art);
        songtitle = (TextView) header.findViewById(R.id.song_title);
        songartist = (TextView) header.findViewById(R.id.song_artist);

        setPanelSlideListeners(panelLayout);

        navDrawerRunnable.postDelayed(new Runnable() {
            @Override
            public void run() {
                setupDrawerContent(navigationView);
                setupNavigationIcons(navigationView);
            }
        }, 700);


//        if (TimberUtils.isMarshmallow()) {
//            checkPermissionAndThenLoad();
//        } else {
        loadEverything();
        //}

        addBackstackListener();

        if (Intent.ACTION_VIEW.equals(action)) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MusicPlayer.clearQueue();
                    MusicPlayer.openFile(getIntent().getData().getPath());
                    MusicPlayer.playOrPause();
                    navigateNowplaying.run();
                }
            }, 350);
        }

        audioWidget = new AudioWidget.Builder(this)
                .lightColor(ContextCompat.getColor(this, R.color.widget_play))
                .darkColor(ContextCompat.getColor(this, R.color.widget_pause))
                .progressColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .progressStrokeWidth(25)
                .expandWidgetColor(ContextCompat.getColor(this, R.color.colorAccent))
                .defaultAlbumDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_music_anyway))
                .crossColor(ContextCompat.getColor(this, R.color.colorAccent))
                .crossOverlappedColor(ContextCompat.getColor(this, R.color.widget_pause))
                .playDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_arrow_white_24dp))
                .pauseDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_white_24dp))
                .playlistDrawable(ContextCompat.getDrawable(this, R.drawable.ic_playlist_play_white_24dp))
                .prevTrackDrawale(ContextCompat.getDrawable(this, R.drawable.ic_skip_previous_white_24dp))
                .nextTrackDrawable(ContextCompat.getDrawable(this, R.drawable.ic_skip_next_white_24dp))
                .progressColor(ContextCompat.getColor(this, R.color.widget_play))
                .build();

        /*audioWidget.controller().start();
        audioWidget.controller().position(0);
        audioWidget.controller().duration((int) MusicPlayer.duration());
        stopTrackingPosition();
        startTrackingPosition();*/

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        floatWidget = sharedPrefs.getBoolean(getString(R.string.pref_switch_key), true);

        if (floatWidget) {
            audioWidget.show(preferences.getInt(KEY_POSITION_X, getInitialX() - 200), preferences.getInt(KEY_POSITION_Y, getInitialY() - 300));
            floatWidget = false;
        }
        audioWidget.controller().onControlsClickListener(new AudioWidget.OnControlsClickListener() {

            @Override
            public boolean onPlaylistClicked() {
                // playlist icon clicked
                // return true to collapse widget, false to stay in expanded state
                Intent intent = new Intent(MainActivity.this, NowPlayingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                return true;
            }

            @Override
            public void onPreviousClicked() {
                // previous track button clicked
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MusicPlayer.previous(MainActivity.this, false);
                        notifyPlayingDrawableChange();
                    }
                }, 200);
            }

            @Override
            public boolean onPlayPauseClicked() {
                if (MusicPlayer.isPlaying()) {
                    //stopTrackingPosition();
                    MusicPlayer.playOrPause();
                    audioWidget.controller().start();
                    paused = true;
                } else {
                    //startTrackingPosition();
                    audioWidget.controller().pause();
                    MusicPlayer.playOrPause();
                    paused = false;
                }
                return true;

            }

            @Override
            public void onNextClicked() {
                // next track button clicked
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MusicPlayer.next();
                        notifyPlayingDrawableChange();
                    }
                }, 200);

            }

            @Override
            public void onAlbumClicked() {
                // album cover clicked
            }

        });

        audioWidget.controller().onWidgetStateChangedListener(new AudioWidget.OnWidgetStateChangedListener() {
            @Override
            public void onWidgetStateChanged(@NonNull AudioWidget.State state) {
                // widget state changed (COLLAPSED, EXPANDED, REMOVED)
            }

            @Override
            public void onWidgetPositionChanged(int cx, int cy) {
                // widget position change. Save coordinates here to reuse them next time AudioWidget.show(int, int) called.
                preferences.edit()
                        .putInt(String.valueOf(getInitialX() - 200), cx)
                        .putInt(String.valueOf(getInitialY() - 300), cy)
                        .apply();
            }
        });
        //getSong(MusicPlayer.getQueuePosition());

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

    private int getInitialX() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        float defaultChatHeadXPosition = width;
        return width;
    }

    private int getInitialY() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        float defaultChatHeadYPosition = height * 0.50f;
        return height;
    }

    public void getSong(int position) {
        SlideUpNowPlayingFragment fragment =
                (SlideUpNowPlayingFragment) getSupportFragmentManager().findFragmentById(R.id.quickcontrols_container);
        fragment.setCurrSongs(position);
    }

    private void startTrackingPosition() {
        timer = new Timer("MusicService Timer");
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                AudioWidget widget = audioWidget;
                //MediaPlayer player = mediaPlayer;
                if (widget != null) {
                    widget.controller().position((int) MusicPlayer.position());
                }
            }
        }, UPDATE_INTERVAL, UPDATE_INTERVAL);
    }

    private void stopTrackingPosition() {
        if (timer == null)
            return;
        timer.cancel();
        timer.purge();
        timer = null;
    }

    private void loadEverything() {
        Runnable navigation = navigationMap.get(action);
        if (navigation != null) {
            navigation.run();
        } else {
            navigateLibrary.run();
        }

        new initQuickControls().execute("");
    }


    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS},
                REQUEST_CODE
        );
    }

    private void checkPermissionAndThenLoad() {
        //check for permission
        if (Nammu.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            loadEverything();
        } else {
            if (Nammu.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(panelLayout, "Timber will need to read external storage to display songs on your device.",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Nammu.askForPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE, permissionReadstorageCallback);
                            }
                        }).show();
            } else {
                Nammu.askForPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, permissionReadstorageCallback);
            }
        }


    }

    public void notifyPlayingDrawableChange() {
        int position = MusicPlayer.getQueuePosition();
        BaseQueueAdapter.currentlyPlayingPosition = position;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
////            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
////                // now you can show audio widget
//////                if (!audioWidget.isShown()) {
//////                    audioWidget.show(preferences.getInt(KEY_POSITION_X, 100), preferences.getInt(KEY_POSITION_Y, 100));
//////                }
////            }
//        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                if (isNavigatingMain()) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                } else super.onBackPressed();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (panelLayout.isPanelExpanded())
            panelLayout.collapsePanel();
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (mBackPressed + TIME_INTERVAL < System.currentTimeMillis()) {
            Toast.makeText(getBaseContext(), "Press again to exit.", Toast.LENGTH_SHORT).show();
        } else
            finishAffinity();
        mBackPressed = System.currentTimeMillis();

    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(final MenuItem menuItem) {
                        updatePosition(menuItem);
                        return true;

                    }
                });
    }

    private void setupNavigationIcons(NavigationView navigationView) {

        //material-icon-lib currently doesn't work with navigationview of design support library 22.2.0+
        //set icons manually for now
        //https://github.com/code-mc/material-icon-lib/issues/15


        navigationView.getMenu().findItem(R.id.nav_library).setIcon(R.drawable.library_music);
        navigationView.getMenu().findItem(R.id.nav_playlists).setIcon(R.drawable.playlist_play);
        navigationView.getMenu().findItem(R.id.nav_queue).setIcon(R.drawable.music_note);
        navigationView.getMenu().findItem(R.id.nav_nowplaying).setIcon(R.drawable.bookmark_music);
        navigationView.getMenu().findItem(R.id.nav_settings).setIcon(R.drawable.settings);
        navigationView.getMenu().findItem(R.id.nav_about).setIcon(R.drawable.information);

    }

    private void updatePosition(final MenuItem menuItem) {
        runnable = null;

        switch (menuItem.getItemId()) {
            case R.id.nav_library:
                runnable = navigateLibrary;

                break;
            case R.id.nav_playlists:
                runnable = navigatePlaylist;
                if(interstitialAd.isAdLoaded()) {
                    interstitialAd.show();
                }

                break;
            case R.id.nav_nowplaying:
                NavigationUtils.navigateToNowplaying(MainActivity.this, false);
                if(interstitialAd.isAdLoaded()) {
                    interstitialAd.show();
                }
                break;
            case R.id.nav_queue:
                runnable = navigateQueue;

                break;
            case R.id.nav_about:
                mDrawerLayout.closeDrawers();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Helpers.showAbout(MainActivity.this);
                    }
                }, 350);
                if(interstitialAd.isAdLoaded()) {
                    interstitialAd.show();
                }
                break;
            case R.id.nav_settings:
                NavigationUtils.navigateToSettings(MainActivity.this);
                if(interstitialAd.isAdLoaded()) {
                    interstitialAd.show();
                }
                break;
        }

        if (runnable != null) {
            menuItem.setChecked(true);
            mDrawerLayout.closeDrawers();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            }, 350);
        }
    }

    public void setDetailsToHeader() {
        String name = MusicPlayer.getTrackName();
        String artist = MusicPlayer.getArtistName();

        if (name != null && artist != null) {
            songtitle.setText(name);
            songartist.setText(artist);
        }
        ImageLoader.getInstance().displayImage(TimberUtils.getAlbumArtUri(MusicPlayer.getCurrentAlbumId()).toString(), albumart,
                new DisplayImageOptions.Builder().cacheInMemory(true)
                        .showImageOnFail(R.drawable.ic_dribble)
                        .resetViewBeforeLoading(true)
                        .build());
    }

    @Override
    public void onMetaChanged() {
        super.onMetaChanged();
        setDetailsToHeader();
    }

    @Override
    public void onResume() {
        super.onResume();
        sMainActivity = this;
//        if (!audioWidget.isShown()) {
//            audioWidget.show(preferences.getInt(KEY_POSITION_X, 100), preferences.getInt(KEY_POSITION_Y, 100));
//        }
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
            //Toast.makeText(Converter_MainActivity.this, "The Ad wasn't loaded yet.", Toast.LENGTH_SHORT).show();
        }
//        if (!audioWidget.isShown()) {
//            audioWidget.show(preferences.getInt(KEY_POSITION_X, 100), preferences.getInt(KEY_POSITION_Y, 100));
//        }
    }

//    @Override
//    public void onRequestPermissionsResult(
//            int requestCode, String[] permissions, int[] grantResults) {
//        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_CODE) {
//            boolean bothGranted = true;
//            for (int i = 0; i < permissions.length; i++) {
//                if (Manifest.permission.RECORD_AUDIO.equals(permissions[i]) || Manifest.permission.MODIFY_AUDIO_SETTINGS.equals(permissions[i])) {
//                    bothGranted &= grantResults[i] == PackageManager.PERMISSION_GRANTED;
//                }
//            }
//            if (bothGranted) {
//                shouldOpenFragment = true;
//            } else {
//                //permissionsNotGranted();
//            }
//        }
//    }

    private boolean isNavigatingMain() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        return (currentFragment instanceof MainFragment || currentFragment instanceof QueueFragment
                || currentFragment instanceof PlaylistFragment);
    }

    private void addBackstackListener() {
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                getSupportFragmentManager().findFragmentById(R.id.fragment_container).onResume();
            }
        });
    }




}


