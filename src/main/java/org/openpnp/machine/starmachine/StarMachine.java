package org.openpnp.machine.starmachine;

import org.openpnp.machine.reference.ContactProbeNozzle;
import org.openpnp.machine.reference.ReferenceMachine;
import org.openpnp.machine.reference.ReferenceNozzle;
import org.openpnp.machine.reference.ReferencePnpJobProcessor;
import org.openpnp.machine.reference.psh.*;
import org.openpnp.spi.*;
import org.openpnp.spi.base.SimplePropertySheetHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StarMachine extends ReferenceMachine {

    protected PnpJobProcessor pnpJobProcessor = new ReferencePnpJobProcessor();

    @Override
    public List<Class<? extends Nozzle>> getCompatibleNozzleClasses() {
        List<Class<? extends Nozzle>> l = new ArrayList<>();
        l.add(StarNozzle.class);
        l.add(ReferenceNozzle.class);
        l.add(ContactProbeNozzle.class);
        return l;
    }
    @Override
    public PropertySheetHolder[] getChildPropertySheetHolders() {

        ArrayList<PropertySheetHolder> children = new ArrayList<>();
        children.add(new AxesPropertySheetHolder(this, "Axes", getAxes(), null));
        children.add(new SignalersPropertySheetHolder(this, "Signalers", getSignalers(), null));
        children.add(new SimplePropertySheetHolder("Feeders", getFeeders()));
        children.add(new SimplePropertySheetHolder("Heads", getHeads()));
        children.add(new NozzleTipsPropertySheetHolder("Nozzle Tips", getNozzleTips(), null));
        children.add(new CamerasPropertySheetHolder(null, "Cameras", getCameras(), null));
        children.add(new ActuatorsPropertySheetHolder(null, "Actuators", getActuators(), null));
        children.add(new DriversPropertySheetHolder(this, "Drivers", getDrivers(), null));
        children.add(new SimplePropertySheetHolder("Job Processors",
                Arrays.asList(getPnpJobProcessor())));
        ArrayList<PropertySheetHolder> visionChildren = new ArrayList<>();

        visionChildren.add(getFiducialLocator());
        visionChildren.add(new PartAlignmentPropertySheetHolder("Part Analysis", getPartAlignments(), null));
        visionChildren.add(new NozzleTipLocatorPropertySheetHolder("Nozzle Tips", getNozzleTipLocators(), null));

        children.add(new VisionPropertySheetHolder(this, "Vision", visionChildren, null));
        return children.toArray(new PropertySheetHolder[] {});
    }
}
