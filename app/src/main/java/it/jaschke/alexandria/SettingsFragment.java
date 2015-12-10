package it.jaschke.alexandria;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by a.g.seliverstov on 10.12.2015.
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String nv = newValue.toString();
        if (preference instanceof ListPreference){
            ListPreference lp = (ListPreference)preference;
            int i = lp.findIndexOfValue(nv);
            if (i>=0){
                preference.setSummary(lp.getEntries()[i]);
            }
        }else{
            preference.setSummary(nv);
        }
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        Preference p = findPreference(getString(R.string.pref_startScreen_key));
        onPreferenceChange(p,PreferenceManager.getDefaultSharedPreferences(p.getContext()).getString(p.getKey(),""));
        p.setOnPreferenceChangeListener(this);
        return view;
    }
}
