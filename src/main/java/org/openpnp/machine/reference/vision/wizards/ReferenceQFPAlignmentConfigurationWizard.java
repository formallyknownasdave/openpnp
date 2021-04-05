package org.openpnp.machine.reference.vision.wizards;

import org.openpnp.gui.support.DoubleConverter;
import org.openpnp.gui.support.IntegerConverter;
import org.openpnp.gui.support.LengthConverter;
import org.openpnp.model.Configuration;
import org.openpnp.model.LeadGroup;
import org.openpnp.model.Package;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class ReferenceQFPAlignmentConfigurationWizard extends AbstractAnalysisConfigurationWizard {
    private Package pkg;

    JPanel panelPreview;
    private JCheckBox enabledCheckbox;
    private JCheckBox preRotCheckbox;
    private JTextField textFieldMaxVisionPasses;
    private JTextField textFieldMaxLinearOffset;
    private JTextField textFieldMaxAngularOffset;
    private JTextField textFieldWidth;
    private JTextField textFieldLength;
    private JTextField textFieldHeight;
    private JCheckBox checkBoxTotalDimensions;

    public ArrayList<LeadGroup> tempLeadGroups = new ArrayList<>();

    public ReferenceQFPAlignmentConfigurationWizard(Package pkg) {
        super(pkg);
        for (LeadGroup group : pkg.getOutline().getLeadGroups()) {
            tempLeadGroups.add(group.clone());
        }

        JSplitPane panelConfiguration = new JSplitPane();
        panelConfiguration.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        contentPanel.add(panelConfiguration);

        JPanel panelDetails = new JPanel();
        panelDetails.setLayout(new BoxLayout(panelDetails, BoxLayout.Y_AXIS));
        panelDetails.add(getDimensionsPanel());
        panelDetails.add(getLeadsPanel());
        panelConfiguration.add(new JScrollPane(panelDetails),JSplitPane.LEFT);
        panelPreview = new PreviewPanel(tempLeadGroups);
        panelConfiguration.add(panelPreview,JSplitPane.RIGHT);
    }

    @Override
    public String getWizardName() {
        return "ReferenceQFPAlignmentConfigurationWizard";
    }

    @Override
    public void createBindings() {
        LengthConverter lengthConverter = new LengthConverter();
        IntegerConverter intConverter = new IntegerConverter();
        DoubleConverter doubleConverter = new DoubleConverter(Configuration.get()
                .getLengthDisplayFormat());
        createLeadsPanelBindings(tempLeadGroups);

        for (LeadGroup group : tempLeadGroups) {
            group.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    panelPreview.validate();
                    panelPreview.repaint();
                }
            });
        }

        panelPreview.validate();
        panelPreview.repaint();
    }
}
