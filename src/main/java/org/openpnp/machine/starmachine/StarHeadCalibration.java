package org.openpnp.machine.starmachine;

import org.apache.commons.io.IOUtils;
import org.opencv.core.KeyPoint;
import org.opencv.core.RotatedRect;
import org.openpnp.gui.MainFrame;
import org.openpnp.machine.reference.ReferenceCamera;
import org.openpnp.machine.reference.ReferenceNozzle;
import org.openpnp.machine.reference.ReferenceNozzleTip;
import org.openpnp.machine.reference.ReferenceNozzleTipCalibration;
import org.openpnp.model.*;
import org.openpnp.spi.Camera;
import org.openpnp.util.MovableUtils;
import org.openpnp.util.OpenCvUtils;
import org.openpnp.util.Utils2D;
import org.openpnp.util.VisionUtils;
import org.openpnp.vision.pipeline.CvPipeline;
import org.openpnp.vision.pipeline.CvStage;
import org.pmw.tinylog.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class StarHeadCalibration extends AbstractModelObject {
    // Properties

    @Attribute(required = false)
    private boolean enabled;
    @Attribute(required = false)
    private double headAngleWindow = 10; // Amount to swing head relative to expected nozzle position
    @Attribute(required = false)
    private double headAngleSubdivisions = 6; // Number of iterations to do
    @Attribute(required = false)
    private double angleStart = -180;
    @Attribute(required = false)
    private double angleStop = 180;
    @Attribute(required = false)
    private int angleSubdivisions = 6;
    @Attribute(required = false)
    private int allowMisdetections = 0;
    @Element(required = false)
    private CvPipeline pipeline = createDefaultPipeline();

    private boolean calibrating;

    // Getters / setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isCalibrating() {
        return calibrating;
    }

    public CvPipeline getPipeline() throws Exception {
        pipeline.setProperty("camera", VisionUtils.getBottomVisionCamera());
        return pipeline;
    }

    public void setPipeline(CvPipeline calibrationPipeline) {
        this.pipeline = calibrationPipeline;
    }

    // Functions

    public void calibrate(StarNozzle nozzle, boolean homing, boolean calibrateCamera) throws Exception {
        if ( !isEnabled() ) {
            return;
        }

        if (!(homing || Configuration.get().getMachine().isHomed())) {
            throw new Exception("Machine not yet homed, head calibration request aborted");
        }

        if (nozzle == null) {
            throw new Exception("Nozzle required to calibrate head.");
        }
        Camera camera = VisionUtils.getBottomVisionCamera();

        ReferenceCamera referenceCamera = null;
        if (camera instanceof ReferenceCamera) {
            referenceCamera = (ReferenceCamera)camera;
        }

        // This is our baseline location. Note: we do not apply the tool specific calibration offset here
        // as this would defy the very purpose of finding a new one here.
        Location cameraLocation = camera.getLocation();
        //Location measureBaseLocation = cameraLocation.derive(null, null, null, 0d)
        //        .add(new Location(this.calibrationZOffset.getUnits(), 0, 0, this.calibrationZOffset.getValue(), 0));

        /*try {
            calibrating = true;
            Location excenter = new Location(measureBaseLocation.getUnits());
            if (! calibrateCamera) {
                reset(nozzle);
            }
            else {
                if (! isCalibrated(nozzle)) {
                    throw new Exception("Calibrate the nozzle tip first.");
                }
                if (referenceCamera == null) {
                    throw new Exception("For calibration the bottom vision camera must be a ReferenceCamera.");
                }
                excenter = VisionUtils.getPixelCenterOffsets(camera,
                        camera.getWidth()/2 + Math.min(camera.getWidth(), camera.getHeight())*excenterRatio,
                        camera.getHeight()/2);
            }

            HashMap<String, Object> params = new HashMap<>();
            params.put("nozzle", nozzle);
            params.put("camera", camera);
            Configuration.get().getScripting().on("HeadCalibration.Starting", params);

            double initialNozzleAngle = nozzle.getNozzlePosition();
            // determine the D angle increments using window size
            double headAngleIncrement = this.headAngleWindow  / this.headAngleSubdivisions;
            // determine the resulting C angle increments
            double angleIncrement = (angleStop - angleStart) / this.angleSubdivisions;
            // determine the number of measurements to be made
            int angleSubdivisions = this.angleSubdivisions;
            if (Math.abs(angleStart + 360 - angleStop) < 0.1) {
                // we're measuring a full circle, the last measurement can be omitted
                angleSubdivisions--;
            }

            for (int i = 0; i <= headAngleSubdivisions; i++) {
                // calc the current measurement-angle
                double measureHeadAngle = initialNozzleAngle - (headAngleWindow / 2) + (i * headAngleIncrement);
                nozzle.setNozzlePosition(measureHeadAngle);
                // move nozzle to the camera location at the start angle - the nozzle must not necessarily be at the center
                // this works because moveTo automatically uses the set nozzle position so we don't have to pass it
                MovableUtils.moveToLocationAtSafeZ(nozzle, measureBaseLocation.derive(null, null, null, angleStart));



                Logger.debug("[HeadCalibration]starting measurement; headAngle: {}, angleStart: {}, angleStop: {}, angleIncrement: {}, angleSubdivisions: {}",
                        measureHeadAngle, angleStart, angleStop, angleIncrement, angleSubdivisions);

                // Capture nozzle tip positions and add them to a list. For these calcs the camera location is considered to be 0/0
                List<double> nozzleTipMeasuredLocations = new ArrayList<>();
                int misdetects = 0;
                for (int j = 0; j <= angleSubdivisions; j++) {
                    // calc the current measurement-angle
                    double measureAngle = angleStart + (j * angleIncrement);

                    Logger.debug("[NozzleCalibration]i: {}, j: {}, measureAngle: {}", i, j, measureAngle);

                    // rotate nozzle to measurement angle
                    Location measureLocation = measureBaseLocation
                            .derive(null, null, null, measureAngle)
                            .add(excenter.rotateXy(measureAngle));
                    nozzle.moveTo(measureLocation);

                    // detect the nozzle tip
                    double diameter = findCircleDiameter(measureLocation);
                    if (diameter != -1) {
                        diameters.add(diameter);
                        Logger.trace("[NozzleCalibration]measured diameter: {}", diameter);
                    } else {
                        misdetects++;
                        if (misdetects > this.allowMisdetections) {
                            throw new Exception("Too many vision misdetects. Check pipeline and threshold.");
                        }
                    }
                }
            }

            if (nozzleTipMeasuredLocations.size() < Math.max(3, angleSubdivisions + 1 - this.allowMisdetections)) {
                throw new Exception("Not enough results from vision. Check pipeline and threshold.");
            }

            Configuration.get().getScripting().on("NozzleCalibration.Finished", params);

            if (!calibrateCamera) {
                switch(this.runoutCompensationAlgorithm) {
                    case ReferenceNozzleTipCalibration.RunoutCompensationAlgorithm.Model:
                        this.setRunoutCompensation(nozzle, new ReferenceNozzleTipCalibration.ModelBasedRunoutCompensation(nozzleTipMeasuredLocations));
                        break;
                    case ReferenceNozzleTipCalibration.RunoutCompensationAlgorithm.ModelAffine:
                        this.setRunoutCompensation(nozzle, new ReferenceNozzleTipCalibration.ModelBasedRunoutCompensation(nozzleTipMeasuredLocations, nozzleTipExpectedLocations));
                        break;
                    case ReferenceNozzleTipCalibration.RunoutCompensationAlgorithm.ModelNoOffset:
                        this.setRunoutCompensation(nozzle, new ReferenceNozzleTipCalibration.ModelBasedRunoutNoOffsetCompensation(nozzleTipMeasuredLocations));
                        break;
                    case ReferenceNozzleTipCalibration.RunoutCompensationAlgorithm.ModelNoOffsetAffine:
                        this.setRunoutCompensation(nozzle, new ReferenceNozzleTipCalibration.ModelBasedRunoutNoOffsetCompensation(nozzleTipMeasuredLocations, nozzleTipExpectedLocations));
                        break;
                    case ReferenceNozzleTipCalibration.RunoutCompensationAlgorithm.ModelCameraOffset:
                        this.setRunoutCompensation(nozzle, new ReferenceNozzleTipCalibration.ModelBasedRunoutCameraOffsetCompensation(nozzleTipMeasuredLocations));
                        break;
                    case ReferenceNozzleTipCalibration.RunoutCompensationAlgorithm.ModelCameraOffsetAffine:
                        this.setRunoutCompensation(nozzle, new ReferenceNozzleTipCalibration.ModelBasedRunoutCameraOffsetCompensation(nozzleTipMeasuredLocations, nozzleTipExpectedLocations));
                        break;
                    default:
                        this.setRunoutCompensation(nozzle, new ReferenceNozzleTipCalibration.TableBasedRunoutCompensation(nozzleTipMeasuredLocations));
                        break;
                }
            }
            else {
                if ((this.runoutCompensationAlgorithm == ReferenceNozzleTipCalibration.RunoutCompensationAlgorithm.ModelAffine) ||
                        (this.runoutCompensationAlgorithm == ReferenceNozzleTipCalibration.RunoutCompensationAlgorithm.ModelNoOffsetAffine) ||
                        (this.runoutCompensationAlgorithm == ReferenceNozzleTipCalibration.RunoutCompensationAlgorithm.ModelCameraOffsetAffine)) {
                    //This camera alignment stuff should be moved out of nozzle tip calibration
                    //and placed with the rest of the camera setup stuff
                    AffineTransform at = Utils2D.deriveAffineTransform(nozzleTipMeasuredLocations, nozzleTipExpectedLocations);
                    Utils2D.AffineInfo ai = Utils2D.affineInfo(at);
                    Logger.debug("[nozzleTipCalibration]bottom camera affine transform: " + ai);
                    Location newCameraPosition = new Location(LengthUnit.Millimeters, ai.xTranslation, ai.yTranslation, 0, 0);
                    newCameraPosition = referenceCamera.getHeadOffsets().derive(newCameraPosition, true, true, false, false);
                    Logger.debug("[nozzleTipCalibration]applying axis offset to bottom camera position: {} - {} = {}",
                            referenceCamera.getHeadOffsets(),
                            referenceCamera.getHeadOffsets().subtract(newCameraPosition),
                            newCameraPosition);
                    referenceCamera.setHeadOffsets(newCameraPosition);
                    double newCameraAngle = referenceCamera.getRotation() - ai.rotationAngleDeg;
                    Logger.debug("[nozzleTipCalibration]applying angle offset to bottom camera rotation: {} - {} = {}",
                            referenceCamera.getRotation(),
                            ai.rotationAngleDeg,
                            newCameraAngle);
                    referenceCamera.setRotation(newCameraAngle);
                } else {
                    ReferenceNozzleTipCalibration.ModelBasedRunoutCompensation cameraCompensation = new ReferenceNozzleTipCalibration.ModelBasedRunoutCompensation(nozzleTipMeasuredLocations);
                    Location newCameraPosition = referenceCamera.getHeadOffsets()
                            .subtract(cameraCompensation.getAxisOffset());
                    Logger.debug("[nozzleTipCalibration]applying axis offset to bottom camera position: {} - {} = {}",
                            referenceCamera.getHeadOffsets(),
                            cameraCompensation.getAxisOffset(),
                            newCameraPosition);
                    referenceCamera.setHeadOffsets(newCameraPosition);
                    // Calculate and apply the new angle
                    double newCameraAngle = referenceCamera.getRotation() - cameraCompensation.getPhaseShift();
                    Logger.debug("[nozzleTipCalibration]applying angle offset to bottom camera rotation: {} - {} = {}",
                            referenceCamera.getRotation(),
                            cameraCompensation.getPhaseShift(),
                            newCameraAngle);
                    referenceCamera.setRotation(newCameraAngle);
                }
            }
        }
        finally {
            // go to camera position (now offset-corrected). prevents the user from being irritated if it's not exactly centered
            nozzle.moveTo(camera.getLocation(nozzle).derive(null, null, measureBaseLocation.getZ(), angleStop));

            // after processing the nozzle returns to safe-z
            nozzle.moveToSafeZ();

            // setting to false in the very end to prevent endless calibration repetitions if calibration was not successful (pipeline not well or similar) and the nozzle is commanded afterwards somewhere else (where the calibration is asked for again ...)
            calibrating = false;
        }*/
    }
    // Image pipeline functions
    private double findCircleDiameter(Location measureLocation) throws Exception {
        Camera camera = VisionUtils.getBottomVisionCamera();
        try (CvPipeline pipeline = getPipeline()) {
            pipeline.setProperty("camera", camera);
            Point maskCenter = VisionUtils.getLocationPixels(camera, measureLocation);
            pipeline.setProperty("MaskCircle.center", new org.opencv.core.Point(maskCenter.getX(), maskCenter.getY()));

            pipeline.process();
            ArrayList<Double> diameters = new ArrayList<>();

            String stageName = VisionUtils.PIPELINE_RESULTS_NAME;
            CvStage.Result pipelineResult = pipeline.getResult(stageName);
            if (pipelineResult == null) {
                throw new Exception(String.format("There should be a \"%s\" stage in the pipeline.", stageName));
            }

            Object results = pipelineResult.model;

            if (results instanceof Exception) {
                throw (Exception)results;
            }

            //show result from pipeline in camera view, but only if GUI is present (not so in UnitTests).
            MainFrame mainFrame = MainFrame.get();
            if (mainFrame != null) {
                mainFrame.getCameraViews().getCameraView(camera).showFilteredImage(
                        OpenCvUtils.toBufferedImage(pipeline.getWorkingImage()), 1000);
            }

            // add all results from pipeline to a Location-list post processing
            if (results instanceof List) {
                // are there any results from the pipeline?
                if (0==((List) results).size()) {
                    // Don't throw new Exception("No results from vision. Check pipeline.");
                    // Instead the number of obtained fixes is evaluated later.
                    return -1;
                }
                for (Object result : (List) results) {
                    if ((result) instanceof CvStage.Result.Circle) {
                        CvStage.Result.Circle circle = ((CvStage.Result.Circle) result);
                        diameters.add(circle.diameter);
                    }
                    // Not sure what to make of these below
                    else if ((result) instanceof KeyPoint) {
                        KeyPoint keyPoint = ((KeyPoint) result);
                        //locations.add(VisionUtils.getPixelCenterOffsets(camera, keyPoint.pt.x, keyPoint.pt.y));
                    }
                    else if ((result) instanceof RotatedRect) {
                        RotatedRect rect = ((RotatedRect) result);
                        //locations.add(VisionUtils.getPixelCenterOffsets(camera, rect.center.x, rect.center.y));
                    }
                    else {
                        throw new Exception("Unrecognized result " + result);
                    }
                }
            }

            // Potentially remove bad points here - use zscore threshold

            // check for a valid resultset
            if (diameters.size() == 0) {
                // Don't throw new Exception("No valid results from pipeline within threshold");
                // Instead the number of obtained fixes is evaluated later.
                return -1;
            } else if (diameters.size() > 1) {
                // Don't throw an exception here either. Since we've gotten more results than expected we can't be
                // sure which, if any, are the correct result so just discard them all and log an info message.
                //Logger.info("[nozzleTipCalibration]Got more than one result from pipeline. For best performance tweak pipeline to return exactly one result only. Discarding all locations (since it is unknown which may be correct) from the following set: " + locations);
                return -1;
            }

            // finally return the location at index (0) which is either a) the only one or b) the one best matching the nozzle tip
            return diameters.get(0);
        }
        finally {
            pipeline.setProperty("MaskCircle.center", null);
        }
    }

    public static CvPipeline createDefaultPipeline() {
        try {
            String xml = IOUtils.toString(ReferenceNozzleTip.class
                    .getResource("ReferenceNozzleTip-Calibration-DefaultPipeline.xml"));
            return new CvPipeline(xml);
        }
        catch (Exception e) {
            throw new Error(e);
        }
    }

    public void resetPipeline() {
        pipeline = createDefaultPipeline();
    }

}
