package net.openhft.chronicle.engine.gui;

import com.vaadin.event.FieldEvents;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * @author Rob Austin.
 */
public class TimeStampSearch {


    public static final int COMBO_BOX_WIDTH = 80;
    private final TextField filterField;
    private final FieldEvents.TextChangeListener textChangeListener;

    private boolean hasFocus;

    public TimeStampSearch(@NotNull TextField filterField, FieldEvents.TextChangeListener textChangeListener) {
        this.filterField = filterField;
        this.textChangeListener = textChangeListener;
    }

    public boolean hasFocus() {
        return hasFocus;
    }

    private class MyTextChangeEvent extends FieldEvents.TextChangeEvent {

        private final String newValue;

        public MyTextChangeEvent(String newValue) {
            super(filterField);
            this.newValue = newValue;
        }

        @Override
        public String getText() {
            return newValue;
        }

        @Override
        public int getCursorPosition() {
            throw new UnsupportedOperationException("todo");
        }
    }

    public void doSeach() {

        // Create a sub-window and set the content
        @NotNull Window subWindow = new Window("Search - Date Time Range");
        subWindow.setClosable(false);
        subWindow.setModal(true);
        subWindow.setResizeLazy(true);
        subWindow.setResizable(false);
        subWindow.setSizeUndefined();
        subWindow.setWidth(920, Sizeable.Unit.PIXELS);
        subWindow.setDraggable(true);
        hasFocus = true;

        @NotNull final Button doneButton = new Button("Done");
        doneButton.addClickListener((Button.ClickListener) event1 -> {
            subWindow.close();
            String newValue = ">" + (System.currentTimeMillis() - (900L * 1000L));
            filterField.setValue(newValue);
            textChangeListener.textChange(new MyTextChangeEvent(newValue));
        });

        @NotNull final Button clearButton = new Button("Clear");
        clearButton.addClickListener((Button.ClickListener) event1 -> {
            subWindow.close();
            String newValue = "";
            filterField.setValue(newValue);
            textChangeListener.textChange(new MyTextChangeEvent(""));
        });


        @NotNull FormLayout form = new FormLayout();
        form.setMargin(false);

        Layout dateLayout = dateLayout();
        form.addComponent(new Label("From (inclusive):"));
        form.addComponent(dateLayout);
        form.setComponentAlignment(dateLayout, Alignment.MIDDLE_LEFT);


        Layout timeLayout = dateLayout();
        form.addComponent(new Label("To (exclusive):"));
        form.addComponent(timeLayout);
        form.setComponentAlignment(timeLayout, Alignment.MIDDLE_LEFT);

        HorizontalLayout buttonParentLayout = new HorizontalLayout();
        buttonParentLayout.setWidth(100, Sizeable.Unit.PERCENTAGE);

        buttonParentLayout.setMargin(true);
        HorizontalLayout padding = new HorizontalLayout();
        padding.setWidth(100, Sizeable.Unit.PERCENTAGE);
        buttonParentLayout.addComponent(padding);
        buttonParentLayout.setComponentAlignment(padding, Alignment.MIDDLE_LEFT);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.addComponent(clearButton);
        buttons.addComponent(doneButton);
        buttonParentLayout.addComponent(buttons);
        buttonParentLayout.setComponentAlignment(buttons, Alignment.MIDDLE_RIGHT);

        form.addComponent(buttonParentLayout);
        form.setComponentAlignment(buttonParentLayout, Alignment.MIDDLE_RIGHT);

        subWindow.setContent(form);


        // Center it in the browser window
        subWindow.center();

        // Open it in the UI
        UI.getCurrent().addWindow(subWindow);
    }


    public Layout dateLayout() {


        AbstractOrderedLayout result = new HorizontalLayout();

        AbstractOrderedLayout dayLayout = new HorizontalLayout();
        dayLayout.setMargin(true);


        ComboBox day = dayComboBox();
        ComboBox month = monthComboBox();
        ComboBox year = yearCombBox();
        dayLayout.setSpacing(true);
        dayLayout.addComponent(day);
        dayLayout.addComponent(month);
        dayLayout.addComponent(year);
        result.addComponent(dayLayout);
        result.setComponentAlignment(dayLayout, Alignment.MIDDLE_LEFT);

        AbstractOrderedLayout timelayout = new HorizontalLayout();
        ComboBox hour = hourCombBox();
        ComboBox min = minCombBox();
        ComboBox seconds = secCombBox();
        ComboBox milliseconds = millisecondsCombBox();
        timelayout.setSpacing(true);
        timelayout.addComponent(hour);
        timelayout.addComponent(min);
        timelayout.addComponent(seconds);
        timelayout.addComponent(milliseconds);
        result.addComponent(timelayout);
        result.setComponentAlignment(timelayout, Alignment.MIDDLE_RIGHT);
        return result;
    }

