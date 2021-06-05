package org.openpnp.spi.base;

import org.openpnp.model.AbstractModelObject;
import org.openpnp.model.Configuration;
import org.openpnp.spi.NozzleTip;
import org.openpnp.spi.NozzleTipLocator;
import org.simpleframework.xml.Attribute;

import javax.swing.*;

public abstract class AbstractNozzleTipLocator extends AbstractModelObject implements NozzleTipLocator {
    @Attribute
    protected String id;

    @Attribute(required = false)
    protected String name;

    public AbstractNozzleTipLocator() {
        this.id = Configuration.createId("NTL");
        this.name = getClass().getSimpleName();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        Object oldValue = this.name;
        this.name = name;
        firePropertyChange("name", oldValue, name);
    }

    @Override
    public Icon getPropertySheetHolderIcon() {
        return null;
    }
}
