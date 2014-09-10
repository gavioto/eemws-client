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
package es.ree.eemws.kit.gui.applications.editor;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.kit.common.Messages;
import es.ree.eemws.kit.config.Configuration;
import es.ree.eemws.kit.gui.common.ServiceMenuListener;


/**
 * Menu items containing possible services to which application can connect.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 09/05/2014
 */
public final class ServiceMenu {

    /** Available services. */
    private List<JRadioButtonMenuItem> services = new ArrayList<JRadioButtonMenuItem>();

    /** 'Other' services. */
    private JRadioButtonMenuItem otherService;

    /**
     * Constructor. Create Service Menu elements.
     * @param mslistener Reference to the application which invokes
     * Service menu creation. Allows communication between both
     * blocks.
     */
    public ServiceMenu(final ServiceMenuListener mslistener) {

        Configuration config = new Configuration();

        try {

            config.readConfiguration();

        } catch (ConfigException cE) {

            cE.printStackTrace();
        }

        otherService = new JRadioButtonMenuItem();
        otherService.setText(Messages.getString("kit.gui.editor.86") + " ");
        otherService.setMnemonic('O');
        services.add(otherService);

        URL url = config.getUrlEndPoint();
        String text = "";
        if (url != null) {
            text = url.toString();
        }
        otherService.setText((new StringBuilder(Messages.getString("kit.gui.editor.86") + " ")).append(text).toString());
        otherService.setEnabled(true);
        otherService.setSelected(true);

        ButtonGroup serviceGroupOptions = new ButtonGroup();

        for (JRadioButtonMenuItem service : services) {

            serviceGroupOptions.add(service);
        }
    }

    /**
     * Add Menu items to Service menu.
     * @param menu Menu item to which Menu items will be added.
     */
    public void addServiceElements(final JMenu menu) {

        for (JRadioButtonMenuItem service : services) {

            menu.add(service);
        }
    }
}