    @NotNull
    private ComboBox monthComboBox() {
        String[] months = new DateFormatSymbols().getMonths();
        ComboBox month = new ComboBox("month", asList(months));


        month.setInvalidAllowed(false);
        month.setNullSelectionAllowed(false);
        month.addItems(months);
        month.setWidth(150, Sizeable.Unit.PIXELS);
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        month.setValue(months[instance.get(Calendar.MONTH)]);
        return month;
    }

    @NotNull
    private ComboBox dayComboBox() {

        ComboBox result = new ComboBox("day");

        result.setInvalidAllowed(false);
        result.setNullSelectionAllowed(false);
        result.setWidth(COMBO_BOX_WIDTH, Sizeable.Unit.PIXELS);
        List days = new ArrayList<>();
        for (int i = 1; i <= 31; i++) {
            days.add(Integer.toString(i));
        }

        result.addItems(days);

        final Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        result.setValue(Integer.toString(instance.get(Calendar.DAY_OF_MONTH)));
        return result;
    }


    @NotNull
    private ComboBox yearCombBox() {

        ComboBox result = new ComboBox("year");

        result.setInvalidAllowed(false);
        result.setNullSelectionAllowed(false);
        result.setWidth(100, Sizeable.Unit.PIXELS);
        List years = new ArrayList<>();
        for (int i = 1970; i < 2070; i++) {
            years.add(Integer.toString(i));
        }

        result.addItems(years);

        final Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        result.setValue(Integer.toString(instance.get(Calendar.YEAR)));
        return result;
    }


    @NotNull
    private ComboBox hourCombBox() {

        ComboBox result = new ComboBox("hour");

        result.setInvalidAllowed(false);
        result.setNullSelectionAllowed(false);
        result.setWidth(COMBO_BOX_WIDTH, Sizeable.Unit.PIXELS);
        List hour = new ArrayList<>();
        for (int i = 0; i <= 23; i++) {
            hour.add(Integer.toString(i));
        }

        result.addItems(hour);

        final Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        result.setValue(Integer.toString(instance.get(Calendar.HOUR_OF_DAY)));
        return result;
    }

    @NotNull
    private ComboBox minCombBox() {

        ComboBox result = new ComboBox("min");

        result.setInvalidAllowed(false);
        result.setNullSelectionAllowed(false);
        result.setWidth(COMBO_BOX_WIDTH, Sizeable.Unit.PIXELS);
        List min = new ArrayList<>();
        for (int i = 0; i <= 59; i++) {
            min.add(Integer.toString(i));
        }

        result.addItems(min);

        final Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        result.setValue(Integer.toString(instance.get(Calendar.MINUTE)));
        return result;
    }

    @NotNull
    private ComboBox secCombBox() {

        ComboBox result = new ComboBox("second");

        result.setInvalidAllowed(false);
        result.setNullSelectionAllowed(false);
        result.setWidth(COMBO_BOX_WIDTH, Sizeable.Unit.PIXELS);
        List min = new ArrayList<>();
        for (int i = 0; i <= 59; i++) {
            min.add(Integer.toString(i));
        }

        result.addItems(min);

        final Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        result.setValue(Integer.toString(instance.get(Calendar.SECOND)));
        return result;
    }

    @NotNull
    private ComboBox millisecondsCombBox() {

        ComboBox result = new ComboBox("milli-second");

        result.setInvalidAllowed(false);
        result.setNullSelectionAllowed(false);
        result.setWidth(120, Sizeable.Unit.PIXELS);
        List min = new ArrayList<>();
        for (int i = 0; i <= 1000; i++) {
            min.add(Integer.toString(i));
        }

        result.addItems(min);

        final Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        result.setValue("0");
        return result;
    }

    public void hasFocus(boolean hasFocus) {
        this.hasFocus = hasFocus;
    }
}
