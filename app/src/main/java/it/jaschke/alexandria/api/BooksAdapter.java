package it.jaschke.alexandria.api;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.skyfishjy.CursorRecyclerViewAdapter;
import com.squareup.picasso.Picasso;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.DownloadImage;

/**
 * Created by saj on 11/01/15.
 */
public class BooksAdapter extends CursorRecyclerViewAdapter<BooksAdapter.ViewHolder> {
    private static final String TAG = BooksAdapter.class.getSimpleName();
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public final ImageView bookCover;
        public final TextView bookTitle;
        public final TextView bookSubTitle;
        public final CardView cardView;

        public ViewHolder(View view) {
            super(view);
            bookCover = (ImageView) view.findViewById(R.id.fullBookCover);
            bookTitle = (TextView) view.findViewById(R.id.listBookTitle);
            bookSubTitle = (TextView) view.findViewById(R.id.listBookSubTitle);
            cardView = (CardView) view.findViewById(R.id.card_view);
        }
    }

    public BooksAdapter(Context context, Cursor c) {
        super(context, c);
        mContext = context;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        String imgUrl = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if (imgUrl!=null) {
            Uri url = Uri.parse(imgUrl);
            Picasso.with(mContext).load(url).into(viewHolder.bookCover);
        }

        String bookTitle = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        viewHolder.bookTitle.setText(bookTitle);

        String bookSubTitle = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        viewHolder.bookSubTitle.setText(bookSubTitle);

        final String ean = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry._ID));

        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Callback callback = (Callback)mContext;
                callback.onItemSelected(ean);
            }
        });

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }
}
