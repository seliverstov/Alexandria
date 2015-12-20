package it.jaschke.alexandria;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;


public class BookDetail extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EAN_KEY = "it.jaschke.alexandria.EAN";
    private final int LOADER_ID = 10;

    private String ean;
    private String bookTitle;
    private ShareActionProvider shareActionProvider;

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbar;
    @Bind(R.id.fullBookTitle) TextView mTitle;
    @Bind(R.id.fullBookSubTitle) TextView mSubTitle;
    @Bind(R.id.authors) TextView mAuthors;
    @Bind(R.id.categories) TextView mCategoris;
    @Bind(R.id.fullBookDesc) TextView mDesc;
    @Bind(R.id.fullBookCover) ImageView mCover;
    @Bind(R.id.fab_delete) FloatingActionButton mDelete;

    public BookDetail(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        View rootView;
        if (arguments != null) {
            ean = arguments.getString(BookDetail.EAN_KEY);
        }else{
            rootView = inflater.inflate(R.layout.no_book, container, false);
            return rootView;
        }

        rootView = inflater.inflate(R.layout.fragment_details, container, false);

        ButterKnife.bind(this, rootView);

        if (getActivity() instanceof DetailsActivity) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
            ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (toolbar!=null) toolbar.setDisplayHomeAsUpEnabled(true);
        }

        mDelete.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent bookIntent = new Intent(getActivity(), BookService.class);
               bookIntent.putExtra(BookService.EAN, ean);
               bookIntent.setAction(BookService.DELETE_BOOK);
               getActivity().startService(bookIntent);
               getActivity().finish();
           }
        });
        getLoaderManager().restartLoader(LOADER_ID, null, this);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.book_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + bookTitle);
        shareActionProvider.setShareIntent(shareIntent);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(ean)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        mCollapsingToolbar.setTitle(bookTitle);
        mTitle.setText(bookTitle);

        if (shareActionProvider!=null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + bookTitle);
            shareActionProvider.setShareIntent(shareIntent);
        }

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        if (bookSubTitle!=null && bookSubTitle.length()>0) {
            mSubTitle.setText(bookSubTitle);
            mSubTitle.setVisibility(View.VISIBLE);
        }

        String desc = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.DESC));
        if (desc!=null && desc.length()>0)
            mDesc.setText(desc);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        if (authors!=null && authors.length()>0){
            String[] authorsArr = authors.split(",");
            mAuthors.setLines(authorsArr.length);
            mAuthors.setText(authors.replace(",", "\n"));
        }


        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(imgUrl!=null && Patterns.WEB_URL.matcher(imgUrl).matches()){
            Uri url = Uri.parse(imgUrl);
            Picasso.with(getActivity()).load(url).placeholder(R.drawable.no_cover).error(R.drawable.no_cover).into(mCover);
        }else{
            mCover.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.no_cover));
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        if (categories!=null && categories.length()>0)
            mCategoris.setText(categories);
        }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

}