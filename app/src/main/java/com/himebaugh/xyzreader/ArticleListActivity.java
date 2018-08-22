package com.himebaugh.xyzreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.himebaugh.xyzreader.adapter.ArticleListAdapter;
import com.himebaugh.xyzreader.data.ArticleLoader;
import com.himebaugh.xyzreader.data.ItemsContract;
import com.himebaugh.xyzreader.data.UpdaterService;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

// TODO: Tablet Layout, Material Fonts, Text spacing, Elevations

// COMPLETED: App theme extends from AppCompat.
//
// COMPLETED: App uses the Design Support library and its provided widget types (FloatingActionButton, AppBarLayout, SnackBar, etc).
//
// COMPLETED: App uses CoordinatorLayout for the main Activity.
//
// COMPLETED: App uses an AppBar and associated Toolbars.
//
// COMPLETED: App provides a Floating Action Button for the most common action(s).
//
// TODO: App properly specifies elevations for app bars, FABs, and other elements specified in the Material Design specification.
//
// COMPLETED: App has a consistent color theme defined in styles.xml. Color theme does not impact usability of the app.
//
// TODO: App provides sufficient space between text and surrounding elements.
//
// COMPLETED: App uses images that are high quality, specific, and full bleed.
//
// TODO: App uses fonts that are either the Android defaults, are complementary, and aren't otherwise distracting.
//
// TODO: App conforms to common standards found in the Android Nanodegree General Project Guidelines.
//
// COMPLETED: App utilizes stable release versions of all libraries, Gradle, and Android Studio.

public class ArticleListActivity extends AppCompatActivity
        implements
        LoaderManager.LoaderCallbacks<Cursor>,
        ArticleListAdapter.OnClickHandler,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = ArticleListActivity.class.getSimpleName();
    private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    private ArticleListAdapter mAdapter;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

        mToolbar = findViewById(R.id.toolbar);

        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = findViewById(R.id.recycler_view);

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new ArticleListAdapter(this);
        mAdapter.setHasStableIds(true);   // not sure what this does?
        mRecyclerView.setAdapter(mAdapter);

        // Start the loader which loads from the database.
        getSupportLoaderManager().initLoader(101, null, this);

        if (savedInstanceState == null) {
            refresh();
        }
    }

    // retrieves json from internet
    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mRefreshingReceiver, new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }

    private boolean mIsRefreshing = false;

    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i(TAG, "onReceive: ");
            
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    private void updateRefreshingUI() {
        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        // return null;
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        mAdapter.loadArticles(data);

        // change depending on device layout
        int columnCount = getResources().getInteger(R.integer.list_column_count);

        StaggeredGridLayoutManager staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(staggeredGridLayoutManager);

        Log.i(TAG, "onLoadFinished: data=" + data.getCount());
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

        mRecyclerView.setAdapter(null);
    }

    @Override
    public void onClick(int position, long id) {

        Log.i(TAG, "onClick: ");
        Log.i(TAG, "position: "+position);
        Log.i(TAG, "id: "+id);
        Log.i(TAG, "ItemsContract.Items.buildItemUri(position): " + ItemsContract.Items.buildItemUri(position));

        Intent intent = new Intent(Intent.ACTION_VIEW, ItemsContract.Items.buildItemUri(position));
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        Log.i(TAG, "onRefresh: Trigger reloading data");

        refresh();
    }

}
