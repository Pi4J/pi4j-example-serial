package com.pi4j.catalog.applications;

import com.pi4j.context.Context;

import com.pi4j.catalog.Application;
import com.pi4j.catalog.components.Ads1115;
import com.pi4j.catalog.components.Potentiometer;

/**
 * This example shows how to use the potentiometer component displaying the values of the hooked potentiometer
 * <P>
 * see <a href="https://pi4j.com/examples/components/potentiometer/">Description on Pi4J website</a>
 */
public class Potentiometer_App implements Application {
    @Override
    public void execute(Context pi4j) {
        System.out.println("Potentiometer test started ...");

        Ads1115 ads1115 = new Ads1115(pi4j);

        Potentiometer poti = new Potentiometer(ads1115, Ads1115.Channel.A0);

        //read current value from poti one time
        System.out.println(String.format("P0 raw value is %.2f V", poti.readCurrentVoltage()));

        //read current value from the poti in percent one time
        System.out.println(String.format("P0 normalized value is %.2f %%", poti.readNormalizedValue()));

        // Register event handlers to print a message when potentiometer is moved
        poti.onNormalizedValueChange((value) -> System.out.println(String.format("P0 slider is at %.2f %%", value)));

        //you can attach a second potentiometer to another channel, if you like
//        Potentiometer potiWithCenterPosition = new Potentiometer(ads1115, Ads1115.Channel.A1, Potentiometer.Range.MINUS_ONE_TO_ONE);
//        potiWithCenterPosition.onNormalizedValueChange((value) -> System.out.println(String.format("P1 slider is at %.2f %%", value)));

        //you have to start the continuous reading on ADC (because you can use up to 4 channels and all of them need to be fully configured before starting to read the values)
        ads1115.startContinuousReading(0.1, 10);

        System.out.println("Move the potentiometer to see it in action!");
        // Wait while handling events before exiting
        delay(15_000);

        ads1115.stopContinuousReading();

        System.out.println("No new values should be reported");
        delay(5_000);

        ads1115.reset();
        System.out.println("Test done\n\n\n");
    }
}
