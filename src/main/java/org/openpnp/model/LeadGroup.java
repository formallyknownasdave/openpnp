package org.openpnp.model;

import org.simpleframework.xml.Attribute;

public class LeadGroup extends AbstractModelObject implements Named {
    @Attribute(required = true)
    private String name;

    @Attribute(required = true)
    private int leadRows, leadColumns;

    @Attribute(required = true)
    private double width, length, pitch;

    @Attribute
    private String skippedLeads; // Comma separated list of skipped lead numbers (starting at 1)

    public LeadGroup(String name, int leadRows, int leadColumns, double width, double length, double pitch, String skippedLeads) {
        this.name = name;
        this.leadRows = leadRows;
        this.leadColumns = leadColumns;
        this.width = width;
        this.length = length;
        this.pitch = pitch;
        this.skippedLeads = skippedLeads;
    }
    public LeadGroup clone() {
        return new LeadGroup(name,leadRows,leadColumns,width,length,pitch,skippedLeads);
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getLeadRows() { return leadRows; }
    public void setLeadRows(int leadRows) {
        int oldValue = this.leadRows;
        this.leadRows = leadRows;
        firePropertyChange("leadRows", oldValue, leadRows);
    }
    public int getLeadColumns() { return leadColumns; }
    public void setLeadColumns(int leadColumns) {
        int oldValue = this.leadColumns;
        this.leadColumns = leadColumns;
        firePropertyChange("leadColumns", oldValue, leadColumns);
    }
    public double getWidth() { return width; }
    public void setWidth(double width) {
        double oldValue = this.width;
        this.width = width;
        firePropertyChange("width", oldValue, width);
    }
    public double getLength() { return length; }
    public void setLength(double length) {
        double oldValue = this.length;
        this.length = length;
        firePropertyChange("length", oldValue, length);
    }
    public double getPitch() { return pitch; }
    public void setPitch(double pitch) {
        double oldValue = this.pitch;
        this.pitch = pitch;
        firePropertyChange("pitch", oldValue, pitch);
    }
}
