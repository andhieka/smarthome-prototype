package id.ac.usbi.smarthome.client;

/**
 * An interface implemented by <code>DeviceBase</code>.
 * This interface ensures that every class that extends <code>DeviceBase</code>
 * have a <code>getController()</code> method.
 * Created by andhieka on 29/5/14.
 */
public interface IControllable {
    /**
     * @return The <code>ClientNode</code> that controls this Device.
     */
    public ClientNode getController();

}
