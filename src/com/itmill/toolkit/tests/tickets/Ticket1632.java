package com.itmill.toolkit.tests.tickets;

import com.itmill.toolkit.Application;
import com.itmill.toolkit.data.Item;
import com.itmill.toolkit.ui.Button;
import com.itmill.toolkit.ui.Table;
import com.itmill.toolkit.ui.Window;
import com.itmill.toolkit.ui.Button.ClickEvent;

/**
 */
public class Ticket1632 extends Application {

    private Table t;

    @Override
    public void init() {

        final Window mainWin = new Window("Test app");
        setMainWindow(mainWin);

        t = new Table();

        t.addContainerProperty("col1", String.class, "");
        t.addContainerProperty("col2", String.class, "");
        t.addContainerProperty("col3", String.class, "");

        t.addItem(new Object[] { "jep", "foo", "bar" }, "1");
        t.addItem(new Object[] { "jep", "foo", "bar" }, "2");
        t.addItem(new Object[] { "jep", "foo", "bar" }, "3");

        t.setVisibleColumns(new Object[] { "col1", "col2" });

        t.addItem(new Object[] { "foo", "bar" }, "4");

        // workaround to add item with all values
        Item i = t.addItem("5");
        i.getItemProperty("col1").setValue("jep");
        i.getItemProperty("col2").setValue("foo");
        i.getItemProperty("col3").setValue("bar");

        mainWin.addComponent(t);

        Button b = new Button("Toggle col3");
        b.addListener(new Button.ClickListener() {
            boolean visible = false;

            public void buttonClick(ClickEvent event) {
                visible = !visible;
                if (visible) {
                    t
                            .setVisibleColumns(new Object[] { "col1", "col2",
                                    "col3" });

                } else {
                    t.setVisibleColumns(new Object[] { "col1", "col2" });

                }

            }
        });

        mainWin.addComponent(b);

    }
}