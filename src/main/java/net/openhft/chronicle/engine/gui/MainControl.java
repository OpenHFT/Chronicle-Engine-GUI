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
    Component newComponent(@NotNull final AssetTree remoteAssetTree) {
        @NotNull MainUI mainUI = new MainUI();
        @NotNull Component userComponent = userUiManager.newComponent();


        VerticalLayout content = mainUI.content;

        mainUI.userButton.addClickListener(clickEvent -> {
            content.removeAllComponents();
            content.addComponent(userUiManager.newComponent());
        });

        @NotNull TreeUI treeUI = new TreeUI();
        new TreeController(remoteAssetTree, treeUI);

        mainUI.treeButton.addClickListener(clickEvent -> {
            content.removeAllComponents();
            content.addComponent(treeUI);
        });


        mainUI.content.addComponent(treeUI);

        return mainUI;
    }

}

