package net.openhft.chronicle.engine.gui;

import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormatSymbols;
import java.time.DateTimeException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.Calendar;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Arrays.asList;

/**
 * @author Rob Austin.
 */
public class TimeStampSearch {

    private final static String[] MONTHS_ARRAY = Arrays.copyOf(new DateFormatSymbols().getMonths
            (), 12);
    public static final List<String> MONTHS = asList(MONTHS_ARRAY);

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

    public void doSearch() {

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


        @NotNull FormLayout form = new FormLayout();
        form.setMargin(false);

        AbstractOrderedLayout fromLayout = new HorizontalLayout();

        Label fromMillisLabel = new Label("fromMillisLabel");
        Supplier<Long> supplyFromMillisUtc = addComboBoxes(fromLayout, s -> fromMillisLabel
                .setValue(s.toString() + " milliseconds UTC"));

        {
            HorizontalLayout components0 = new HorizontalLayout();
            components0.addComponent(new Label("From (inclusive):"));
            components0.addComponent(fromMillisLabel);
            form.addComponent(components0);
        }

        form.addComponent(fromLayout);
        form.setComponentAlignment(fromLayout, Alignment.MIDDLE_LEFT);

        AbstractOrderedLayout toLayout = new HorizontalLayout();
        Label toMillisLabel = new Label("toMillisLabel");
        Supplier<Long> supplyToMillisUtc = addComboBoxes(toLayout, s -> toMillisLabel.setValue
                (s.toString() + " milliseconds UTC"));
        {
            HorizontalLayout components0 = new HorizontalLayout();
            components0.addComponent(new Label("To (exclusive):"));
            components0.addComponent(toMillisLabel);
            form.addComponent(components0);
        }

        form.addComponent(toLayout);
        form.setComponentAlignment(toLayout, Alignment.MIDDLE_LEFT);

        // buttons layout ---------------

        HorizontalLayout buttonParentLayout = new HorizontalLayout();
        buttonParentLayout.setWidth(100, Sizeable.Unit.PERCENTAGE);

        buttonParentLayout.setMargin(true);
        HorizontalLayout padding = new HorizontalLayout();
        padding.setWidth(100, Sizeable.Unit.PERCENTAGE);
        buttonParentLayout.addComponent(padding);
        buttonParentLayout.setComponentAlignment(padding, Alignment.MIDDLE_LEFT);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);

        //   ---------------


        @NotNull final Button doneButton = new Button("Done");
        doneButton.addClickListener((Button.ClickListener) event1 -> {
            subWindow.close();

            String newValue = ">" + (System.currentTimeMillis() - (900L * 1000L));

            long fromMillis = supplyFromMillisUtc.get();
            long toMillis = supplyToMillisUtc.get();

            filterField.setValue(">" + fromMillis + ",<" + toMillis);
            textChangeListener.textChange(new MyTextChangeEvent(newValue));
        });

        @NotNull final Button clearButton = new Button("Clear");
        clearButton.addClickListener((Button.ClickListener) event1 -> {
            subWindow.close();
            String newValue = "";
            filterField.setValue(newValue);
            textChangeListener.textChange(new MyTextChangeEvent(""));
        });


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


    private Supplier<Long> addComboBoxes(final AbstractOrderedLayout result, Consumer<Long> onChange) {

        AbstractOrderedLayout dayLayout = new HorizontalLayout();
        dayLayout.setMargin(true);

        ComboBox day = dayComboBox();
        day.setNullSelectionAllowed(false);
        ComboBox month = monthComboBox();
        month.setNullSelectionAllowed(false);
        ComboBox year = yearCombBox();
        year.setNullSelectionAllowed(false);

        dayLayout.setSpacing(true);
        dayLayout.addComponent(day);
        dayLayout.addComponent(month);
        dayLayout.addComponent(year);
        result.addComponent(dayLayout);
        result.setComponentAlignment(dayLayout, Alignment.MIDDLE_LEFT);

        AbstractOrderedLayout timeLayout = new HorizontalLayout();
        ComboBox hour = hourCombBox();
        hour.setNullSelectionAllowed(false);
        ComboBox min = minCombBox();
        min.setNullSelectionAllowed(false);

        ComboBox seconds = secCombBox();
        seconds.setNullSelectionAllowed(false);

        ComboBox milliseconds = millisecondsCombBox();
        milliseconds.setNullSelectionAllowed(false);

        timeLayout.setSpacing(true);
        timeLayout.addComponent(hour);
        timeLayout.addComponent(min);
        timeLayout.addComponent(seconds);
        timeLayout.addComponent(milliseconds);
        result.addComponent(timeLayout);
        result.setComponentAlignment(timeLayout, Alignment.MIDDLE_RIGHT);

        final Supplier<Long> utcMillisSupplier = () -> {
            for (int i = 0; i < 5; i++) {

                int year0 = Integer.valueOf(year.getValue().toString());

                int month0 = monthSelected(month);
                int dayOfMonth0 = Integer.valueOf(day.getValue().toString());
                int hour0 = Integer.valueOf(hour.getValue().toString());
                int minute0 = Integer.valueOf(min.getValue().toString());
                int second0 = Integer.valueOf(seconds.getValue().toString());
                int milliseconds0 = Integer.valueOf(milliseconds.getValue().toString());
                int nanoOfSecond0 = milliseconds0 * 1_000_000;

                try {
                    ZonedDateTime time = ZonedDateTime.of(year0, month0, dayOfMonth0, hour0, minute0,
                            second0,
                            nanoOfSecond0, ZoneOffset.UTC);
                    return (time.toEpochSecond() * 1000) + milliseconds0;
                } catch (DateTimeException e) {
                    day.setValue(Integer.toString(dayOfMonth0 - 1));
                }

            }
            throw new IllegalStateException();
        };

        Property.ValueChangeListener listener = new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                onChange.accept(utcMillisSupplier.get());
            }

        };


        day.addValueChangeListener(listener);
        month.addValueChangeListener(listener);
        year.addValueChangeListener(listener);
        hour.addValueChangeListener(listener);
        min.addValueChangeListener(listener);
        seconds.addValueChangeListener(listener);
        milliseconds.addValueChangeListener(listener);

        return utcMillisSupplier;

    }

    private int monthSelected(ComboBox month) {
        int i = MONTHS.indexOf(month.getValue());
        if (i == -1)
            i = 0;
        return i + 1;
    }

    @NotNull
    private ComboBox monthComboBox() {

        ComboBox month = new ComboBox("month", MONTHS);


        month.setInvalidAllowed(false);
        month.setNullSelectionAllowed(false);
        month.addItems(MONTHS);
        month.setWidth(150, Sizeable.Unit.PIXELS);
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        month.setValue(MONTHS_ARRAY[instance.get(Calendar.MONTH)]);

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
