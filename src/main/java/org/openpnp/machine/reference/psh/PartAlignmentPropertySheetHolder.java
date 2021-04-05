package org.openpnp.machine.reference.psh;

import org.openpnp.gui.MainFrame;
import org.openpnp.gui.components.ClassSelectionDialog;
import org.openpnp.gui.support.Icons;
import org.openpnp.gui.support.MessageBoxes;
import org.openpnp.machine.reference.vision.ReferenceBottomVision;
import org.openpnp.model.Configuration;
import org.openpnp.spi.Actuator;
import org.openpnp.spi.PartAlignment;
import org.openpnp.spi.PropertySheetHolder;
import org.openpnp.spi.base.SimplePropertySheetHolder;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class PartAlignmentPropertySheetHolder extends SimplePropertySheetHolder {

    public PartAlignmentPropertySheetHolder(String title, List<? extends PropertySheetHolder> children,
                                            Icon icon) {
        super(title, children, icon);
    }
    @Override
    public Action[] getPropertySheetHolderActions() {
        return new Action[] {newAction};
    }

    public Action newAction = new AbstractAction() {
        {
            putValue(SMALL_ICON, Icons.add);
            putValue(NAME, "New Part Alignment...");
            putValue(SHORT_DESCRIPTION, "Create a new part alignment type.");
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            Configuration configuration = Configuration.get();
            ClassSelectionDialog<PartAlignment> dialog = new ClassSelectionDialog<>(MainFrame.get(),
                    "Select Part Alignment Type...", "Please select a part alignment implemention from the list below.",
                    configuration.getMachine().getCompatiblePartAlignmentClasses());
            dialog.setVisible(true);
            Class<? extends PartAlignment> cls = dialog.getSelectedClass();
            if (cls == null) {
                return;
            }
            try {
                PartAlignment partAlignment = cls.newInstance();
                Configuration.get().getMachine().addPartAlignment(partAlignment);

            }
            catch (Exception e) {
                MessageBoxes.errorBox(MainFrame.get(), "Camera Error", e);
            }
        }
    };
}
