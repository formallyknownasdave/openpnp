package org.openpnp.machine.starmachine;

import com.sun.xml.bind.annotation.OverrideAnnotationOf;
import org.openpnp.ConfigurationListener;
import org.openpnp.gui.MainFrame;
import org.openpnp.gui.support.Icons;
import org.openpnp.gui.support.MessageBoxes;
import org.openpnp.gui.support.PropertySheetWizardAdapter;
import org.openpnp.gui.support.Wizard;
import org.openpnp.machine.reference.*;
import org.openpnp.machine.reference.ReferenceNozzle;
import org.openpnp.machine.reference.axis.ReferenceControllerAxis;
import org.openpnp.machine.reference.wizards.*;
import org.openpnp.machine.starmachine.wizards.StarNozzleCompatibleNozzleTipsWizard;
import org.openpnp.machine.starmachine.wizards.StarNozzleConfigurationWizard;
import org.openpnp.machine.starmachine.wizards.StarNozzleOffsetWizard;
import org.openpnp.model.*;
import org.openpnp.spi.*;
import org.openpnp.spi.base.AbstractAxis;
import org.openpnp.spi.base.AbstractMachine;
import org.openpnp.spi.base.AbstractNozzle;
import org.openpnp.spi.base.AbstractTransformedAxis;
import org.openpnp.util.MovableUtils;
import org.openpnp.util.SimpleGraph;
import org.pmw.tinylog.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.core.Commit;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public class StarNozzle extends AbstractNozzle implements ReferenceHeadMountable {
    @Element
    private Location headOffsets = new Location(LengthUnit.Millimeters);

    @Attribute(required = false)
    private int pickDwellMilliseconds;

    @Attribute(required = false)
    private double nozzlePosition = 0.0;

    @Attribute(required = false)
    private int placeDwellMilliseconds;

    @Attribute(required = false)
    private String currentNozzleTipId;

    @Attribute(required = false)
    private boolean changerEnabled = false;

    @Attribute(required = false)
    private boolean nozzleTipChangedOnManualFeed = false;

    @Element(required = false)
    private Location manualNozzleTipChangeLocation = new Location(LengthUnit.Millimeters);

    @Deprecated
    @Element(required = false)
    protected Length safeZ = null;

    @Attribute(required = false)
    private boolean enableDynamicSafeZ = false;

    @Element(required = false)
    protected String vacuumSenseActuatorName = ".migrate";

    @Element(required = false)
    protected String vacuumActuatorName = ".migrate";

    @Element(required = false)
    protected String blowOffActuatorName;

    /**
     * If limitRotation is enabled the nozzle will reverse directions when commanded to rotate past
     * 180 degrees. So, 190 degrees becomes -170 and -190 becomes 170.
     */
    @Deprecated
    @Attribute(required = false)
    private boolean limitRotation = true;

    protected StarNozzleTip nozzleTip;

    public StarNozzle() {
        Configuration.get().addListener(new ConfigurationListener.Adapter() {
            @Override
            public void configurationLoaded(Configuration configuration) throws Exception {
                nozzleTip = (StarNozzleTip) configuration.getMachine().getNozzleTip(currentNozzleTipId);
            }
        });
    }

    public double getNozzlePosition() {
        return nozzlePosition;
    }

    public void setNozzlePosition(double nozzlePosition) {
        this.nozzlePosition = nozzlePosition;
    }
    @Commit
    public void commit() {
        // Migration of these has gone back and forth, cumbersome resolution needed.
        if (vacuumSenseActuatorName.equals(".migrate")) {
            if (vacuumActuatorName.equals(".migrate")) {
                vacuumActuatorName = null;
            }
            vacuumSenseActuatorName = vacuumActuatorName;
        }
        else if (vacuumActuatorName.equals(".migrate")) {
            vacuumActuatorName = vacuumSenseActuatorName;
        }
    }

    public StarNozzle(String id) {
        this();
        this.id = id;
    }

    @Deprecated
    public boolean isLimitRotation() {
        return limitRotation;
    }

    public boolean isEnableDynamicSafeZ() {
        return enableDynamicSafeZ;
    }

    public void setEnableDynamicSafeZ(boolean enableDynamicSafeZ) {
        this.enableDynamicSafeZ = enableDynamicSafeZ;
    }

    public int getPickDwellMilliseconds() {
        return pickDwellMilliseconds;
    }

    public void setPickDwellMilliseconds(int pickDwellMilliseconds) {
        this.pickDwellMilliseconds = pickDwellMilliseconds;
    }

    public int getPlaceDwellMilliseconds() {
        return placeDwellMilliseconds;
    }

    public void setPlaceDwellMilliseconds(int placeDwellMilliseconds) {
        this.placeDwellMilliseconds = placeDwellMilliseconds;
    }

    @Override
    public Location getHeadOffsets() {
        return headOffsets;
    }

    @Override
    public void setHeadOffsets(Location headOffsets) {
        Object oldValue = this.headOffsets;
        this.headOffsets = headOffsets;
        firePropertyChange("headOffsets", oldValue, headOffsets);
        // Changing a head offset invalidates the nozzle tip calibration.
        ReferenceNozzleTipCalibration.resetAllNozzleTips();
    }

    public String getVacuumSenseActuatorName() {
        return vacuumSenseActuatorName;
    }

    public void setVacuumSenseActuatorName(String vacuumSenseActuatorName) {
        this.vacuumSenseActuatorName = vacuumSenseActuatorName;
    }

    public String getVacuumActuatorName() {
        return vacuumActuatorName;
    }

    public void setVacuumActuatorName(String vacuumActuatorName) {
        this.vacuumActuatorName = vacuumActuatorName;
    }

    public String getBlowOffActuatorName() {
        return blowOffActuatorName;
    }

    public void setBlowOffActuatorName(String blowActuatorName) {
        this.blowOffActuatorName = blowActuatorName;
    }

    @Override
    public StarNozzleTip getNozzleTip() {
        return nozzleTip;
    }

    public String getNozzleTipId() {
        return currentNozzleTipId;
    }

    @Override
    public boolean isNozzleTipChangedOnManualFeed() {
        return nozzleTipChangedOnManualFeed;
    }

    public void setNozzleTipChangedOnManualFeed(boolean nozzleTipChangedOnManualFeed) {
        this.nozzleTipChangedOnManualFeed = nozzleTipChangedOnManualFeed;
    }

    public Location getManualNozzleTipChangeLocation() {
        return manualNozzleTipChangeLocation;
    }

    public void setManualNozzleTipChangeLocation(Location manualNozzleTipChangeLocation) {
        Object oldValue = this.manualNozzleTipChangeLocation;
        this.manualNozzleTipChangeLocation = manualNozzleTipChangeLocation;
        firePropertyChange("manualNozzleTipChangeLocation", oldValue, manualNozzleTipChangeLocation);
    }

    @Override
    public void moveToPickLocation(Feeder feeder) throws Exception {
        // The default ReferenceNozzle implementation just moves to the feeder part pickLocation at safe Z.
        // But see Overrides such ContactProbeNozzle.
        Location pickLocation = feeder.getPickLocation();
        if (feeder.isPartHeightAbovePickLocation()) {
            Length partHeight = getSafePartHeight(feeder.getPart());
            pickLocation = pickLocation.add(new Location(partHeight.getUnits(), 0, 0, partHeight.getValue(), 0));
        }
        MovableUtils.moveToLocationAtSafeZ(this, pickLocation);
    }

    @Override
    public void pick(Part part) throws Exception {
        Logger.debug("{}.pick()", getName());
        if (part == null) {
            throw new Exception("Can't pick null part");
        }
        if (nozzleTip == null) {
            throw new Exception("Can't pick, no nozzle tip loaded");
        }

        try {
            Map<String, Object> globals = new HashMap<>();
            globals.put("nozzle", this);
            globals.put("part", part);
            Configuration.get().getScripting().on("Nozzle.BeforePick", globals);
        }
        catch (Exception e) {
            Logger.warn(e);
        }

        this.part = part;

        // if the method needs it, store one measurement up front
        storeBeforePickVacuumLevel();

        double pickVacuumThreshold = part.getPackage().getPickVacuumLevel();
        if (Double.compare(pickVacuumThreshold, Double.valueOf(0.0)) != 0) {
            actuateVacuumValve(pickVacuumThreshold);
        }
        else {
            actuateVacuumValve(true);
        }

        // wait for the Dwell Time and/or make sure the vacuum level builds up to the desired range (with timeout)
        establishPickVacuumLevel(this.getPickDwellMilliseconds() + nozzleTip.getPickDwellMilliseconds());

        getMachine().fireMachineHeadActivity(head);

        try {
            Map<String, Object> globals = new HashMap<>();
            globals.put("nozzle", this);
            globals.put("part", part);
            Configuration.get().getScripting().on("Nozzle.AfterPick", globals);
        }
        catch (Exception e) {
            Logger.warn(e);
        }
    }

    @Override
    public void moveToPlacementLocation(Location placementLocation, Part part) throws Exception {
        // The default ReferenceNozzle implementation just moves to the placementLocation + partHeight at safe Z.
        if (part != null) {
            placementLocation = placementLocation
                    .add(new Location(part.getHeight().getUnits(), 0, 0, part.getHeight().getValue(), 0));
        }
        MovableUtils.moveToLocationAtSafeZ(this, placementLocation);
    }

    @Override
    public void place() throws Exception {
        Logger.debug("{}.place()", getName());
        if (nozzleTip == null) {
            throw new Exception("Can't place, no nozzle tip loaded");
        }

        try {
            Map<String, Object> globals = new HashMap<>();
            globals.put("nozzle", this);
            Configuration.get().getScripting().on("Nozzle.BeforePlace", globals);
        }
        catch (Exception e) {
            Logger.warn(e);
        }

        // if the method needs it, store one measurement up front
        storeBeforePlaceVacuumLevel();

        if (part != null) {
            double placeBlowLevel = part.getPackage().getPlaceBlowOffLevel();
            if (Double.compare(placeBlowLevel, Double.valueOf(0.0)) != 0) {
                actuateBlowValve(placeBlowLevel);
            }
            else {
                actuateVacuumValve(false);
            }
        }
        else {
            actuateVacuumValve(false);
        }


        // wait for the Dwell Time and/or make sure the vacuum level decays to the desired range (with timeout)
        establishPlaceVacuumLevel(this.getPlaceDwellMilliseconds() + nozzleTip.getPlaceDwellMilliseconds());

        this.part = null;
        getMachine().fireMachineHeadActivity(head);

        try {
            Map<String, Object> globals = new HashMap<>();
            globals.put("nozzle", this);
            Configuration.get().getScripting().on("Nozzle.AfterPlace", globals);
        }
        catch (Exception e) {
            Logger.warn(e);
        }
    }

    private StarNozzleTip getUnloadedNozzleTipStandin() {
        for (NozzleTip nozzleTip : this.getCompatibleNozzleTips()) {
            if (nozzleTip instanceof StarNozzleTip) {
                StarNozzleTip referenceNozzleTip = (StarNozzleTip)nozzleTip;
                if (referenceNozzleTip.isUnloadedNozzleTipStandin()) {
                    return referenceNozzleTip;
                }
            }
        }
        return null;
    }

    public StarNozzleTip getCalibrationNozzleTip() {
        if (nozzleTip != null) {
            // normally we have the loaded nozzle tip as the calibration nozzle tip
            StarNozzleTip calibrationNozzleTip = null;
            if (nozzleTip instanceof StarNozzleTip) {
                calibrationNozzleTip = (StarNozzleTip)nozzleTip;
            }
            return calibrationNozzleTip;
        } else {
            // if no tip is mounted, we use the "unloaded" nozzle tip stand-in, so we
            // can still calibrate
            return getUnloadedNozzleTipStandin();
        }
    }

    @Override
    public Location getCameraToolCalibratedOffset(Camera camera) {
        // Apply the axis offset from runout calibration here.
        StarNozzleTip calibrationNozzleTip = getCalibrationNozzleTip();
        if (calibrationNozzleTip != null && calibrationNozzleTip.getCalibration().isCalibrated(this)) {
            return calibrationNozzleTip.getCalibration().getCalibratedCameraOffset(this, camera);
        }

        return new Location(camera.getUnitsPerPixel().getUnits());
    }

    @Override
    public void calibrate() throws Exception {
        /*ReferenceNozzleTip calibrationNozzleTip = getCalibrationNozzleTip();
        if (calibrationNozzleTip != null) {
            calibrationNozzleTip.getCalibration().calibrate(this);
        }*/
    }

    @Override
    public boolean isCalibrated() {
        StarNozzleTip calibrationNozzleTip = getCalibrationNozzleTip();
        if (calibrationNozzleTip != null) {
            return calibrationNozzleTip.getCalibration().isCalibrated(this);
        }
        // No calibration needed.
        return true;
    }

    @Override
    public Location toHeadLocation(Location location, Location currentLocation, LocationOption... options) {
        location = super.toHeadLocation(location, currentLocation);
        // Apply runout compensation.
        StarNozzleTip calibrationNozzleTip = getCalibrationNozzleTip();
        // check if totally raw move, in that case disable nozzle calibration
        for (LocationOption option: options) {
            if (option == LocationOption.SuppressDynamicCompensation) {
                calibrationNozzleTip = null;
            }
        }
        if (calibrationNozzleTip != null && calibrationNozzleTip.getCalibration().isCalibrated(this)) {
            Location correctionOffset = calibrationNozzleTip.getCalibration().getCalibratedOffset(this, location.getRotation());
            location = location.subtract(correctionOffset);
            Logger.trace("{}.transformToHeadLocation({}, ...) runout compensation: {}", getName(), location, correctionOffset);
        } else {
            Logger.trace("{}.transformToHeadLocation({}, ...)", getName(), location);
        }
        return location;
    }

    @Override
    public Location toHeadMountableLocation(Location location, Location currentLocation, LocationOption... options) {
        location = super.toHeadMountableLocation(location, currentLocation);
        // Unapply runout compensation.
        StarNozzleTip calibrationNozzleTip = getCalibrationNozzleTip();
        // Check SuppressCompensation, in that case disable nozzle calibration.
        for (LocationOption option: options) {
            if (option == LocationOption.SuppressDynamicCompensation) {
                calibrationNozzleTip = null;
            }
        }
        if (calibrationNozzleTip != null && calibrationNozzleTip.getCalibration().isCalibrated(this)) {
            Location offset =
                    calibrationNozzleTip.getCalibration().getCalibratedOffset(this, location.getRotation());
            location = location.add(offset);
        }
        return location;
    }

    @Override
    public Length getSafePartHeight(Part part) {
        if (part != null) {
            if (part.isPartHeightUnknown() && nozzleTip != null) {
                return nozzleTip.getMaxPartHeight();
            }
            else {
                return part.getHeight();
            }
        }
        return new Length(0, LengthUnit.Millimeters);
    }

    @Override
    public Length getEffectiveSafeZ() throws Exception {
        Length safeZ = super.getEffectiveSafeZ();
        if (safeZ == null) {
            throw new Exception("Nozzle "+getName()+" has no Z axis with Safe Zone mapped.");
        }
        if (enableDynamicSafeZ) {
            // if a part is loaded, decrease (higher) safeZ
            if (part != null) {
                safeZ = safeZ.add(part.getHeight());
                // Note, the safeZ value will be validated in moveToSafeZ()
                // to make sure it is not outside the Safe Z Zone.
            }
        }
        return safeZ;
    }

    @Override
    public void home() throws Exception {
        Logger.debug("{}.home()", getName());
        for (NozzleTip attachedNozzleTip : this.getCompatibleNozzleTips()) {
            if (attachedNozzleTip instanceof StarNozzleTip) {
                StarNozzleTip calibrationNozzleTip = (StarNozzleTip)attachedNozzleTip;
                if (calibrationNozzleTip.getCalibration().isRecalibrateOnHomeNeeded(this)) {
                    if (calibrationNozzleTip == this.getCalibrationNozzleTip()) {
                        // The currently mounted nozzle tip.
                        Logger.debug("{}.home() nozzle tip {} calibration neeeded", getName(), calibrationNozzleTip.getName());
                        calibrationNozzleTip.getCalibration().calibrate(this, true, false);
                    }
                    else {
                        // Not currently mounted so just reset.
                        Logger.debug("{}.home() nozzle tip {} calibration reset", getName(), calibrationNozzleTip.getName());
                        calibrationNozzleTip.getCalibration().reset(this);
                    }
                }
            }
        }
    }

    @Override
    public void loadNozzleTip(NozzleTip nozzleTip) throws Exception {
        if (this.nozzleTip == nozzleTip) {
            return;
        }


        StarNozzleTip nt = (StarNozzleTip) nozzleTip;

        // bert start
        Actuator tcPostOneActuator = getMachine().getActuatorByName(nt.getChangerActuatorPostStepOne());
        Actuator tcPostTwoActuator = getMachine().getActuatorByName(nt.getChangerActuatorPostStepTwo());
        Actuator tcPostThreeActuator = getMachine().getActuatorByName(nt.getChangerActuatorPostStepThree());
        // bert stop

        if (!getCompatibleNozzleTips().contains(nt)) {
            throw new Exception("Can't load incompatible nozzle tip.");
        }

        if (nt.getNozzleAttachedTo() != null) {
            // Nozzle tip is on different nozzle - unload it from there first.
            nt.getNozzleAttachedTo().unloadNozzleTip();
        }

        unloadNozzleTip();

        double speed = getHead().getMachine().getSpeed();
        if (!nt.isUnloadedNozzleTipStandin()) {
            if (changerEnabled) {
                Logger.debug("{}.loadNozzleTip({}): Start", getName(), nozzleTip.getName());

                try {
                    Map<String, Object> globals = new HashMap<>();
                    globals.put("head", getHead());
                    globals.put("nozzle", this);
                    globals.put("nozzleTip", nt);
                    Configuration.get()
                            .getScripting()
                            .on("NozzleTip.BeforeLoad", globals);
                }
                catch (Exception e) {
                    Logger.warn(e);
                }

                Logger.debug("{}.loadNozzleTip({}): moveTo Start Location",
                        new Object[] {getName(), nozzleTip.getName()});
                MovableUtils.moveToLocationAtSafeZ(this, nt.getChangerStartLocation(), speed);

                // bert start
                if (tcPostOneActuator != null) {
                    tcPostOneActuator.actuate(true);
                }
                // bert stop

                Logger.debug("{}.loadNozzleTip({}): moveTo Mid Location",
                        new Object[] {getName(), nozzleTip.getName()});
                moveTo(nt.getChangerMidLocation(), nt.getChangerStartToMidSpeed() * speed);

                // bert start
                if (tcPostTwoActuator !=null) {
                    tcPostTwoActuator.actuate(true);
                }
                // bert stop

                Logger.debug("{}.loadNozzleTip({}): moveTo Mid Location 2",
                        new Object[] {getName(), nozzleTip.getName()});
                moveTo(nt.getChangerMidLocation2(), nt.getChangerMidToMid2Speed() * speed);

                // bert start
                if (tcPostThreeActuator !=null) {
                    tcPostThreeActuator.actuate(true);
                }
                //bert stop
            }

            Logger.debug("{}.loadNozzleTip({}): moveTo End Location",
                    new Object[] {getName(), nozzleTip.getName()});
            moveTo(nt.getChangerEndLocation(), nt.getChangerMid2ToEndSpeed() * speed);
            moveToSafeZ(getHead().getMachine().getSpeed());

            Logger.debug("{}.loadNozzleTip({}): Finished",
                    new Object[] {getName(), nozzleTip.getName()});

            if (changerEnabled) {
                try {
                    Map<String, Object> globals = new HashMap<>();
                    globals.put("head", getHead());
                    globals.put("nozzle", this);
                    Configuration.get()
                            .getScripting()
                            .on("NozzleTip.Loaded", globals);
                }
                catch (Exception e) {
                    Logger.warn(e);
                }
            }
        }

        this.nozzleTip = nt;
        currentNozzleTipId = nozzleTip.getId();
        firePropertyChange("nozzleTip", null, getNozzleTip());
        ((ReferenceMachine) head.getMachine()).fireMachineHeadActivity(head);

        if (!nt.isUnloadedNozzleTipStandin()) {
            if (!changerEnabled) {
                if (this.nozzleTip.getCalibration().isRecalibrateOnNozzleTipChangeNeeded(this)
                        || this.nozzleTip.getCalibration().isRecalibrateOnNozzleTipChangeInJobNeeded(this)) {
                    Logger.debug("{}.loadNozzleTip() nozzle tip {} calibration reset", getName(), this.nozzleTip.getName());
                    // can't automatically recalibrate with manual change - reset() for now
                    this.nozzleTip.getCalibration().reset(this);
                }
                throw new Exception("Manual NozzleTip "+nt.getName()+" load on Nozzle "+getName()+" required!");
            }
        }

        if (this.nozzleTip.getCalibration().isRecalibrateOnNozzleTipChangeNeeded(this)) {
            Logger.debug("{}.loadNozzleTip() nozzle tip {} calibration needed", getName(), this.nozzleTip.getName());
            this.nozzleTip.getCalibration().calibrate(this);
        }
        else if (this.nozzleTip.getCalibration().isRecalibrateOnNozzleTipChangeInJobNeeded(this)) {
            Logger.debug("{}.loadNozzleTip() nozzle tip {} calibration reset", getName(), this.nozzleTip.getName());
            // it will be recalibrated by the job - just reset() for now
            this.nozzleTip.getCalibration().reset(this);
        }
    }

    @Override
    public void unloadNozzleTip() throws Exception {
        if (nozzleTip == null) {
            return;
        }

        StarNozzleTip nt = (StarNozzleTip) nozzleTip;

        Actuator tcPostOneActuator = getMachine().getActuatorByName(nt.getChangerActuatorPostStepOne());
        Actuator tcPostTwoActuator = getMachine().getActuatorByName(nt.getChangerActuatorPostStepTwo());
        Actuator tcPostThreeActuator = getMachine().getActuatorByName(nt.getChangerActuatorPostStepThree());

        if (!nt.isUnloadedNozzleTipStandin()) {
            Logger.debug("{}.unloadNozzleTip(): Start", getName());

            if (changerEnabled) {
                try {
                    Map<String, Object> globals = new HashMap<>();
                    globals.put("head", getHead());
                    globals.put("nozzle", this);
                    globals.put("nozzleTip", nt);
                    Configuration.get()
                            .getScripting()
                            .on("NozzleTip.BeforeUnload", globals);
                }
                catch (Exception e) {
                    Logger.warn(e);
                }
            }

            double speed = getHead().getMachine().getSpeed();

            Logger.debug("{}.unloadNozzleTip(): moveTo End Location", getName());
            MovableUtils.moveToLocationAtSafeZ(this, nt.getChangerEndLocation(), speed);

            if (changerEnabled) {

                // bert start
                if (tcPostThreeActuator !=null) {
                    tcPostThreeActuator.actuate(true);
                }
                //bert stop

                Logger.debug("{}.unloadNozzleTip(): moveTo Mid Location 2", getName());
                moveTo(nt.getChangerMidLocation2(), nt.getChangerMid2ToEndSpeed() * speed);


                // bert start
                if (tcPostTwoActuator !=null) {
                    tcPostTwoActuator.actuate(true);
                }
                // bert stop


                Logger.debug("{}.unloadNozzleTip(): moveTo Mid Location", getName());
                moveTo(nt.getChangerMidLocation(), nt.getChangerMidToMid2Speed() * speed);

                // bert start
                if (tcPostOneActuator != null) {
                    tcPostOneActuator.actuate(true);
                }
                // bert stop

                Logger.debug("{}.unloadNozzleTip(): moveTo Start Location", getName());
                moveTo(nt.getChangerStartLocation(), nt.getChangerStartToMidSpeed() * speed);
                moveToSafeZ(getHead().getMachine().getSpeed());

                Logger.debug("{}.unloadNozzleTip(): Finished", getName());

                try {
                    Map<String, Object> globals = new HashMap<>();
                    globals.put("head", getHead());
                    globals.put("nozzle", this);
                    Configuration.get()
                            .getScripting()
                            .on("NozzleTip.Unloaded", globals);
                }
                catch (Exception e) {
                    Logger.warn(e);
                }
            }
        }

        nozzleTip = null;
        currentNozzleTipId = null;
        firePropertyChange("nozzleTip", null, getNozzleTip());
        ((ReferenceMachine) head.getMachine()).fireMachineHeadActivity(head);

        if (!changerEnabled) {
            throw new Exception("Manual NozzleTip "+nt.getName()+" unload from Nozzle "+getName()+" required!");
        }
        // May need to calibrate the "unloaded" nozzle tip stand-in i.e. the naked nozzle tip holder.
        StarNozzleTip calibrationNozzleTip = this.getCalibrationNozzleTip();
        if (calibrationNozzleTip != null && calibrationNozzleTip.getCalibration().isRecalibrateOnNozzleTipChangeNeeded(this)) {
            Logger.debug("{}.unloadNozzleTip() nozzle tip {} calibration needed", getName(), calibrationNozzleTip.getName());
            calibrationNozzleTip.getCalibration().calibrate(this);
        }
    }

    public boolean isChangerEnabled() {
        return changerEnabled;
    }

    public void setChangerEnabled(boolean changerEnabled) {
        this.changerEnabled = changerEnabled;
    }

    @Override
    public Wizard getConfigurationWizard() {
        return new StarNozzleConfigurationWizard(getMachine(), this);
    }

    @Override
    public String getPropertySheetHolderTitle() {
        return getClass().getSimpleName() + " " + getName();
    }

    @Override
    public PropertySheetHolder[] getChildPropertySheetHolders() {
        return null;
    }

    public PropertySheet[] getPropertySheets() {
        return new PropertySheet[] {
                new PropertySheetWizardAdapter(getConfigurationWizard()),
                new PropertySheetWizardAdapter(new StarNozzleCompatibleNozzleTipsWizard(this), "Nozzle Tips"),
                //new PropertySheetWizardAdapter(new ReferenceNozzleVacuumWizard(this), "Vacuum"),
                //new PropertySheetWizardAdapter(new ReferenceNozzleToolChangerWizard(this), "Tool Changer"),
                new PropertySheetWizardAdapter(new StarNozzleOffsetWizard(this), "Nozzle Offset Wizard"),
        };
    }

    @Override
    public Action[] getPropertySheetHolderActions() {
        return new Action[] {deleteAction};
    }

    public Action deleteAction = new AbstractAction("Delete Nozzle") {
        {
            putValue(SMALL_ICON, Icons.nozzleRemove);
            putValue(NAME, "Delete Nozzle");
            putValue(SHORT_DESCRIPTION, "Delete the currently selected nozzle.");
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (getHead().getNozzles().size() == 1) {
                MessageBoxes.errorBox(null, "Error: Nozzle Not Deleted", "Can't delete last nozzle. There must be at least one nozzle.");
                return;
            }
            int ret = JOptionPane.showConfirmDialog(MainFrame.get(),
                    "Are you sure you want to delete " + getName() + "?",
                    "Delete " + getName() + "?", JOptionPane.YES_NO_OPTION);
            if (ret == JOptionPane.YES_OPTION) {
                getHead().removeNozzle(StarNozzle.this);
            }
        }
    };

    @Override
    public String toString() {
        return getName() + " " + getId();
    }

    protected AbstractMachine getMachine() {
        return (AbstractMachine) Configuration.get().getMachine();
    }

    protected boolean isVaccumActuatorEnabled() {
        return vacuumActuatorName != null && !vacuumActuatorName.isEmpty();
    }

    @Override
    public boolean isPartOnEnabled(Nozzle.PartOnStep step) {
        if ((step == PartOnStep.AfterPick && getNozzleTip().isPartOnCheckAfterPick())
                || (step == PartOnStep.Align && getNozzleTip().isPartOnCheckAlign())
                || (step == PartOnStep.BeforePlace && getNozzleTip().isPartOnCheckBeforePlace())) {
            return isVaccumActuatorEnabled()
                    && (getNozzleTip().getMethodPartOn() != StarNozzleTip.VacuumMeasurementMethod.None);
        }
        return false;
    }

    @Override
    public boolean isPartOffEnabled(Nozzle.PartOffStep step) {
        if ((step == PartOffStep.AfterPlace && getNozzleTip().isPartOffCheckAfterPlace())
                || (step == PartOffStep.BeforePick && getNozzleTip().isPartOffCheckBeforePick())) {
            return isVaccumActuatorEnabled()
                    && (getNozzleTip().getMethodPartOff() != StarNozzleTip.VacuumMeasurementMethod.None);
        }
        return false;
    }


    protected Actuator getVacuumSenseActuator() throws Exception {
        Actuator actuator = getHead().getActuatorByName(vacuumSenseActuatorName);
        if (actuator == null) {
            throw new Exception(String.format("Can't find vacuum sense actuator %s", vacuumSenseActuatorName));
        }
        return actuator;
    }

    protected Actuator getVacuumActuator() throws Exception {
        Actuator actuator = getHead().getActuatorByName(vacuumActuatorName);
        if (actuator == null) {
            throw new Exception(String.format("Can't find vacuum actuator %s", vacuumActuatorName));
        }
        return actuator;
    }

    protected Actuator getBlowOffActuator() throws Exception {
        Actuator actuator = getHead().getActuatorByName(blowOffActuatorName);
        if (actuator == null) {
            throw new Exception(String.format("Can't find blow actuator %s", blowOffActuatorName));
        }
        return actuator;
    }

    protected boolean hasPartOnAnyOtherNozzle() {
        for (Nozzle nozzle : getHead().getNozzles()) {
            if (nozzle != this ) {
                if (nozzle.getPart() != null) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void actuatePump(boolean on) throws Exception {
        Actuator pump = getHead().getPump();
        if (pump != null && !hasPartOnAnyOtherNozzle()) {
            pump.actuate(on);
        }
    }

    protected void actuateVacuumValve(boolean on) throws Exception {
        if (on) {
            actuatePump(true);
        }

        getVacuumActuator().actuate(on);

        if (! on) {
            actuatePump(false);
        }
    }

    protected void actuateVacuumValve(double value) throws Exception {
        actuatePump(true);

        getVacuumActuator().actuate(value);
    }

    protected void actuateBlowValve(double value) throws Exception {
        getBlowOffActuator().actuate(value);

        actuatePump(false);
    }

    protected double readVacuumLevel() throws Exception {
        return Double.parseDouble(getVacuumSenseActuator().read());
    }

    protected boolean isPartOnGraphEnabled() {
        StarNozzleTip nt = getNozzleTip();
        return nt.getMethodPartOn() != StarNozzleTip.VacuumMeasurementMethod.None
                && (nt.getMethodPartOn().isDifferenceMethod() || nt.isEstablishPartOnLevel());
    }

    protected boolean isPartOffGraphEnabled() {
        StarNozzleTip nt = getNozzleTip();
        return nt.getMethodPartOff() != StarNozzleTip.VacuumMeasurementMethod.None
                && (nt.getMethodPartOff().isDifferenceMethod() || nt.isEstablishPartOffLevel());
    }

    protected void storeBeforePickVacuumLevel() throws Exception {
        StarNozzleTip nt = getNozzleTip();
        if (isPartOnGraphEnabled()) {
            // start a new graph
            double vacuumLevel = readVacuumLevel();
            SimpleGraph vacuumGraph = nt.startNewVacuumGraph(vacuumLevel, true);
            // store on the nozzle tip ... to be continued
            nt.setVacuumPartOnGraph(vacuumGraph);
        }
        else {
            nt.setVacuumPartOnGraph(null);
        }
    }

    protected void storeBeforePlaceVacuumLevel() throws Exception {
        StarNozzleTip nt = getNozzleTip();
        if (isPartOffGraphEnabled()) {
            // start a new graph
            double vacuumLevel = readVacuumLevel();
            SimpleGraph vacuumGraph = nt.startNewVacuumGraph(vacuumLevel, false);
            // store on the nozzle tip ... to be continued
            nt.setVacuumPartOffGraph(vacuumGraph);
        }
        else {
            nt.setVacuumPartOffGraph(null);
        }
    }

    protected void establishPickVacuumLevel(int milliseconds) throws Exception {
        StarNozzleTip nt = getNozzleTip();
        SimpleGraph vacuumGraph = nt.getVacuumPartOnGraph();
        if (vacuumGraph != null) {
            // valve is sure on
            vacuumGraph.getRow(StarNozzleTip.BOOLEAN, StarNozzleTip.VALVE_ON)
                    .recordDataPoint(vacuumGraph.getT(), 1);
            long timeout = System.currentTimeMillis() + milliseconds;
            SimpleGraph.DataRow vacuumData = vacuumGraph.getRow(StarNozzleTip.PRESSURE, StarNozzleTip.VACUUM);
            double vacuumLevel;
            do {
                vacuumLevel = readVacuumLevel();
                vacuumData.recordDataPoint(vacuumGraph.getT(), vacuumLevel);
                if (nt.isEstablishPartOnLevel()
                        && vacuumLevel >= nt.getVacuumLevelPartOnLow() && vacuumLevel <= nt.getVacuumLevelPartOnHigh()) {
                    // within range, we're done
                    break;
                }
            }
            while (System.currentTimeMillis() < timeout);
            // valve is still on
            vacuumGraph.getRow(StarNozzleTip.BOOLEAN, StarNozzleTip.VALVE_ON)
                    .recordDataPoint(vacuumGraph.getT(), 1);
            nt.setVacuumPartOnGraph(vacuumGraph);
            if (nt.getMethodPartOn().isDifferenceMethod()) {
                nt.setVacuumLevelPartOnReading(vacuumLevel);
            }
        }
        else {
            // simple method, just dwell
            Thread.sleep(milliseconds);
        }
    }

    protected void establishPlaceVacuumLevel(int milliseconds) throws Exception {
        StarNozzleTip nt = getNozzleTip();
        SimpleGraph vacuumGraph = nt.getVacuumPartOffGraph();
        if (vacuumGraph != null) {
            // valve is sure off
            vacuumGraph.getRow(StarNozzleTip.BOOLEAN, StarNozzleTip.VALVE_ON)
                    .recordDataPoint(vacuumGraph.getT(), 0);
            long timeout = System.currentTimeMillis() + milliseconds;
            SimpleGraph.DataRow vacuumData = vacuumGraph.getRow(StarNozzleTip.PRESSURE, StarNozzleTip.VACUUM);
            double vacuumLevel;
            do {
                vacuumLevel = readVacuumLevel();
                vacuumData.recordDataPoint(vacuumGraph.getT(), vacuumLevel);
                if (nt.isEstablishPartOffLevel()
                        && vacuumLevel >= nt.getVacuumLevelPartOffLow() && vacuumLevel <= nt.getVacuumLevelPartOffHigh()) {
                    // within range, we're done
                    break;
                }
            }
            while (System.currentTimeMillis() < timeout);
            // valve is still off
            vacuumGraph.getRow(StarNozzleTip.BOOLEAN, StarNozzleTip.VALVE_ON)
                    .recordDataPoint(vacuumGraph.getT(), 0);
            nt.setVacuumPartOffGraph(vacuumGraph);
            if (nt.getMethodPartOff().isDifferenceMethod()) {
                nt.setVacuumLevelPartOffReading(vacuumLevel);
            }
        }
        else {
            // simple method, just dwell
            Thread.sleep(milliseconds);
        }
    }

    protected double probePartOffVacuumLevel(int probingMilliseconds, int dwellMilliseconds) throws Exception {
        StarNozzleTip nt = getNozzleTip();
        SimpleGraph vacuumGraph = null;
        double returnedVacuumLevel = Double.NaN; // this should always be overwritten in one or the other if/else combo
        if (isPartOnGraphEnabled()) {
            vacuumGraph = nt.getVacuumPartOffGraph();
            if (vacuumGraph == null || vacuumGraph.getT() > 1000.0) {
                // Time since last action too long, this is probably a BeforePick check, start a new graph.
                vacuumGraph = nt.startNewVacuumGraph(readVacuumLevel(), true);
                nt.setVacuumPartOffGraph(vacuumGraph);
            }
            // record valve off
            vacuumGraph.getRow(StarNozzleTip.BOOLEAN, StarNozzleTip.VALVE_ON)
                    .recordDataPoint(vacuumGraph.getT(), 0);
        }

        if (nt.getMethodPartOff().isDifferenceMethod()) {
            // we might have multiple partOff checks, so refresh the difference baseline
            double vacuumLevel = readVacuumLevel();
            // store in graph, if one is present
            if (vacuumGraph != null) {
                vacuumGraph.getRow(StarNozzleTip.PRESSURE, StarNozzleTip.VACUUM)
                        .recordDataPoint(vacuumGraph.getT(), vacuumLevel);
            }
            // store as baseline
            nt.setVacuumLevelPartOffReading(vacuumLevel);
        }

        try {
            // switch vacuum on for the test
            actuateVacuumValve(true);

            if (vacuumGraph != null) {
                // record valve on
                vacuumGraph.getRow(StarNozzleTip.BOOLEAN, StarNozzleTip.VALVE_ON)
                        .recordDataPoint(vacuumGraph.getT(), 1);
                // record the slope of the vacuum level
                long timeout = System.currentTimeMillis() + probingMilliseconds;
                SimpleGraph.DataRow vacuumData = vacuumGraph.getRow(StarNozzleTip.PRESSURE, StarNozzleTip.VACUUM);
                double vacuumLevel;
                do {
                    vacuumLevel = readVacuumLevel();
                    vacuumData.recordDataPoint(vacuumGraph.getT(), vacuumLevel);
                }
                while (System.currentTimeMillis() < timeout);
                // record valve still on
                vacuumGraph.getRow(StarNozzleTip.BOOLEAN, StarNozzleTip.VALVE_ON)
                        .recordDataPoint(vacuumGraph.getT(), 1);
                if (dwellMilliseconds <= 0) {
                    returnedVacuumLevel = vacuumLevel;
                }
            }
            else {
                // simple method, just dwell
                Thread.sleep(probingMilliseconds);
                if (dwellMilliseconds <= 0) {
                    returnedVacuumLevel = readVacuumLevel();
                }
            }
        }
        finally {
            // always make sure the valve is off
            actuateVacuumValve(false);
        }

        if (vacuumGraph != null) {
            // record valve off
            vacuumGraph.getRow(StarNozzleTip.BOOLEAN, StarNozzleTip.VALVE_ON)
                    .recordDataPoint(vacuumGraph.getT(), 0);
            // record the slope of the vacuum level
            long timeout = System.currentTimeMillis() + dwellMilliseconds;
            SimpleGraph.DataRow vacuumData = vacuumGraph.getRow(StarNozzleTip.PRESSURE, StarNozzleTip.VACUUM);
            double vacuumLevel;
            do {
                vacuumLevel = readVacuumLevel();
                vacuumData.recordDataPoint(vacuumGraph.getT(), vacuumLevel);
            }
            while (System.currentTimeMillis() < timeout);
            // record valve still off
            vacuumGraph.getRow(StarNozzleTip.BOOLEAN, StarNozzleTip.VALVE_ON)
                    .recordDataPoint(vacuumGraph.getT(), 0);
            // save the graph back (for the property change to fire)
            nt.setVacuumPartOffGraph(vacuumGraph);
            if (dwellMilliseconds > 0) {
                returnedVacuumLevel = vacuumLevel;
            }
            // return the vacuum level, either from before or after valve closed
            return returnedVacuumLevel;
        }
        else {
            // simple method, just dwell and then read the level
            if (dwellMilliseconds > 0) {
                Thread.sleep(dwellMilliseconds);
                returnedVacuumLevel = readVacuumLevel();
            }
            // return the vacuum level, either from before or after valve closed
            return returnedVacuumLevel;
        }
    }

    @Override
    public boolean isPartOn() throws Exception {
        StarNozzleTip nt = getNozzleTip();
        double vacuumLevel = readVacuumLevel();
        // store in graph, if one is present
        SimpleGraph vacuumGraph = nt.getVacuumPartOnGraph();
        if (vacuumGraph != null) {
            vacuumGraph.getRow(StarNozzleTip.PRESSURE, StarNozzleTip.VACUUM)
                    .recordDataPoint(vacuumGraph.getT(), vacuumLevel);
            // valve is still on
            vacuumGraph.getRow(StarNozzleTip.BOOLEAN, StarNozzleTip.VALVE_ON)
                    .recordDataPoint(vacuumGraph.getT(), 1);
        }
        if (nt.getMethodPartOn().isDifferenceMethod()) {
            // observe the trend as a difference from the baseline reading
            double vacuumBaselineLevel = nt.getVacuumLevelPartOnReading();
            double vacuumDifference = vacuumLevel - vacuumBaselineLevel;
            nt.setVacuumDifferencePartOnReading(vacuumDifference);
            // check the reference range
            if (vacuumBaselineLevel < nt.getVacuumLevelPartOnLow() || vacuumBaselineLevel > nt.getVacuumLevelPartOnHigh()) {
                Logger.debug("Nozzle tip {} baseline vacuum level {} outside PartOn range {} .. {}",
                        nt.getName(), vacuumBaselineLevel, nt.getVacuumLevelPartOnLow(), nt.getVacuumLevelPartOnHigh());
                return false;
            }
            // so far so good, check the difference
            if (vacuumDifference < nt.getVacuumDifferencePartOnLow() || vacuumDifference > nt.getVacuumDifferencePartOnHigh()) {
                Logger.debug("Nozzle tip {} vacuum level difference {} outside PartOn range {} .. {}",
                        nt.getName(), vacuumDifference, nt.getVacuumDifferencePartOnLow(), nt.getVacuumDifferencePartOnHigh());
                return false;
            }
        }
        else {
            // absolute method, store this as last level reading
            nt.setVacuumLevelPartOnReading(vacuumLevel);
            // no trend
            nt.setVacuumDifferencePartOnReading(null);
            // check the range
            if (vacuumLevel < nt.getVacuumLevelPartOnLow() || vacuumLevel > nt.getVacuumLevelPartOnHigh()) {
                Logger.debug("Nozzle tip {} absolute vacuum level {} outside PartOn range {} .. {}",
                        nt.getName(), vacuumLevel, nt.getVacuumLevelPartOnLow(), nt.getVacuumLevelPartOnHigh());
                return false;
            }
        }
        // success
        return true;
    }

    @Override
    public boolean isPartOff() throws Exception {
        StarNozzleTip nt = getNozzleTip();
        // perform the probing pulse and decay dwell, get the resulting vacuum level
        double vacuumLevel = probePartOffVacuumLevel(nt.getPartOffProbingMilliseconds(), nt.getPartOffDwellMilliseconds());

        if (nt.getMethodPartOff().isDifferenceMethod()) {
            // observe the trend as a difference from the baseline reading
            double vacuumBaselineLevel = nt.getVacuumLevelPartOffReading();
            double vacuumDifference = vacuumLevel - vacuumBaselineLevel;
            nt.setVacuumDifferencePartOffReading(vacuumDifference);
            // check the reference range
            if (vacuumBaselineLevel < nt.getVacuumLevelPartOffLow() || vacuumBaselineLevel > nt.getVacuumLevelPartOffHigh()) {
                Logger.debug("Nozzle tip {} baseline vacuum level {} outside PartOff range {} .. {}",
                        nt.getName(), vacuumBaselineLevel, nt.getVacuumLevelPartOffLow(), nt.getVacuumLevelPartOffHigh());
                return false;
            }
            // so far so good, check the difference
            if (vacuumDifference < nt.getVacuumDifferencePartOffLow() || vacuumDifference > nt.getVacuumDifferencePartOffHigh()) {
                Logger.debug("Nozzle tip {} vacuum level difference {} outside PartOff range {} .. {}",
                        nt.getName(), vacuumDifference, nt.getVacuumDifferencePartOffLow(), nt.getVacuumDifferencePartOffHigh());
                return false;
            }
        }
        else {
            // absolute method, store this as last level reading
            nt.setVacuumLevelPartOffReading(vacuumLevel);
            // no trend
            nt.setVacuumDifferencePartOffReading(null);
            // check the range
            if (vacuumLevel < nt.getVacuumLevelPartOffLow() || vacuumLevel > nt.getVacuumLevelPartOffHigh()) {
                Logger.debug("Nozzle tip {} absolute vacuum level {} outside PartOff range {} .. {}",
                        nt.getName(), vacuumLevel, nt.getVacuumLevelPartOffLow(), nt.getVacuumLevelPartOffHigh());
                return false;
            }
        }
        // success
        return true;
    }

    @Deprecated
    public void migrateSafeZ() {
        if (safeZ == null) {
            safeZ = new Length(0, LengthUnit.Millimeters);
        }
        CoordinateAxis coordAxis = getCoordinateAxisZ();
        if (coordAxis instanceof ReferenceControllerAxis) {
            ReferenceControllerAxis rawAxis = (ReferenceControllerAxis) coordAxis;
            try {
                Length rawZ = headMountableToRawZ(rawAxis, safeZ);
                rawAxis.setSafeZoneLow(rawZ);
                rawAxis.setSafeZoneLowEnabled(true);
                rawAxis.setSafeZoneHigh(rawZ);
                rawAxis.setSafeZoneHighEnabled(true);
                // Get rid of the old setting.
                safeZ = null;
            }
            catch (Exception e) {
                Logger.error(e);
            }
        }
        else if (coordAxis != null) {
            coordAxis.setHomeCoordinate(safeZ);
        }
    }
}