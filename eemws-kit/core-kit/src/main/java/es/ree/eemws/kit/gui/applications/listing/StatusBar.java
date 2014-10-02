/*
 * Copyright 2014 Red Eléctrica de España, S.A.U.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation, version 3 of the license.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTIBIILTY or FITNESS FOR A PARTICULAR PURPOSE. See GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see
 * http://www.gnu.org/licenses/.
 *
 * Any redistribution and/or modification of this program has to make
 * reference to Red Eléctrica de España, S.A.U. as the copyright owner of
 * the program.
 */


package es.ree.eemws.kit.gui.applications.listing;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

/**
 * Simple status bar for application.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 02/06/2014
 *
 */
public final class StatusBar {

    /** Default text for status bar. */
    private static final String DEFAULT_STATUS = "Ready";

    /** Label containing current status text. */
    private JLabel lblStatus = null;

    /**
     * Return status bar panel.
     * @return status bar panel.
     */
    public JPanel getPanel() {

       lblStatus = new JLabel();
       lblStatus.setHorizontalAlignment(SwingConstants.LEFT);
       JPanel pnlStatus = new JPanel();
       pnlStatus.setBorder(new EtchedBorder());
       pnlStatus.setLayout(new BorderLayout());
       pnlStatus.add(lblStatus, BorderLayout.WEST);

       setStatus(DEFAULT_STATUS);

       return pnlStatus;
    }

    /**
     * Displays on bar the status passed as parameter.
     * @param status Status text.
     */
    public void setStatus(final String status) {
        lblStatus.setText(" " + status);
    }

}
