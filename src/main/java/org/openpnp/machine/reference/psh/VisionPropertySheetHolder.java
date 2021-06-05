package org.openpnp.machine.reference.psh;

import org.openpnp.spi.Machine;
import org.openpnp.spi.PropertySheetHolder;
import org.openpnp.spi.base.SimplePropertySheetHolder;

import javax.swing.*;
import java.util.List;

public class VisionPropertySheetHolder extends SimplePropertySheetHolder {
    final Machine machine;
    public VisionPropertySheetHolder(Machine machine, String title, List<? extends PropertySheetHolder> children,
                                     Icon icon) {
        super(title, children, icon);
        this.machine = machine;
    }

    @Override
    public Action[] getPropertySheetHolderActions() {
        return null;
    }
}
