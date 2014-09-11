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
package es.ree.eemws.kit.gui.applications.configuration;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import es.ree.eemws.kit.common.Messages;
import es.ree.eemws.kit.config.Configuration;


/**
 * Miscellaneous settings panel.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 09/05/2014
 */
public final class MiscPanel extends JFrame {

    /** Class ID. */
    private static final long serialVersionUID = 703377863772246313L;

    /** Panel name (in tab bar). */
    private static final String PANEL_NAME = Messages.getString("kit.gui.configuration.40");

    /** Sets whether requests must be signed. */
    private JCheckBox cbSignRequest = null;

    /** Sets whether the response must be verified. */
    private JCheckBox cbVerifyResponse = null;

    /**
     * Load miscellaneous settings into form.
     * @param cm Configuration object from which values are read.
     */
    public void loadValues(final Configuration cm) {

        cbSignRequest.setSelected(cm.isSignResquest());
        cbVerifyResponse.setSelected(cm.isVerifySignResponse());
    }

    /**
     * Return panel containing Message options (misc.).
     * @return Panel containing Message options.
     */
    public JPanel getPanel() {

        JPanel outgoingOptionsPanel = new JPanel();
        outgoingOptionsPanel.setLayout(null);
        outgoingOptionsPanel.setBounds(new Rectangle(30, 10, 440, 100));
        outgoingOptionsPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.RAISED, Color.white, new Color(142, 142, 142)), " " + Messages.getString("kit.gui.configuration.42") + " "));

        cbSignRequest = new JCheckBox();
        cbSignRequest.setSelected(false);
        cbSignRequest.setBounds(new Rectangle(30, 40, 335, 20));
        cbSignRequest.setText(Messages.getString("kit.gui.configuration.41"));
        cbSignRequest.setMnemonic('S');

        outgoingOptionsPanel.add(cbSignRequest, null);

        JPanel incomingOptionsPanel = new JPanel();
        incomingOptionsPanel.setLayout(null);
        incomingOptionsPanel.setBounds(new Rectangle(30, 120, 440, 100));
        incomingOptionsPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.RAISED, Color.white, new Color(142, 142, 142)), " " + Messages.getString("kit.gui.configuration.43") + " "));

        cbVerifyResponse = new JCheckBox();
        cbVerifyResponse.setSelected(false);
        cbVerifyResponse.setBounds(new Rectangle(30, 40, 335, 20));
        cbVerifyResponse.setText(Messages.getString("kit.gui.configuration.44"));
        cbVerifyResponse.setMnemonic('V');

        incomingOptionsPanel.add(cbVerifyResponse, null);

        JPanel misc = new JPanel();
        misc.setMinimumSize(new Dimension(1, 1));
        misc.setLayout(null);

        misc.add(outgoingOptionsPanel, null);
        misc.add(incomingOptionsPanel, null);

        return misc;
    }

    /**
     * Sets message settings.
     * @param config Configuration object message settings.
     */
    public void setValues(final Configuration config) {

        config.setVerifySignResponse(cbVerifyResponse.isSelected());
        config.setSignResquest(cbSignRequest.isSelected());
    }

    /**
     * Return panel name.
     * @return Panel name.
     */
    public String getPanelName() {

        return PANEL_NAME;
    }
}
