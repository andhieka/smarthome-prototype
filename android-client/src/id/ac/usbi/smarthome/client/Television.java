package id.ac.usbi.smarthome.client;

import android.content.Context;
import android.widget.Toast;
import id.ac.usbi.smarthome.client.widget.DeviceList;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by andhieka on 30/5/14.
 */
public class Television extends DeviceBase implements IPowerSwitchable, IChannelControllable, IVolumeControllable {
    //CHANNEL VARIABLES
    private Integer _min_channel = 0;
    private Integer _max_channel = 0;
    private Integer _cur_channel;

    //POWER VARIABLES
    private PowerState _power = null;

    /* VOLUME VARIABLES */
    private int _min_volume;
    private int _max_volume;
    private int _cur_volume;
    private boolean _isMute;

    public Television(ClientNode controller, String device_id, String device_name) throws InvalidDeviceIdException {
        super(controller, device_id, device_name);
        setPicture(R.drawable.tv);
    }

    @Override
    public void setDeviceInfo(JSONObject deviceInfo) throws InvalidDeviceIdException {
        super.setDeviceInfo(deviceInfo);
        try {
            setPower((deviceInfo.getInt("power") == 1) ? PowerState.POWER_ON : PowerState.POWER_OFF);
            _min_volume = deviceInfo.getInt("min_volume");
            _max_volume = deviceInfo.getInt("max_volume");
            setVolume(deviceInfo.getInt("cur_volume"));
            _isMute = deviceInfo.getInt("isMute") == 1;
            _min_channel = deviceInfo.getInt("min_channel");
            _max_channel = deviceInfo.getInt("max_channel");
            setChannel(deviceInfo.getInt("cur_channel"));
            _isRefreshing = false;
        } catch (JSONException e) {
            e.printStackTrace();
            Context context = DeviceListActivity.getActiveInstance();
            Toast.makeText(context, context.getString(R.string.json_error_message), Toast.LENGTH_SHORT).show();
        }
    }

    /* IChannelControllable METHODS */
    @Override
    public Integer getMinimumChannel() {
        return _min_channel;
    }

    @Override
    public Integer getMaximumChannel() {
        return _max_channel;
    }

    @Override
    public Integer getCurrentChannel() {
        return _cur_channel;
    }

    /**
     * Please be sure to set _min_channel and _max_channel first
     * before calling this <code>setChannel(int)</code> function.
     * @param new_channel New channel to be set.
     */
    @Override
    public void setChannel(Integer new_channel) {
        new_channel = Math.min(new_channel, _max_channel);
        new_channel = Math.max(new_channel, _min_channel);
        _cur_channel = new_channel;
        if (!_isRefreshing) {
            final Integer final_channel = new_channel;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        IntegHome.server().setValue(_device_id, "cur_channel", final_channel.toString());
                    } catch (WebServerCommunicator.WebServerException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

    }


    /* IPowerSwitchable METHODS */
    @Override
    public void setPower (final PowerState newState) {
        if (_power == newState) {
            return;
        }
        _power = newState;
        if (!_isRefreshing) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String val = (newState == PowerState.POWER_ON) ? "1" : "0";
                        IntegHome.server().setValue(_device_id, "power", val);
                    } catch (WebServerCommunicator.WebServerException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    @Override
    public PowerState getPowerStatus() {
        return _power;
    }

    /* IVolumeControllable METHODS */
    @Override
    public Integer getMinVolume() {
        return _min_volume;
    }

    @Override
    public Integer getMaxVolume() {
        return _max_volume;
    }

    @Override
    public Integer getCurrentVolume() {
        return _cur_volume;
    }

    @Override
    public void setVolume(final Integer new_volume) {
        if (_cur_volume == new_volume) return;
        _cur_volume = new_volume;
        if (!_isRefreshing) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        IntegHome.server().setValue(_device_id, "cur_volume", new_volume.toString());
                    } catch (WebServerCommunicator.WebServerException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    @Override
    public Boolean isMute() {
        return _isMute;
    }

    @Override
    public void setMute(final boolean value) {
        if (_isRefreshing) {
            _isMute = value;
        } else {
            if (_isMute == value) {
                //do nothing
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            IntegHome.server().setValue(_device_id, "isMute", (value ? "1" : "0"));
                        } catch (WebServerCommunicator.WebServerException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }

}
