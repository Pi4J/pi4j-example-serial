package com.pi4j.example.applications;

import com.pi4j.context.Context;
import com.pi4j.example.Application;
import com.pi4j.example.components.helpers.PIN;
import com.pi4j.example.components.SimpleButton;

/**
 * This example shows how to use the simpleButton component by registering events for different interactions with the button
 */
public class SimpleButton_App implements Application {
    @Override
    public void execute(Context pi4j) {
        logInfo("Simple button  app started ...");

        // Initialize the button component
        final var button = new SimpleButton(pi4j, PIN.D26, Boolean.FALSE);

        // Register event handlers to print a message when pressed (onDown) and depressed (onUp)
        button.onDown      (() -> logInfo("Pressing the button"));
        button.whilePressed(() -> logInfo("Pressing"), 1000);
        button.onUp        (() -> logInfo("Stopped pressing."));

        // Wait for 15 seconds while handling events before exiting
        System.out.println("Press the button to see it in action!");
        delay(15_000);

        // Unregister all event handlers to exit this application in a clean way
        button.deRegisterAll();

        /*
        if you want to deRegister only a single function, you can do so like this:
        button.onUp(null);
        */

        logInfo("Simple button app done.");
    }
}