package id.ac.usbi.smarthome.client;

/**
 * Created by andhieka on 29/5/14.
 */
public class WirelessLight extends DeviceBase implements IPowerSwitchable {
    public WirelessLight(ClientNode controller, String device_id, String device_name) throws InvalidDeviceIdException {
        super(controller, device_id, device_name);
        this.setPicture(R.drawable.light);
    }

    /* IPowerSwitchable Methods */
    @Override
    public void setPower(PowerState newState) {

    }

    @Override
    public PowerState getPowerStatus() {
        return null;
    }

}
