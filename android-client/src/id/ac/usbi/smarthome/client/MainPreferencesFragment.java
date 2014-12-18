package id.ac.usbi.smarthome.client;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;


/**
 * Show inside PreferencesActivity.
 * Created by andhieka on 7/7/14.
 */
public class MainPreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String KEY_SERVER_HOSTNAME = "pref_server_hostname";
    public static final String KEY_SERVER_PORT = "pref_server_port";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        // Add Validator for preference inputs
        EditTextPreference prefHostname = (EditTextPreference) getPreferenceManager().findPreference(KEY_SERVER_HOSTNAME);
        prefHostname.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean isValid = Patterns.WEB_URL.matcher((String) newValue).matches();
                if (!isValid) {
                    Toast.makeText(getActivity(), getString(R.string.preference_hostname_invalid), Toast.LENGTH_LONG).show();
                }
                return isValid;
            }
        });
        EditTextPreference prefPort = (EditTextPreference) getPreferenceManager().findPreference(KEY_SERVER_PORT);
        prefPort.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int val = Integer.parseInt((String) newValue);
                boolean isValid = (val >= 0) && (val < (1 << 16)); // port number must be less than 2^16
                if (!isValid) {
                    Toast.makeText(getActivity(), getString(R.string.preference_port_invalid), Toast.LENGTH_LONG).show();
                }
                return isValid;
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); ++i) {
            Preference pref = getPreferenceScreen().getPreference(i);
            if (pref instanceof PreferenceGroup) {
                PreferenceGroup pg = (PreferenceGroup) pref;
                for (int j = 0; j < pg.getPreferenceCount(); ++j) {
                    updatePreference(pg.getPreference(j));
                }
            } else {
                updatePreference(pref);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreference(findPreference(key));
        // show dialog asking user to restart the app
        if (key.equals(KEY_SERVER_HOSTNAME) || key.equals(KEY_SERVER_PORT)) {
            new ConfirmAppRestartDialogFragment().show(getFragmentManager(), "fragmentDialog");
        }
    }

    private void updatePreference(Preference pref) {
        if (pref instanceof EditTextPreference) {
            EditTextPreference txtPref = (EditTextPreference) pref;
            txtPref.setSummary(txtPref.getText());
        }
    }

    private void restartApp() {
        Log.e("IntegHome Preferences", "restarting app");
        Context context = getActivity();
        Intent restartIntent = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName());
        restartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, 0, restartIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis()+ 500, pi);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private class ConfirmAppRestartDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getString(R.string.preference_connection_restart))
                   .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           restartApp();
                       }
                   })
                   .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           // do nothing
                       }
                   });
            return builder.create();
        }


    }
}
