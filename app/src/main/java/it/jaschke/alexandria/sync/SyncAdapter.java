package it.jaschke.alexandria.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import it.jaschke.alexandria.ListOfBooks;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.AlexandriaContract;

/**
 * Created by a.g.seliverstov on 14.12.2015.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter{
    private static final String TAG = SyncAdapter.class.getSimpleName();

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "onPerformSync");
        try{
            Cursor bookEntry = provider.query(
                    AlexandriaContract.BookEntry.CONTENT_URI,
                    new String[]{AlexandriaContract.BookEntry._ID}, // leaving "columns" null just returns all the columns.
                    AlexandriaContract.BookEntry.IS_NEW+" = ?", // cols for "where" clause
                    new String[]{"1"}, // values for "where" clause
                    null  // sort order
            );

            if (!bookEntry.moveToFirst()) return;
            Log.i(TAG,"onPerformSync: waiting to sync "+bookEntry.getCount());
            do{
                String ean = String.valueOf(bookEntry.getLong(bookEntry.getColumnIndex(AlexandriaContract.BookEntry._ID)));
                Log.i(TAG,"Sync ISBN "+ean);
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String bookJsonString = null;

                try {
                    final String FORECAST_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
                    final String QUERY_PARAM = "q";

                    final String ISBN_PARAM = "isbn:" + ean;

                    Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                            .appendQueryParameter(QUERY_PARAM, ISBN_PARAM)
                            .build();

                    URL url = new URL(builtUri.toString());

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        return;
                    }

                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                        buffer.append("\n");
                    }

                    if (buffer.length() == 0) {
                        return;
                    }
                    bookJsonString = buffer.toString();
                } catch (Exception e) {
                    Log.e(TAG, "Error ", e);
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e(TAG, "Error closing stream", e);
                        }
                    }

                }

                final String ITEMS = "items";

                final String VOLUME_INFO = "volumeInfo";

                final String TITLE = "title";
                final String SUBTITLE = "subtitle";
                final String AUTHORS = "authors";
                final String DESC = "description";
                final String CATEGORIES = "categories";
                final String IMG_URL_PATH = "imageLinks";
                final String IMG_URL = "thumbnail";

                try {
                    JSONObject bookJson = new JSONObject(bookJsonString);
                    JSONArray bookArray;
                    if(bookJson.has(ITEMS)){
                        bookArray = bookJson.getJSONArray(ITEMS);

                        JSONObject bookInfo = ((JSONObject) bookArray.get(0)).getJSONObject(VOLUME_INFO);

                        String title = bookInfo.getString(TITLE);

                        String subtitle = "";
                        if(bookInfo.has(SUBTITLE)) {
                            subtitle = bookInfo.getString(SUBTITLE);
                        }

                        String desc="";
                        if(bookInfo.has(DESC)){
                            desc = bookInfo.getString(DESC);
                        }

                        String imgUrl = "";
                        if(bookInfo.has(IMG_URL_PATH) && bookInfo.getJSONObject(IMG_URL_PATH).has(IMG_URL)) {
                            imgUrl = bookInfo.getJSONObject(IMG_URL_PATH).getString(IMG_URL);
                        }

                        writeBackBook(provider, ean, title, subtitle, desc, imgUrl);

                        if(bookInfo.has(AUTHORS)) {
                            writeBackAuthors(provider,ean, bookInfo.getJSONArray(AUTHORS));
                        }
                        if(bookInfo.has(CATEGORIES)){
                            writeBackCategories(provider,ean,bookInfo.getJSONArray(CATEGORIES) );
                        }

                        Intent messageIntent = new Intent(ListOfBooks.MESSAGE_EVENT);
                        messageIntent.putExtra(ListOfBooks.MESSAGE_KEY,ean);
                        messageIntent.putExtra(ListOfBooks.MESSAGE_IS_FOUND,true);
                        LocalBroadcastManager.getInstance(getContext().getApplicationContext()).sendBroadcast(messageIntent);
                    }else{
                        provider.delete(AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(ean)),null,null);
                        Intent messageIntent = new Intent(ListOfBooks.MESSAGE_EVENT);
                        messageIntent.putExtra(ListOfBooks.MESSAGE_KEY,ean);
                        messageIntent.putExtra(ListOfBooks.MESSAGE_IS_FOUND,false);
                        LocalBroadcastManager.getInstance(getContext().getApplicationContext()).sendBroadcast(messageIntent);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error ", e);
                } catch (RemoteException e){
                    Log.e(TAG, "Error ", e);
                }
            }while(bookEntry.moveToNext());
        }catch (RemoteException e){
            Log.e(TAG, "Error ", e);
        }

    }



    private void writeBackBook(ContentProviderClient provider,String ean, String title, String subtitle, String desc, String imgUrl) throws RemoteException {
        ContentValues values= new ContentValues();
        values.put(AlexandriaContract.BookEntry._ID, ean);
        values.put(AlexandriaContract.BookEntry.TITLE, title);
        values.put(AlexandriaContract.BookEntry.IMAGE_URL, imgUrl);
        values.put(AlexandriaContract.BookEntry.SUBTITLE, subtitle);
        values.put(AlexandriaContract.BookEntry.DESC, desc);
        values.put(AlexandriaContract.BookEntry.CREATED_AT, System.currentTimeMillis());
        values.put(AlexandriaContract.BookEntry.IS_NEW, 0);
        provider.insert(AlexandriaContract.BookEntry.CONTENT_URI, values);
    }

    private void writeBackAuthors(ContentProviderClient provider,String ean, JSONArray jsonArray) throws JSONException, RemoteException {
        ContentValues values= new ContentValues();
        for (int i = 0; i < jsonArray.length(); i++) {
            values.put(AlexandriaContract.AuthorEntry._ID, ean);
            values.put(AlexandriaContract.AuthorEntry.AUTHOR, jsonArray.getString(i));
            provider.insert(AlexandriaContract.AuthorEntry.CONTENT_URI, values);
            values= new ContentValues();
        }
    }

    private void writeBackCategories(ContentProviderClient provider,String ean, JSONArray jsonArray) throws JSONException, RemoteException {
        ContentValues values= new ContentValues();
        for (int i = 0; i < jsonArray.length(); i++) {
            values.put(AlexandriaContract.CategoryEntry._ID, ean);
            values.put(AlexandriaContract.CategoryEntry.CATEGORY, jsonArray.getString(i));
            provider.insert(AlexandriaContract.CategoryEntry.CONTENT_URI, values);
            values= new ContentValues();
        }
    }

    public static void syncNow(Context context){
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);

        AccountManager accountManager =
                (AccountManager)context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), "alexandria.jaschke.it");

        if ( null == accountManager.getPassword(newAccount) ) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                Log.i(TAG,"Account doesn't exist");
                return;
            }
        }
        ContentResolver.requestSync(newAccount, AlexandriaContract.CONTENT_AUTHORITY, bundle);
    }
}
