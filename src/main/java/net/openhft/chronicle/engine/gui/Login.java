package net.openhft.chronicle.engine.gui;

import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.event.UIEvents;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Reindeer;
import net.openhft.chronicle.core.annotation.Nullable;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.network.VanillaSessionDetails;
import net.openhft.chronicle.network.connection.ClientConnectionMonitor;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;

import static net.openhft.chronicle.engine.gui.EntryPoint.MAINVIEW;
import static net.openhft.chronicle.engine.gui.EntryPoint.remote;

public class Login extends CustomComponent implements View, Button.ClickListener {

    private static final String NAME = "login";
    private static final String DEFAULT_HOSTPORT = "localhost:8080";
    private static final String DEFAULT_USER = "admin";
    private final TextField user;
    private final TextField hostPort;
    private final PasswordField password;
    private final Button loginButton;
    private final Navigator navigator;

    private volatile UIEvents.PollListener eventListener;

    public Login(Navigator navigator, final String caption) {
        this.navigator = navigator;

        setSizeFull();

        // Create the host input field
        hostPort = new TextField("HostPort:");
        hostPort.setWidth("300px");

        hostPort.setInputPrompt(DEFAULT_HOSTPORT);
        hostPort.setInvalidAllowed(false);

        StreamResource streamResource = new StreamResource((StreamResource.StreamSource) () -> Login.class.getResourceAsStream("Chronicle-Engine_200px.png"), "Chronicle-Engine_200px.png");

        Image image = new Image("", streamResource);

        // Create the user input field
        user = new TextField("User:");
        user.setWidth("300px");
        user.setInputPrompt(DEFAULT_USER);
        user.setInvalidAllowed(false);

        // Create the password input field
        password = new PasswordField("Password:");
        password.setWidth("300px");
        password.addValidator(new PasswordValidator());

        password.setValue("");
        password.setNullRepresentation("");

        // Create login button
        loginButton = new Button("Login", this);

        // Add both to a panel
        VerticalLayout fields = new VerticalLayout(image, hostPort, user, password, loginButton);
        fields.setCaption(caption);
        fields.setSpacing(true);
        fields.setMargin(new MarginInfo(true, true, true, false));
        fields.setSizeUndefined();

        // The view root layout
        VerticalLayout viewLayout = new VerticalLayout(fields);
        viewLayout.setSizeFull();
        viewLayout.setComponentAlignment(fields, Alignment.MIDDLE_CENTER);
        viewLayout.setStyleName(Reindeer.LAYOUT_BLUE);
        setCompositionRoot(viewLayout);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event0) {
        // focus the username field when user arrives to the login view
        isConnected = null;

        UIEvents.PollListener eventListener0 = this.eventListener;
        if (eventListener0 != null)
            getUI().getCurrent().removePollListener(eventListener0);

        this.eventListener = event -> {
            if (isConnected == null)
                return;
            if (isConnected) {
                getSession().setAttribute("tree", remoteTree);
                navigator.navigateTo(MAINVIEW);
                getUI().getCurrent().removePollListener(this.eventListener);
                this.eventListener = null;
            } else {
                getSession().setAttribute("tree", null);
                remoteTree.close();
                remoteTree = null;
                navigator.navigateTo("");
                isConnected = null;
                getUI().getCurrent().removePollListener(this.eventListener);
                this.eventListener = null;
            }
        };

        getUI().getCurrent().setPollInterval(1000);
        getUI().getCurrent().addPollListener(this.eventListener);

    }

    // Validator for validating the passwords
    private static final class PasswordValidator extends
            AbstractValidator<String> {

        private PasswordValidator() {
            super("The password provided is not valid");
        }

        @Override
        protected boolean isValidValue(String value) {
            return true;
        }

        @Override
        public Class<String> getType() {
            return String.class;
        }
    }

    private volatile VanillaAssetTree remoteTree = null;

    private volatile Boolean isConnected = null;

    @Override
    public void buttonClick(Button.ClickEvent event) {

        //
        // Validate the fields using the navigator. By using validors for the
        // fields we reduce the amount of queries we have to use to the database
        // for wrongly entered passwords
        //
       /* if (!user.isValid() || !password.isValid()) {
            return;
        }*/

        String username = user.getValue();
        String password = this.password.getValue();

        //
        // Validate username and password with database here. For examples sake
        // I use a dummy username and password.
        //


        // Store the current user in the service session
        getSession().setAttribute("user", username);
        getSession().setAttribute("password", password);


        // Navigate to main view
        //getUI().getNavigator().navigateTo(NAME);
        final String hostPort = this.hostPort.getValue();

        final VanillaSessionDetails sessionDetails = new VanillaSessionDetails();
        sessionDetails.userId(username == null ? DEFAULT_USER : username);
        sessionDetails.securityToken(password);


        final String remoteHost = "".isEmpty() ? DEFAULT_HOSTPORT : hostPort;


        final ClientConnectionMonitor monitor = new ClientConnectionMonitor() {

            @Override
            public void onConnected(@Nullable String name,
                                    @NotNull SocketAddress socketAddress) {
                isConnected = true;
            }

            @Override
            public void onDisconnected(@Nullable String name,
                                       @NotNull SocketAddress socketAddress) {
                isConnected = false;
            }
        };

        remoteTree = remote(remoteHost, sessionDetails, monitor);

    }


}