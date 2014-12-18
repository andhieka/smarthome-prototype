package id.ac.usbi.smarthome.client;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

import id.ac.usbi.smarthome.client.IFrequencyControllable.FreqBand;

/**
 * This is instantiated when user taps on a device at DeviceListActivity.
 * Created by andhieka on 31/5/14.
 */
public class ControllerListActivity extends Activity {
    private DeviceBase _device = null;
    private boolean isChannel = true;
    private boolean isFrequency = true;
    private boolean isPower = true;
    private boolean isVolume = true;

    public Handler handler;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.appliance_control_action, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.appliance_control_action_refresh:
                refreshAll();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // GET DATA FROM INTENT
        Intent intent = getIntent();
        String device_id = intent.getStringExtra(DeviceListActivity.SELECTED_DEVICE_ID);
        _device = DeviceBase.getDevice(device_id);
        // SET CONTENT VIEW
        setContentView(R.layout.appliance_control);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) { // Make sure we're running on Honeycomb or higher to use ActionBar APIs
            getActionBar().setDisplayHomeAsUpEnabled(true); // Show the Up button in the action bar.
        }
        filterControls();
        setupControls();
    }

    private void filterControls() {
        if (!(_device instanceof IPowerSwitchable)) {
            findViewById(R.id.power_switch_table).setVisibility(View.GONE);
            isPower = false;
        }
        if (!(_device instanceof IVolumeControllable)) {
            findViewById(R.id.volume_control_table).setVisibility(View.GONE);
            isVolume = false;
        }
        if (!(_device instanceof IChannelControllable)) {
            findViewById(R.id.channel_control_table).setVisibility(View.GONE);
            isChannel = false;
        }
        if (!(_device instanceof IFrequencyControllable)) {
            findViewById(R.id.frequency_control_table).setVisibility(View.GONE);
            isFrequency = false;
        }
    }

    private void setupControls() {
        if (isPower) {
            setupPowerControls();
        }
        if (isChannel) {
            setupChannelControls();
        }
        if (isFrequency) {
            setupFrequencyControls();
        }
        if (isVolume) {
            setupVolumeControls();
        }
    }

    public void refreshAll() {
        new RefreshDeviceAsyncTask().execute();
    }

    private void setupPowerControls() {
        final IPowerSwitchable device = (IPowerSwitchable) _device;
        Switch pwrSwitch = (Switch) findViewById(R.id.power_switch_btn);
        pwrSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        device.setPower(isChecked ? IPowerSwitchable.PowerState.POWER_ON
                                : IPowerSwitchable.PowerState.POWER_OFF);
                    }
                }).start();
            }
        });
        refreshPowerControls();
    }
    private void refreshPowerControls() {
        final IPowerSwitchable device = (IPowerSwitchable) _device;
        Switch pwrSwitch = (Switch) findViewById(R.id.power_switch_btn);
        pwrSwitch.setChecked(device.getPowerStatus() == IPowerSwitchable.PowerState.POWER_ON);
    }

    private void setupChannelControls() {
        final IChannelControllable device = (IChannelControllable) _device;
        NumberPicker npChannel = (NumberPicker) findViewById(R.id.channel_control_number);
        int max = device.getMaximumChannel();
        int min = device.getMinimumChannel();
        int length = max - min + 1;
        final ArrayList<Integer> channel_values = new ArrayList<Integer>(length);
        final String[] channel_display = new String[length];
        for (Integer i = min; i <= max; i++) {
            channel_values.add(i);
            channel_display[i] = i.toString();
        }
        npChannel.setMinValue(0);
        npChannel.setMaxValue(length - 1);
        npChannel.setDisplayedValues(channel_display);
        npChannel.setWrapSelectorWheel(true);
        npChannel.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                final Integer new_channel = channel_values.get(newVal);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        device.setChannel(new_channel);
                    }
                }).start();
            }
        });
        refreshChannelControls();
    }

    private void refreshChannelControls() {
        final IChannelControllable device = (IChannelControllable) _device;
        NumberPicker npChannel = (NumberPicker) findViewById(R.id.channel_control_number);
        npChannel.setValue(device.getCurrentChannel());
    }

    private void setupFrequencyControls() {
        final IFrequencyControllable device = (IFrequencyControllable) _device;
        // SETTING FREQUENCY BAND SWITCH
        Switch sw = (Switch) findViewById(R.id.frequency_band_switch);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final FreqBand newBand = (isChecked) ? FreqBand.AM : FreqBand.FM;
                device.setFrequencyBand(newBand);
                refreshFrequencyControls();
            }
        });
        // SETTING FREQUENCY NUMBER PICKER
        NumberPicker np_fm = (NumberPicker) findViewById(R.id.frequency_control_number_fm);
        final ArrayList<Float> values_fm = Radio.getFrequencyValues(FreqBand.FM);
        final String[] display_fm = Radio.getFrequencyDisplayValues(FreqBand.FM);
        np_fm.setDisplayedValues(display_fm);
        np_fm.setMinValue(0);
        np_fm.setMaxValue(values_fm.size() - 1);
        np_fm.setWrapSelectorWheel(true);
        np_fm.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, final int newVal) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        device.setFrequency(values_fm.get(newVal));
                    }
                }).start();
            }
        });
        NumberPicker np_am = (NumberPicker) findViewById(R.id.frequency_control_number_am);
        final ArrayList<Float> values_am = Radio.getFrequencyValues(FreqBand.AM);
        final String[] display_am = Radio.getFrequencyDisplayValues(FreqBand.AM);
        np_am.setDisplayedValues(display_am);
        np_am.setMinValue(0);
        np_am.setMaxValue(values_am.size() - 1);
        np_am.setWrapSelectorWheel(true);
        np_am.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, final int newVal) {
                device.setFrequency(values_am.get(newVal));
            }
        });
        // Calls Refresh
        refreshFrequencyControls();
    }

    private void refreshFrequencyControls() {
        final IFrequencyControllable device = (IFrequencyControllable) _device;
        //refreshing frequency band switch
        final Switch sw = (Switch) findViewById(R.id.frequency_band_switch);
        if (device.getFrequencyBand() == FreqBand.FM) {
            sw.setChecked(false);
        } else {
            sw.setChecked(true);
        }
        //refreshing frequency number picker
        NumberPicker np_am = (NumberPicker) findViewById(R.id.frequency_control_number_am);
        NumberPicker np_fm = (NumberPicker) findViewById(R.id.frequency_control_number_fm);
        if (device.getFrequencyBand() == FreqBand.FM) {
            np_am.setVisibility(View.GONE);
            np_fm.setVisibility(View.VISIBLE);
            np_fm.setValue(Collections.binarySearch(Radio.getFrequencyValues(FreqBand.FM), device.getCurrentFrequency()));
        } else {
            np_fm.setVisibility(View.GONE);
            np_am.setVisibility(View.VISIBLE);
            np_am.setValue(Collections.binarySearch(Radio.getFrequencyValues(FreqBand.AM), device.getCurrentFrequency()));
        }
    }

    private void setupVolumeControls() {
        final IVolumeControllable device = (IVolumeControllable) _device;
        // CONFIGURE MUTE SWITCH
        Switch muteSwitch = (Switch) findViewById(R.id.volume_control_mute_switch);
        muteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        device.setMute(isChecked);
                    }
                }).start();
            }
        });
        // CONFIGURE SEEK BAR
        SeekBar seekBar = (SeekBar) findViewById(R.id.volume_control_seek_bar);
        seekBar.setMax(device.getMaxVolume());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, boolean fromUser) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        device.setVolume(progress);
                    }
                }).start();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // do nothing :p
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // do nothing :p
            }
        });
        refreshVolumeControls();
    }
    private void refreshVolumeControls() {
        final IVolumeControllable device = (IVolumeControllable) _device;
        Switch muteSwitch = (Switch) findViewById(R.id.volume_control_mute_switch);
        muteSwitch.setChecked(device.isMute());
        SeekBar seekBar = (SeekBar) findViewById(R.id.volume_control_seek_bar);
        seekBar.setProgress(device.getCurrentVolume());
    }

    private class RefreshDeviceAsyncTask extends AsyncTask<Void, Void, Void> {
        boolean isSuccess = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(ControllerListActivity.this, getString(R.string.refreshing_device_info), Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                _device.refreshDeviceInfo();
                isSuccess = true;
            } catch (WebServerCommunicator.WebServerException e) {
                e.printStackTrace();
                isSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (isFrequency) {
                refreshFrequencyControls();
            }
            if (isVolume) {
                refreshVolumeControls();
            }
            if (isPower) {
                refreshPowerControls();
            }
            if (isChannel) {
                refreshChannelControls();
            }
            String toastNotif = (isSuccess) ? getString(R.string.device_refresh_success) : getString(R.string.device_refresh_failure);
            Toast.makeText(ControllerListActivity.this, toastNotif, Toast.LENGTH_SHORT).show();
        }
    }
}
