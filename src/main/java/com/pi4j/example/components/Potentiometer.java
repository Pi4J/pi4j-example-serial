package com.pi4j.example.components;

import com.pi4j.config.exception.ConfigException;

public class Potentiometer extends Component {
    /**
     * ads1115 instance
     */
    private final ADS1115 ads1115;

    /**

     * AD channel connected to potentiometer (must be between 0 to 3)
     */
    private final int channel;
  
    /**
     * min value which potentiometer has reached
     */
    private double minValue;

    /**
     * max value which potentiometer has reached
     */
    private double maxValue;

    /**
     * fast continuous reading is active
     */
    private boolean fastContinuousReadingActive = false;

    /**
     * slow continuous reading is active
     */
    private boolean slowContinuousReadingActive = false;

    /**
     * Create a new potentiometer component with custom chanel and custom maxVoltage
     *
     * @param ads1115    ads instance
     * @param chanel     custom ad chanel
     * @param maxVoltage custom maxVoltage
     */
    public Potentiometer(ADS1115 ads1115, int chanel, double maxVoltage) {
        this.ads1115 = ads1115;
        this.minValue = ads1115.getPga().gain() * 0.1;
        this.maxValue = maxVoltage;
        this.channel = chanel;

        //check if chanel is in range of ad converter
        if (chanel < 0 || chanel > 3) {
            throw new ConfigException("Channel number for ad converter not possible, choose channel between 0 to 3");
        }
    }

    /**
     * Create a new potentiometer component with default chanel and maxVoltage for Raspberry pi

     *
     * @param ads1115 ads instance
     */
    public Potentiometer(ADS1115 ads1115) {
        this.ads1115 = ads1115;
        this.minValue = ads1115.getPga().gain() * 0.1;
        this.maxValue = 3.3;
        this.channel = 0;
    }

    /**
     * Returns actual voltage from potentiometer
     *
     * @return voltage from potentiometer
     */
    public double singleShotGetVoltage() {
        double result = 0.0;
        switch (channel) {
            case 0:
                result = ads1115.singleShotAIn0();
                break;
            case 1:
                result = ads1115.singleShotAIn1();
                break;
            case 2:
                result = ads1115.singleShotAIn2();
                break;
            case 3:
                result = ads1115.singleShotAIn3();
                break;
        }
        updateMinMaxValue(result);
        return result;
    }

    /**
     * Returns normalized value from 0 to 1
     *
     * @return normalized value
     */
    public double singleShotGetNormalizedValue() {
        return singleShotGetVoltage() / maxValue;
    }

    /**
     * Returns actual voltage value from continuous reading
     *
     * @return actual voltage value
     */
    public double continuousReadingGetVoltage() {
        double result = 0.0;
        if (fastContinuousReadingActive) {
            result = ads1115.getFastContinuousReadAI();
        } else {
            switch (channel) {
                case 0:
                    result = ads1115.getSlowContinuousReadAIn0();
                    break;
                case 1:
                    result = ads1115.getSlowContinuousReadAIn1();
                    break;
                case 2:
                    result = ads1115.getSlowContinuousReadAIn2();
                    break;
                case 3:
                    result = ads1115.getSlowContinuousReadAIn3();
                    break;
            }
        }
        updateMinMaxValue(result);
        return result;
    }

    /**
     * Returns actual normalized value form 0 to 1 from continuous reading
     *
     * @return normalized value
     */
    public double continuousReadingGetNormalizedValue() {
        return continuousReadingGetVoltage() / maxValue;
    }

    /**
     * Sets or disables the handler for the onValueChange event.
     * This event gets triggered whenever the value changes.
     * Only a single event handler can be registered at once.
     *
     * @param method Event handler to call or null to disable
     */
    public void setRunnableFastRead(Runnable method) {
        ads1115.setRunnableFastRead(method);
    }

