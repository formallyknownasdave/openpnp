package org.openpnp.machine.starmachine;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import org.jdesktop.beansbinding.AutoBinding;
import org.openpnp.ConfigurationListener;
import org.openpnp.gui.components.ComponentDecorators;
import org.openpnp.gui.support.ActuatorsComboBoxModel;
import org.openpnp.gui.support.LengthConverter;
import org.openpnp.gui.support.MutableLocationProxy;
import org.openpnp.gui.support.Wizard;
import org.openpnp.machine.reference.ReferenceHead;
import org.openpnp.machine.reference.ReferenceMachine;
import org.openpnp.machine.starmachine.wizards.StarHeadConfigurationWizard;
import org.openpnp.machine.starmachine.wizards.StarNozzleConfigurationWizard;
import org.openpnp.model.AxesLocation;
import org.openpnp.model.Configuration;
import org.openpnp.model.Location;
import org.openpnp.model.Motion;
import org.openpnp.spi.HeadMountable;
import org.openpnp.spi.Locatable;
import org.openpnp.spi.base.AbstractAxis;
import org.openpnp.spi.base.AbstractMachine;
import org.openpnp.spi.base.AbstractTransformedAxis;
import org.pmw.tinylog.Logger;
import org.simpleframework.xml.Attribute;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class StarHead extends ReferenceHead {

    public AbstractAxis axisNozzlePosition;

    @Attribute(required = false)
    private String axisNozzlePositionId;

    public StarHead() {
        Configuration.get().addListener(new ConfigurationListener.Adapter() {

            @Override
            public void configurationLoaded(Configuration configuration) throws Exception {
                axisNozzlePosition = (AbstractAxis) configuration.getMachine().getAxis(axisNozzlePositionId);
            }
        });
    }

    public AbstractAxis getAxisNozzlePosition() {
        return axisNozzlePosition;
    }

    public void setAxisNozzlePosition(AbstractAxis axisNozzlePosition) {
        this.axisNozzlePosition = axisNozzlePosition;
        this.axisNozzlePositionId = (axisNozzlePosition == null) ? null : axisNozzlePosition.getId();
    }

    protected AbstractMachine getGenericMachine() {
        return (AbstractMachine) Configuration.get().getMachine();
    }
    @Override
    public void moveTo(HeadMountable hm, Location location, double speed, Motion.MotionOption... options) throws Exception {
        AbstractMachine machine = getGenericMachine();
        AxesLocation mappedAxes = hm.getMappedAxes(machine);
        if (!mappedAxes.isEmpty()) {
            AxesLocation axesLocation = hm.toRaw(location);
            //if (hm instanceof StarNozzle) {
            axesLocation = axesLocation.put(new AxesLocation(axisNozzlePosition, ((StarNozzle) hm).getNozzlePosition()));
            //}
            Logger.warn("StarHead: injected nozzle position {} on axis {}",((StarNozzle) hm).getNozzlePosition(),axisNozzlePosition.getName());
            machine.getMotionPlanner().moveTo(hm, axesLocation, speed, options);
        }
    }

    @Override
    public Wizard getConfigurationWizard() {
        return new StarHeadConfigurationWizard(getGenericMachine(), this);
    }
}
