package id.ac.usbi.smarthome.client;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import id.ac.usbi.smarthome.client.DeviceBase.DeviceType;

import java.util.ArrayList;

/**
 * Created by andhieka on 30/5/14.
 */
public class IntegHome {
    public static MasterNode master_node;
    public static ClientNode[] client_nodes;

    public static WirelessLight[] wireless_lights;
    public static Radio[] radios;
    public static Television[] televisions;
    private static WebServerCommunicator wsc;
    private static boolean isInitialized = false;

    public static void setup(Context context) {
        if (isInitialized) {
            return;
        }
        try {
            // GETTING VALUES FROM SHARED PREFERENCES
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            String hostname = pref.getString(MainPreferencesFragment.KEY_SERVER_HOSTNAME, "");
            Integer port = Integer.parseInt(pref.getString(MainPreferencesFragment.KEY_SERVER_PORT, "0"));

            wsc = new WebServerCommunicator(hostname, port);
            master_node = new MasterNode("integ-master");
            client_nodes = new ClientNode[]{
                    new ClientNode("integ-slave0"),
                    new ClientNode("integ-slave1")
            };
            ArrayList<JSONObject> lst = wsc.getAllDevices();
            for (JSONObject deviceInfo : lst) {
                DeviceType device_type = DeviceType.valueOf(deviceInfo.getString("device_type"));
                String device_name = deviceInfo.getString("device_name");
                String device_id = deviceInfo.getString("device_id");
                if (device_type == DeviceType.Television) {
                    Television tv = new Television(client_nodes[0], device_id, device_name);
                    tv.setDeviceInfo(deviceInfo);
                } else if (device_type == DeviceType.Radio) {
                    Radio radio = new Radio(client_nodes[0], device_id, device_name);
                    radio.setDeviceInfo(deviceInfo);
                } else if (device_type == DeviceType.WirelessLight) {
                    WirelessLight wl = new WirelessLight(client_nodes[0], device_id, device_name);
                    wl.setDeviceInfo(deviceInfo);
                } else {
                    Log.d("IntegHome Setup", "Invalid Device Type!");
                }
            }
            isInitialized = true;
        } catch (JSONException e) {
            Log.e("IntegHome", "There is problem parsing the JSON Object.");
            e.printStackTrace();
        } catch (DeviceBase.InvalidDeviceIdException e) {
            e.printStackTrace();
        } catch (NodeBase.InvalidNodeIdException e) {
            e.printStackTrace(); // not vital
        } catch (WebServerCommunicator.WebServerException e) {
            e.printStackTrace();
        }
        /*
        wireless_lights = new WirelessLight[] {
                new WirelessLight(client_nodes[0], "integ-slave0-light1", "Bedroom Ceiling Light"),
                new WirelessLight(client_nodes[1], "integ-slave1-light1", "Bedside Lights"),
                new WirelessLight(client_nodes[1], "integ-slave1-light2", "Floor Lights")
        };
        radios = new Radio[] {
                new Radio(client_nodes[0], "integ-slave0-radio1", "Bedroom Radio")
        };
        televisions = new Television[] {
                new Television(client_nodes[0], "integ-slave0-tv1", "Bedroom TV")
        };
        */
    }

    public static WebServerCommunicator server() {
        return wsc;
    }

}