    /**
     * Sets or disables the handler for the onValueChange event.
     * This event gets triggered whenever the value changes.
     * Only a single event handler can be registered at once.
     *
     * @param method Event handler to call or null to disable
     */
    public void setRunnableSlowReadChan(Runnable method) {
        switch (channel) {
            case 0:
                ads1115.setRunnableSlowReadChannel0(method);
                break;
            case 1:
                ads1115.setRunnableSlowReadChannel1(method);
                break;
            case 2:
                ads1115.setRunnableSlowReadChannel2(method);
                break;
            case 3:
                ads1115.setRunnableSlowReadChannel3(method);
                break;
        }
    }

    /**
     * start slow continuous reading. In this mode, up to 4 devices can be connected to the analog to digital
     * converter. For each device a single read command is sent to the ad converter and waits for the response.
     * The maximum sampling frequency of the analog signals depends on how many devices are connected to the AD
     * converter at the same time.
     * The maximum allowed sampling frequency of the signal is 1/2 the sampling rate of the ad converter.
     * The reciprocal of this sampling rate finally results in the minimum response time to a signal request.
     * (the delay of the bus is not included).
     * <p>
     * This leads to the following table for the maximum allowed readFrequency by a sampling rate of 128 sps:
     * 1 chanel in use -> readFrequency max 64Hz (min. response time = 16ms)
     * 2 chanel in use -> readFrequency max 32Hz (min. response time = 32ms)
     * 3 chanel in use -> readFrequency max 21Hz (min. response time = 48ms)
     * 4 chanel in use -> readFrequency max 16Hz (min. response time = 63ms)
     *
     * @param threshold     threshold for trigger new value change event (+- voltage)
     * @param readFrequency read frequency to get new value from device, must be lower than 1/2
     *                      sampling rate of device
     */
    public void startSlowContinuousReading(double threshold, int readFrequency) {
        if (fastContinuousReadingActive) {
            logDebug("fast continuous reading currently active");
        } else {
            //set slow continuous reading active to lock fast continuous reading
            slowContinuousReadingActive = true;
            ads1115.startSlowContinuousReading(threshold, readFrequency);
        }
    }

    /**
     * stops slow continuous reading
     */
    public void stopSlowContinuousReading() {
        logInfo("Stop continuous reading");
        slowContinuousReadingActive = false;
        ads1115.stopSlowReadContinuousReading();
    }

    /**
     * Starts fast continuous reading. In this mode only on device can be connected to the ad converter.
     * The maximum allowed readFrequency ist equal to the sample rate of the ad converter
     *
     * @param threshold     threshold for trigger new value change event (+- voltage)
     * @param readFrequency read frequency to get new value from device, must be lower than the
     *                      sampling rate of the device
     */
    public void startFastContinuousReading(double threshold, int readFrequency) {
        if (slowContinuousReadingActive) {
            logDebug("slow continuous reading currently active");
        } else {
            //set fast continuous reading active to lock slow continuous reading
            fastContinuousReadingActive = true;

            //start continuous reading on ads1115
            ads1115.startFastContinuousReading(channel, threshold, readFrequency);
        }
    }

    /**
     * stops fast continuous reading
     */
    public void stopFastContinuousReading() {
        logInfo("Stop fast continuous reading");
        fastContinuousReadingActive = false;
        //stop continuous reading
        ads1115.stopFastContinuousReading();
    }

    /**
     * disables all handlers
     */
    public void deregisterAll() {
        switch (channel) {
            case 0:
                ads1115.setRunnableSlowReadChannel0(null);
                break;
            case 1:
                ads1115.setRunnableSlowReadChannel1(null);
                break;
            case 2:
                ads1115.setRunnableSlowReadChannel2(null);
                break;
            case 3:
                ads1115.setRunnableSlowReadChannel3(null);
                break;
        }
    }

    /**
     * Check if new value is bigger than current max value or lower than min value
     * In this case update min or max value
     *
     * @param result value to check against min Max value
     */
    private void updateMinMaxValue(double result) {
        if (result < minValue) {
            minValue = result;
        } else if (result > maxValue) {
            maxValue = result;
        }
    }
}
