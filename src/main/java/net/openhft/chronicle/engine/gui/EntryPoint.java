package net.openhft.chronicle.engine.gui;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.network.VanillaSessionDetails;
import net.openhft.chronicle.network.connection.ClientConnectionMonitor;
import org.jetbrains.annotations.NotNull;

import javax.servlet.annotation.WebServlet;

import static net.openhft.chronicle.wire.WireType.BINARY;


/**
 * This UI is the application entry point. A UI may either represent a browser window
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@SuppressWarnings("WeakerAccess")
@Theme("mytheme")
public class EntryPoint extends UI {

    Navigator navigator;
    protected static final String MAINVIEW = "main";


    public static class LoginView extends VerticalLayout implements View {
        Login c;

        public LoginView(Navigator navigator) {
            c = new Login(navigator, "Welcome to chronicle-engine, Please login :");
            addComponent(c);
        }

        @Override
        public void enter(ViewChangeListener.ViewChangeEvent event) {
            Notification.show("Please Login");
            c.enter(event);
        }
    }


    public class MainView extends VerticalLayout implements View {


        @Override
        public void enter(ViewChangeListener.ViewChangeEvent event) {
            removeAllComponents();
            VanillaAssetTree tree = (VanillaAssetTree) getSession().getAttribute("tree");
            if (tree == null) {
                navigator.navigateTo("");
                return;
            }
            Component c = new MainControl().newComponent(tree);
            addComponent(c);
            setSizeFull();
        }
    }

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        getPage().setTitle("Chronicle Engine");
        // Create a navigator to control the views
        navigator = new Navigator(this, this);

        navigator.addView("", new LoginView(navigator));
        navigator.addView(MAINVIEW, new MainView());


    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = EntryPoint.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {

    }

    static VanillaAssetTree remote(final String connectionDetails) {
        return new VanillaAssetTree().forRemoteAccess(connectionDetails);
    }

    static VanillaAssetTree remote(@NotNull final String connectionDetails,
                                   @NotNull VanillaSessionDetails sessionDetails,
                                   @NotNull ClientConnectionMonitor cm) {
        final VanillaAssetTree tree = new VanillaAssetTree();
        tree.root().forRemoteAccess(new String[]{connectionDetails}, BINARY, sessionDetails, cm);
        return tree;
    }

}
