package org.openpnp.spi;

import org.openpnp.gui.support.Wizard;
import org.openpnp.model.*;

/**
 * A method to allow alignment the nozzle. Bottom vision
 * is an implementation of this interface, but other implementations could include laser
 * alignment or pit alignment.
 */
public interface NozzleTipLocator extends Identifiable, Named, PropertySheetHolder {

    public class NozzleTipAlignmentOffset
    {
        private Location location;

        public Location getLocation()
        {
            return location;
        }


        public NozzleTipAlignmentOffset(Location loc)
        {
            location=loc;
        }

        public String toString() {
            return "offset ( location: " + location.toString() + " )";
        }
    }

    /**
     * Perform the part alignment operation. The method must return a Location containing
     * the offsets on the nozzle of the aligned part and these offsets will be applied
     * by the JobProcessor. The offsets returned may be zero if the alignment process
     * results in physical alignment of the part as in the case of pit based alignment. The
     * Z portion of the Location is ignored.
     * @param tip
     * @param location
     * @param nozzle
     * @return
     * @throws Exception if the alignment fails for any reason. The caller may retry.
     */
    NozzleTipAlignmentOffset findOffsets(NozzleTip tip, Location location, Nozzle nozzle) throws Exception;

    /**
     * Get the Wizard
     * Part.
     * @return
     */
    Wizard getConfigurationWizard();

}
