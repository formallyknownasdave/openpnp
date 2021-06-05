package org.openpnp.machine.starmachine.wizards;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import org.jdesktop.beansbinding.AutoBinding;
import org.openpnp.gui.components.ComponentDecorators;
import org.openpnp.gui.support.*;
import org.openpnp.machine.reference.ReferenceHead;
import org.openpnp.machine.starmachine.StarHead;
import org.openpnp.model.Configuration;
import org.openpnp.model.Length;
import org.openpnp.model.Location;
import org.openpnp.spi.Axis;
import org.openpnp.spi.Camera;
import org.openpnp.spi.base.AbstractAxis;
import org.openpnp.spi.base.AbstractHead;
import org.openpnp.spi.base.AbstractMachine;
import org.openpnp.util.MovableUtils;
import org.openpnp.util.UiUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class StarHeadConfigurationWizard extends AbstractConfigurationWizard {

    // Locations panel
    private JTextField homingFiducialX, homingFiducialY, parkX, parkY;
    private JComboBox visualHomingMethod;

    // Nozzle Position Panel
    private JComboBox comboBoxAxisNozzlePosition;

    // Z Probe Panel
    private JComboBox comboBoxZProbeActuator;

    // Pump Panel
    private JComboBox comboBoxPumpActuator;

    private final StarHead head;
    private final AbstractMachine machine;

    public StarHeadConfigurationWizard(AbstractMachine machine, StarHead head) {
        this.head = head;
        this.machine = machine;
        createUi();
    }

    private void createUi() {
        createLocationsUi();
        createNozzlePositionUi();
        createZProbeUi();
        createPumpUi();
    }

    public void createLocationsUi() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(null, "Locations", TitledBorder.LEADING, TitledBorder.TOP,
                null, null));
        contentPanel.add(panel);
        panel.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("max(80dlu;default)"),
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,},
                new RowSpec[] {
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,}));

        JLabel lblX = new JLabel("X");
        panel.add(lblX, "4, 2, center, default");

        JLabel lblY = new JLabel("Y");
        panel.add(lblY, "6, 2, center, default");

        JLabel lblHomingFiducial = new JLabel("Homing Fiducial");
        panel.add(lblHomingFiducial, "2, 4, right, default");

        homingFiducialX = new JTextField();
        panel.add(homingFiducialX, "4, 4, fill, default");
        homingFiducialX.setColumns(10);

        homingFiducialY = new JTextField();
        homingFiducialY.setText("");
        panel.add(homingFiducialY, "6, 4, fill, default");
        homingFiducialY.setColumns(10);

        JButton btnCaptureHome = new JButton(captureHomeCoordinatesAction);
        btnCaptureHome.setHideActionText(true);
        panel.add(btnCaptureHome, "8, 4");

        JButton btnPositionHome = new JButton(positionHomeCoordinatesAction);
        btnPositionHome.setHideActionText(true);
        panel.add(btnPositionHome, "10, 4");

        JLabel lblHomingMethod = new JLabel("Homing Method");
        panel.add(lblHomingMethod, "2, 6, right, default");

        visualHomingMethod = new JComboBox(AbstractHead.VisualHomingMethod.values());
        visualHomingMethod.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                adaptDialog();
            }
        });
        panel.add(visualHomingMethod, "4, 6, 3, 1, fill, default");

        JLabel lblWarningChangingThese = new JLabel("<html><p>\r\n<strong>Important Notice</strong>: the homing fiducial should be mounted \r\nand configured early in the build process, before you start capturing a large number of\r\nlocations for the Machine Setup (nozzle tip changer, feeders etc.) \r\n</p>\r\n<p style=\"color:red\">Each time the above settings are changed or the fiducial physically moved, all the already captured locations in the Machine Setup will be broken. </p></html>");
        lblWarningChangingThese.setForeground(Color.BLACK);
        panel.add(lblWarningChangingThese, "4, 8, 7, 1");

        JLabel lblParkLocation = new JLabel("Park Location");
        panel.add(lblParkLocation, "2, 12, right, default");

        parkX = new JTextField();
        panel.add(parkX, "4, 12, fill, default");
        parkX.setColumns(5);

        parkY = new JTextField();
        parkY.setColumns(5);
        panel.add(parkY, "6, 12, fill, default");

        JButton btnNewButton = new JButton(captureParkCoordinatesAction);
        btnNewButton.setHideActionText(true);
        panel.add(btnNewButton, "8, 12");

        JButton btnNewButton_1 = new JButton(positionParkCoordinatesAction);
        btnNewButton_1.setHideActionText(true);
        panel.add(btnNewButton_1, "10, 12");
    }

    private void createNozzlePositionUi() {
        JPanel panelNozzlePosition = new JPanel();
        panelNozzlePosition.setBorder(new TitledBorder(null, "Nozzle Position", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        contentPanel.add(panelNozzlePosition);
        panelNozzlePosition.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,},
                new RowSpec[] {
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,}));

        JLabel lblNewLabel_4 = new JLabel("Nozzle Position Axis");
        panelNozzlePosition.add(lblNewLabel_4, "2, 2, right, default");

        comboBoxAxisNozzlePosition = new JComboBox();
        comboBoxAxisNozzlePosition.setModel(new AxesComboBoxModel(machine, AbstractAxis.class, null, true));
        panelNozzlePosition.add(comboBoxAxisNozzlePosition, "4, 2");
    }
    private void createZProbeUi() {
        JPanel panel_2 = new JPanel();
        panel_2.setBorder(new TitledBorder(null, "Z Probe", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        contentPanel.add(panel_2);
        panel_2.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("max(80dlu;default)"),
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("max(50dlu;default)"),},
                new RowSpec[] {
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,}));

        JLabel lblNewLabel_4 = new JLabel("Z Probe Actuator");
        panel_2.add(lblNewLabel_4, "2, 2, right, default");

        comboBoxZProbeActuator = new JComboBox();
        comboBoxZProbeActuator.setModel(new ActuatorsComboBoxModel(head));
        panel_2.add(comboBoxZProbeActuator, "4, 2");
    }
    private void createPumpUi() {
        JPanel panel_3 = new JPanel();
        panel_3.setBorder(new TitledBorder(null, "Pump", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        contentPanel.add(panel_3);
        panel_3.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("max(80dlu;default)"),
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("max(50dlu;default)"),},
                new RowSpec[] {
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,}));

        JLabel lblVacuumPumpActuator = new JLabel("Vacuum Pump Actuator");
        panel_3.add(lblVacuumPumpActuator, "2, 2, 2, 1, right, default");

        comboBoxPumpActuator = new JComboBox();
        comboBoxPumpActuator.setModel(new ActuatorsComboBoxModel(head));
        panel_3.add(comboBoxPumpActuator, "4, 2, fill, default");
    }
    @Override
    public void createBindings() {
        LengthConverter lengthConverter = new LengthConverter();
        NamedConverter<Axis> axisConverter = new NamedConverter<>(machine.getAxes());
        MutableLocationProxy homingFiducialLocation = new MutableLocationProxy();
        bind(AutoBinding.UpdateStrategy.READ_WRITE, head, "homingFiducialLocation", homingFiducialLocation, "location");
        addWrappedBinding(homingFiducialLocation, "lengthX", homingFiducialX, "text", lengthConverter);
        addWrappedBinding(homingFiducialLocation, "lengthY", homingFiducialY, "text", lengthConverter);

        addWrappedBinding(head, "visualHomingMethod", visualHomingMethod, "selectedItem");

        MutableLocationProxy parkLocation = new MutableLocationProxy();
        bind(AutoBinding.UpdateStrategy.READ_WRITE, head, "parkLocation", parkLocation, "location");
        addWrappedBinding(parkLocation, "lengthX", parkX, "text", lengthConverter);
        addWrappedBinding(parkLocation, "lengthY", parkY, "text", lengthConverter);


        addWrappedBinding(head, "axisNozzlePosition", comboBoxAxisNozzlePosition, "selectedItem", axisConverter);
        addWrappedBinding(head, "zProbeActuatorName", comboBoxZProbeActuator, "selectedItem");
        addWrappedBinding(head, "pumpActuatorName", comboBoxPumpActuator, "selectedItem");

        ComponentDecorators.decorateWithAutoSelectAndLengthConversion(homingFiducialX);
        ComponentDecorators.decorateWithAutoSelectAndLengthConversion(homingFiducialY);
        ComponentDecorators.decorateWithAutoSelectAndLengthConversion(parkX);
        ComponentDecorators.decorateWithAutoSelectAndLengthConversion(parkY);

        adaptDialog();
    }

    protected void adaptDialog() {
        boolean homingCapture = (visualHomingMethod.getSelectedItem() == AbstractHead.VisualHomingMethod.ResetToFiducialLocation);
        boolean homingFiducial = (visualHomingMethod.getSelectedItem() != AbstractHead.VisualHomingMethod.None);
        captureHomeCoordinatesAction.setEnabled(homingCapture);
        positionHomeCoordinatesAction.setEnabled(homingCapture);
        homingFiducialX.setEnabled(homingFiducial);
        homingFiducialY.setEnabled(homingFiducial);
    }

    private static Location getParsedLocation(JTextField textFieldX, JTextField textFieldY) {
        double x = 0, y = 0, z = 0, rotation = 0;
        if (textFieldX != null) {
            x = Length.parse(textFieldX.getText())
                    .getValue();
        }
        if (textFieldY != null) {
            y = Length.parse(textFieldY.getText())
                    .getValue();
        }
        return new Location(Configuration.get()
                .getSystemUnits(),
                x, y, z, rotation);
    }
    private Action captureHomeCoordinatesAction =
            new AbstractAction("Get Camera Coordinates", Icons.captureCamera) {
                {
                    putValue(Action.SHORT_DESCRIPTION,
                            "Capture the location that the camera is centered on.");
                }

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    UiUtils.messageBoxOnException(() -> {
                        Location l = head.getDefaultCamera()
                                .getLocation();
                        Helpers.copyLocationIntoTextFields(l, homingFiducialX, homingFiducialY, null, null);
                    });
                }
            };


    private Action positionHomeCoordinatesAction =
            new AbstractAction("Position Camera", Icons.centerCamera) {
                {
                    putValue(Action.SHORT_DESCRIPTION,
                            "Position the camera over the center of the location.");
                }

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    UiUtils.submitUiMachineTask(() -> {
                        Camera camera = head.getDefaultCamera();
                        Location location = getParsedLocation(homingFiducialX, homingFiducialY);
                        MovableUtils.moveToLocationAtSafeZ(camera, location);
                    });
                }
            };


    private Action captureParkCoordinatesAction =
            new AbstractAction("Get Camera Coordinates", Icons.captureCamera) {
                {
                    putValue(Action.SHORT_DESCRIPTION,
                            "Capture the location that the camera is centered on.");
                }

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    UiUtils.messageBoxOnException(() -> {
                        Location l = head.getDefaultCamera()
                                .getLocation();
                        Helpers.copyLocationIntoTextFields(l, parkX, parkY, null, null);
                    });
                }
            };


    private Action positionParkCoordinatesAction =
            new AbstractAction("Position Camera", Icons.centerCamera) {
                {
                    putValue(Action.SHORT_DESCRIPTION,
                            "Position the camera over the center of the location.");
                }

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    UiUtils.submitUiMachineTask(() -> {
                        Camera camera = head.getDefaultCamera();
                        Location location = getParsedLocation(parkX, parkY);
                        MovableUtils.moveToLocationAtSafeZ(camera, location);
                    });
                }
            };
}
