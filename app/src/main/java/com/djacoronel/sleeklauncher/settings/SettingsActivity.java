package com.djacoronel.sleeklauncher.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import com.djacoronel.sleeklauncher.R;
import com.djacoronel.sleeklauncher.data.DbHelper;
import com.djacoronel.sleeklauncher.iconutils.IconPackManager;

import java.util.HashMap;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PrefsFragment()).commit();
    }

    public static class PrefsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.settings);
            setupCustomListPref();
            setupResetPref();
            setupBgPref();
        }

        private void setupCustomListPref() {
            ListPreference customListPref = (ListPreference) findPreference("iconPack");

            customListPref.setDialogTitle("Available Icon Packs");
            customListPref.setPersistent(true);
            customListPref.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            setIconPackPref((String) newValue);
                            return false;
                        }
                    }
            );

            setCustomListEntries(customListPref);
            setSavedPrefSummary(customListPref);
        }

        ListPreference setCustomListEntries(ListPreference customListPref){
            IconPackManager icManager = new IconPackManager(getActivity());
            HashMap<String, String> iconPacks = icManager.getAvailableIconPacks();
            CharSequence[] entries = iconPacks.keySet().toArray(new CharSequence[0]);
            CharSequence[] entryValues = iconPacks.values().toArray(new CharSequence[0]);

            customListPref.setEntries(entries);
            customListPref.setEntryValues(entryValues);

            return customListPref;
        }

        ListPreference setSavedPrefSummary(ListPreference customListPref){
            final SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            String icon_pack = sharedPref.getString(customListPref.getKey(), "Default");
            CharSequence entries[] = customListPref.getEntries();
            int index = customListPref.findIndexOfValue(icon_pack);

            if (index != -1) {
                customListPref.setSummary(entries[index]);
                customListPref.setValueIndex(index);
            } else {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(customListPref.getKey(), "");
                editor.apply();
            }

            return customListPref;
        }

        void setupResetPref(){
            Preference resetPref = findPreference("resetIcons");
            resetPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                            .setTitle("Icon Packs")
                            .setMessage("Uninstall or hide the app?")
                            .setCancelable(true)
                            .setNegativeButton("Reset", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DbHelper dbHelper = new DbHelper(getContext());
                                    dbHelper.removeAllFromCustom();

                                    setIconPackPref("");
                                }
                            })
                            .setPositiveButton("Cancel", null)
                            .create();
                    alertDialog.show();
                    return false;
                }
            });
        }

        void setupBgPref(){
            Preference bgPref = findPreference("backgroundPref");
            bgPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), BackgroundSettingsActivity.class);
                    startActivity(intent);

                    return false;
                }
            });
        }

        void setIconPackPref(String newValue) {
            ListPreference customListPref = (ListPreference) findPreference("iconPack");
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(customListPref.getKey(), newValue);
            editor.apply();

            CharSequence summary = customListPref.getEntries()[customListPref.findIndexOfValue(newValue)];
            customListPref.setSummary(summary);
            customListPref.setValueIndex(customListPref.findIndexOfValue(newValue));
        }
    }
}
