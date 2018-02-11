package com.djacoronel.sleeklauncher.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.ListPreference
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import com.djacoronel.sleeklauncher.R
import com.djacoronel.sleeklauncher.data.room.IconPrefsDao
import com.djacoronel.sleeklauncher.iconutils.IconPackManager
import dagger.android.AndroidInjection
import org.jetbrains.anko.alert
import javax.inject.Inject

class SettingsActivity : PreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, PrefsFragment()).commit()
    }

    class PrefsFragment : PreferenceFragment() {

        @Inject
        lateinit var iconPrefsDao: IconPrefsDao

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            AndroidInjection.inject(this)

            addPreferencesFromResource(R.xml.settings)
            setupCustomListPref()
            setupRowCountList()
            setupResetPref()
            setupBgPref()
        }

        private fun setupCustomListPref() {
            val customListPref = findPreference("iconPack") as ListPreference

            customListPref.dialogTitle = "Available Icon Packs"
            customListPref.isPersistent = true
            customListPref.setOnPreferenceChangeListener{ _, newValue ->
                setIconPackPref(newValue as String)
                false
            }

            setCustomListEntries(customListPref)
            setSavedPrefSummary(customListPref)
        }

        private fun setCustomListEntries(customListPref: ListPreference): ListPreference {
            val icManager = IconPackManager(activity)
            val iconPacks = icManager.availableIconPacks
            val entries = iconPacks.keys.toTypedArray<CharSequence>()
            val entryValues = iconPacks.values.toTypedArray<CharSequence>()

            customListPref.entries = entries
            customListPref.entryValues = entryValues

            return customListPref
        }

        private fun setSavedPrefSummary(customListPref: ListPreference): ListPreference {
            val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
            val iconPack = sharedPref.getString(customListPref.key, "Default")
            val entries = customListPref.entries
            val index = customListPref.findIndexOfValue(iconPack)

            if (index != -1) {
                customListPref.summary = entries[index]
                customListPref.setValueIndex(index)
            } else {
                val editor = sharedPref.edit()
                editor.putString(customListPref.key, "")
                editor.apply()
                customListPref.summary = "Default"
            }

            return customListPref
        }

        private fun setupRowCountList(){
            val rowCountPref = findPreference("rowCount") as ListPreference

            rowCountPref.dialogTitle = "Choose number of rows"
            rowCountPref.isPersistent = true

            rowCountPref.entries = arrayOf("3","4","5","6")
            rowCountPref.entryValues = arrayOf("3","4","5","6")

            rowCountPref.setOnPreferenceChangeListener { _, rowCount ->
                setRowCountPref(rowCount.toString().toInt())
                false
            }

            setSavedPrefSummary(rowCountPref)
        }

        private fun setRowCountPref(rowCount: Int){
            val customListPref = findPreference("rowCount") as ListPreference
            val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString(customListPref.key, rowCount.toString())
            editor.apply()

            val summary = rowCount.toString()
            customListPref.summary = summary
            customListPref.setValueIndex(customListPref.findIndexOfValue(rowCount.toString()))
        }

        private fun setupResetPref() {
            val resetPref = findPreference("resetIcons")
            resetPref.setOnPreferenceClickListener {
                alert {
                    title = "Reset Icon Preferences"
                    message = "Remove all custom icons and labels?"
                    negativeButton("Cancel") {}
                    positiveButton("Reset") {
                        iconPrefsDao.resetIconPrefs()
                        setIconPackPref("")
                    }
                }.show()
                false
            }
        }

        private fun setupBgPref() {
            val bgPref = findPreference("backgroundPref")
            bgPref.setOnPreferenceClickListener {
                val intent = Intent(activity.applicationContext, BackgroundSettingsActivity::class.java)
                startActivity(intent)
                false
            }
        }

        private fun setIconPackPref(newValue: String) {
            val customListPref = findPreference("iconPack") as ListPreference
            val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString(customListPref.key, newValue)
            editor.apply()

            val summary = customListPref.entries[customListPref.findIndexOfValue(newValue)]
            customListPref.summary = summary
            customListPref.setValueIndex(customListPref.findIndexOfValue(newValue))
        }
    }
}
