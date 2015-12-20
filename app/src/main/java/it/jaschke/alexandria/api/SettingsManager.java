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

    public static final int SEARCH_VIEW_EXPANDED = 0;
    public static final int SEARCH_VIEW_COLLAPSED = 1;

    public SettingsManager(Context context){
        mContext = context;
        mSp = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public int getCurrentSortOrder(){
        return Integer.valueOf(mSp.getString(mContext.getString(R.string.pref_sort_key), String.valueOf(SORT_ORDER_DATE_ASC)));
    }

    public void setSortOrder(int sortOrder){
        mSp.edit().putString(mContext.getString(R.string.pref_sort_key),String.valueOf(sortOrder)).apply();
    }

    public String getSortOrderForDb(){
        final String prefix = AlexandriaContract.BookEntry.IS_NEW+" DESC, ";
        switch (getCurrentSortOrder()){
            case SORT_ORDER_DATE_ASC: return prefix + AlexandriaContract.BookEntry.CREATED_AT +" ASC";
            case SORT_ORDER_DATE_DESC: return prefix + AlexandriaContract.BookEntry.CREATED_AT +" DESC";
            case SORT_ORDER_TITLE_ASC: return prefix + AlexandriaContract.BookEntry.TITLE +" ASC";
            case SORT_ORDER_TITLE_DESC: return prefix + AlexandriaContract.BookEntry.TITLE +" DESC";
            default: return null;
        }
    }

    public int getCurrentSearchViewMode(){
        return Integer.valueOf(mSp.getString(mContext.getString(R.string.pref_search_key), String.valueOf(SEARCH_VIEW_COLLAPSED)));
    }

    public void setCurrentSearchViewMode(int searchViewMode){
        mSp.edit().putString(mContext.getString(R.string.pref_search_key),String.valueOf(searchViewMode)).apply();
    }

    public boolean isSearchViewIconified(){
        return SEARCH_VIEW_COLLAPSED == getCurrentSearchViewMode();
    }

}
