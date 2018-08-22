package com.himebaugh.xyzreader.adapter;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.himebaugh.xyzreader.R;
import com.himebaugh.xyzreader.data.ArticleLoader;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class ArticleListAdapter extends RecyclerView.Adapter<ArticleListAdapter.ListItemViewHolder> {

    private static final String TAG = ArticleListAdapter.class.getSimpleName();

    // An on-click handler to make it easy for an Activity to interface with the RecyclerView
    private OnClickHandler mClickHandler;

    // The interface that receives onClick messages
    public interface OnClickHandler {
        //void onClick(Movie movie);
        void onClick(int position, long id);
    }

    private Cursor mCursor;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    public ArticleListAdapter(OnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(ArticleLoader.Query._ID);
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_article, viewGroup, false);
        return new ListItemViewHolder(view);
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

    @Override
    public void onBindViewHolder(@NonNull ListItemViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));

        Date publishedDate = parsePublishedDate();
        if (!publishedDate.before(START_OF_EPOCH.getTime())) {

            holder.subtitleView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            publishedDate.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + "<br/>" + " by "
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)));
        } else {
            holder.subtitleView.setText(Html.fromHtml(
                    outputFormat.format(publishedDate)
                            + "<br/>" + " by "
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)));
        }

        String imageURL = mCursor.getString(ArticleLoader.Query.THUMB_URL);

        Picasso.get().load(imageURL).into(holder.thumbnailView);
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
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

    public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView thumbnailView;
        public TextView titleView;
        public TextView subtitleView;

        private ListItemViewHolder(View view) {
            super(view);
            thumbnailView = (ImageView) view.findViewById(R.id.thumbnail);
            titleView = (TextView) view.findViewById(R.id.article_title);
            subtitleView = (TextView) view.findViewById(R.id.article_subtitle);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            int adapterPosition = getAdapterPosition();

            Log.i(TAG, "getItemId(): " + getItemId());
            Log.i(TAG, "getAdapterPosition(): " + getAdapterPosition());

            mClickHandler.onClick(adapterPosition, getItemId());
        }
    }
}


