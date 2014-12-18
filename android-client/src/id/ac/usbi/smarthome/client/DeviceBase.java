package id.ac.usbi.smarthome.client;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import id.ac.usbi.smarthome.client.WebServerCommunicator.WebServerException;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * ApplianceBase is the base class of all appliances.
 * An ApplianceBase must have a controller of type <code>ClientNode</code>.
 * Created by andhieka on 29/5/14.
 */
public abstract class DeviceBase implements IControllable {
    protected ClientNode _controller = null;
    protected String _device_id;
    protected String _device_name = null;
    protected Integer _device_picture = null;
    protected JSONObject _device_info = null;
    protected boolean _isRefreshing = false;

    private static TreeMap<String, DeviceBase> _members = new TreeMap<String, DeviceBase>();

    public enum DeviceType {
        Television, Radio, WirelessLight
    }

    public DeviceBase(ClientNode controller, String device_id) throws InvalidDeviceIdException {
        if (_members.containsKey(device_id)) {
            throw new InvalidDeviceIdException("DeviceBase::DeviceBase -- There is already a device with the same ID.");
        } else {
            _controller = controller;
            _device_id = device_id;
            _device_name = device_id;
            _device_picture = R.drawable.device_icon;
            _members.put(device_id, this);
        }
    }
    public DeviceBase(ClientNode controller, String device_id, String device_name) throws InvalidDeviceIdException {
        this(controller, device_id);
        _device_name = device_name;
    }

    @Override
    public ClientNode getController() {
        return _controller;
    }

    public void refreshDeviceInfo() throws WebServerException {
        int retries = 2;
        while (retries-- > 0) {
            try {
                JSONObject device_info = IntegHome.server().getDeviceInfo(_device_id);
                setDeviceInfo(device_info);
            } catch (WebServerCommunicator.WebServerException e) {
                Log.e("RefreshDeviceInfo", "Failed attempt to refreshDeviceInfo()");
                if (retries > 0) Log.e("RefreshDeviceInfo", "Retrying...");
                else throw e;
            } catch (InvalidDeviceIdException e) {
                Log.wtf("RefreshDeviceInfo", "This should never happen!");
                throw new RuntimeException("Should never be thrown.");
            }
        }
    }
    public void setDeviceInfo(JSONObject deviceInfo) throws InvalidDeviceIdException {
        try {
            if (deviceInfo.getString("device_id").equals(_device_id)) {
                _device_info = deviceInfo;
                _isRefreshing = true; //NOTE: The subclasses must switch _isRefreshing = false before the end of setDeviceInfo.
            } else {
                throw new InvalidDeviceIdException("Device ID in the provided JSONObject does not match target device id!");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public JSONObject getDeviceInfo() throws WebServerException {
        if (_device_info == null) {
            refreshDeviceInfo();
        }
        return _device_info;
    }

    public String getName() {
        return _device_name;
    }
    public String getId() {
        return _device_id;
    }
    public void setPicture(Integer picture) {
        _device_picture = picture;
    }
    public Integer getPicture() {
        return _device_picture;
    }

    /* STATIC METHODS */
    public static List<DeviceBase> getDevices() {
        return new ArrayList<DeviceBase>(_members.values());
    }
    public static DeviceBase getDevice(String id) { return _members.get(id); }

    public class InvalidDeviceIdException extends Exception {
        public InvalidDeviceIdException(String detailMessage) {
            super(detailMessage);
        }
    }
}
