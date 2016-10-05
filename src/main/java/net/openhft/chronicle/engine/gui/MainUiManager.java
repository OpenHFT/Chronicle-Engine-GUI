package net.openhft.chronicle.engine.gui;

import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Rob Austin.
 */
public class MainUiManager {

    private final UserUiManager userUiManager = new UserUiManager();
    private final TreeUiManager treeUiManager = new TreeUiManager();

    protected Component newComponent() {
        MainUI mainUI = new MainUI();
        Component c = userUiManager.newComponent();
        mainUI.content.addComponent(c);

        VerticalLayout content = mainUI.content;

        mainUI.userButton.addClickListener(clickEvent -> {
            content.removeAllComponents();
            content.addComponent(userUiManager.newComponent());
        });

        mainUI.treeButton.addClickListener(clickEvent -> {
            content.removeAllComponents();
            content.addComponent(treeUiManager.newComponent());
        });


        return mainUI;
    }

}

