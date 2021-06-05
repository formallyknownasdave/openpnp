package org.openpnp.machine.starmachine.wizards;

import com.jgoodies.forms.layout.*;
import org.jdesktop.beansbinding.AutoBinding;
import org.openpnp.gui.components.ComponentDecorators;
import org.openpnp.gui.support.*;
import org.openpnp.machine.starmachine.StarNozzle;
import org.openpnp.model.Configuration;
import org.openpnp.spi.Axis;
import org.openpnp.spi.base.AbstractAxis;
import org.openpnp.spi.base.AbstractMachine;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class StarNozzleConfigurationWizard extends AbstractConfigurationWizard {

    private final StarNozzle nozzle;
    private AbstractMachine machine;

    // Properties panel
    private JPanel panelProperties;

    private JLabel lblName;
    private JTextField nameTf;

    // Offsets panel
    private JPanel panelOffsets;

    private JLabel lblAxis, lblOffset;
    private JLabel lblX, lblY, lblZ, lblRotation, lblNozzlePosition;

    private JComboBox axisX, axisY, axisZ, axisRotation, axisNozzleSelect;
    private JTextField locationX, locationY, locationZ, locationRotation, nozzlePosition;

    // Safety panel
    private JPanel panelSafety;

    private JLabel lblDynamicSafeZ;

    private JTextField textFieldSafeZ;
    private JCheckBox chckbxDynamicsafez;

    // Settings panel
    private JPanel panelSettings;

    private JLabel lblDwellTime, lblPickDwellTime, lblPlaceDwellTime;

    private JTextField pickDwellTf, placeDwellTf;


    public StarNozzleConfigurationWizard(AbstractMachine machine, StarNozzle nozzle) {
        this.nozzle = nozzle;
        this.machine = machine;
        createUi();
    }

    private void createUi() {
        createPropertiesUi();
        createOffsetsUi();
        createSafetyUi();
        createSettingsUi();
    }
    private void createPropertiesUi() {
        panelProperties = new JPanel();
        panelProperties.setBorder(new TitledBorder(null, "Properties", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        contentPanel.add(panelProperties);
        panelProperties.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,},
                new RowSpec[] {
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,}));

        lblName = new JLabel("Name");
        panelProperties.add(lblName, "2, 2, right, default");

        nameTf = new JTextField();
        panelProperties.add(nameTf, "4, 2");
        nameTf.setColumns(20);
    }
    private void createOffsetsUi() {
        panelOffsets = new JPanel();
        panelOffsets.setBorder(new TitledBorder(null,
                "Coordinate System", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        panelOffsets.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
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
                        FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,}));

        lblX = new JLabel("X");
        panelOffsets.add(lblX, "4, 2");

        lblY = new JLabel("Y");
        panelOffsets.add(lblY, "6, 2");

        lblZ = new JLabel("Z");
        panelOffsets.add(lblZ, "8, 2");

        lblRotation = new JLabel("Rotation");
        panelOffsets.add(lblRotation, "10, 2");

        lblNozzlePosition = new JLabel("Nozzle Position");
        panelOffsets.add(lblNozzlePosition, "12, 2");

        lblAxis = new JLabel("Axis");
        panelOffsets.add(lblAxis, "2, 4, right, default");

        axisX = new JComboBox(new AxesComboBoxModel(machine, AbstractAxis.class, Axis.Type.X, true));
        panelOffsets.add(axisX, "4, 4, fill, default");

        axisY = new JComboBox(new AxesComboBoxModel(machine, AbstractAxis.class, Axis.Type.Y, true));
        panelOffsets.add(axisY, "6, 4, fill, default");

        axisZ = new JComboBox(new AxesComboBoxModel(machine, AbstractAxis.class, Axis.Type.Z, true));
        panelOffsets.add(axisZ, "8, 4, fill, default");

        axisRotation = new JComboBox(new AxesComboBoxModel(machine, AbstractAxis.class, Axis.Type.Rotation, true));
        panelOffsets.add(axisRotation, "10, 4, fill, default");

        axisNozzleSelect = new JComboBox(new AxesComboBoxModel(machine, AbstractAxis.class, null, true));
        axisNozzleSelect.setEnabled(false);
        panelOffsets.add(axisNozzleSelect, "12, 4, fill, default");

        lblOffset = new JLabel("Offset");
        panelOffsets.add(lblOffset, "2, 6, right, default");

        locationX = new JTextField();
        panelOffsets.add(locationX, "4, 6");
        locationX.setColumns(10);

        locationY = new JTextField();
        panelOffsets.add(locationY, "6, 6");
        locationY.setColumns(10);

        locationZ = new JTextField();
        panelOffsets.add(locationZ, "8, 6");
        locationZ.setColumns(10);

        locationRotation = new JTextField();
        panelOffsets.add(locationRotation, "10, 6, fill, default");
        locationRotation.setColumns(10);

        nozzlePosition = new JTextField();
        panelOffsets.add(nozzlePosition, "12, 6, fill, default");
        nozzlePosition.setColumns(10);

        contentPanel.add(panelOffsets);
    }
    private void createSafetyUi() {
        panelSafety = new JPanel();
        panelSafety.setBorder(new TitledBorder(null, "Safe Z", TitledBorder.LEADING,
                TitledBorder.TOP, null, null));
        contentPanel.add(panelSafety);
        panelSafety.setLayout(new FormLayout(new ColumnSpec[] {
                ColumnSpec.decode("max(81dlu;default)"),
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("114px"),
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,},
                new RowSpec[] {
                        RowSpec.decode("24px"),
                        RowSpec.decode("19px"),}));

        JLabel lblSafeZ = new JLabel("Safe Z");
        panelSafety.add(lblSafeZ, "1, 1, right, center");

        textFieldSafeZ = new JTextField();
        textFieldSafeZ.setEditable(false);
        panelSafety.add(textFieldSafeZ, "3, 1, fill, top");
        textFieldSafeZ.setColumns(10);

        lblDynamicSafeZ = new JLabel("Dynamic Safe Z");
        lblDynamicSafeZ.setToolTipText("");
        lblDynamicSafeZ.setHorizontalAlignment(SwingConstants.TRAILING);
        panelSafety.add(lblDynamicSafeZ, "1, 2");

        chckbxDynamicsafez = new JCheckBox("");
        chckbxDynamicsafez.setToolTipText("dynamically adjust the safeZ, so the bottom of a loaded part is at safeZ if possible");
        panelSafety.add(chckbxDynamicsafez, "3, 2");


    }
    private void createSettingsUi() {
        panelSettings = new JPanel();
        panelSettings.setBorder(new TitledBorder(null,
                "Settings", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        contentPanel.add(panelSettings);
        panelSettings
                .setLayout(
                        new FormLayout(new ColumnSpec[] {
                                FormSpecs.RELATED_GAP_COLSPEC,
                                FormSpecs.DEFAULT_COLSPEC,
                                FormSpecs.RELATED_GAP_COLSPEC,
                                FormSpecs.DEFAULT_COLSPEC,
                                FormSpecs.RELATED_GAP_COLSPEC,
                                ColumnSpec.decode("default:grow"),
                                FormSpecs.RELATED_GAP_COLSPEC,
                                ColumnSpec.decode("default:grow"),
                                FormSpecs.RELATED_GAP_COLSPEC,
                                ColumnSpec.decode("default:grow"),},
                                new RowSpec[] {
                                        FormSpecs.RELATED_GAP_ROWSPEC,
                                        FormSpecs.DEFAULT_ROWSPEC,
                                        FormSpecs.RELATED_GAP_ROWSPEC,
                                        FormSpecs.DEFAULT_ROWSPEC,
                                        FormSpecs.RELATED_GAP_ROWSPEC,
                                        FormSpecs.DEFAULT_ROWSPEC,}));

        lblPickDwellTime = new JLabel("Pick Dwell Time (ms)");
        panelSettings.add(lblPickDwellTime, "2, 2, right, default");

        pickDwellTf = new JTextField();
        panelSettings.add(pickDwellTf, "4, 2, fill, default");
        pickDwellTf.setColumns(10);

        lblPlaceDwellTime = new JLabel("Place Dwell Time (ms)");
        panelSettings.add(lblPlaceDwellTime, "2, 4, right, default");

        placeDwellTf = new JTextField();
        panelSettings.add(placeDwellTf, "4, 4, fill, default");
        placeDwellTf.setColumns(10);

        CellConstraints cc = new CellConstraints();
        lblDwellTime = new JLabel("Note: Total Dwell Time is the sum of Nozzle Dwell Time plus the Nozzle Tip Dwell Time.");
        panelSettings.add(lblDwellTime, "2, 6, 9, 1, fill, default");
    }
    @Override
    public void createBindings() {
        LengthConverter lengthConverter = new LengthConverter();
        IntegerConverter intConverter = new IntegerConverter();
        DoubleConverter doubleConverter = new DoubleConverter(Configuration.get().getLengthDisplayFormat());
        NamedConverter<Axis> axisConverter = new NamedConverter<>(machine.getAxes());

        addWrappedBinding(nozzle, "name", nameTf, "text");

        addWrappedBinding(nozzle, "axisX", axisX, "selectedItem", axisConverter);
        addWrappedBinding(nozzle, "axisY", axisY, "selectedItem", axisConverter);
        addWrappedBinding(nozzle, "axisZ", axisZ, "selectedItem", axisConverter);
        addWrappedBinding(nozzle, "axisRotation", axisRotation, "selectedItem", axisConverter);
        // Not editable from this page
        addWrappedBinding(nozzle.getHead(), "axisNozzlePosition", axisNozzleSelect, "selectedItem", axisConverter);

        MutableLocationProxy headOffsets = new MutableLocationProxy();
        bind(AutoBinding.UpdateStrategy.READ_WRITE, nozzle, "headOffsets", headOffsets, "location");
        addWrappedBinding(headOffsets, "lengthX", locationX, "text", lengthConverter);
        addWrappedBinding(headOffsets, "lengthY", locationY, "text", lengthConverter);
        addWrappedBinding(headOffsets, "lengthZ", locationZ, "text", lengthConverter);
        addWrappedBinding(headOffsets, "rotation", locationRotation, "text", doubleConverter);
        addWrappedBinding(nozzle, "nozzlePosition", nozzlePosition, "text", doubleConverter);

        addWrappedBinding(nozzle, "enableDynamicSafeZ", chckbxDynamicsafez, "selected");
        addWrappedBinding(nozzle, "safeZ", textFieldSafeZ, "text", lengthConverter);
        addWrappedBinding(nozzle, "pickDwellMilliseconds", pickDwellTf, "text", intConverter);
        addWrappedBinding(nozzle, "placeDwellMilliseconds", placeDwellTf, "text", intConverter);

        ComponentDecorators.decorateWithAutoSelect(nameTf);
        ComponentDecorators.decorateWithAutoSelect(pickDwellTf);
        ComponentDecorators.decorateWithAutoSelect(placeDwellTf);
        ComponentDecorators.decorateWithAutoSelectAndLengthConversion(locationX);
        ComponentDecorators.decorateWithAutoSelectAndLengthConversion(locationY);
        ComponentDecorators.decorateWithAutoSelectAndLengthConversion(locationZ);
        ComponentDecorators.decorateWithAutoSelect(locationRotation);
        ComponentDecorators.decorateWithAutoSelect(nozzlePosition);
        ComponentDecorators.decorateWithAutoSelectAndLengthConversion(textFieldSafeZ);
    }
}
