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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.core.utils.security.CryptoException;
import es.ree.eemws.core.utils.security.CryptoManager;
import es.ree.eemws.kit.common.Messages;
import es.ree.eemws.kit.config.Configuration;

/**
 * Certificate settings panel.
 * 
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 09/05/2014
 */
public final class IdentityPanel extends JPanel {

	/** Class ID. */
	private static final long serialVersionUID = -7404267855042270397L;

	/** Panel name (int tab bar). */
	private static final String PANEL_NAME = Messages.getString("SETTINGS_IDENTITY_TAB"); //$NON-NLS-1$

	/** Dialogue to select certificate. */
	private JFileChooser certFileChooser;

	/** Text box display the path to Certificate File . */
	private JTextField certFile;

	/** Password to access certificate. */
	private JPasswordField certPassword1;

	/** Confirm password to access certificate. */
	private JPasswordField certPassword2;

	/** Key store type. */
	private JComboBox<String> storeType;

	/**
	 * Constructor. Creates a new panel with all the elements to setup the client certificate.
	 */
	public IdentityPanel() {

		certFile = new JTextField();
		certFile.setText(""); //$NON-NLS-1$
		certFile.setBounds(new Rectangle(154, 65, 204, 20));

		JLabel fileLbl = new JLabel();
		fileLbl.setDoubleBuffered(false);
		fileLbl.setRequestFocusEnabled(true);
		fileLbl.setDisplayedMnemonic(Messages.getString("SETTINGS_IDENTITY_CERTIFICATE_FILE_HK").charAt(0)); //$NON-NLS-1$
		fileLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		fileLbl.setLabelFor(certFile);
		fileLbl.setText(Messages.getString("SETTINGS_IDENTITY_CERTIFICATE_FILE")); //$NON-NLS-1$
		fileLbl.setBounds(new Rectangle(17, 63, 134, 21));

		certPassword1 = new JPasswordField();
		certPassword1.setText(""); //$NON-NLS-1$
		certPassword1.setBounds(new Rectangle(154, 93, 204, 20));

		JLabel passLbl = new JLabel();
		passLbl.setDisplayedMnemonic(Messages.getString("SETTINGS_IDENTITY_CERTIFICATE_PASSWORD_HK").charAt(0)); //$NON-NLS-1$
		passLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		passLbl.setLabelFor(certPassword1);
		passLbl.setText(Messages.getString("SETTINGS_IDENTITY_CERTIFICATE_PASSWORD")); //$NON-NLS-1$
		passLbl.setBounds(new Rectangle(17, 92, 129, 21));

		certPassword2 = new JPasswordField();
		certPassword2.setBounds(new Rectangle(154, 121, 204, 20));
		certPassword2.setText(""); //$NON-NLS-1$

		JLabel pass2Lbl = new JLabel();
		pass2Lbl.setBounds(new Rectangle(17, 121, 129, 17));
		pass2Lbl.setDisplayedMnemonic(Messages.getString("SETTINGS_IDENTITY_CERTIFICATE_PASSWORD2_HK").charAt(0)); //$NON-NLS-1$
		pass2Lbl.setHorizontalAlignment(SwingConstants.RIGHT);
		pass2Lbl.setLabelFor(certPassword2);
		pass2Lbl.setText(Messages.getString("SETTINGS_IDENTITY_CERTIFICATE_PASSWORD2")); //$NON-NLS-1$

		JButton examine = new JButton();
		examine.setBounds(new Rectangle(369, 60, 110, 26));
		examine.setMnemonic(Messages.getString("SETTINGS_IDENTITY_CERTIFICATE_BROWSE_HK").charAt(0)); //$NON-NLS-1$
		examine.setText(Messages.getString("SETTINGS_IDENTITY_CERTIFICATE_BROWSE")); //$NON-NLS-1$
		examine.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {  // NOSONAR event is not used.
				findCertificate();
			}
		});

		JLabel dataLbl = new JLabel();
		dataLbl.setText(Messages.getString("SETTINGS_IDENTITY_CERTIFICATE_DATA")); //$NON-NLS-1$
		dataLbl.setBounds(new Rectangle(18, 35, 418, 16));

		certFileChooser = new JFileChooser();

		storeType = new JComboBox<>();
		storeType.setBounds(154, 150, 204, 20);
		storeType.addItem("PKCS12"); //$NON-NLS-1$
		storeType.addItem("JKS"); //$NON-NLS-1$

		JLabel strtypeLbl = new JLabel();
		strtypeLbl.setText(Messages.getString("SETTINGS_IDENTITY_CERTIFICATE_TYPE")); //$NON-NLS-1$
		strtypeLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		strtypeLbl.setDisplayedMnemonic(Messages.getString("SETTINGS_IDENTITY_CERTIFICATE_TYPE_HK").charAt(0)); //$NON-NLS-1$
		strtypeLbl.setBounds(17, 150, 129, 17);
		strtypeLbl.setLabelFor(storeType);

	 
		setMinimumSize(new Dimension(1, 1));
		setLayout(null);
		add(certFileChooser, null);
		add(certPassword1, null);
		add(fileLbl, null);
		add(passLbl, null);
		add(pass2Lbl, null);
		add(certPassword2, null);
		add(certFile, null);
		add(examine, null);
		add(storeType, null);
		add(strtypeLbl, null);

	}

	/**
	 * Load certificate settings into form.
	 * @param cm Configuration object from which values are read.
	 */
	public void loadValues(final Configuration cm) {

		String value;
		value = cm.getKeyStoreFile();
		if (value == null) {
			value = ""; //$NON-NLS-1$
		}
		certFile.setText(value);

		value = cm.getKeyStorePassword();
		if (value == null) {
			value = ""; //$NON-NLS-1$
		}
		certPassword1.setText(value);
		certPassword2.setText(value);

		value = cm.getKeyStoreType();
		storeType.setSelectedItem(value);
	}

	/**
	 * Open file chooser to select path to certificate file.
	 */
	private void findCertificate() {

		if (certFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

			File file = certFileChooser.getSelectedFile();
			if (!file.exists()) {

				JOptionPane.showMessageDialog(this, Messages.getString("SETTINGS_IDENTITY_FILE_DOESNT_EXISTS"), //$NON-NLS-1$
						Messages.getString("SETTINGS_IDENTITY_FILE_DOESNT_EXISTS"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$

			} else {

				String path = file.getAbsolutePath();
				path = path.replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
				certFile.setText(path);
			}
		}
	}

	/**
	 * Validate certificate panel.
	 * @throws ConfigException If cannot access the certificate.
	 */
	public void validateConfig() throws ConfigException {

		String certFileStr = certFile.getText().trim();
		if (certFileStr.isEmpty()) {
			throw new ConfigException(getPanelName() + " " + Messages.getString("SETTINGS_PANEL_SAYS") + " "   //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
					+ Messages.getString("SETTINGS_IDENTITY_MUST_PROVIDE_CERTIFICATE_FILE")); //$NON-NLS-1$

		}
		
		File file = new File(certFileStr);
		if (!file.exists()) {

			throw new ConfigException(getPanelName() + " " + Messages.getString("SETTINGS_PANEL_SAYS") + " "   //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
					+ Messages.getString("SETTINGS_IDENTITY_FILE_DOESNT_EXISTS")); //$NON-NLS-1$

		} else if (!file.canRead()) {
			throw new ConfigException(getPanelName() + " " + Messages.getString("SETTINGS_PANEL_SAYS") + " "   //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$            
					+ Messages.getString("SETTINGS_IDENTITY_FILE_CANNOT_READ")); //$NON-NLS-1$

		} else {

			String pass1 = new String(certPassword1.getPassword());
			String pass2 = new String(certPassword2.getPassword());

			if (!pass1.equals(pass2)) {
				throw new ConfigException(getPanelName() + " " + Messages.getString("SETTINGS_PANEL_SAYS") + " "   //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
						+ Messages.getString("SETTINGS_IDENTITY_PASSWORD_MATCH")); //$NON-NLS-1$

			}

			try (FileInputStream certFileIS = new FileInputStream(certFile.getText())) {
				KeyStore ks = KeyStore.getInstance((String) storeType.getSelectedItem());

				ks.load(certFileIS, pass1.toCharArray());
				
				 Enumeration<String> keyAlias = ks.aliases();
		         String entryAlias = null;
		            boolean okAlias = false;

		            while (!okAlias && keyAlias.hasMoreElements()) {

		                try {

		                    entryAlias = keyAlias.nextElement();
		                    RSAPrivateKey privateKey = (RSAPrivateKey) ks.getKey(entryAlias, pass1.toCharArray());
		                    X509Certificate certificate = (X509Certificate) ks.getCertificate(entryAlias);
		                    certificate.checkValidity();
		                    
		                    // do not use keystore entries with no private key (usually a CA certificate)
		                    okAlias = privateKey != null;

		                } catch (CertificateException | UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {

		                    okAlias = false;
		                }
		            }

		            if (!okAlias) {

		            	throw new ConfigException(getPanelName() + " " + Messages.getString("SETTINGS_PANEL_SAYS") + " "   //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
								+ Messages.getString("SETTINGS_IDENTITY_NO_USABLE_CERTIFICATE")); //$NON-NLS-1$
		            }
				 
			} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
				throw new ConfigException(getPanelName() + " " + Messages.getString("SETTINGS_PANEL_SAYS") + " "   //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
						+ Messages.getString("SETTINGS_IDENTITY_CERTIFICATE_CANNOT_BE_READ")); //$NON-NLS-1$
			}

		}
	}

	/**
	 * Sets Certificate settings.
	 * @param config Configuration object containing certificate settings.
	 */
	public void setValues(final Configuration config) {

		config.setKeyStoreFile(certFile.getText());
		try {
			config.setKeyStorePassword(CryptoManager.encrypt(new String(certPassword1.getPassword())));
		} catch (CryptoException e) {
			config.setKeyStorePassword(new String(certPassword1.getPassword()));
		}
		config.setKeyStoreType((String) storeType.getSelectedItem());
	}

	/**
	 * Return panel name.
	 * @return Panel name.
	 */
	public String getPanelName() {

		return PANEL_NAME;
	}
}
