
package com.neeraj.musicX.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.ads.AudienceNetworkAds;
import com.neeraj.musicX.R;
import com.neeraj.musicX.adapters.SearchAdapter;
import com.neeraj.musicX.dataloaders.AlbumLoader;
import com.neeraj.musicX.dataloaders.ArtistLoader;
import com.neeraj.musicX.dataloaders.SongLoader;
import com.neeraj.musicX.models.Album;
import com.neeraj.musicX.models.Artist;
import com.neeraj.musicX.models.Song;
import com.neeraj.musicX.provider.SearchHistory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, View.OnTouchListener {

    private SearchView mSearchView;
    private InputMethodManager mImm;
    private String queryString;

    private SearchAdapter adapter;
    private RecyclerView recyclerView;

    private List searchResults = Collections.emptyList();

    private com.facebook.ads.AdView adView;
    private com.facebook.ads.InterstitialAd interstitialAd;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        faninitialize();

        mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchAdapter(this);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        getMenuInflater().inflate(R.menu.menu_search, menu);

        mSearchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_search));

        mSearchView.setOnQueryTextListener(this);
        mSearchView.setQueryHint(getString(R.string.search_library));

        mSearchView.setIconifiedByDefault(false);
        mSearchView.setIconified(false);

        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.menu_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                finish();
                return false;
            }
        });

        menu.findItem(R.id.menu_search).expandActionView();
        return super.onCreateOptionsMenu(menu);
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

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(final String query) {
        onQueryTextChange(query);
        hideInputManager();

        return true;
    }

    @Override
    public boolean onQueryTextChange(final String newText) {

        if (newText.equals(queryString)) {
            return true;
        }
        queryString = newText;
        if (!queryString.trim().equals("")) {
            this.searchResults = new ArrayList();
            List<Song> songList = SongLoader.searchSongs(this, queryString);
            List<Album> albumList = AlbumLoader.getAlbums(this, queryString);
            List<Artist> artistList = ArtistLoader.getArtists(this, queryString);

            if (!songList.isEmpty()) {
                searchResults.add("Songs");
            }
            searchResults.addAll((Collection) (songList.size() < 10 ? songList : songList.subList(0, 10)));
            if (!albumList.isEmpty()) {
                searchResults.add("Albums");
            }
            searchResults.addAll((Collection) (albumList.size() < 7 ? albumList : albumList.subList(0, 7)));
            if (!artistList.isEmpty()) {
                searchResults.add("Artists");
            }
            searchResults.addAll((Collection) (artistList.size() < 7 ? artistList : artistList.subList(0, 7)));
        } else {
            searchResults.clear();
            adapter.updateSearchResults(searchResults);
            adapter.notifyDataSetChanged();
        }

        adapter.updateSearchResults(searchResults);
        adapter.notifyDataSetChanged();

        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        hideInputManager();
        return false;
    }

    public void hideInputManager() {
        if (mSearchView != null) {
            if (mImm != null) {
                mImm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
            }
            mSearchView.clearFocus();

            SearchHistory.getInstance(this).addSearchString(queryString);
        }
    }
}
