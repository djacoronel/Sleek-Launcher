package com.djacoronel.sleeklauncher.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.ListPreference
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.djacoronel.sleeklauncher.R
import com.djacoronel.sleeklauncher.data.room.IconPrefsDao
import com.djacoronel.sleeklauncher.iconutils.IconPackManager
import dagger.android.AndroidInjection
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
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
            setupColumnCountList()
            setupResetPref()
            setupBgPref()
        }

        private fun setupCustomListPref() {
            val customListPref = findPreference("iconPack") as ListPreference

            customListPref.dialogTitle = "Available Icon Packs"
            customListPref.isPersistent = true
            customListPref.setOnPreferenceChangeListener { _, newValue ->
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

        private fun setupColumnCountList() {
            val columnCountPref = findPreference("columnCount") as ListPreference

            columnCountPref.dialogTitle = "Choose number of columns"
            columnCountPref.isPersistent = true

            columnCountPref.entries = arrayOf("3", "4", "5", "6")
            columnCountPref.entryValues = arrayOf("3", "4", "5", "6")

            columnCountPref.setOnPreferenceChangeListener { _, columnCount ->
                setColumnCountPref(columnCount.toString().toInt())
                false
            }

            setSavedPrefSummary(columnCountPref)
        }

        private fun setColumnCountPref(columnCount: Int) {
            val customListPref = findPreference("columnCount") as ListPreference
            val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString(customListPref.key, columnCount.toString())
            editor.apply()

            val summary = columnCount.toString()
            customListPref.summary = summary
            customListPref.setValueIndex(customListPref.findIndexOfValue(columnCount.toString()))
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
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(activity.applicationContext, BackgroundSettingsActivity::class.java)
                    startActivity(intent)
                } else {
                    toast("Storage access needed for background settings. Enable permission in app settings.")
                    requestPermission()
                }
                false
            }
        }

        private fun requestPermission() {
            val storagePermission = ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE)

            if (storagePermission != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(activity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
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
