package org.mgwa.w40k.pairing.gui;

/**
 * Dedicated class as the main program entrypoint.
 * 
 * Here is why we do not use {@link AppWindow}: https://stackoverflow.com/questions/52653836/maven-shade-javafx-runtime-components-are-missing
 */
public final class Main {

    private Main() {}

    public static void main(String[] args) {
        AppWindow.main(new String[0]);
    }

}
