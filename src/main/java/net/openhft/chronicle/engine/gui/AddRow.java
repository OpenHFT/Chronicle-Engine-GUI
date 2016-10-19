package net.openhft.chronicle.engine.gui;

import com.vaadin.data.Validator;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;
import net.openhft.chronicle.core.util.ObjectUtils;
import net.openhft.chronicle.engine.api.column.Column;
import net.openhft.chronicle.engine.api.column.ColumnView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static java.util.Collections.EMPTY_MAP;

/**
 * @author Rob Austin.
 */
public class AddRow {

    private ColumnView columnView;

    public AddRow(ColumnView columnView) {
        this.columnView = columnView;
    }

    public void init() {

        // Create a sub-window and set the content
        Window subWindow = new Window("Add Row");
        subWindow.setClosable(false);
        subWindow.setModal(true);
        subWindow.setResizeLazy(true);
        subWindow.setResizable(false);
        subWindow.setSizeUndefined();
        subWindow.setWidth(300, Sizeable.Unit.PIXELS);
        subWindow.setDraggable(true);

        FormLayout form = new FormLayout();
        form.setMargin(true);
        ArrayList<AbstractField> fields = new ArrayList<>();
        final List<Column> columns1 = columnView.columns();
        for (Column column : columns1) {

            AbstractField field;

            if (column.type == Date.class)
                field = new DateField(column.name);
            else if (column.type == boolean.class)
                field = new CheckBox(column.name);
            else
                field = new TextField(column.name);
            field.setData(column.type);
            fields.add(field);

            if (column.primaryKey)
                field.setRequired(true);
            if (column.type == Date.class)
                field.setValue(new Date());
            else if (column.type == boolean.class)
                field.setValue(false);
            else if (column.type.isPrimitive() || Number.class.isAssignableFrom(column.type))
                field.setValue(ObjectUtils.convertTo(column.type, 0).toString());

            field.addValidator((Validator) value -> {
                try {

                    if (column.type == Date.class && (value == null || value.equals("")))
                        throw new Validator.InvalidValueException("can not convert to Date");
                    if (!(column.type.isAssignableFrom(value.getClass())))
                        ObjectUtils.convertTo(column.type, value);
                } catch (Exception e) {
                    throw new Validator.InvalidValueException("can not convert to " + column.type.getSimpleName());
                }
            });

            form.addComponent(field);
        }

        final HorizontalLayout buttons = new HorizontalLayout();
        buttons.setMargin(true);
        buttons.setSpacing(true);
        final Button cancel = new Button("Cancel");
        cancel.addClickListener((Button.ClickListener) event1 -> subWindow.close());
        buttons.addComponent(cancel);
        final Button add = new Button("Add");
        add.addClickListener((Button.ClickListener) event1 -> {

            final HashMap<String, Object> row = new HashMap<>();

            for (AbstractField field : fields) {

                try {
                    field.validate();
                } catch (Validator.InvalidValueException e) {
                    return;
                }
                row.put(field.getCaption(), ObjectUtils.convertTo((Class) field.getData(), field
                        .getValue()));
            }
            columnView.changedRow(row, EMPTY_MAP);
            subWindow.close();
        });
        buttons.addComponent(add);
        buttons.setComponentAlignment(add, Alignment.MIDDLE_RIGHT);
        buttons.setComponentAlignment(cancel, Alignment.MIDDLE_RIGHT);
        form.addComponent(buttons);
        subWindow.setContent(form);


        // Center it in the browser window
        subWindow.center();

        // Open it in the UI
        UI.getCurrent().addWindow(subWindow);
    }
}
