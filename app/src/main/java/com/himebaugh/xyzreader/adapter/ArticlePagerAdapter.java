package com.himebaugh.xyzreader.adapter;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.himebaugh.xyzreader.ArticleDetailFragment;
import com.himebaugh.xyzreader.data.ArticleLoader;

public class ArticlePagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = ArticlePagerAdapter.class.getSimpleName();

    private Cursor mCursor;

    public ArticlePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        ArticleDetailFragment fragment = (ArticleDetailFragment) object;
    }

    @Override
    public Fragment getItem(int position) {
        mCursor.moveToPosition(position);

        Log.i(TAG, "ArticleLoader.Query._ID: " + ArticleLoader.Query._ID);
        Log.i(TAG, "mCursor.getLong(ArticleLoader.Query._ID): " + mCursor.getLong(ArticleLoader.Query._ID));

        return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID));
    }

    @Override
    public int getCount() {
        return (mCursor != null) ? mCursor.getCount() : 0;
    }


    /**
     * When data changes and a re-query occurs, this function swaps the old Cursor
     * with a newly updated Cursor (Cursor c) that is passed in.
     */
    public Cursor loadArticles(Cursor articleCursor) {

        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == articleCursor) {
            return null; // bc nothing has changed
        }
        Cursor temp = mCursor;
        mCursor = articleCursor; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (articleCursor != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }

}
