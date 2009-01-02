package com.itmill.toolkit.tests.tickets;

import com.itmill.toolkit.Application;
import com.itmill.toolkit.ui.Button;
import com.itmill.toolkit.ui.CustomLayout;
import com.itmill.toolkit.ui.VerticalLayout;
import com.itmill.toolkit.ui.Window;
import com.itmill.toolkit.ui.Button.ClickEvent;
import com.itmill.toolkit.ui.Button.ClickListener;

public class Ticket2347 extends Application {

    private Button b1;

    @Override
    public void init() {
        Window w = new Window(getClass().getSimpleName());
        setMainWindow(w);
        setTheme("tests-tickets");
        createUI((VerticalLayout) w.getLayout());
    }

    private void createUI(VerticalLayout layout) {
        CustomLayout cl = new CustomLayout("Ticket2347");
        b1 = new Button("200px button");
        b1.addListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                if (b1.getWidth() == 200.0) {
                    b1.setWidth("300px");
                } else {
                    b1.setWidth("200px");

                }
                b1.setCaption(b1.getWidth() + "px button");

            }

        });
        b1.setWidth("200px");
        Button b2 = new Button("100% button");
        b2.setWidth("100%");

        cl.addComponent(b1, "button1");
        cl.addComponent(b2, "button2");

        layout.addComponent(cl);
    }
}
