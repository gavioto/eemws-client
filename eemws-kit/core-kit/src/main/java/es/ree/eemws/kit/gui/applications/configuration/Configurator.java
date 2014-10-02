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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.kit.common.Messages;
import es.ree.eemws.kit.config.Configuration;


/**
 * Settings manager for the connection kit.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 09/05/2014
 */
public final class Configurator extends JFrame {

    /** Class ID. */
    private static final long serialVersionUID = 906215308146814064L;

    /** Certificate settings Panel. */
    private IdentityPanel identityPanel;

    /** Proxy access Settings. */
    private ProxyPanel proxyPanel;

    /** Server and service settings panel . */
    private ServerPanel serverPanel;

    /** Magic folder settings panel. */
    private FolderPanel folderPanel;

    /** Main panel on which panels are put. */
    private JTabbedPane main = null;

    /**
     * Starts Application.
     * @param args -Ignored-
     */
    public static void main(final String[] args) {

        Locale.setDefault(Locale.ENGLISH);
        Configurator config = new Configurator();
        config.setVisible(true);
    }

    /**
     * Constructor. Invoke creation graphic elements,
     * read current settings and update elements
     * according to this settings.
     */
    public Configurator() {

        try {

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        } catch (Exception ex) {

            /*
             * ClassNotFoundException, InstantiationException,
             * IllegalAccessException,
             * UnsupportedLookAndFeelException
             * This block should be unreachable.
             */
            JOptionPane.showMessageDialog(this, Messages.getString("kit.gui.configuration.0") + ex.getMessage(), Messages.getString("kit.gui.configuration.11"), JOptionPane.ERROR_MESSAGE);
        }

        boolean reportedError = false;
        Configuration config = new Configuration();
        try {

            config.readConfiguration();

        } catch (ConfigException ex) {

            reportedError = true;

        }

        identityPanel = new IdentityPanel();
        proxyPanel = new ProxyPanel();
        serverPanel = new ServerPanel();
        folderPanel = new FolderPanel();
        initGraphicElements();

        try {

            loadValues(config);

        } catch (ConfigException ex) {

            /* Prevent showing incorrect settings panel twice. */
            if (!reportedError) {

                JOptionPane.showMessageDialog(null, Messages.getString("kit.gui.configuration.2") + ex.getMessage(), Messages.getString("kit.gui.configuration.12"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Load settings into panels.
     * @param cm Configuration module from which settings are read.
     * @throws ConfigException If settings cannot be loaded.
     */
    private void loadValues(final Configuration cm) throws ConfigException {

        identityPanel.loadValues(cm);
        proxyPanel.loadValues(cm);
        serverPanel.loadValues(cm);
        folderPanel.loadValues();
        
    }

    /**
     * Initialize graphic elements. Create and arrange elements on screen.
     */
    private void initGraphicElements() {

        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource(es.ree.eemws.kit.gui.common.Constants.ICON_PATH)));

        /* Adding buttons */
        getContentPane().add(getButtonPanel(), BorderLayout.SOUTH);

        /* Adding central panel (tab panel) */
        getContentPane().add(getMainPanel(), BorderLayout.CENTER);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(Messages.getString("kit.gui.configuration.3"));

        /* Event triggered when window closes. */
        addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent e) {
                cancelChanges();
            }
        });

        setResizable(false);
        setSize(520, 360);
        Dimension screen = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
        setLocation(screen.width / 2 - getSize().width / 2, screen.height / 2 - getSize().height / 2);
    }

    /**
     * Return panel containing every settings tab.
     * @return Panel Panel containing settings tabs.
     */
    private JTabbedPane getMainPanel() {

        main = new JTabbedPane();
        main.setMinimumSize(new Dimension(500, 500));
        main.add(serverPanel.getPanel(), serverPanel.getPanelName());
        main.add(identityPanel.getPanel(), identityPanel.getPanelName());
        main.add(proxyPanel.getPanel(), proxyPanel.getPanelName());
        main.add(folderPanel.getPanel(), folderPanel.getPanelName());
        
        return main;
    }

