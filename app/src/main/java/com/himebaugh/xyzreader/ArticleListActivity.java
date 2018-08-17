package com.himebaugh.xyzreader;

import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.himebaugh.xyzreader.data.ArticleLoader;
import com.himebaugh.xyzreader.data.UpdaterService;

public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ArticleListActivity.class.getSimpleName();

    private TextView mHelloView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

        mHelloView = findViewById(R.id.hello_view);

        // Start the loader which loads from the database.
        getSupportLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {
            refresh();
        }
    }

    // retrieves json from internet
    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        // return null;
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        Log.i(TAG, "onLoadFinished: data=" + data.getCount());

        mHelloView.setText("onLoadFinished: count=" + data.getCount());
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}
