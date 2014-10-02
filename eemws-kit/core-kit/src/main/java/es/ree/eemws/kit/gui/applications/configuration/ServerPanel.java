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

import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.kit.common.Messages;
import es.ree.eemws.kit.config.Configuration;


/**
 * Server connection settings Panel.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 09/05/2014
 */
public final class ServerPanel extends JFrame {

    /** Class ID. */
    private static final long serialVersionUID = 0x4a75828270df61c0L;

    /** Panel name (on tab panel ). */
    private static final String PANEL_NAME = Messages.getString("kit.gui.configuration.61");

    /** Text field To display a non-preset URL. */
    private JTextField anotherURL;

    /** Label for {@link #anotherURL}. */
    private JLabel enterURLLbl;

    /**
     * Load server settings into form.
     * @param cm Configuration object from which values are read.
     */
    public void loadValues(final Configuration cm) {

        URL serviceURL = cm.getUrlEndPoint();

        if (serviceURL == null) {

            anotherURL.setText("");

        } else {

            anotherURL.setText(serviceURL.toString());
        }

        anotherURL.setVisible(true);
        enterURLLbl.setVisible(true);
    }

    /**
     * Obtains a panel containing Server connection settings.
     * @return Panel containing server connection settings.
     */
    public JPanel getPanel() {

        JPanel panelSrv = new JPanel();
        panelSrv.setLayout(null);
        panelSrv.setBounds(30, 20, 450, 180);
        panelSrv.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.RAISED, java.awt.Color.white, new java.awt.Color(142, 142, 142)), " " + Messages.getString("kit.gui.configuration.62") + " "));

        anotherURL = new JTextField();
        anotherURL.setText("");
        anotherURL.setBounds(100, 73, 320, 20);
        panelSrv.add(anotherURL);

        enterURLLbl = new JLabel(Messages.getString("kit.gui.configuration.63") + " ");
        enterURLLbl.setLabelFor(anotherURL);
        enterURLLbl.setDisplayedMnemonic('E');
        enterURLLbl.setBounds(25, 55, 105, 55);
        panelSrv.add(enterURLLbl, null);

        JPanel server = new JPanel();
        server.setLayout(null);
        server.setOpaque(true);
        server.add(panelSrv, null);

        return server;
    }

    /**
     * Sets Server connection settings.
     * @param config Configuration object.
     */
    public void setValues(final Configuration config) {

        String urlUriValue = anotherURL.getText().trim();
        config.setUrl(urlUriValue);
    }

    /**
     * Validate panel.
     * @throws ConfigException If the URL (on text box) is incorrect.
     */
    public void validar() throws ConfigException {

        String val = anotherURL.getText().trim();
        String errMsg = null;
        if (val.length() == 0) {

            errMsg = Messages.getString("kit.gui.configuration.64");

        } else if (!val.startsWith("https://")) {

            errMsg = Messages.getString("kit.gui.configuration.65");

        } else {

            int pos = val.indexOf("//") + 2;
            pos = val.indexOf('/', pos);
            if (pos == -1) {

                errMsg = Messages.getString("kit.gui.configuration.66");
            }
        }

        if (errMsg != null) {

            throw new ConfigException(errMsg);
        }
    }

    /**
     * Return Panel name.
     * @return Panel name.
     */
    public String getPanelName() {

        return PANEL_NAME;
    }
}