    /**
     * Return panel contaning 'OK' and 'Cancel' buttons.
     * @return Panel containing these common buttons.
     */
    private JPanel getButtonPanel() {

        JButton accept = new JButton();
        accept.setMaximumSize(new Dimension(100, 26));
        accept.setMinimumSize(new Dimension(100, 26));
        accept.setPreferredSize(new Dimension(100, 26));
        accept.setToolTipText(Messages.getString("kit.gui.configuration.4"));
        accept.setIcon(null);
        accept.setMnemonic('O');
        accept.setText(Messages.getString("kit.gui.configuration.5"));
        accept.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                saveChanges();
            }
        });

        JLabel buttonSeparator = new JLabel();
        buttonSeparator.setText("          ");
        JButton cancel = new JButton();
        cancel.setMaximumSize(new Dimension(100, 26));
        cancel.setMinimumSize(new Dimension(100, 26));
        cancel.setPreferredSize(new Dimension(100, 26));
        cancel.setMnemonic('N');
        cancel.setText(Messages.getString("kit.gui.configuration.6"));
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                cancelChanges();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(accept, null);
        buttonPanel.add(buttonSeparator, null);
        buttonPanel.add(cancel, null);
        return buttonPanel;
    }

    /**
     * Method triggered when 'Cancel' button is pressed.
     * Exit program without making changes.
     */
    private void cancelChanges() {

        int respuesta = JOptionPane.showConfirmDialog(this,
                Messages.getString("kit.gui.configuration.7"),
                Messages.getString("kit.gui.configuration.8"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (respuesta == JOptionPane.OK_OPTION) {

            System.exit(0);
        }
    }

    /**
     * Method triggered when 'OK' button is pressed.
     * Save changes and exit.
     */
    private void saveChanges() {

        int respuesta = JOptionPane.showConfirmDialog(this,
                Messages.getString("kit.gui.configuration.9"),
                Messages.getString("kit.gui.configuration.8"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (respuesta == JOptionPane.OK_OPTION) {

            try {

                validateElements();
                if (validateFolderPanel()) {
                    saveConfig();
                    System.exit(0);
                }

            } catch (ConfigException ex) {

                Object[] paramsText = {ex.getMessage()};
                String errorText = MessageFormat.format(Messages.getString("kit.gui.configuration.10"), paramsText);
                JOptionPane.showMessageDialog(this, errorText, Messages.getString("kit.gui.configuration.12"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Save settings file.
     * @throws ConfigException If settings file cannot be saved.
     */
    private void saveConfig() throws ConfigException {

        Configuration config = new Configuration();
        identityPanel.setValues(config);
        serverPanel.setValues(config);
        proxyPanel.setValues(config);
        config.writeConfiguration();
        if (folderPanel != null) {

            folderPanel.setValues();
        }
    }

    /**
     * Validate values entered in forms.
     * @throws ConfigException If any validation is incorrect.
     */
    private void validateElements() throws ConfigException {

        serverPanel.validar();
        identityPanel.validar();
        proxyPanel.validar();
    }

    /**
     * Validate folder panel. Errors in this panel are shown as warnings, is
     * possible store changes despite they (folder) are incorrect
     * @return <code>true</code> If settings file can be saved
     * (either folder settings are correct or are incorrect but user chooses
     * "continue"). <code>false</code> If folder settings are incorrect
     * user chooses "cancel".
     */
    private boolean validateFolderPanel() {
        boolean continua = true;

        if (folderPanel != null) {
            try {
                folderPanel.validation();
            } catch (ConfigException ex) {

                /* Ignore unable to access certificate error. */
                String errMsg = ex.getMessage();
                if (errMsg == null
                        || (errMsg != null && errMsg.indexOf(Messages.getString("kit.gui.configuration.39")) == -1)) {
                    int opcion = JOptionPane.showConfirmDialog(this, ex.getMessage()
                            + Messages.getString("kit.gui.configuration.67"),
                            Messages.getString("kit.gui.configuration.11"), JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION);
                    continua = (opcion == JOptionPane.OK_OPTION);
                }
            }
        }

        return continua;
    }
}
