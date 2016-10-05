package net.openhft.chronicle.engine.gui;

import com.vaadin.ui.Component;

/**
 * @author Rob Austin.
 */
public class TreeUiManager {

    protected Component newComponent() {
        return new TreeUI();
    }

}
