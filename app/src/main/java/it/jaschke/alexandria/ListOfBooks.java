package it.jaschke.alexandria;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
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
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;

import it.jaschke.alexandria.api.BooksAdapter;
import it.jaschke.alexandria.api.Callback;
import it.jaschke.alexandria.api.SettingsManager;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;


public class ListOfBooks extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = ListOfBooks.class.getSimpleName();

    public static final int SCAN_REQUEST = 0;

    public static final String MESSAGE_EVENT = "it.jaschke.alexandria.MESSAGE_EVENT";
    public static final String MESSAGE_KEY = "it.jaschke.alexandria.MESSAGE_EXTRA";
    public static final String MESSAGE_IS_FOUND = "it.jaschke.alexandria.MESSAGE_IS_FOUND";
    public static final String MESSAGE_IN_LIST = "it.jaschke.alexandria.MESSAGE_IN_LIST";

    public static final String SCAN_RESULT = "it.jaschke.alexandria.SCAN_RESULT";

    private BooksAdapter bookListAdapter;
    private RecyclerView bookList;
    private SearchView mSearchView;
    private TextView mBookNotFound;

    private BroadcastReceiver messageReceiver;

    private SettingsManager settingsManager;

    public static final int LOADER_ID = 10;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        bookListAdapter = new BooksAdapter(getActivity(), null);
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);

        bookList = (RecyclerView) rootView.findViewById(R.id.listOfBooks);
        bookList.setAdapter(bookListAdapter);
        bookList.setLayoutManager(new LinearLayoutManager(getActivity()));

        mBookNotFound = (TextView)rootView.findViewById(R.id.book_not_found);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(TAG, "onCreateLoader");
        final String selection = AlexandriaContract.BookEntry.TITLE +" LIKE ? OR " + AlexandriaContract.BookEntry.SUBTITLE + " LIKE ? ";

        String searchString = (mSearchView!=null)?mSearchView.getQuery().toString():"";

        if(!isQueryMathISBN(searchString) && searchString.length()>0){
            searchString = "%"+searchString+"%";
            return new CursorLoader(
                    getActivity(),
                    AlexandriaContract.BookEntry.CONTENT_URI,
                    null,
                    selection,
                    new String[]{searchString,searchString},
                    settingsManager.getSortOrderForDb());
        }else{
            return new CursorLoader(
                    getActivity(),
                    AlexandriaContract.BookEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    settingsManager.getSortOrderForDb());
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data==null || data.getCount()==0){

            mBookNotFound.setVisibility(View.VISIBLE);
        }else {
            Log.i(TAG,"data count "+ data.getCount());
            mBookNotFound.setVisibility(View.GONE);
            bookListAdapter.swapCursor(data);
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
            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (Pattern.compile("(^[\\d]{10}$)|(^[\\d]{13}$)").matcher(query).find()) {
                        if (query.length() == 10 && !query.startsWith("978")) {
                            query = "978" + query;
                        }
                        Intent bookIntent = new Intent(getActivity(), BookService.class);
                        bookIntent.putExtra(BookService.EAN, query);
                        bookIntent.setAction(BookService.FETCH_BOOK);
                        getActivity().startService(bookIntent);
                        mSearchView.setQuery("", false);
                    }
                    getLoaderManager().restartLoader(LOADER_ID, null, ListOfBooks.this);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (newText == null || "".equals(newText)) {
                        getLoaderManager().restartLoader(LOADER_ID, null, ListOfBooks.this);
                    }
                    return false;
                }
            });

            mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    getLoaderManager().restartLoader(LOADER_ID, null, ListOfBooks.this);
                    return false;
                }
            });

            mSearchView.setIconifiedByDefault(settingsManager.isSearchViewIconified());
            mSearchView.setIconified(settingsManager.isSearchViewIconified());
            mSearchView.clearFocus();
            mSearchView.setQueryHint(getActivity().getString(R.string.search_hint));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        settingsManager = new SettingsManager(getActivity());
        messageReceiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction()==MESSAGE_EVENT){
                    String ean = intent.getStringExtra(MESSAGE_KEY);
                    if (intent.getBooleanExtra(MESSAGE_IN_LIST,false)){
                        ((Callback)getActivity()).onItemSelected(ean);
                    }else {
                        Boolean isFound = intent.getBooleanExtra(MESSAGE_IS_FOUND, false);
                        String message = (isFound) ? getActivity().getString(R.string.book_was_added) : getActivity().getString(R.string.book_not_found);
                        Toast.makeText(context, String.format(message, ean), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(messageReceiver, filter);
        getLoaderManager().initLoader(LOADER_ID, null, this).forceLoad();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "requestCode " + requestCode + ", resultCode " + resultCode);
        if (requestCode==SCAN_REQUEST && resultCode == Activity.RESULT_OK){
            String query = data.getStringExtra(SCAN_RESULT);
            Log.i(TAG, query);
            mSearchView.setQuery(query, true);
        }
    }

    boolean isQueryMathISBN(String query){
        return Pattern.compile("[\\d]{10}|[\\d]]{13}").matcher(query).find();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(messageReceiver);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getActivity().getString(R.string.pref_sort_key))){
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }else if (key.equals(getActivity().getString(R.string.pref_search_key))){
            mSearchView.setIconifiedByDefault(settingsManager.isSearchViewIconified());
            mSearchView.setIconified(settingsManager.isSearchViewIconified());
            mSearchView.clearFocus();
        }
    }
}
