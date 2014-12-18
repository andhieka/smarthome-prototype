package id.ac.usbi.smarthome.client;

/**
 * Created by andhieka on 30/5/14.
 */
public interface IFrequencyControllable extends IControllable {
    public enum FreqBand {
        FM,
        AM
    }

    public Float getMinimumFrequency();
    public Float getMaximumFrequency();
    public Float getFrequencyIncrement();

    public void setFrequencyBand(FreqBand fb);
    public FreqBand getFrequencyBand();

    public Float getCurrentFrequency();
    public void setFrequency(Float new_frequency);

}
