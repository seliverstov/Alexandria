package it.jaschke.alexandria;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import it.jaschke.alexandria.api.BooksAdapter;
import it.jaschke.alexandria.data.AlexandriaContract;


public class ListOfBooks extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ListOfBooks.class.getSimpleName();
    private BooksAdapter bookListAdapter;
    private RecyclerView bookList;
    private SearchView mSearchView;

    private int position = ListView.INVALID_POSITION;

    private final int LOADER_ID = 10;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        bookListAdapter = new BooksAdapter(getActivity(), null);
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        /*searchText = (EditText) rootView.findViewById(R.id.searchText);
        rootView.findViewById(R.id.searchButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ListOfBooks.this.restartLoader();
                    }
                }
        );*/

        bookList = (RecyclerView) rootView.findViewById(R.id.listOfBooks);
        bookList.setAdapter(bookListAdapter);
        bookList.setLayoutManager(new LinearLayoutManager(getActivity()));



        /*bookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = bookListAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    ((Callback)getActivity())
                            .onItemSelected(cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry._ID)));
                }
            }
        });
*/
        return rootView;
    }

    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(TAG,"onCreateLoader");
        final String selection = AlexandriaContract.BookEntry.TITLE +" LIKE ? OR " + AlexandriaContract.BookEntry.SUBTITLE + " LIKE ? ";

        String searchString = (mSearchView!=null)?mSearchView.getQuery().toString():"";

        if(searchString.length()>0){
            searchString = "%"+searchString+"%";
            return new CursorLoader(
                    getActivity(),
                    AlexandriaContract.BookEntry.CONTENT_URI,
                    null,
                    selection,
                    new String[]{searchString,searchString},
                    null
            );
        }

        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(TAG, "onLoadFinished");
        if (data!=null){
            Log.i(TAG, "cursor size "+data.getCount());
        }
        bookListAdapter.swapCursor(data);
        if (position != ListView.INVALID_POSITION) {
            bookList.smoothScrollToPosition(position);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bookListAdapter.swapCursor(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem!=null){
            mSearchView  = (SearchView)searchItem.getActionView();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID,null,this).forceLoad();
    }
}
