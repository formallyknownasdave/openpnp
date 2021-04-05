package org.openpnp.machine.reference.vision.wizards;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.openpnp.gui.components.ComponentDecorators;
import org.openpnp.gui.support.AbstractConfigurationWizard;
import org.openpnp.gui.support.DoubleConverter;
import org.openpnp.gui.support.IntegerConverter;
import org.openpnp.model.LeadGroup;
import org.openpnp.model.Package;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;

public class AbstractAnalysisConfigurationWizard extends AbstractConfigurationWizard {
    private Package pkg;
    JPanel panelPreview;
    private JTextField textFieldWidth;
    private JTextField textFieldLength;
    private JTextField textFieldHeight;
    private JCheckBox checkBoxTotalDimensions;
    public class PreviewPanel extends JPanel {
        private double scale = 50;
        private double width = 1.75;
        private double length =  2.75;
        private double height = 1.47;
        ArrayList<LeadGroup> groups;
        PreviewPanel(ArrayList<LeadGroup> groups) {
            this.setBackground(Color.black);
            this.groups = groups;
        }
        public LeadGroup getLeadGroupByName(String name) {
            for (LeadGroup group : groups) {
                if (group.getName().equals(name)) {
                    return group;
                }
            }
            return null;
        }
        public void paintComponent(Graphics g) {

            super.paintComponent(g);
            int xOrigin = this.getWidth() / 2, yOrigin = this.getHeight() / 2;
            this.scale = Math.min((this.getWidth() / this.width) * 0.5, (this.getHeight() / this.length) * 0.5);
            g.setColor(Color.darkGray);
            g.fillRect(
                    xOrigin - (int)(this.width * this.scale / 2),
                    yOrigin - (int)(this.length * this.scale / 2),
                    (int)(this.width * this.scale),
                    (int)(this.length * this.scale));
            LeadGroup group = groups.get(0);
            g.setColor(Color.lightGray);
            // Left
            group = getLeadGroupByName("R1");
            for (int i = 0; i < group.getLeadRows(); i++) {
                g.fillRect(
                        xOrigin - (int)((this.width/2 + group.getLength()) * this.scale),
                        yOrigin + (int)((- (group.getPitch() * (group.getLeadRows() - 1)) / 2 - (group.getWidth() / 2) + (i * (group.getPitch()))) * this.scale),
                        (int)(group.getLength() * this.scale),
                        (int)(group.getWidth() * this.scale));
            }
            // Bottom
            group = getLeadGroupByName("R2");
            for (int i = 0; i < group.getLeadColumns(); i++) {
                g.fillRect(
                        xOrigin + (int)((- (group.getPitch() * (group.getLeadColumns() - 1)) / 2 - (group.getWidth() / 2) + (i * (group.getPitch()))) * this.scale),
                        yOrigin + (int)((this.length/2) * this.scale),
                        (int)(group.getWidth() * this.scale),
                        (int)(group.getLength() * this.scale));
            }
            //Right
            group = getLeadGroupByName("R3");
            for (int i = 0; i < group.getLeadRows(); i++) {
                g.fillRect(
                        xOrigin + (int)((this.width/2) * this.scale),
                        yOrigin + (int)((- (group.getPitch() * (group.getLeadRows() - 1)) / 2 - (group.getWidth() / 2) + (i * (group.getPitch()))) * this.scale),
                        (int)(group.getLength() * this.scale),
                        (int)(group.getWidth() * this.scale));
            }
            //Top
            group = getLeadGroupByName("R4");
            for (int i = 0; i < group.getLeadColumns(); i++) {
                g.fillRect(
                        xOrigin + (int)((- (group.getPitch() * (group.getLeadColumns() - 1)) / 2 - (group.getWidth() / 2) + (i * (group.getPitch()))) * this.scale),
                        yOrigin - (int)(((this.length/2) + group.getLength()) * this.scale),
                        (int)(group.getWidth() * this.scale),
                        (int)(group.getLength() * this.scale));
            }
        }
    }

