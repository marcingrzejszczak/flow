package com.itmill.toolkit.tests.tickets;

import com.itmill.toolkit.data.Item;
import com.itmill.toolkit.data.Property;
import com.itmill.toolkit.ui.AbstractSelect;
import com.itmill.toolkit.ui.CustomComponent;
import com.itmill.toolkit.ui.Label;
import com.itmill.toolkit.ui.OrderedLayout;
import com.itmill.toolkit.ui.Panel;
import com.itmill.toolkit.ui.TextField;
import com.itmill.toolkit.ui.Tree;
import com.itmill.toolkit.ui.Window;

public class Ticket1245 extends com.itmill.toolkit.Application {

    TextField f = new TextField();

    @Override
    public void init() {
        final Window main = new Window(getClass().getName().substring(
                getClass().getName().lastIndexOf(".") + 1));
        setMainWindow(main);

        main.addComponent(new TreeExample());
    }
}

class TreeExample extends CustomComponent {

    // Id for the caption property
    private static final Object CAPTION_PROPERTY = "caption";

    private static final String desc = "non-first tree in non-sized orderedlayout seems to be the problem";

    Tree tree;

    public TreeExample() {
        final OrderedLayout main = new OrderedLayout();
        setCompositionRoot(main);

        // Panel w/ Tree
        main.setStyleName(Panel.STYLE_LIGHT);
        main.setWidth(200);
        // // Description, this is needed. Works in first slot
        main.addComponent(new Label(desc));

        // setting either width or height fixes the issue
        // p.setWidth(500);
        // p.setHeight(800);

        // Tree with a few items
        tree = new Tree();
        tree.setImmediate(true);
        // we'll use a property for caption instead of the item id ("value"),
        // so that multiple items can have the same caption
        tree.addContainerProperty(CAPTION_PROPERTY, String.class, "");
        tree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);
        tree.setItemCaptionPropertyId(CAPTION_PROPERTY);
        for (int i = 1; i <= 3; i++) {
            final Object id = addCaptionedItem("Section " + i, null);
            tree.expandItem(id);
            addCaptionedItem("Team A", id);
            addCaptionedItem("Team B", id);
        }
        main.addComponent(tree);
    }

    /**
     * Helper to add an item with specified caption and (optional) parent.
     * 
     * @param caption
     *            The item caption
     * @param parent
     *            The (optional) parent item id
     * @return the created item's id
     */
    private Object addCaptionedItem(String caption, Object parent) {
        // add item, let tree decide id
        final Object id = tree.addItem();
        // get the created item
        final Item item = tree.getItem(id);
        // set our "caption" property
        final Property p = item.getItemProperty(CAPTION_PROPERTY);
        p.setValue(caption);
        if (parent != null) {
            tree.setChildrenAllowed(parent, true);
            tree.setParent(id, parent);
            tree.setChildrenAllowed(id, false);
        }
        return id;
    }

}