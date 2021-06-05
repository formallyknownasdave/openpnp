package org.openpnp.machine.reference.psh;

import org.openpnp.gui.MainFrame;
import org.openpnp.gui.support.Icons;
import org.openpnp.gui.support.MessageBoxes;
import org.openpnp.machine.reference.vision.ReferenceNozzleTipLocator;
import org.openpnp.model.Configuration;
import org.openpnp.spi.NozzleTipLocator;
import org.openpnp.spi.PropertySheetHolder;
import org.openpnp.spi.base.SimplePropertySheetHolder;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class NozzleTipLocatorPropertySheetHolder extends SimplePropertySheetHolder {

    public NozzleTipLocatorPropertySheetHolder(String title, List<? extends PropertySheetHolder> children,
                                               Icon icon) {
        super(title, children, icon);
    }
    @Override
    public Action[] getPropertySheetHolderActions() {
        return new Action[] {newAction};
    }

    public Action newAction = new AbstractAction() {
        {
            putValue(SMALL_ICON, Icons.nozzleTipAdd);
            putValue(NAME, "New Nozzle Tip Pipeline...");
            putValue(SHORT_DESCRIPTION, "Create a new nozzle tip vision pipeline.");
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            Configuration configuration = Configuration.get();
            Class<? extends NozzleTipLocator> cls = ReferenceNozzleTipLocator.class;
            try {
                NozzleTipLocator locator = cls.newInstance();
                configuration.getMachine().addNozzleTipLocator(locator);
            }


            catch (Exception e) {
                MessageBoxes.errorBox(MainFrame.get(), "Error", e);
            }
        }
    };
}