    public AbstractAnalysisConfigurationWizard(org.openpnp.model.Package pkg) {
        this.pkg = pkg;
    }
    public JPanel getDimensionsPanel(){
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(null, "Dimensions", TitledBorder.LEADING, TitledBorder.TOP,
                null, null));
        panel.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("right:default"),
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"),},
                new RowSpec[] {
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,}));


        JLabel lblWidth = new JLabel("Width");
        panel.add(lblWidth, "2, 2");

        textFieldWidth = new JTextField();
        panel.add(textFieldWidth, "4, 2");

        JLabel lblLength = new JLabel("Length");
        panel.add(lblLength, "2, 4");

        textFieldLength = new JTextField();
        panel.add(textFieldLength, "4, 4");

        JLabel lblHeight = new JLabel("Height");
        panel.add(lblHeight, "2, 6");

        textFieldHeight = new JTextField();
        panel.add(textFieldHeight, "4, 6");

        JLabel lblTotalDimensions = new JLabel("Total Dimensions?");
        panel.add(lblTotalDimensions, "2, 8");

        checkBoxTotalDimensions = new JCheckBox();
        panel.add(checkBoxTotalDimensions, "4, 8");
        return panel;
    }
    private ArrayList<JTextField> inputLeads = new ArrayList<>();
    private ArrayList<JTextField> inputWidth = new ArrayList<>();
    private ArrayList<JTextField> inputLength = new ArrayList<>();
    private ArrayList<JTextField> inputPitch = new ArrayList<>();
    public JPanel getLeadsPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(null, "Leads", TitledBorder.LEADING, TitledBorder.TOP,
                null, null));
        panel.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("right:default"),
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"),
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"),
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"),
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"),},
                new RowSpec[] {
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        RowSpec.decode("center:default"),
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,}));

        panel.add(new JLabel("Real #"),"2,4");
        panel.add(new JLabel("Length"),"2,6");
        panel.add(new JLabel("Width"),"2,8");
        panel.add(new JLabel("Pitch"),"2,10");
        for (int i = 0; i < 4; i++)
        {
            JTextField field;
            panel.add(new JLabel("R"+(i+1)),(4+i*2)+",2");
            field = new JTextField();
            inputLeads.add(field);
            panel.add(field,(4+i*2)+",4");
            field = new JTextField();
            inputLength.add(field);
            panel.add(field,(4+i*2)+",6");
            field = new JTextField();
            inputWidth.add(field);
            panel.add(field,(4+i*2)+",8");
            field = new JTextField();
            inputPitch.add(field);
            panel.add(field,(4+i*2)+",10");
        }
        return panel;
    }

    private LeadGroup getLeadGroupByName(ArrayList<LeadGroup> groups, String name) {
        for (LeadGroup group : groups) {
            if (group.getName().equals(name)) {
                return group;
            }
        }
        return null;
    }
    public void createLeadsPanelBindings(ArrayList<LeadGroup> groups) {
        IntegerConverter intConverter = new IntegerConverter();
        DoubleConverter doubleConverter = new DoubleConverter("%.3f");

        BeanProperty<LeadGroup, Integer> leadGroupLeadRowsProperty = BeanProperty.create("leadRows");
        BeanProperty<LeadGroup, Integer> leadGroupLeadColumnsProperty = BeanProperty.create("leadColumns");
        BeanProperty<LeadGroup, Double> leadGroupWidthProperty = BeanProperty.create("width");
        BeanProperty<LeadGroup, Double> leadGroupLengthProperty = BeanProperty.create("length");
        BeanProperty<LeadGroup, Double> leadGroupPitchProperty = BeanProperty.create("pitch");
        BeanProperty<JTextField, String> jTextFieldProperty = BeanProperty.create("text");

        for (int i = 0; i < 4; i++)
        {
            LeadGroup group = getLeadGroupByName(groups,"R"+(i+1));
            if (group == null) {
                if (i % 2 == 0) {
                    groups.add(new LeadGroup("R" + (i + 1), 3, 0, 0.5, 1, 1, ""));
                } else {
                    groups.add(new LeadGroup("R" + (i + 1), 0, 3, 0.5, 1, 1, ""));
                }
                group = getLeadGroupByName(groups,"R"+(i+1));
            }
            if (i % 2 == 0){
                //addWrappedBinding(group,"leadRows", inputLeads.get(i), "text", intConverter);
                AutoBinding<LeadGroup, Integer, JTextField, String> autoBindingLeadRows =
                        Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, group,
                                leadGroupLeadRowsProperty, inputLeads.get(i), jTextFieldProperty);
                autoBindingLeadRows.setConverter(intConverter);
                autoBindingLeadRows.bind();
            }
            else {
                //addWrappedBinding(group,"leadColumns", inputLeads.get(i), "text", intConverter);
                AutoBinding<LeadGroup, Integer, JTextField, String> autoBindingLeadColumns =
                        Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, group,
                                leadGroupLeadColumnsProperty, inputLeads.get(i), jTextFieldProperty);
                autoBindingLeadColumns.setConverter(intConverter);
                autoBindingLeadColumns.bind();
            }
            //addWrappedBinding(group,"width", inputWidth.get(i), "text", doubleConverter);
            //addWrappedBinding(group,"length", inputLength.get(i), "text", doubleConverter);
            //addWrappedBinding(group,"pitch", inputPitch.get(i), "text", doubleConverter);

            AutoBinding<LeadGroup, Double, JTextField, String> autoBindingWidth =
                    Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, group,
                            leadGroupWidthProperty, inputWidth.get(i), jTextFieldProperty);
            autoBindingWidth.setConverter(doubleConverter);
            autoBindingWidth.bind();

            AutoBinding<LeadGroup, Double, JTextField, String> autoBindingLength =
                    Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, group,
                            leadGroupLengthProperty, inputLength.get(i), jTextFieldProperty);
            autoBindingLength.setConverter(doubleConverter);
            autoBindingLength.bind();

            AutoBinding<LeadGroup, Double, JTextField, String> autoBindingPitch =
                    Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, group,
                            leadGroupPitchProperty, inputPitch.get(i), jTextFieldProperty);
            autoBindingPitch.setConverter(doubleConverter);
            autoBindingPitch.bind();

            ComponentDecorators.decorateWithAutoSelectAndLengthConversion(inputWidth.get(i));
            ComponentDecorators.decorateWithAutoSelectAndLengthConversion(inputLength.get(i));
            ComponentDecorators.decorateWithAutoSelectAndLengthConversion(inputPitch.get(i));
        }
    }
    @Override
    public void createBindings() {
        /*ComponentDecorators.decorateWithAutoSelect(textFieldMaxVisionPasses);
        ComponentDecorators.decorateWithAutoSelectAndLengthConversion(textFieldMaxLinearOffset);
        ComponentDecorators.decorateWithAutoSelect(textFieldMaxAngularOffset);
*/
    }
}
