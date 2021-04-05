/*
 * Copyright (C) 2011 Jason von Nieda <jason@vonnieda.org>
 * 
 * This file is part of OpenPnP.
 * 
 * OpenPnP is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * OpenPnP is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with OpenPnP. If not, see
 * <http://www.gnu.org/licenses/>.
 * 
 * For more information about OpenPnP visit http://openpnp.org
 */

package org.openpnp.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.openpnp.gui.components.AutoSelectTextTable;
import org.openpnp.gui.components.CameraView;
import org.openpnp.gui.components.ComponentDecorators;
import org.openpnp.gui.components.reticle.FootprintReticle;
import org.openpnp.gui.components.reticle.Reticle;
import org.openpnp.gui.support.*;
import org.openpnp.gui.tablemodel.FootprintTableModel;
import org.openpnp.model.*;
import org.openpnp.model.Footprint.Pad;
import org.openpnp.model.Package;
import org.openpnp.spi.Camera;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import org.openpnp.spi.Machine;
import org.openpnp.spi.PartAlignment;
import org.openpnp.spi.base.AbstractMachine;

@SuppressWarnings("serial")
public class PackageVisionPanel extends JPanel implements WizardContainer {
    private Package pkg;
    // Fields
    private JTextField bodyWidthTf;
    private JTextField bodyHeightTf;
    private JComboBox unitsCombo;
    private JComboBox inputPartAlignmentCombo;
    // Models
    private ModelComboBoxModel<PartAlignment> inputPartAlignmentModel;
    // Panels
    JPanel panelWizard;

    public PackageVisionPanel(Package pkg) {
        this.pkg = pkg;
        AbstractMachine machine = (AbstractMachine)(Configuration.get().getMachine());

        setLayout(new BorderLayout(0, 0));


        JPanel propertiesPanel = new JPanel();
        add(propertiesPanel, BorderLayout.NORTH);
        propertiesPanel.setBorder(
                new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Settings",
                        TitledBorder.LEADING, TitledBorder.TOP, null));
        propertiesPanel.setLayout(new FormLayout(
                new ColumnSpec[] {FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
                        FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
                        FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
                        FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),},
                new RowSpec[] {FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,FormSpecs.RELATED_GAP_ROWSPEC}));

        JLabel lblUnits = new JLabel("Units");
        propertiesPanel.add(lblUnits, "2, 2, right, default");

        unitsCombo = new JComboBox(LengthUnit.values());
        propertiesPanel.add(unitsCombo, "4, 2, left, default");

        JLabel lblPartAlignment = new JLabel("Part Alignment");
        propertiesPanel.add(lblPartAlignment, "2, 4, right, default");

        inputPartAlignmentModel = new ModelComboBoxModel<PartAlignment>(machine, "partAlignments", false);
        inputPartAlignmentCombo = new JComboBox(inputPartAlignmentModel);
        propertiesPanel.add(inputPartAlignmentCombo, "4, 4, left, default");

        JLabel lblBodyWidth = new JLabel("Body Width");
        propertiesPanel.add(lblBodyWidth, "6, 2, right, default");

        bodyWidthTf = new JTextField();
        propertiesPanel.add(bodyWidthTf, "8, 2, left, default");
        bodyWidthTf.setColumns(10);

        JLabel lblBodyHeight = new JLabel("Body Length");
        propertiesPanel.add(lblBodyHeight, "6, 4, right, default");

        bodyHeightTf = new JTextField();
        propertiesPanel.add(bodyHeightTf, "8, 4, left, default");
        bodyHeightTf.setColumns(10);

        panelWizard = new JPanel();
        panelWizard.setLayout(new BorderLayout(0,0));
        // Add alignment configuration wizard
        PartAlignment alignment = this.pkg.getPartAlignment();
        if (alignment != null) {
            Wizard wizard = alignment.getConfigurationWizard(this.pkg);
            if (wizard != null) {
                setWizardPanel(wizard);
            }
            else
            {
                setMessage("No configuration wizard available for this alignment");
            }
        }
        else {
            setMessage("No alignment method specified");
        }
        add(panelWizard, BorderLayout.CENTER);

        this.pkg.addPropertyChangeListener("partAlignment", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                PartAlignment method = (PartAlignment)(evt.getNewValue());
                panelWizard.removeAll();
                if (method != null) {
                    if (method.getConfigurationWizard(pkg) != null) {
                        setWizardPanel(method.getConfigurationWizard(pkg));
                    }
                    else {
                        setMessage("No configuration wizard available for this alignment");
                    }
                }
                else {
                    setMessage("No alignment method specified");
                }
                panelWizard.validate();
                panelWizard.repaint();
            }
        });
        initDataBindings();
    }

    protected void initDataBindings() {
        DoubleConverter doubleConverter =
                new DoubleConverter(Configuration.get().getLengthDisplayFormat());

        BeanProperty<Outline, LengthUnit> outlineBeanProperty = BeanProperty.create("units");
        BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
        AutoBinding<Outline, LengthUnit, JComboBox, Object> autoBinding =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, pkg.getOutline(),
                        outlineBeanProperty, unitsCombo, jComboBoxBeanProperty);
        autoBinding.bind();
        //
        BeanProperty<Outline, Double> outlineBeanProperty_1 = BeanProperty.create("bodyWidth");
        BeanProperty<JTextField, String> jTextFieldBeanProperty = BeanProperty.create("text");
        AutoBinding<Outline, Double, JTextField, String> autoBinding_1 =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, pkg.getOutline(),
                        outlineBeanProperty_1, bodyWidthTf, jTextFieldBeanProperty);
        autoBinding_1.setConverter(doubleConverter);
        autoBinding_1.bind();
        //
        BeanProperty<Outline, Double> outlineBeanProperty_2 = BeanProperty.create("bodyHeight");
        BeanProperty<JTextField, String> jTextFieldBeanProperty_1 = BeanProperty.create("text");
        AutoBinding<Outline, Double, JTextField, String> autoBinding_2 =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, pkg.getOutline(),
                        outlineBeanProperty_2, bodyHeightTf, jTextFieldBeanProperty_1);
        autoBinding_2.setConverter(doubleConverter);
        autoBinding_2.bind();
        //
        inputPartAlignmentModel.bind(this.pkg,"partAlignment", inputPartAlignmentCombo);

        ComponentDecorators.decorateWithAutoSelect(bodyWidthTf);
        ComponentDecorators.decorateWithAutoSelect(bodyHeightTf);
    }

    public void setMessage(String message) {
        JPanel panel = new JPanel();
        panel.add(new JLabel(message));
        panelWizard.removeAll();
        panelWizard.add(panel,BorderLayout.CENTER);
    }

    public void setWizardPanel(Wizard wizard) {
        panelWizard.add(wizard.getWizardPanel(),BorderLayout.CENTER);
        wizard.setWizardContainer(PackageVisionPanel.this);
    }

    @Override
    public void wizardCompleted(Wizard wizard) {}

    @Override
    public void wizardCancelled(Wizard wizard) {}

}
