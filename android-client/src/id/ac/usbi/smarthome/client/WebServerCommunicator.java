package id.ac.usbi.smarthome.client;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Formatter;

import android.util.Log;
import android.widget.Toast;
import id.ac.usbi.smarthome.client.widget.DeviceList;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Created by andhieka on 7/1/14.
 */
public class WebServerCommunicator {
    private InetSocketAddress _serverisa;
    private static final String SENTINEL = "$$$END_OF_MESSAGE$$$";
    private static final int SOCKET_CONNECT_MAX_WAIT = 0; //millisecond
    private static final int MAX_WAIT = 3000; // millisecond
    private static final int SLEEP_CYCLE = 100; // millisecond
    public boolean connection_error = false;
    public int error_count = 0;

    public WebServerCommunicator(String hostname, int port) throws WebServerException {
        _serverisa = new InetSocketAddress(hostname, port);
        if (_serverisa.isUnresolved()) {
            String err_msg = "Problem resolving hostname: " + hostname + ":" + port;
            System.out.println("WebServerCommunicator -- " + err_msg);
            throw new WebServerException(err_msg);
        }
    }


    // SETTER METHODS
    public void setValue(String device, String parameter, String value) throws WebServerException {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        formatter.format("SET: %s; %s; %s;", device, parameter, value);
        sendMessageToServer(sb.toString());
    }

    //GETTER METHODS
    public JSONObject getDeviceInfo(String device_id) throws WebServerException {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        formatter.format("GET: %s;", device_id);
        String response = sendMessageToServer(sb.toString());
        JSONObject deviceInfo = null;
        try {
            deviceInfo = new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new WebServerException("ERROR PARSING JSON.");
        }
        return deviceInfo;
    }


    public ArrayList<String> getAllDeviceIds() throws WebServerException {
        String response = sendMessageToServer("LISTNAME: ");
        ArrayList<String> lst = new ArrayList<String>();
        try {
            JSONArray lstDevices = new JSONArray(response);
            for (int i = 0; i < lstDevices.length(); i++) {
                lst.add(lstDevices.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            throw new WebServerException("ERROR PARSING JSON.");
        }
        return lst;
    }

    public ArrayList<JSONObject> getAllDevices() throws WebServerException {
        String response = sendMessageToServer("LISTDEVICES: ");
        ArrayList<JSONObject> lst = new ArrayList<JSONObject>();
        try {
            JSONArray lstDevices = new JSONArray(response);
            for (int i = 0; i < lstDevices.length(); i++) {
                lst.add(lstDevices.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            throw new WebServerException("ERROR PARSING JSON.");
        }
        return lst;
    }


    // PRIVATE METHOD
    /**
     * An internal function that opens a new Socket connection, sends a message, and reads the reply from server.
     * @param message Message to be sent to the server.
     * @return A String containing the reply given by the server.
     */
    private String sendMessageToServer(String message) throws WebServerException {
        StringBuilder response = new StringBuilder();
        try {
            Socket socket = new Socket(_serverisa.getAddress(), _serverisa.getPort());
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println(message);
            out.println(SENTINEL);
            out.flush();
            int count = 0;
            while (!in.ready() && count < MAX_WAIT / SLEEP_CYCLE) {
                try {
                    Thread.sleep(SLEEP_CYCLE);
                    count++;
                } catch (InterruptedException e) {
                    Log.e("sendMessageToServer", "Error receiving reply from server.");
                    e.printStackTrace();
                }
            }
            while (in.ready()) {
                response.append(in.readLine());
            }
            error_count = 0; // a successful transaction will reset the "consecutive error" counter.
        } catch (IOException e) {
            e.printStackTrace();
            connection_error = true;
            error_count++; // TODO: Notify user once it has reached a number of error
            throw new WebServerException("Error communicating with server.");
        }
        return response.toString();
    }

    public class WebServerException extends Exception {
        public WebServerException(String detailMessage) {
            super(detailMessage);
        }
    }


}
