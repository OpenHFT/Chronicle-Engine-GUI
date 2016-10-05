package net.openhft.chronicle.engine.gui;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Component;

/**
 * @author Rob Austin.
 */
public class MainUiManager {

    UserUiManager userUiManager = new UserUiManager();

    protected Component newComponent(VaadinRequest vaadinRequest) {
        MainUI components = new MainUI();
        components.content.addComponent(userUiManager.newComponent());
        return components;
    }

}
