package org.openpnp.gui.support;

import org.apache.commons.beanutils.PropertyUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.openpnp.model.AbstractModelObject;
import org.openpnp.model.Identifiable;
import org.openpnp.model.Named;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class ModelComboBoxModel<I extends Named & Identifiable> extends DefaultComboBoxModel implements PropertyChangeListener {
    final private AbstractModelObject parent;
    final private boolean addEmpty;
    final private String propertyName;
    //private Converter<I> converter = null;

    public ModelComboBoxModel(AbstractModelObject parent, String propertyName, boolean addEmpty) {
        this.parent = parent;
        this.addEmpty = addEmpty;
        this.propertyName = propertyName;
        if (parent != null) { // we're not in Window Builder Design Mode
            try {
                java.util.List<I> items = (java.util.List<I>) PropertyUtils.getProperty(parent, propertyName);
                addAllElements(items);
                //this.converter = new NamedConverter<I>(items);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            this.parent.addPropertyChangeListener(propertyName, this);
        }
    }
    private void addAllElements(java.util.List<I> items) {
        for (I item : items) {
            addElement(item);
        }
        if (addEmpty) {
            addElement(null);
        }
    }
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        removeAllElements();
        if (evt.getNewValue() instanceof java.util.List){
            addAllElements((List<I>) evt.getNewValue());
        }
    }
    public void bind(Object target, String property, JComboBox field) {
        BeanProperty<Object, I> targetBeanProperty = BeanProperty.create(property);
        BeanProperty<JComboBox, I> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
        AutoBinding<Object, I, JComboBox, I> autoBinding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, target, targetBeanProperty, field, jComboBoxBeanProperty);
        //autoBinding.setConverter(converter);
        autoBinding.bind();
    }
}