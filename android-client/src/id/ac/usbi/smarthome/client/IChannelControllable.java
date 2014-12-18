package id.ac.usbi.smarthome.client;

/**
 * Created by andhieka on 30/5/14.
 */
public interface IChannelControllable extends IControllable {

    public Integer getMinimumChannel();
    public Integer getMaximumChannel();
    public Integer getCurrentChannel();
    public void setChannel(Integer new_channel);

}
