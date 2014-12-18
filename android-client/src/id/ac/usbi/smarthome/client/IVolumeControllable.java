package id.ac.usbi.smarthome.client;

/**
 * Created by andhieka on 30/5/14.
 */
public interface IVolumeControllable extends IControllable {

    public Integer getMinVolume();
    public Integer getMaxVolume();
    public Integer getCurrentVolume();
    public void setVolume(Integer new_volume);

    public Boolean isMute();
    public void setMute(boolean value);

}
