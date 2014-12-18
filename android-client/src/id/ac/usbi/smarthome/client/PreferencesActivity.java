package id.ac.usbi.smarthome.client;

import android.app.Activity;
import android.os.Bundle;
/**
 * Created by andhieka on 7/7/14.
 */
public class PreferencesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new MainPreferencesFragment())
                .commit();
    }

}
