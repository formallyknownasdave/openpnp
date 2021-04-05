package org.openpnp.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

import java.util.ArrayList;
import java.util.List;

public class Outline {
    @Attribute
    private LengthUnit units = LengthUnit.Millimeters;

    @ElementList(inline = true, required = false)
    private ArrayList<LeadGroup> leadGroups = new ArrayList<>();

    @Attribute(required = false)
    private double bodyWidth;

    @Attribute(required = false)
    private double bodyHeight;

    public LengthUnit getUnits() {
        return units;
    }

    public void setUnits(LengthUnit units) {
        this.units = units;
    }

    public List<LeadGroup> getLeadGroups() {
        return leadGroups;
    }

    public void removeLeadGroup(LeadGroup group) {
        leadGroups.remove(group);
    }

    public void addPad(LeadGroup group) {
        leadGroups.add(group);
    }

    public LeadGroup getLeadGroupByName(String name) {
        for (LeadGroup group : leadGroups) {
            if (group.getName().equals(name)) {
                return group;
            }
        }
        return null;
    }

    public double getBodyWidth() {
        return bodyWidth;
    }

    public void setBodyWidth(double bodyWidth) {
        this.bodyWidth = bodyWidth;
    }

    public double getBodyHeight() {
        return bodyHeight;
    }

    public void setBodyHeight(double bodyHeight) {
        this.bodyHeight = bodyHeight;
    }

}
