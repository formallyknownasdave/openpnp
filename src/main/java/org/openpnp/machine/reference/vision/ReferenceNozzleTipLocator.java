package org.openpnp.machine.reference.vision;

import org.apache.commons.io.IOUtils;
import org.opencv.core.KeyPoint;
import org.opencv.core.RotatedRect;
import org.openpnp.gui.MainFrame;
import org.openpnp.gui.support.Icons;
import org.openpnp.gui.support.PropertySheetWizardAdapter;
import org.openpnp.gui.support.Wizard;
import org.openpnp.machine.reference.vision.wizards.ReferenceNozzleTipLocatorConfigurationWizard;
import org.openpnp.model.*;
import org.openpnp.spi.*;
import org.openpnp.spi.base.AbstractMachine;
import org.openpnp.spi.base.AbstractNozzleTipLocator;
import org.openpnp.util.OpenCvUtils;
import org.openpnp.util.VisionUtils;
import org.openpnp.vision.pipeline.CvPipeline;
import org.openpnp.vision.pipeline.CvStage;
import org.pmw.tinylog.Logger;
import org.simpleframework.xml.Element;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class ReferenceNozzleTipLocator extends AbstractNozzleTipLocator {

    @Element(required = false)
    protected CvPipeline pipeline = createDefaultPipeline();

    // Getters / Setters
    public CvPipeline getPipeline() {
        return pipeline;
    }

    public void setPipeline(CvPipeline pipeline) {
        this.pipeline = pipeline;
    }

    // Functions
    public static CvPipeline createDefaultPipeline() {
        try {
            String xml = IOUtils.toString(ReferenceNozzleTipLocator.class.getResource(
                    "ReferenceNozzleTipLocator-DefaultPipeline.xml"));
            return new CvPipeline(xml);
        }
        catch (Exception e) {
            throw new Error(e);
        }
    }
    private void showPreview(Camera camera, CvPipeline pipeline, long duration) {
        //show result from pipeline in camera view, but only if GUI is present (not so in UnitTests).
        MainFrame mainFrame = MainFrame.get();
        if (mainFrame != null) {
            mainFrame.getCameraViews().getCameraView(camera).showFilteredImage(
                    OpenCvUtils.toBufferedImage(pipeline.getWorkingImage()), duration);
        }
    }
    public NozzleTipLocator.NozzleTipAlignmentOffset findOffsets(NozzleTip tip, Location location, Nozzle nozzle) throws Exception {
        if (nozzle == null) {
            throw new Exception("No nozzle.");
        }
        if (tip == null) {
            throw new Exception("No Nozzle tip.");
        }

        // Need better than this TODO
        Camera camera = VisionUtils.getBottomVisionCamera();

        // Initialize properties of pipeline
        pipeline.setProperty("camera", camera);
        Point maskCenter = VisionUtils.getLocationPixels(camera, location);
        pipeline.setProperty("MaskCircle.center", new org.opencv.core.Point(maskCenter.getX(), maskCenter.getY()));

        pipeline.process();
        List<Location> locations = new ArrayList<>();

        String stageName = VisionUtils.PIPELINE_RESULTS_NAME;
        CvStage.Result pipelineResult = pipeline.getResult(stageName);
        if (pipelineResult == null) {
            throw new Exception(String.format("There should be a \"%s\" stage in the pipeline.", stageName));
        }

        Object results = pipelineResult.model;

        if (results instanceof Exception) {
            throw (Exception)results;
        }

        // show result from pipeline in camera view
        showPreview(camera, pipeline, 1000);

        // add all results from pipeline to a Location-list post processing
        if (results instanceof List) {
            // are there any results from the pipeline?
            if (0==((List) results).size()) {
                // Don't throw new Exception("No results from vision. Check pipeline.");
                // Instead the number of obtained fixes is evaluated later.
                return null;
            }
            for (Object result : (List) results) {
                if ((result) instanceof CvStage.Result.Circle) {
                    CvStage.Result.Circle circle = ((CvStage.Result.Circle) result);
                    locations.add(VisionUtils.getPixelCenterOffsets(camera, circle.x, circle.y));
                }
                else if ((result) instanceof KeyPoint) {
                    KeyPoint keyPoint = ((KeyPoint) result);
                    locations.add(VisionUtils.getPixelCenterOffsets(camera, keyPoint.pt.x, keyPoint.pt.y));
                }
                else if ((result) instanceof RotatedRect) {
                    RotatedRect rect = ((RotatedRect) result);
                    locations.add(VisionUtils.getPixelCenterOffsets(camera, rect.center.x, rect.center.y));
                }
                else {
                    throw new Exception("Unrecognized result " + result);
                }
            }
        }
        else if ((results) instanceof CvStage.Result.Circle) {
            CvStage.Result.Circle circle = ((CvStage.Result.Circle) results);
            locations.add(VisionUtils.getPixelCenterOffsets(camera, circle.x, circle.y));
        }
        else if ((results) instanceof KeyPoint) {
            KeyPoint keyPoint = ((KeyPoint) results);
            locations.add(VisionUtils.getPixelCenterOffsets(camera, keyPoint.pt.x, keyPoint.pt.y));
        }
        else if ((results) instanceof RotatedRect) {
            RotatedRect rect = ((RotatedRect) results);
            locations.add(VisionUtils.getPixelCenterOffsets(camera, rect.center.x, rect.center.y));
        }
        else {
            throw new Exception("Unrecognized result " + results);
        }
        // remove all results that are above threshold
        /*Iterator<Location> locationsIterator = locations.iterator();
        while (locationsIterator.hasNext()) {
            Location tmpLocation = locationsIterator.next();
            Location measureLocationRelative = location.convertToUnits(location.getUnits()).
                    subtract(camera.getLocation());
            double convertedThreshold = threshold.convertToUnits(location.getUnits()).getValue();
            if (location.getLinearDistanceTo(measureLocationRelative) > convertedThreshold) {
                locationsIterator.remove();
                Logger.trace("[findOffsets]Removed offset location {} from results; measured distance {} exceeds offsetThresholdLength {}", location, location.getLinearDistanceTo(0., 0.), threshold);
            }
        }*/

        // check for a valid resultset
        if (locations.size() == 0) {
            // Don't throw new Exception("No valid results from pipeline within threshold");
            // Instead the number of obtained fixes is evaluated later.
            return null;
        } else if (locations.size() > 1) {
            // Don't throw an exception here either. Since we've gotten more results than expected we can't be
            // sure which, if any, are the correct result so just discard them all and log an info message.
            Logger.info("[findOffsets]Got more than one result from pipeline. For best performance tweak pipeline to return exactly one result only. Discarding all locations (since it is unknown which may be correct) from the following set: " + locations);
            return null;
        }

        // finally return the location at index (0) which is either a) the only one or b) the one best matching the nozzle tip
        return new NozzleTipLocator.NozzleTipAlignmentOffset(locations.get(0));
    }
    public Wizard getConfigurationWizard() {
        return new ReferenceNozzleTipLocatorConfigurationWizard(this);
    }

    public String getPropertySheetHolderTitle() {
        return getClass().getSimpleName() + " " + getName();
    }

    public PropertySheetHolder[] getChildPropertySheetHolders() {
        return null;
    }

    @Override
    public PropertySheetHolder.PropertySheet[] getPropertySheets() {
        return new PropertySheetHolder.PropertySheet[] {
                new PropertySheetWizardAdapter(getConfigurationWizard())
        };
    }

    public Action[] getPropertySheetHolderActions() {
        return new Action[] {deleteAction};
    }

    @Override
    public Icon getPropertySheetHolderIcon() {
        return null;
    }

    public Action deleteAction = new AbstractAction("Delete Nozzle Tip Locator") {
        {
            putValue(SMALL_ICON, Icons.nozzleTipRemove);
            putValue(NAME, "Delete Nozzle Tip Locator");
            putValue(SHORT_DESCRIPTION, "Delete the currently selected nozzle tip locator.");
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            int ret = JOptionPane.showConfirmDialog(MainFrame.get(),
                    "Are you sure you want to delete " + getName() + "?",
                    "Delete " + getName() + "?", JOptionPane.YES_NO_OPTION);
            if (ret == JOptionPane.YES_OPTION) {
                Configuration.get().getMachine().removeNozzleTipLocator(ReferenceNozzleTipLocator.this);
            }
        }
    };

}
