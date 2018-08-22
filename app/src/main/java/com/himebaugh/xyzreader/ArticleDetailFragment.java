package com.himebaugh.xyzreader;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.himebaugh.xyzreader.data.ArticleLoader;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    CollapsingToolbarLayout mCollapsingToolbarLayout;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        setHasOptionsMenu(true);
    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.

        getLoaderManager().initLoader(103, null, this);

        // getActivity().getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        Toolbar toolbar = mRootView.findViewById(R.id.toolbar);
        mCollapsingToolbarLayout = mRootView.findViewById(R.id.toolbar_layout);

        // https://stackoverflow.com/questions/25037356/getsupportactionbar-not-available-in-fragment-workaround-leads-to-nullpointere/25037502
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Implemented Up Navigation in AndroidManifest but seems inconsistent.
        // Can't determine why.
        // This helps
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        bindViews();

        return mRootView;
    }

    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        ImageView photoView = mRootView.findViewById(R.id.photo);
        //mPhotoContainerView =   mRootView.findViewById(R.id.photo_container);
        TextView titleView =    mRootView.findViewById(R.id.article_title);
        TextView bylineView =   mRootView.findViewById(R.id.article_byline);
        TextView bodyView =     mRootView.findViewById(R.id.article_body);

        if (mCursor != null) {

            mCollapsingToolbarLayout.setTitle(mCursor.getString(ArticleLoader.Query.TITLE));

            Picasso.get().load(mCursor.getString(ArticleLoader.Query.PHOTO_URL)).into(photoView);

            titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));

            Date publishedDate = parsePublishedDate();

            if (!publishedDate.before(START_OF_EPOCH.getTime())) {
                bylineView.setText(Html.fromHtml(
                        DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()
                                + " by <font color='#ffffff'>"
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)
                                + "</font>"));

            } else {
                // If date is before 1902, just show the string
                bylineView.setText(Html.fromHtml(
                        outputFormat.format(publishedDate) + " by <font color='#ffffff'>"
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)
                                + "</font>"));

            }

            // FULL TEXT HERE...
            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY).replaceAll("(\r\n|\n)", "<br />")));

            // TRUNCATED TEXT HERE... to speed up...
//            String bodyString = mCursor.getString(ArticleLoader.Query.BODY);
//            bodyString = bodyString.substring(0, Math.min(bodyString.length(), 50000));
//            bodyString.replaceAll("(\r\n|\n)", "<br />");
//            bodyView.setText(Html.fromHtml(bodyString));
        }



        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {

            String shareTitle = "";
            String shareBody = "";

            @Override
            public void onClick(View view) {

                if (mCursor != null) {
                    shareTitle = mCursor.getString(ArticleLoader.Query.TITLE);
                    // Limit size of text to share...
                    // Caused by: android.os.TransactionTooLargeException: data parcel size 1161216 bytes
                    shareBody = mCursor.getString(ArticleLoader.Query.BODY);
                    // upTo 50,000 Characters
                    // https://stackoverflow.com/questions/1583940/how-do-i-get-the-first-n-characters-of-a-string-without-checking-the-size-or-goi/1583968
                    shareBody = shareBody.substring(0, Math.min(shareBody.length(), 5000));
                }

                // https://medium.com/google-developers/sharing-content-between-android-apps-2e6db9d1368b

                Intent shareIntent = ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setSubject(shareTitle)
                        .setText(shareBody)
                        .setChooserTitle(R.string.action_share)
                        .getIntent();
                if (shareIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.action_share)));
                }
            }
        });

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }

}
