package id.ac.usbi.smarthome.client;

/**
 * Created by andhieka on 29/5/14.
 */
public interface IPowerSwitchable extends IControllable {
    public enum PowerState {
        POWER_ON,
        POWER_OFF
    }

    public void setPower(PowerState newState);

    public PowerState getPowerStatus();

}
