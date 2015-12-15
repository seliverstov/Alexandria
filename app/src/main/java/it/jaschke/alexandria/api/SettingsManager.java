package it.jaschke.alexandria.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.AlexandriaContract;

/**
 * Created by a.g.seliverstov on 15.12.2015.
 */
public class SettingsManager {
    private Context mContext;
    private SharedPreferences mSp;

    public static final int SORT_ORDER_DATE_ASC = 0;
    public static final int SORT_ORDER_DATE_DESC = 1;
    public static final int SORT_ORDER_TITLE_ASC = 2;
    public static final int SORT_ORDER_TITLE_DESC = 3;


    public SettingsManager(Context context){
        mContext = context;
        mSp = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public int getCurrentSortOrder(){
        int sortOrder = Integer.valueOf(mSp.getString(mContext.getString(R.string.pref_sort_key), String.valueOf(SORT_ORDER_DATE_ASC)));
        return sortOrder;
    }

    public void setSortOrder(int sortOrder){
        mSp.edit().putString(mContext.getString(R.string.pref_sort_key),String.valueOf(sortOrder)).apply();
    }

    public String getSortOrderForDb(){
        switch (getCurrentSortOrder()){
            case SORT_ORDER_DATE_ASC: return AlexandriaContract.BookEntry.CREATED_AT +" ASC";
            case SORT_ORDER_DATE_DESC: return AlexandriaContract.BookEntry.CREATED_AT +" ASC";
            case SORT_ORDER_TITLE_ASC: return AlexandriaContract.BookEntry.TITLE +" ASC";
            case SORT_ORDER_TITLE_DESC: return AlexandriaContract.BookEntry.TITLE +" DESC";
            default: return null;
        }
    }
}
