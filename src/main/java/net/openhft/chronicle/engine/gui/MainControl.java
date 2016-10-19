package net.openhft.chronicle.engine.gui;

import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import net.openhft.chronicle.engine.api.tree.AssetTree;
import org.jetbrains.annotations.NotNull;

/**
 * @author Rob Austin.
 */
class MainControl {

    private final UserControl userUiManager = new UserControl();


    @NotNull
    Component newComponent(final AssetTree assetTree) {
        @NotNull MainUI mainUI = new MainUI();
        Component c = userUiManager.newComponent();
        mainUI.content.addComponent(c);

        VerticalLayout content = mainUI.content;

        mainUI.userButton.addClickListener(clickEvent -> {
            content.removeAllComponents();
            content.addComponent(userUiManager.newComponent());
        });


        @NotNull TreeUI treeUI = new TreeUI();
        new TreeController(assetTree, treeUI);

        mainUI.treeButton.addClickListener(clickEvent -> {
            content.removeAllComponents();
            content.addComponent(treeUI);
        });


        return mainUI;
    }

}

