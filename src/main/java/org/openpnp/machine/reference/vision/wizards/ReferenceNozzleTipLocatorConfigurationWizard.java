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

package org.openpnp.machine.reference.vision.wizards;

import com.jgoodies.forms.layout.*;
import org.openpnp.gui.MainFrame;
import org.openpnp.gui.components.ComponentDecorators;
import org.openpnp.gui.support.AbstractConfigurationWizard;
import org.openpnp.gui.support.IntegerConverter;
import org.openpnp.gui.support.LengthConverter;
import org.openpnp.machine.reference.ReferenceNozzleTip;
import org.openpnp.machine.reference.vision.ReferenceNozzleTipLocator;
import org.openpnp.model.Configuration;
import org.openpnp.spi.Camera;
import org.openpnp.spi.NozzleTipLocator;
import org.openpnp.vision.pipeline.CvPipeline;
import org.openpnp.vision.pipeline.ui.CvPipelineEditor;
import org.openpnp.vision.pipeline.ui.CvPipelineEditorDialog;
import org.pmw.tinylog.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class ReferenceNozzleTipLocatorConfigurationWizard extends ReferencePipelineConfigurationWizard {

    private JPanel panel;
    private JLabel lblName;
    private JTextField nameTf;
    private ReferenceNozzleTipLocator nozzleTipLocator;

    public ReferenceNozzleTipLocatorConfigurationWizard(ReferenceNozzleTipLocator nozzleTipLocator) {
        this.nozzleTipLocator = nozzleTipLocator;
        panel = new JPanel();
        panel.setBorder(new TitledBorder(null, "Properties", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        contentPanel.add(panel);
        panel.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,},
                new RowSpec[] {
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,}));

        lblName = new JLabel("Name");
        panel.add(lblName, "2, 2, right, default");

        nameTf = new JTextField();
        panel.add(nameTf, "4, 2, fill, default");
        nameTf.setColumns(10);

        contentPanel.add(createPipelinePanel());
    }

    /*private void editPipeline() throws Exception {
        CvPipeline pipeline = partSettings.getPipeline();
        Camera camera = Configuration.get().getMachine().getDefaultHead().getDefaultCamera();
        pipeline.setProperty("camera", camera);
        pipeline.setProperty("part", part);
        pipeline.setProperty("package", part.getPackage());
        pipeline.setProperty("footprint", part.getPackage().getFootprint());
        CvPipelineEditor editor = new CvPipelineEditor(pipeline);
        JDialog dialog = new CvPipelineEditorDialog(MainFrame.get(), "Fiducial Locator Pipeline", editor);
        dialog.setVisible(true);
    }*/

    @Override
    public String getWizardName() {
        return "Reference Nozzle Tip Locator";
    }

    @Override
    public void createBindings() {
        addWrappedBinding(nozzleTipLocator, "name", nameTf, "text");
        ComponentDecorators.decorateWithAutoSelect(nameTf);
    }

    private void setPipeline(CvPipeline pipeline) {
        nozzleTipLocator.setPipeline(pipeline);
    }

    private CvPipeline getPipeline() {
        return nozzleTipLocator.getPipeline();
    }
    @Override
    public void editPipeline() throws Exception {
        Logger.warn("This clicked");
        CvPipeline pipeline = getPipeline();
        Camera camera = Configuration.get().getMachine().getDefaultHead().getDefaultCamera();
        pipeline.setProperty("camera", camera);
        pipeline.setProperty("locator", nozzleTipLocator);
        CvPipelineEditor editor = new CvPipelineEditor(pipeline);
        JDialog dialog = new CvPipelineEditorDialog(MainFrame.get(), "Nozzle Tip Locator Pipeline", editor);
        dialog.setVisible(true);
    }
}
