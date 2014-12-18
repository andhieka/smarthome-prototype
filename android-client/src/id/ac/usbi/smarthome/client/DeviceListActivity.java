package id.ac.usbi.smarthome.client;

import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import id.ac.usbi.smarthome.client.widget.DeviceList;

import java.util.List;

/**
 * List of Devices registered on smart home web server.
 * This is currently the parent activity of this android application.
 * Created by andhieka on 30/5/14.
 */
public class DeviceListActivity extends Activity {
    public final static String SELECTED_DEVICE_ID = "id.ac.usbi.smarthome.client.SELECTED_DEVICE_ID";
    private static DeviceListActivity _active_instance;

    private ListView _list;
    private String[] _device_names;
    private Integer[] _device_pictures;
    private List<DeviceBase> _devices;

    public DeviceListActivity() {
        super();
        _active_instance = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PreferenceManager.setDefaultValues(DeviceListActivity.this, R.xml.preferences, false); //When false, the system sets the default values only if this method has never been called in the past
        super.onCreate(savedInstanceState);
        new PopulateDeviceList().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.appliances_main_action, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.appliances_main_action_settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openSettings() {
        Intent intent = new Intent(DeviceListActivity.this, PreferencesActivity.class);
        startActivity(intent);
    }

    private class PopulateDeviceList extends AsyncTask<Void, Void, Void> {
        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(DeviceListActivity.this);
            pd.setTitle(getString(R.string.app_name));
            pd.setMessage(getString(R.string.loading_devices));
            pd.setCancelable(true);
            pd.setCanceledOnTouchOutside(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            pd.dismiss(); //remove progress dialog from view.

            if (IntegHome.server().connection_error) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DeviceListActivity.this);
                builder.setMessage(R.string.connection_error_message).setTitle(R.string.connection_error_title);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // moveTaskToBack(true);
                        // Maybe it's better to leave the app there instead of hiding it
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            setContentView(R.layout.appliances_main);
            DeviceList adapter = new DeviceList(DeviceListActivity.this, _device_names, _device_pictures);
            _list = (ListView) findViewById(R.id.appliance_list);
            _list.setAdapter(adapter);
            _list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                // Inner class async task


                //Click Handler
                @Override
                public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                    final DeviceBase device = _devices.get(position);
                    class RefreshThenShowDevice extends AsyncTask<Void, Void, Void> {
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            Toast.makeText(DeviceListActivity.this, getString(R.string.device_opening), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        protected Void doInBackground(Void... params) {
                            try {
                                device.refreshDeviceInfo();
                            } catch (WebServerCommunicator.WebServerException e) {
                                Log.i("DeviceListActivity", "The information shown about this device may not be updated.");
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            Intent intent = new Intent(view.getContext(), ControllerListActivity.class);
                            String selected_device_id = device.getId();
                            intent.putExtra(SELECTED_DEVICE_ID, selected_device_id);
                            startActivity(intent);
                        }
                    }
                    new RefreshThenShowDevice().execute();
                }
            });
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                IntegHome.setup(DeviceListActivity.this);
                _devices = DeviceBase.getDevices();
                int len = _devices.size();
                _device_names = new String[len];
                _device_pictures = new Integer[len];
                for (int i = 0; i < len; i++) {
                    _device_names[i] = _devices.get(i).getName();
                    _device_pictures[i] = _devices.get(i).getPicture();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            return null;
        }
    }

    public static DeviceListActivity getActiveInstance() {
        return _active_instance;
    }
}
