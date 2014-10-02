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
import java.security.cert.CertificateException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.kit.common.Messages;
import es.ree.eemws.kit.config.Configuration;


/**
 * Certificate settings panel.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 09/05/2014
 */
public final class IdentityPanel extends JFrame {

    /** Class ID. */
    private static final long serialVersionUID = 708755156498447247L;

    /** Panel name (int tab bar). */
    private static final String PANEL_NAME = Messages.getString("kit.gui.configuration.23");

    /** Dialogue to select certificate. */
    private JFileChooser certFileChooser;

    /** Text box display the path to Certificate File . */
    private JTextField certFile;

    /** Password to access certificate. */
    private JPasswordField certPassword1;

    /** Confirm password to access certificate. */
    private JPasswordField certPassword2;

    /**
     * Load certificate settings into form.
     * @param cm Configuration object from which values are read.
     */
    public void loadValues(final Configuration cm) {

        String value;
        value = cm.getKeyStoreFile();
        if (value == null) {
            value = Messages.getString("kit.gui.configuration.24");
        }
        certFile.setText(value);

        value = cm.getKeyStorePassword();
        if (value == null) {
            value = "";
        }
        certPassword1.setText(value);
        certPassword2.setText(value);
    }

    /**
     * Return panel containing Certificate settings.
     * @return Panel containing Certificate settings.
     */
    public JPanel getPanel() {

        certFile = new JTextField();
        certFile.setText("");
        certFile.setBounds(new Rectangle(154, 65, 204, 20));

        JLabel fileLbl = new JLabel();
        fileLbl.setDoubleBuffered(false);
        fileLbl.setRequestFocusEnabled(true);
        fileLbl.setDisplayedMnemonic('F');
        fileLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        fileLbl.setLabelFor(certFile);
        fileLbl.setText(Messages.getString("kit.gui.configuration.25"));
        fileLbl.setBounds(new Rectangle(17, 63, 134, 21));

        certPassword1 = new JPasswordField();
        certPassword1.setText("");
        certPassword1.setBounds(new Rectangle(154, 93, 204, 20));

        JLabel passLbl = new JLabel();
        passLbl.setDisplayedMnemonic('C');
        passLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        passLbl.setLabelFor(certPassword1);
        passLbl.setText(Messages.getString("kit.gui.configuration.26"));
        passLbl.setBounds(new Rectangle(17, 92, 129, 21));

        certPassword2 = new JPasswordField();
        certPassword2.setBounds(new Rectangle(154, 121, 204, 20));
        certPassword2.setText("");

        JLabel pass2Lbl = new JLabel();
        pass2Lbl.setBounds(new Rectangle(17, 121, 129, 17));
        pass2Lbl.setMaximumSize(new Dimension(88, 16));
        pass2Lbl.setDisplayedMnemonic('O');
        pass2Lbl.setHorizontalAlignment(SwingConstants.RIGHT);
        pass2Lbl.setHorizontalTextPosition(SwingConstants.LEFT);
        pass2Lbl.setVerticalTextPosition(SwingConstants.CENTER);
        pass2Lbl.setLabelFor(certPassword2);
        pass2Lbl.setText(Messages.getString("kit.gui.configuration.27"));

        JButton examine = new JButton();
        examine.setBounds(new Rectangle(369, 61, 110, 26));
        examine.setMnemonic('E');
        examine.setText(Messages.getString("kit.gui.configuration.28"));
        examine.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                findCertificate();
            }
        });

        JLabel dataLbl = new JLabel();
        dataLbl.setText(Messages.getString("kit.gui.configuration.29"));
        dataLbl.setBounds(new Rectangle(18, 35, 418, 16));

        certFileChooser = new JFileChooser();

        JPanel identity = new JPanel();
        identity.setMinimumSize(new Dimension(1, 1));
        identity.setLayout(null);
        identity.add(certFileChooser, null);
        identity.add(certPassword1, null);
        identity.add(fileLbl, null);
        identity.add(passLbl, null);
        identity.add(pass2Lbl, null);
        identity.add(certPassword2, null);
        identity.add(certFile, null);
        identity.add(examine, null);

        identity.add(dataLbl, null);
        return identity;
    }

    /**
     * Open file chooser to select path to certificate file.
     */
    private void findCertificate() {

        if (certFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

            File file = certFileChooser.getSelectedFile();
            if (!file.exists()) {

                JOptionPane.showMessageDialog(this, Messages.getString("kit.gui.configuration.30"), Messages.getString("kit.gui.configuration.31"), JOptionPane.ERROR_MESSAGE);

            } else {

                String path = file.getAbsolutePath();
                path = path.replaceAll("\\\\", "/");
                certFile.setText(path);
            }
        }
    }

    /**
     * Validate certificate panel.
     * @throws ConfigException If cannot access the certificate.
     */
    public void validar() throws ConfigException {

        String errMsg = null;

        File file = new File(certFile.getText());
        if (!file.exists()) {

            errMsg = Messages.getString("kit.gui.configuration.30");

        } else if (!file.canRead()) {

            errMsg = Messages.getString("kit.gui.configuration.32");

        } else {

            String pass1 = new String(certPassword1.getPassword());
            String pass2 = new String(certPassword2.getPassword());

            if (!pass1.equals(pass2)) {

                errMsg = Messages.getString("kit.gui.configuration.33");

            } else {

                try {

                    KeyStore ks = KeyStore.getInstance("PKCS12");
                    ks.load(new FileInputStream(certFile.getText()), pass1.toCharArray());

                } catch (KeyStoreException ex) {

                    errMsg = (new StringBuilder(Messages.getString("kit.gui.configuration.34"))).append(ex.getMessage()).toString();

                } catch (NoSuchAlgorithmException ex) {

                    errMsg = Messages.getString("kit.gui.configuration.35");

                } catch (CertificateException ex) {

                    errMsg = (new StringBuilder(Messages.getString("kit.gui.configuration.36"))).append(ex.getMessage()).toString();

                } catch (IOException ex) {

                    errMsg = (new StringBuilder(Messages.getString("kit.gui.configuration.37"))).append(ex.getMessage()).toString();

                } catch (SecurityException ex) {

                    String msg = ex.getMessage();
                    if (msg.indexOf("Unsupported keysize") != -1 && pass1.length() > 7) {

                        errMsg = Messages.getString("kit.gui.configuration.38");

                    } else {

                        errMsg = Messages.getString("kit.gui.configuration.39") + ex.getMessage();
                    }
                }
            }
        }

        if (errMsg != null) {

            throw new ConfigException(errMsg);
        }
    }

    /**
     * Sets Certificate settings.
     * @param config Configuration object containing certificate settings.
     */
    public void setValues(final Configuration config) {

        config.setKeyStoreFile(certFile.getText());
        config.setKeyStorePassword(new String(certPassword1.getPassword()));
    }

    /**
     * Return panel name.
     * @return Panel name.
     */
    public String getPanelName() {

        return PANEL_NAME;
    }
}
