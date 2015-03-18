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
import java.util.logging.Level;
import java.util.logging.Logger;

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
	private static final long serialVersionUID = 5557129823051375172L;

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
		Configurator config = new Configurator();
		config.setVisible(true);
	}

	/**
	 * Constructor. Invoke creation graphic elements, read current settings and update elements according to this
	 * settings.
	 */
	public Configurator() {

		try {

			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		} catch (Exception ex) {

			/*
			 * ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
			 * This block should be unreachable.
			 */
			JOptionPane.showMessageDialog(this, Messages.getString("SETTINGS_NO_GUI") + ex.getMessage(), Messages.getString("MSG_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
		}

		Configuration commonConfig = new Configuration();
		try {
			commonConfig.readConfiguration();
		} catch (ConfigException ex) {

			JOptionPane.showMessageDialog(this,  
        			Messages.getString("SETTINGS_NO_CONFIG"),  //$NON-NLS-1$
        			Messages.getString("MSG_INFO_TITLE"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
			Logger.getLogger(getClass().getName()).log(Level.FINE, "", ex); //$NON-NLS-1$
		}

		identityPanel = new IdentityPanel();
		identityPanel.loadValues(commonConfig);

		proxyPanel = new ProxyPanel();
		proxyPanel.loadValues(commonConfig);

		serverPanel = new ServerPanel();
		serverPanel.loadValues(commonConfig);

		folderPanel = new FolderPanel();
		try {
			folderPanel.loadValues();
		} catch (ConfigException ex) {

			/* Ignore errors on load. */
			Logger.getLogger(getClass().getName()).log(Level.FINE, "", ex); //$NON-NLS-1$
		}

		initGraphicElements();
	}

	/**
	 * Initializes graphic elements. Create and arrange elements on screen.
	 */
	private void initGraphicElements() {

		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource(es.ree.eemws.kit.gui.common.Constants.ICON_PATH)));

		/* Adding buttons */
		getContentPane().add(getButtonPanel(), BorderLayout.SOUTH);

		/* Adding central panel (tab panel) */
		getContentPane().add(getMainPanel(), BorderLayout.CENTER);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setTitle(Messages.getString("SETTINGS_TITLE")); //$NON-NLS-1$

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
	 * Returns panel containing every settings tab.
	 * @return Panel Panel containing settings tabs.
	 */
	private JTabbedPane getMainPanel() {

		main = new JTabbedPane();
		main.setMinimumSize(new Dimension(500, 500));
		main.add(serverPanel, serverPanel.getPanelName());
		main.add(identityPanel, identityPanel.getPanelName());
		main.add(proxyPanel, proxyPanel.getPanelName());
		main.add(folderPanel, folderPanel.getPanelName());

		return main;
	}

	/**
	 * Returns panel contaning 'OK' and 'Cancel' buttons.
	 * @return Panel containing these common buttons.
	 */
	private JPanel getButtonPanel() {

		JButton accept = new JButton();
		accept.setMaximumSize(new Dimension(100, 26));
		accept.setMinimumSize(new Dimension(100, 26));
		accept.setPreferredSize(new Dimension(100, 26));
		accept.setToolTipText(Messages.getString("SETTINGS_OK_BUTTON_TIP")); //$NON-NLS-1$
		accept.setIcon(null);
		accept.setMnemonic(Messages.getString("SETTINGS_OK_BUTTON_HK").charAt(0)); //$NON-NLS-1$
		accept.setText(Messages.getString("SETTINGS_OK_BUTTON")); //$NON-NLS-1$
		accept.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				saveChanges();
			}
		});

		JLabel buttonSeparator = new JLabel();
		buttonSeparator.setText("          "); //$NON-NLS-1$
		JButton cancel = new JButton();
		cancel.setMaximumSize(new Dimension(100, 26));
		cancel.setMinimumSize(new Dimension(100, 26));
		cancel.setPreferredSize(new Dimension(100, 26));
		cancel.setMnemonic(Messages.getString("SETTINGS_CANCEL_BUTTON_HK").charAt(0)); //$NON-NLS-1$
		cancel.setText(Messages.getString("SETTINGS_CANCEL_BUTTON")); //$NON-NLS-1$
		cancel.setToolTipText(Messages.getString("SETTINGS_CANCEL_BUTTON_TIP")); //$NON-NLS-1$
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
	 * Method triggered when 'Cancel' button is pressed. Exit program without making changes.
	 */
	private void cancelChanges() {

		int respuesta = JOptionPane.showConfirmDialog(this,
				Messages.getString("SETTINGS_CANCEL_TEXT"), //$NON-NLS-1$
				Messages.getString("MSG_CONFIRM_TITLE"), //$NON-NLS-1$				
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

		if (respuesta == JOptionPane.OK_OPTION) {

			System.exit(0); //NOSONAR We want to force application to exit.
		}
	}

	/**
	 * Method triggered when 'OK' button is pressed. Save changes and exit.
	 */
	private void saveChanges() {

        int res = JOptionPane.showConfirmDialog(this,                
                Messages.getString("SETTINGS_SAVE_CHANGES"), //$NON-NLS-1$
                Messages.getString("MSG_CONFIRM_TITLE"), //$NON-NLS-1$
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
        	boolean wantToSave = true;
        	
            try {
            	serverPanel.validateConfig();
                identityPanel.validateConfig();
                proxyPanel.validateConfig();
                
                try {
					folderPanel.validateConfig();
				} catch (ConfigException e) {
					res = JOptionPane.showConfirmDialog(this,
							Messages.getString("SETTINGS_CONFIG_MAGIC_FOLDER") + "\n" + e.getMessage(), //$NON-NLS-1$ //$NON-NLS-2$
			                Messages.getString("MSG_WARNING_TITLE"), //$NON-NLS-1$			               
			                JOptionPane.OK_CANCEL_OPTION,
			                JOptionPane.QUESTION_MESSAGE);
					
					wantToSave = (res == JOptionPane.OK_OPTION);
						 
				}
                
                if (wantToSave) {
                	saveConfig();
                	System.exit(0); //NOSONAR We want to force application to exit.
                }
              
            } catch (ConfigException ex) {
            	
            	JOptionPane.showMessageDialog(this,  
            			Messages.getString("SETTINGS_CONFIG_HAS_ERRORS") + ex.getMessage(),  //$NON-NLS-1$
            			Messages.getString("MSG_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
            } 
        }
    }

	/**
	 * Saves settings file.
	 * @throws ConfigException If settings file cannot be saved.
	 */
	private void saveConfig() throws ConfigException {

		Configuration config = new Configuration();
		identityPanel.setValues(config);
		serverPanel.setValues(config);
		proxyPanel.setValues(config);
		config.writeConfiguration();

		folderPanel.setValues();
	}

}
