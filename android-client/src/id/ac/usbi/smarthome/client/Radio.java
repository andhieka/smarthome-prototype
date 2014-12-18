package id.ac.usbi.smarthome.client;

import android.content.Context;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by andhieka on 30/5/14.
 */
public class Radio extends DeviceBase implements IPowerSwitchable, IVolumeControllable, IFrequencyControllable {
    /* FREQUENCY VARIABLES */
    private static Float _fm_min_freq = 87.50f; //MHz
    private static Float _fm_max_freq = 108.00f; //MHz
    private static Float _fm_freq_inc = 0.05f; //MHz
    private Float _fm_cur_freq = _fm_min_freq;
    private static Float _am_min_freq = 0.540f; //MHz
    private static Float _am_max_freq = 1.600f; //MHz
    private static Float _am_freq_inc = 0.010f; //MHz
    private Float _am_cur_freq = _am_min_freq;
    private Float _cur_freq;
    private static Float _cur_min_freq;
    private static Float _cur_max_freq;
    private Float _cur_freq_inc;
    private FreqBand _freqBand = null;
    private static ArrayList<Float> _freq_vals_am = null;
    private static ArrayList<Float> _freq_vals_fm = null;
    private static String[] _freq_disp_am = null;
    private static String[] _freq_disp_fm = null;

    /* VOLUME VARIABLES */
    private int _min_volume;
    private int _max_volume;
    private int _cur_volume;
    private boolean _isMute;

    /* POWER VARIABLES */
    private PowerState _power = null;


    public Radio(ClientNode controller, String device_id, String device_name) throws InvalidDeviceIdException {
        super(controller, device_id, device_name);
        setPicture(R.drawable.radio);
    }

    @Override
    public void setDeviceInfo(JSONObject deviceInfo) throws InvalidDeviceIdException {
        super.setDeviceInfo(deviceInfo);
        try {
            setFrequencyBand(FreqBand.valueOf(deviceInfo.getString("cur_freq_band")));
            setFrequency((float) deviceInfo.getDouble("cur_freq"));
            setPower((deviceInfo.getInt("power") == 1) ? PowerState.POWER_ON : PowerState.POWER_OFF);
            _min_volume = deviceInfo.getInt("min_volume");
            _max_volume = deviceInfo.getInt("max_volume");
            setVolume(deviceInfo.getInt("cur_volume"));
            _isMute = deviceInfo.getInt("isMute") == 1;
            _isRefreshing = false; //NOTE:
        } catch (JSONException e) {
            e.printStackTrace();
            Context context = DeviceListActivity.getActiveInstance();
            Toast.makeText(context, context.getString(R.string.json_error_message), Toast.LENGTH_SHORT).show();
        }
    }

    /* IFrequencyControllable METHODS */
    @Override
    public Float getMinimumFrequency() {
        return _cur_min_freq;
    }

    @Override
    public Float getMaximumFrequency() {
        return _cur_max_freq;
    }

    @Override
    public Float getFrequencyIncrement() {
        return _cur_freq_inc;
    }

    @Override
    public void setFrequencyBand(final FreqBand fb) {
        if (_freqBand == fb) {
            return; //do nothing
        }
        if (!_isRefreshing) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        IntegHome.server().setValue(_device_id, "cur_freq_band", fb.name());
                    } catch (WebServerCommunicator.WebServerException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        //Save current frequency
        if (_freqBand == FreqBand.FM) {
            _fm_cur_freq = _cur_freq;
        } else if (_freqBand == FreqBand.AM) {
            _am_cur_freq = _cur_freq;
        }
        //Change transmission band
        if (fb == FreqBand.AM) { // changed to AM
            _cur_max_freq = _am_max_freq;
            _cur_min_freq = _am_min_freq;
            _cur_freq_inc = _am_freq_inc;
            _freqBand = FreqBand.AM;
            setFrequency(_am_cur_freq);
        } else { // changed to FM
            _cur_max_freq = _fm_max_freq;
            _cur_min_freq = _fm_min_freq;
            _cur_freq_inc = _fm_freq_inc;
            _freqBand = FreqBand.FM;
            setFrequency(_fm_cur_freq);
        }

    }

    @Override
    public FreqBand getFrequencyBand() {
        return _freqBand;
    }

    @Override
    public Float getCurrentFrequency() {
        return _cur_freq;
    }

    @Override
    public void setFrequency(final Float new_frequency) {
        if (new_frequency >= _cur_min_freq && new_frequency <= _cur_max_freq) {
            if (!_isRefreshing) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            IntegHome.server().setValue(_device_id, "cur_freq", new_frequency.toString());
                        } catch (WebServerCommunicator.WebServerException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
            _cur_freq = new_frequency;
        } else {
            System.out.println("Radio::setFrequency -- Someone tried to set an invalid radio frequency.");
        }
    }

    /* IPowerSwitchable METHODS */
    @Override
    public void setPower(final PowerState newState) {
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


    // STATIC METHODS
    public static String[] getFrequencyDisplayValues(FreqBand fb) {
        if (fb == FreqBand.FM) {
            if (_freq_disp_fm == null) {
                ArrayList<Float> freqVals = getFrequencyValues(fb);
                _freq_disp_fm = new String[freqVals.size()];
                String pattern = "#.00";
                DecimalFormat df = new DecimalFormat(pattern);
                for (int i=0; i < freqVals.size(); i++) {
                    _freq_disp_fm[i] = df.format(freqVals.get(i));
                }
            }
            return _freq_disp_fm;
        } else {
            if (_freq_disp_am == null) {
                ArrayList<Float> freqVals = getFrequencyValues(fb);
                _freq_disp_am = new String[freqVals.size()];
                String pattern = "#.000";
                DecimalFormat df = new DecimalFormat(pattern);
                for (int i=0; i < freqVals.size(); i++) {
                    _freq_disp_am[i] = df.format(freqVals.get(i));
                }
            }
            return _freq_disp_am;
        }
    }

    public static ArrayList<Float> getFrequencyValues(FreqBand fb) {
        if (fb == FreqBand.FM) {
            if (_freq_vals_fm == null) {
                _freq_vals_fm = new ArrayList<Float>();
                float accuracy = 0.01f;
                for (Float val = _fm_min_freq; val <= _fm_max_freq + accuracy; val += _fm_freq_inc) {
                    _freq_vals_fm.add(val);
                }
            }
            return _freq_vals_fm;
        } else { // AM
            if (_freq_vals_am == null) {
                _freq_vals_am = new ArrayList<Float>();
                float accuracy = 0.001f;
                for (Float val = _am_min_freq; val <= _am_max_freq + accuracy; val += _am_freq_inc) {
                    _freq_vals_am.add(val);
                }
            }
            return _freq_vals_am;
        }

    }
}
