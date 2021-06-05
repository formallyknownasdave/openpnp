/*
 * Copyright (C) 2020 <mark@makr.zone>
 * inspired and based on work
 * Copyright (C) 2011 Jason von Nieda <jason@vonnieda.org>
 *
 * This file is part of OpenPnP.
 *
 * OpenPnP is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * OpenPnP is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with OpenPnP. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * For more information about OpenPnP visit http://openpnp.org
 */

package org.openpnp.gui.support;

import org.openpnp.spi.Driver;
import org.openpnp.spi.NozzleTipLocator;
import org.openpnp.spi.base.AbstractMachine;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

@SuppressWarnings({"serial", "rawtypes"})
public class NozzleTipLocatorsComboBoxModel extends DefaultComboBoxModel implements PropertyChangeListener {
    final private AbstractMachine machine;
    final private boolean addEmpty;

    public NozzleTipLocatorsComboBoxModel(AbstractMachine machine, boolean addEmpty) {
        this.machine = machine;
        this.addEmpty = addEmpty;
        addAllElements();
        if (machine != null) { // we're not in Window Builder Design Mode
            this.machine.addPropertyChangeListener("nozzleTipLocators", this);
        }
    }

    private void addAllElements() {
        if (machine == null) {
            return;// we're in Window Builder Design Mode
        }
        for (NozzleTipLocator locator : machine.getNozzleTipLocators()) {
            addElement(locator.getName());
        }
        if (addEmpty) {
            addElement(new String());
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        removeAllElements();
        addAllElements();
    }
}
