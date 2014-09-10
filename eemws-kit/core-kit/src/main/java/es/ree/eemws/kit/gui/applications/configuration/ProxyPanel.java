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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.kit.common.Messages;
import es.ree.eemws.kit.config.Configuration;


/**
 * Proxy settings panel.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 09/05/2014
 */
public final class ProxyPanel extends JFrame {

    /** Class ID. */
    private static final long serialVersionUID = -6849711727505413667L;

    /** Default proxy port value. */
    private static final String DEFAULT_PROXY_PORT = "8080";

    /** Panel name (on tab bar). */
    private static final String PANEL_NAME = Messages.getString("kit.gui.configuration.45");

    /** Label for {@link #proxyUser}. */
    private JLabel proxyUserLbl;

    /** Proxy user name. */
    private JTextField proxyUser;

    /** Label for {@link #proxyPassword1}. */
    private JLabel proxyPasswordLbl;

    /** Password for the proxy user. */
    private JPasswordField proxyPassword1;

    /** Confirm password for the proxy user. */
    private JPasswordField proxyPassword2;

    /** Label for {@link #proxyHost}. */
    private JLabel proxyHostLbl;

    /** Hostname for the proxy server. */
    private JTextField proxyHost;

    /** Listening port for the proxy server. */
    private JTextField proxyPort;

    /** Label for {@link #proxyPort}. */
    private JLabel proxyPortLbl;

    /** Button to indicate the connection will NOT be done through a proxy. */
    private JRadioButton proxyDontUseProxyChoice;

    /** Button to indicate the connection will be done through a proxy. */
    private JRadioButton proxyUseProxyChoice;

    /** Label for {@link #proxyPassword2}. */
    private JLabel proxyPasswdConfirmLbl;

    /**
     * Load proxy settings into form.
     * @param cm Configuration object from which values are read.
     */
    public void loadValues(final Configuration cm) {

        String value = cm.getProxyHost();
        if (value != null) {

            enableProxyElements(true);
            proxyUseProxyChoice.setSelected(true);
            proxyHost.setText(cm.getProxyHost());
            proxyUser.setText(cm.getProxyUser());
            proxyPort.setText(String.valueOf(cm.getProxyPort()));
            proxyPassword1.setText(cm.getProxyPassword());
            proxyPassword2.setText(cm.getProxyPassword());

        } else {

            proxyDontUseProxyChoice.setSelected(true);
            enableProxyElements(false);
        }
    }

