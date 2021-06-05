package org.openpnp.machine.starmachine.wizards;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.openpnp.gui.MainFrame;
import org.openpnp.gui.components.ComponentDecorators;
import org.openpnp.gui.support.*;
import org.openpnp.machine.reference.ReferenceNozzle;
import org.openpnp.machine.reference.ReferenceNozzleTipCalibration;
import org.openpnp.machine.starmachine.StarNozzle;
import org.openpnp.machine.starmachine.StarNozzleTip;
import org.openpnp.machine.starmachine.StarNozzleTipCalibration;
import org.openpnp.model.Configuration;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.model.Motion;
import org.openpnp.model.Point;
import org.openpnp.spi.Camera;
import org.openpnp.spi.HeadMountable;
import org.openpnp.util.*;
import org.openpnp.vision.FluentCv;
import org.pmw.tinylog.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StarNozzleOffsetWizard extends AbstractConfigurationWizard {

    private JComboBox<CameraItem> camerasComboBox;

    private JTextField nozzleLocationX, nozzleLocationY, nozzleLocationZ, nozzleLocationD;
    private JTextField nozzleOffsetLocationX, nozzleOffsetLocationY, nozzleOffsetLocationZ;
    private JCheckBox chckbxIncludeZ;
    private JLabel nozzleLocationZLabel;
    private JLabel nozzleOffsetZLabel;

    private StarNozzle nozzle;
    private MutableLocationProxy nozzleMarkLocation, nozzleOffsetLocation;

    public StarNozzleOffsetWizard(StarNozzle nozzle) {
        this.nozzle = nozzle;
        this.nozzleMarkLocation = new MutableLocationProxy();
        this.nozzleOffsetLocation = new MutableLocationProxy();

        JPanel visionPanel = new JPanel();
        visionPanel.setBorder(new TitledBorder(null, "1. Vision", TitledBorder.LEADING, TitledBorder.TOP, null, null));

        visionPanel.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"),},
                new RowSpec[] {
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC,
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
                        FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,})
        );


        JLabel step1Label = new JLabel("Select the up looking camera to view the nozzle.");
        visionPanel.add(step1Label, "2, 7");

        // We need to know through which camera we are doing the wizard, only relevant if there is more than one
        camerasComboBox = new JComboBox<>();

        List<Camera> cameraList = Configuration.get().getMachine().getCameras();
        for (Camera camera : cameraList) {
            camerasComboBox.addItem(new CameraItem(camera));
        }

        if(cameraList.size() == 1) {
            camerasComboBox.setSelectedIndex(0);
        }

        visionPanel.add(camerasComboBox, "2, 9");

        JPanel nozzlePositionPanel = new JPanel();
        nozzlePositionPanel.setBorder(new TitledBorder(null, "Nozzle position", TitledBorder.LEADING, TitledBorder.TOP, null, null));

        nozzlePositionPanel.setLayout(new FormLayout(new ColumnSpec[] {
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
                        FormSpecs.DEFAULT_ROWSPEC,})
        );

        JLabel nozzleLocationXLabel = new JLabel("X");
        nozzlePositionPanel.add(nozzleLocationXLabel, "2, 2");

        JLabel nozzleLocationYLabel = new JLabel("Y");
        nozzlePositionPanel.add(nozzleLocationYLabel, "4, 2");

        JLabel nozzleLocationZLabel = new JLabel("Z");
        nozzlePositionPanel.add(nozzleLocationZLabel, "6, 2");

        nozzleLocationX = new JTextField();
        nozzlePositionPanel.add(nozzleLocationX, "2, 4");
        nozzleLocationX.setColumns(10);

        nozzleLocationY = new JTextField();
        nozzlePositionPanel.add(nozzleLocationY, "4, 4");
        nozzleLocationY.setColumns(10);

        nozzleLocationZ = new JTextField();
        nozzlePositionPanel.add(nozzleLocationZ, "6, 4");
        nozzleLocationZ.setColumns(10);

        JButton buttonCenterTool = new JButton(positionToolAction);
        buttonCenterTool.setHideActionText(true);
        nozzlePositionPanel.add(buttonCenterTool,"8,4");

        JLabel nozzleLocationDLabel = new JLabel("D");
        nozzlePositionPanel.add(nozzleLocationDLabel, "4, 6");

        nozzleLocationD = new JTextField();
        nozzlePositionPanel.add(nozzleLocationD, "4, 8");
        nozzleLocationD.setColumns(10);

        JButton counterclockwiseButton = new JButton(dNegativeJogAction);
        counterclockwiseButton.setHideActionText(true);
        nozzlePositionPanel.add(counterclockwiseButton, "2, 8"); //$NON-NLS-1$

        JButton clockwiseButton = new JButton(dPositiveJogAction);
        clockwiseButton.setHideActionText(true);
        nozzlePositionPanel.add(clockwiseButton, "6, 8"); //$NON-NLS-1$

        visionPanel.add(nozzlePositionPanel, "2, 25");

        contentPanel.add(visionPanel);

        JButton calibrateButton = new JButton("Calibrate");
        calibrateButton.setAction(calibrateAction);
        visionPanel.add(calibrateButton, "2, 27");

        initDataBindings();
    }


    @SuppressWarnings("serial")
    public Action dNegativeJogAction = new AbstractAction("D-", Icons.rotateCounterclockwise) { //$NON-NLS-1$
        @Override
        public void actionPerformed(ActionEvent arg0) {
            UiUtils.submitUiMachineTask(() -> {
                // Need to check this for safety
                nozzle.setNozzlePosition(nozzle.getNozzlePosition() - 1);
                nozzle.moveTo(nozzle.getLocation(), Motion.MotionOption.JogMotion);
            });
        }
    };
    @SuppressWarnings("serial")
    public Action dPositiveJogAction = new AbstractAction("D+", Icons.rotateClockwise) { //$NON-NLS-1$
        @Override
        public void actionPerformed(ActionEvent arg0) {
            UiUtils.submitUiMachineTask(() -> {
                nozzle.setNozzlePosition(nozzle.getNozzlePosition() + 1);
                nozzle.moveTo(nozzle.getLocation(), Motion.MotionOption.JogMotion);
            });
        }
    };

    private Action calibrateAction = new AbstractAction("Calibrate") {
        @Override
        public void actionPerformed(ActionEvent e) {
            calibrate();
        }
    };

    private Action applyNozzleOffsetAction = new AbstractAction("Calculate nozzle offset") {
        @Override
        public void actionPerformed(ActionEvent e) {
            CameraItem selectedCameraItem = (CameraItem) camerasComboBox.getSelectedItem();

            if(selectedCameraItem != null) {
                LengthConverter lengthConverter = new LengthConverter();
                Camera currentCamera = selectedCameraItem.getCamera();
                Location cameraLocation = currentCamera.getLocation();
                Location headOffsets = cameraLocation.subtract(nozzleMarkLocation.getLocation());
                if (! chckbxIncludeZ.isSelected()) {
                    // keep the old Z offset
                    headOffsets = headOffsets.derive(nozzle.getHeadOffsets(), false, false, true, false);
                }
                nozzleOffsetLocationX.setText(lengthConverter.convertForward(headOffsets.getLengthX()));
                nozzleOffsetLocationY.setText(lengthConverter.convertForward(headOffsets.getLengthY()));
                nozzleOffsetLocationZ.setText(lengthConverter.convertForward(headOffsets.getLengthZ()));
                Logger.info("Nozzle offset wizard set head offset to location: " + headOffsets.toString());
            }
        }
    };
    private Action positionToolAction = new AbstractAction("Position Tool", Icons.centerTool) {
        {
            putValue(Action.SHORT_DESCRIPTION,
                    "Position the tool over the selected camera.");
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            UiUtils.submitUiMachineTask(() -> {
                HeadMountable tool = nozzle;
                CameraItem selectedCameraItem = (CameraItem) camerasComboBox.getSelectedItem();
                Camera camera = selectedCameraItem.getCamera();
                Location location = camera.getLocation();
                MovableUtils.moveToLocationAtSafeZ(tool, location);
            });
        }
    };


    @Override
    public void createBindings() {
        LengthConverter lengthConverter = new LengthConverter();

        nozzleMarkLocation.setLocation(new Location(nozzle.getHeadOffsets().getUnits()));
        /*addWrappedBinding(nozzleMarkLocation, "lengthX", nozzleMarkLocationX, "text", lengthConverter);
        addWrappedBinding(nozzleMarkLocation, "lengthY", nozzleMarkLocationY, "text", lengthConverter);
        addWrappedBinding(nozzleMarkLocation, "lengthZ", nozzleMarkLocationZ, "text", lengthConverter);
*/
        MutableLocationProxy headOffsets = new MutableLocationProxy();
        bind(UpdateStrategy.READ_WRITE, nozzle, "headOffsets", nozzleOffsetLocation, "location");
        addWrappedBinding(nozzleOffsetLocation, "lengthX", nozzleOffsetLocationX, "text", lengthConverter);
        addWrappedBinding(nozzleOffsetLocation, "lengthY", nozzleOffsetLocationY, "text", lengthConverter);
        addWrappedBinding(nozzleOffsetLocation, "lengthZ", nozzleOffsetLocationZ, "text", lengthConverter);
/*
        ComponentDecorators.decorateWithAutoSelectAndLengthConversion(nozzleMarkLocationX);
        ComponentDecorators.decorateWithAutoSelectAndLengthConversion(nozzleMarkLocationY);
        ComponentDecorators.decorateWithAutoSelectAndLengthConversion(nozzleMarkLocationZ);*/
        ComponentDecorators.decorateWithAutoSelectAndLengthConversion(nozzleOffsetLocationX);
        ComponentDecorators.decorateWithAutoSelectAndLengthConversion(nozzleOffsetLocationY);
        ComponentDecorators.decorateWithAutoSelectAndLengthConversion(nozzleOffsetLocationZ);
    }
    protected void initDataBindings() {
        BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
        BeanProperty<JLabel, Boolean> jLabelBeanProperty = BeanProperty.create("visible");
        //AutoBinding<JCheckBox, Boolean, JLabel, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, chckbxIncludeZ, jCheckBoxBeanProperty, nozzleMarkLocationZLabel, jLabelBeanProperty);
        //autoBinding.bind();
        //
        //BeanProperty<JTextField, Boolean> jTextFieldBeanProperty = BeanProperty.create("visible");
        //AutoBinding<JCheckBox, Boolean, JTextField, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, chckbxIncludeZ, jCheckBoxBeanProperty, nozzleMarkLocationZ, jTextFieldBeanProperty);
        //autoBinding_1.bind();
        //
        //AutoBinding<JCheckBox, Boolean, JLabel, Boolean> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, chckbxIncludeZ, jCheckBoxBeanProperty, nozzleOffsetZLabel, jLabelBeanProperty);
        //autoBinding_2.bind();
        //
        //AutoBinding<JCheckBox, Boolean, JTextField, Boolean> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ, chckbxIncludeZ, jCheckBoxBeanProperty, nozzleOffsetLocationZ, jTextFieldBeanProperty);
        //autoBinding_5.bind();
    }

    private void calibrate() {
        UiUtils.submitUiMachineTask(() -> {
            StarNozzleTipCalibration calibration = nozzle.getNozzleTip().getCalibration();
            CameraItem selectedCameraItem = (CameraItem) camerasComboBox.getSelectedItem();
            Camera camera = selectedCameraItem.getCamera();
            StarNozzleTipCalibration.RunoutCompensation model;
            Location cameraLocation = selectedCameraItem.getCamera().getLocation().derive(null, null, null, 0d);
            nozzle.setHeadOffsets(nozzle.getHeadOffsets().add(nozzle.getLocation().derive(null, null, null, 0d)).subtract(cameraLocation));
            model = calibration.generateModel(nozzle, selectedCameraItem.getCamera(), false, -180, 180,6, 3, StarNozzleTipCalibration.RunoutCompensationAlgorithm.Model);

            nozzle.setHeadOffsets(nozzle.getHeadOffsets().add(model.getAxisOffset()));
            nozzle.moveTo(selectedCameraItem.getCamera().getLocation());
            List<Location> averageOffsets = new ArrayList<>();

            double window = 6, segments = 4;
            double start = nozzle.getNozzlePosition() - (window / 2.0);
            List<Double> positions = new ArrayList<>();
            List<Double> distances = new ArrayList<>();
            boolean right = false;
            for (int i = 0; i < segments; i++) {
                double angle = start + (i * (window / segments));
                nozzle.setNozzlePosition(angle);
                if (right) {
                    model = calibration.generateModel(nozzle, camera, false, -180, 180, 6, 3, StarNozzleTipCalibration.RunoutCompensationAlgorithm.Model);
                } else {
                    model = calibration.generateModel(nozzle, camera, false, 180, -180, 6, 3, StarNozzleTipCalibration.RunoutCompensationAlgorithm.Model);
                }
                right = !right;
                positions.add(angle);
                distances.add(((StarNozzleTipCalibration.ModelBasedRunoutCompensation)model).getRadius().getValue());
                averageOffsets.add(model.getAxisOffset());
                Logger.warn("calibrate: Logged point {}, {}",angle,((StarNozzleTipCalibration.ModelBasedRunoutCompensation)model).getRadius().getValue());

                //show result from calibration in camera view, but only if GUI is present (not so in UnitTests).
                MainFrame mainFrame = MainFrame.get();
                if (mainFrame != null) {

                    // Draw the result onto the pipeline image.
                    Mat image = OpenCvUtils.toMat(camera.capture());

                    /*if (Logger.getLevel() == org.pmw.tinylog.Level.DEBUG || Logger.getLevel() == org.pmw.tinylog.Level.TRACE) {
                        File file = Configuration.get().createResourceFile(getClass(), "push-pull-feeder", ".png");
                        Imgcodecs.imwrite(file.getAbsolutePath(), image);
                    }*/
                    // Draw nozzle locations
                    /*for (int j = 0; j < averageOffsets.size(); j++) {
                        Point pos = VisionUtils.getLocationPixels(camera,averageOffsets.get(j));
                        Imgproc.circle(image, new org.opencv.core.Point(pos.getX(),pos.getY()), 20, FluentCv.colorToScalar(Color.red),5);
                    }*/
                    Point lastPoint = null;
                    for (int j = 0; j < 360; j += 10) {

                        Point point = VisionUtils.getLocationPixels(camera,camera,camera.getLocation().derive(null, null, null, 0d).add(model.getOffset(j)));
                        if (lastPoint != null) {
                            Imgproc.line(image,new org.opencv.core.Point(lastPoint.getX(),lastPoint.getY()), new org.opencv.core.Point(point.getX(),point.getY()), FluentCv.colorToScalar(Color.magenta), 5);
                        }
                        lastPoint = point;
                    }
                    // Draw radius
                    Point pos = VisionUtils.getLocationPixels(camera,camera.getLocation().add(model.getAxisOffset()));
                    int radius = (int)((StarNozzleTipCalibration.ModelBasedRunoutCompensation) model).getRadius().getValue();
                    Imgproc.circle(image, new org.opencv.core.Point(pos.getX(),pos.getY()), radius, FluentCv.colorToScalar(Color.blue),5);

                    BufferedImage showResult = OpenCvUtils.toBufferedImage(image);
                    image.release();
                    MainFrame.get().getCameraViews().getCameraView(camera)
                            .showFilteredImage(showResult, 500);
                }
            }
            Logger.warn("Positions: {}, errors: {}",positions.size(),distances.size());
            for (int i = 0; i < positions.size(); i++) {
                Logger.warn("StarNozzleOffsetWizard: Angle {} - Distance {}",positions.get(i),distances.get(i));
            }
            List<Double> result = Utils2D.polynomialRegression(positions,distances,2);
            Logger.warn("StarNozzleOffsetWizard: Calculated coefficients {}, {}, {}",result.get(0),result.get(1),result.get(2));
            double position = -result.get(1) / (2*result.get(2));
            Logger.trace("StarNozzleOffsetWizard: Calculated D angle: {}",position);
            nozzle.setNozzlePosition(position);
            double xx = 0, yy = 0;
            for (int i = 0; i < averageOffsets.size(); i++) {
                Location tmp = averageOffsets.get(i);
                xx += tmp.getX();
                yy += tmp.getY();
            }
            // Remove this
            Location headOffsets = nozzle.getHeadOffsets().add(new Location(LengthUnit.Millimeters,xx / (double)averageOffsets.size(),yy / (double)averageOffsets.size(),0,0));
            nozzle.setHeadOffsets(nozzle.getHeadOffsets().add(new Location(LengthUnit.Millimeters,xx / (double)averageOffsets.size(),yy / (double)averageOffsets.size(),0,0)));
            nozzle.moveTo(selectedCameraItem.getCamera().getLocation());

            LengthConverter lengthConverter = new LengthConverter();
            nozzleOffsetLocationX.setText(lengthConverter.convertForward(headOffsets.getLengthX()));
            nozzleOffsetLocationY.setText(lengthConverter.convertForward(headOffsets.getLengthX()));
            nozzleOffsetLocationZ.setText(lengthConverter.convertForward(headOffsets.getLengthX()));
        });
    }
}