    /**
     * Return panel containing Proxy settings.
     * @return Panel containing Proxy settings.
     */
    public JPanel getPanel() {

        proxyPassword1 = new JPasswordField();
        proxyPassword1.setEnabled(false);
        proxyPassword1.setText("");
        proxyPassword1.setBounds(new Rectangle(114, 79, 158, 19));

        proxyHost = new JTextField();
        proxyHost.setEnabled(false);
        proxyHost.setText("");
        proxyHost.setBounds(new Rectangle(115, 24, 156, 19));

        proxyHostLbl = new JLabel();
        proxyHostLbl.setEnabled(false);
        proxyHostLbl.setDisplayedMnemonic('H');
        proxyHostLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        proxyHostLbl.setLabelFor(proxyHost);
        proxyHostLbl.setText(Messages.getString("kit.gui.configuration.46"));
        proxyHostLbl.setBounds(new Rectangle(10, 27, 101, 16));

        proxyPort = new JTextField();
        proxyPort.setEnabled(false);
        proxyPort.setText("");
        proxyPort.setBounds(new Rectangle(332, 25, 43, 19));

        proxyPortLbl = new JLabel();
        proxyPortLbl.setEnabled(false);
        proxyPortLbl.setDisplayedMnemonic('P');
        proxyPortLbl.setLabelFor(proxyPort);
        proxyPortLbl.setText(Messages.getString("kit.gui.configuration.47"));
        proxyPortLbl.setBounds(new Rectangle(284, 25, 50, 22));

        proxyPasswdConfirmLbl = new JLabel();
        proxyPasswdConfirmLbl.setEnabled(false);
        proxyPasswdConfirmLbl.setDisplayedMnemonic('O');
        proxyPasswdConfirmLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        proxyPasswdConfirmLbl.setLabelFor(proxyPassword2);
        proxyPasswdConfirmLbl.setText(Messages.getString("kit.gui.configuration.48"));
        proxyPasswdConfirmLbl.setBounds(new Rectangle(10, 106, 101, 16));

        proxyPassword2 = new JPasswordField();
        proxyPassword2.setEnabled(false);
        proxyPassword2.setText("");
        proxyPassword2.setBounds(new Rectangle(115, 107, 158, 19));

        proxyUser = new JTextField();
        proxyUser.setEnabled(false);
        proxyUser.setText("");
        proxyUser.setBounds(new Rectangle(115, 52, 156, 19));

        proxyUserLbl = new JLabel();
        proxyUserLbl.setEnabled(false);
        proxyUserLbl.setDisplayedMnemonic('U');
        proxyUserLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        proxyUserLbl.setLabelFor(proxyUser);
        proxyUserLbl.setText(Messages.getString("kit.gui.configuration.49"));
        proxyUserLbl.setBounds(new Rectangle(10, 53, 101, 16));

        proxyPasswordLbl = new JLabel();
        proxyPasswordLbl.setEnabled(false);
        proxyPasswordLbl.setDisplayedMnemonic('C');
        proxyPasswordLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        proxyPasswordLbl.setLabelFor(proxyPassword1);
        proxyPasswordLbl.setText(Messages.getString("kit.gui.configuration.50"));
        proxyPasswordLbl.setBounds(new Rectangle(10, 80, 101, 16));

        JPanel proxyPanel = new JPanel();
        TitledBorder bordeProxy;
        bordeProxy = new TitledBorder(new EtchedBorder(EtchedBorder.RAISED, Color.white, new Color(142, 142, 142)), " " + Messages.getString("kit.gui.configuration.51") + " ");
        proxyPanel.setBorder(bordeProxy);
        proxyPanel.setBounds(new Rectangle(47, 89, 396, 139));
        proxyPanel.setLayout(null);
        proxyPanel.add(proxyPassword1, null);
        proxyPanel.add(proxyPassword2, null);
        proxyPanel.add(proxyPort, null);
        proxyPanel.add(proxyPortLbl, null);
        proxyPanel.add(proxyUser, null);
        proxyPanel.add(proxyHost, null);
        proxyPanel.add(proxyPasswdConfirmLbl, null);
        proxyPanel.add(proxyHostLbl, null);
        proxyPanel.add(proxyUserLbl, null);
        proxyPanel.add(proxyPasswordLbl, null);

        proxyDontUseProxyChoice = new JRadioButton();
        proxyDontUseProxyChoice.setEnabled(true);
        proxyDontUseProxyChoice.setMnemonic('N');
        proxyDontUseProxyChoice.setSelected(true);
        proxyDontUseProxyChoice.setText(Messages.getString("kit.gui.configuration.52"));
        proxyDontUseProxyChoice.setBounds(new Rectangle(43, 34, 215, 24));
        proxyDontUseProxyChoice.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                enableProxyElements(false);
            }
        });

        proxyUseProxyChoice = new JRadioButton();
        proxyUseProxyChoice.setMnemonic('X');
        proxyUseProxyChoice.setText(Messages.getString("kit.gui.configuration.53"));
        proxyUseProxyChoice.setBounds(new Rectangle(43, 59, 213, 24));
        proxyUseProxyChoice.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                enableProxyElements(true);
            }
        });

        ButtonGroup grupoProxy = new ButtonGroup();
        grupoProxy.add(proxyDontUseProxyChoice);
        grupoProxy.add(proxyUseProxyChoice);

        JLabel proLbl = new JLabel();
        proLbl.setRequestFocusEnabled(true);
        proLbl.setText(Messages.getString("kit.gui.configuration.54"));
        proLbl.setBounds(new Rectangle(43, 11, 420, 24));

        JPanel proxy = new JPanel();
        proxy.setAlignmentX((float) 0.5);
        proxy.setLayout(null);
        proxy.add(proLbl, null);
        proxy.add(proxyDontUseProxyChoice, null);
        proxy.add(proxyUseProxyChoice, null);
        proxy.add(proxyPanel, null);

        return proxy;
    }

    /**
     * Disable proxy form fields when '[do NOT] use proxy'
     * is selected.
     * @param useProxy <code>true</code> user selects use proxy.
     * , <code>false</code> otherwise.
     */
    private void enableProxyElements(final boolean useProxy) {

        proxyHostLbl.setEnabled(useProxy);
        proxyUserLbl.setEnabled(useProxy);
        proxyPasswordLbl.setEnabled(useProxy);
        proxyPortLbl.setEnabled(useProxy);
        proxyHost.setEnabled(useProxy);
        proxyUser.setEnabled(useProxy);
        proxyPassword1.setEnabled(useProxy);
        proxyPassword2.setEnabled(useProxy);
        proxyPort.setEnabled(useProxy);
        proxyHost.setEditable(useProxy);
        proxyUser.setEditable(useProxy);
        proxyPassword1.setEditable(useProxy);
        proxyPassword2.setEditable(useProxy);
        proxyPort.setEditable(useProxy);
        proxyPasswdConfirmLbl.setEnabled(useProxy);
    }

    /**
     * Set proxy settings.
     * @param config Object containing proxy settings.
     */
    public void setValues(final Configuration config) {

        if (proxyUseProxyChoice.isSelected()) {

            config.setProxyHost(proxyHost.getText());
            config.setProxyPort(proxyPort.getText());
            config.setProxyPassword(new String(proxyPassword1.getPassword()));
            config.setProxyUser(proxyUser.getText());

        } else {

            config.setProxyHost(null);
            config.setProxyPort(null);
            config.setProxyPassword(null);
            config.setProxyUser(null);
        }
    }

    /**
     * Validate proxy settings.
     * @throws ConfigException If any incorrect value is found.
     */
    public void validar() throws ConfigException {

        String errMsg = null;

        if (proxyUseProxyChoice.isSelected()) {

            String value = proxyHost.getText().trim();
            if (value.length() == 0) {

                errMsg = Messages.getString("kit.gui.configuration.55");

            } else {

                proxyHost.setText(value);
                value = proxyPort.getText().trim();
                if (value.length() == 0) {

                    value = DEFAULT_PROXY_PORT;
                }

                try {

                    int val = Integer.parseInt(value);
                    if (val < 0 || val > 65535) {

                        errMsg = Messages.getString("kit.gui.configuration.56");
                    }

                } catch (NumberFormatException ex) {

                    errMsg = Messages.getString("kit.gui.configuration.57");
                }

                proxyPort.setText(value);

                if (errMsg == null) {

                    String pass1 = new String(proxyPassword1.getPassword());
                    String pass2 = new String(proxyPassword2.getPassword());

                    if (!pass1.equals(pass2)) {

                        errMsg = Messages.getString("kit.gui.configuration.58");

                    } else {

                        String prUser = proxyUser.getText().trim();
                        if (prUser.length() > 0 && pass1.length() == 0) {

                            errMsg = Messages.getString("kit.gui.configuration.59");

                        } else if (prUser.length() == 0 && pass1.length() != 0) {

                            errMsg = Messages.getString("kit.gui.configuration.60");
                        }
                    }
                }
            }

            if (errMsg != null) {

                throw new ConfigException(errMsg);
            }
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
