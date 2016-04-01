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
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.kit.common.Messages;
import es.ree.eemws.kit.folders.Configuration;

/**
 * Panel containing Magic Folder settings (input, output, backup).
 * 
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 09/05/2014
 */
public final class FolderPanel extends JPanel {

	/** Class ID. */
	private static final long serialVersionUID = -3411637974163842418L;
	 
	/** Panel Name (in tab bar). */
	private static final String PANEL_NAME = Messages.getString("SETTINGS_FOLDER_TAB"); //$NON-NLS-1$

	/** Text box where absolute path to input folder is entered. */
	private JTextField txtInputFolderPath = null;

	/** Text box where absolute path to response folder is entered. */
	private JTextField txtResponseFolderPath = null;

	/** Text box where absolute path to processing folder is entered. */
	private JTextField txtProcessedFolderPath = null;

	/** Text box where absolute path to output folder is entered. */
	private JTextField txtOutputFolderPath = null;

	/** Text box where absolute path to backup folder is entered. */
	private JTextField txtBackupFolderPath = null;

	/**
	 * Obtains panel containing folder settings.
	 */
	public FolderPanel() {
		setLayout(null);
		setOpaque(true);
		add(getInputPanel(), null);
		add(getOutputPanel(), null);
		add(getBackupPanel(), null);
	}

	/**
	 * Obtain sub-panel containing settings for backup folder.
	 * @return Sub-panel containing settings for backup folder.
	 */
	private JPanel getBackupPanel() {

		txtBackupFolderPath = new JTextField(""); //$NON-NLS-1$
		txtBackupFolderPath.setBounds(new Rectangle(90, 20, 320, 20));

		JLabel backupLbl = new JLabel(Messages.getString("SETTINGS_FOLDER_BACKUP_FOLDER")); //$NON-NLS-1$
		backupLbl.setDisplayedMnemonic(Messages.getString("SETTINGS_FOLDER_BACKUP_FOLDER_HK").charAt(0)); //$NON-NLS-1$
		backupLbl.setLabelFor(txtBackupFolderPath);
		backupLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		backupLbl.setBounds(new Rectangle(19, 20, 70, 20));

		JButton backupBtn = new JButton("..."); //$NON-NLS-1$
		backupBtn.setBounds(new Rectangle(420, 20, 30, 20));
		backupBtn.setToolTipText(Messages.getString("SETTINGS_FOLDER_BROWSE")); //$NON-NLS-1$
		backupBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(final ActionEvent e) {  // NOSONAR event is not used.
				examine(txtBackupFolderPath);
			}
		});

		JPanel backupPanel = new JPanel();
		backupPanel.setLayout(null);
		backupPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.RAISED, Color.white, new Color(142, 142, 142)), " " + //$NON-NLS-1$
				Messages.getString("SETTINGS_FOLDER_BACKUP_BORDER") + " ")); //$NON-NLS-1$//$NON-NLS-2$
		backupPanel.setDebugGraphicsOptions(0);
		backupPanel.setBounds(new Rectangle(19, 175, 469, 55));
		backupPanel.add(txtBackupFolderPath, null);
		backupPanel.add(backupLbl, null);
		backupPanel.add(backupBtn, null);

		return backupPanel;
	}

	/**
	 * Obtain sub-panel containing settings for output folder.
	 * @return Sub-panel containing settings for output folder.
	 */
	private JPanel getOutputPanel() {

		txtOutputFolderPath = new JTextField(""); //$NON-NLS-1$
		txtOutputFolderPath.setBounds(new Rectangle(90, 20, 320, 20));

		JLabel lblOutput = new JLabel(Messages.getString("SETTINGS_FOLDER_OUTPUT_FOLDER")); //$NON-NLS-1$
		lblOutput.setDisplayedMnemonic(Messages.getString("SETTINGS_FOLDER_OUTPUT_FOLDER_HK").charAt(0)); //$NON-NLS-1$
		lblOutput.setLabelFor(txtOutputFolderPath);
		lblOutput.setHorizontalAlignment(SwingConstants.RIGHT);
		lblOutput.setBounds(new Rectangle(19, 20, 70, 20));

		JButton btnOutput = new JButton("..."); //$NON-NLS-1$
		btnOutput.setBounds(new Rectangle(420, 20, 30, 20));
		btnOutput.setToolTipText(Messages.getString("SETTINGS_FOLDER_BROWSE")); //$NON-NLS-1$
		btnOutput.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(final ActionEvent e) {   // NOSONAR event is not used.
				examine(txtOutputFolderPath);
			}
		});

		JPanel pnlOutput = new JPanel();
		pnlOutput.setLayout(null);
		pnlOutput.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.RAISED, Color.white, new Color(142, 142, 142)), " " //$NON-NLS-1$
				+ Messages.getString("SETTINGS_FOLDER_OUPUT_BORDER") + " ")); //$NON-NLS-1$//$NON-NLS-2$
		pnlOutput.setDebugGraphicsOptions(0);
		pnlOutput.setBounds(new Rectangle(19, 116, 469, 55));
		pnlOutput.add(txtOutputFolderPath, null);
		pnlOutput.add(lblOutput, null);
		pnlOutput.add(btnOutput, null);

		return pnlOutput;
	}

	/**
	 * Obtain sub-panel containing settings for output folder.
	 * @return Sub-panel containing settings for output folder.
	 */
	public JPanel getInputPanel() {

		txtInputFolderPath = new JTextField(""); //$NON-NLS-1$
		txtInputFolderPath.setBounds(new Rectangle(90, 20, 320, 20));

		JLabel lblInput = new JLabel(Messages.getString("SETTINGS_FOLDER_INPUT_FOLDER")); //$NON-NLS-1$
		lblInput.setHorizontalAlignment(SwingConstants.RIGHT);
		lblInput.setDisplayedMnemonic(Messages.getString("SETTINGS_FOLDER_INPUT_FOLDER_HK").charAt(0)); //$NON-NLS-1$
		lblInput.setLabelFor(txtInputFolderPath);
		lblInput.setBounds(new Rectangle(19, 20, 70, 20));

		JButton btnInput = new JButton("..."); //$NON-NLS-1$
		btnInput.setBounds(new Rectangle(420, 20, 30, 20));
		btnInput.setToolTipText(Messages.getString("SETTINGS_FOLDER_BROWSE")); //$NON-NLS-1$
		btnInput.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(final ActionEvent e) {   // NOSONAR event is not used.
				examine(txtInputFolderPath);
			}
		});

		txtResponseFolderPath = new JTextField(""); //$NON-NLS-1$
		txtResponseFolderPath.setBounds(new Rectangle(90, 45, 320, 20));

		JLabel lblResponse = new JLabel(Messages.getString("SETTINGS_FOLDER_ACKNOWLEDGEMENT_FOLDER")); //$NON-NLS-1$
		lblResponse.setDisplayedMnemonic(Messages.getString("SETTINGS_FOLDER_ACKNOWLEDGEMENT_FOLDER_HK").charAt(0)); //$NON-NLS-1$
		lblResponse.setHorizontalAlignment(SwingConstants.RIGHT);
		lblResponse.setLabelFor(txtResponseFolderPath);
		lblResponse.setBounds(new Rectangle(19, 45, 70, 20));

		JButton btnResponse = new JButton("..."); //$NON-NLS-1$
		btnResponse.setBounds(new Rectangle(420, 45, 30, 20));
		btnResponse.setToolTipText(Messages.getString("SETTINGS_FOLDER_BROWSE")); //$NON-NLS-1$
		btnResponse.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(final ActionEvent e) {   // NOSONAR event is not used.
				examine(txtResponseFolderPath);
			}
		});

		txtProcessedFolderPath = new JTextField(""); //$NON-NLS-1$
		txtProcessedFolderPath.setBounds(new Rectangle(90, 70, 320, 20));

		JLabel lblProcessed = new JLabel(Messages.getString("SETTINGS_FOLDER_PROCESSED")); //$NON-NLS-1$
		lblProcessed.setHorizontalAlignment(SwingConstants.RIGHT);
		lblProcessed.setDisplayedMnemonic(Messages.getString("SETTINGS_FOLDER_PROCESSED_HK").charAt(0)); //$NON-NLS-1$
		lblProcessed.setLabelFor(txtProcessedFolderPath);
		lblProcessed.setBounds(new Rectangle(19, 70, 70, 20));

		JButton btnProcesed = new JButton("..."); //$NON-NLS-1$
		btnProcesed.setBounds(new Rectangle(420, 70, 30, 20));
		btnProcesed.setToolTipText(Messages.getString("SETTINGS_FOLDER_BROWSE")); //$NON-NLS-1$
		btnProcesed.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(final ActionEvent e) {   // NOSONAR event is not used.
				examine(txtProcessedFolderPath);
			}
		});

		JPanel pnlInput = new JPanel();
		pnlInput.setLayout(null);
		pnlInput.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.RAISED, Color.white, new Color(142, 142, 142)), " " //$NON-NLS-1$
				+ Messages.getString("SETTINGS_FOLDER_INPUT_BORDER") + " ")); //$NON-NLS-1$ //$NON-NLS-2$

		pnlInput.setBounds(new Rectangle(19, 12, 469, 100));
		pnlInput.add(txtInputFolderPath, null);
		pnlInput.add(lblInput, null);
		pnlInput.add(btnInput, null);
		pnlInput.add(txtResponseFolderPath, null);
		pnlInput.add(lblResponse, null);
		pnlInput.add(btnResponse, null);
		pnlInput.add(txtProcessedFolderPath, null);
		pnlInput.add(lblProcessed, null);
		pnlInput.add(btnProcesed, null);

		return pnlInput;
	}

	/**
	 * Open file chooser to select path to certificate file.
	 * @param textField Text box to which file path will be written.
	 */
	private void examine(final JTextField textField) {

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setSelectedFile(new File(textField.getText()));

		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

			File file = fileChooser.getSelectedFile();
			if (file.isDirectory()) {

				textField.setText(changeFileNameSeparator(file.getAbsolutePath()));

			} else {

				JOptionPane.showMessageDialog(this, Messages.getString("MSG_ERROR_TITLE"),  //$NON-NLS-1$
						Messages.getString("SETTINGS_FOLDER_FOLDER_DOESNT_EXIST"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Changes Windows file path (a\b\c) to Unix file path (a/b/c).
	 * @param filename Absolute file name to convert.
	 * @return New file name changing '\' character into '/'
	 */
	private String changeFileNameSeparator(final String filename) {
		String retValue;

		if (filename == null) {
			retValue = null;
		} else {
			retValue = filename.replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return retValue;
	}

	/**
	 * Loads path names of  folders into their respective form fields.
	 * @throws ConfigException If settings cannot be read.
	 */
	public void loadValues() throws ConfigException {

		Configuration cf = new Configuration();
		cf.readConfiguration();
		txtInputFolderPath.setText(changeFileNameSeparator(cf.getInputFolder(0)));
		txtResponseFolderPath.setText(changeFileNameSeparator(cf.getResponseFolder(0)));
		txtProcessedFolderPath.setText(changeFileNameSeparator(cf.getProcessedFolder(0)));
		txtOutputFolderPath.setText(changeFileNameSeparator(cf.getOutputFolder(0)));
		txtBackupFolderPath.setText(changeFileNameSeparator(cf.getBackupFolder()));
	}

	/**
	 * Sets folder setting values.
	 */
	public void setValues() {

		Configuration cf = new Configuration();

		try {
			cf.setInputFolder(changeFileNameSeparator(txtInputFolderPath.getText()));
			cf.setResponseFolder(changeFileNameSeparator(txtResponseFolderPath.getText()));
			cf.setProcessedFolder(changeFileNameSeparator(txtProcessedFolderPath.getText()));
			cf.setBackupFolder(changeFileNameSeparator(txtBackupFolderPath.getText()));
			cf.setOutputFolder(changeFileNameSeparator(txtOutputFolderPath.getText()));
			cf.writeConfiguration();

		} catch (ConfigException ex) {

			/*
			 * Settings should be already validated at this point, thus this exception should be unreachable.
			 */
			JOptionPane.showMessageDialog(this, ex.toString(), Messages.getString("MSG_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
		}

	}

	/**
	 * Validates folder panel.
	 * @throws ConfigException If entered folders are incorrect.
	 */
	public void validateConfig() throws ConfigException {

		Configuration cf = new Configuration();
		
		String inputFolderPath = changeFileNameSeparator(txtInputFolderPath.getText().trim());
		if (inputFolderPath.length() == 0) {
			inputFolderPath = null;
		}
		cf.setInputFolder(inputFolderPath);

		String responseFolderPath = changeFileNameSeparator(txtResponseFolderPath.getText().trim());
		if (responseFolderPath.length() == 0) {
			responseFolderPath = null;
		}
		cf.setResponseFolder(responseFolderPath);

		String processingFolderPath = changeFileNameSeparator(txtProcessedFolderPath.getText().trim());
		if (processingFolderPath.length() == 0) {
			processingFolderPath = null;
		}
		cf.setProcessedFolder(processingFolderPath);

		String outputFolderPath = changeFileNameSeparator(txtOutputFolderPath.getText().trim());
		if (outputFolderPath.length() == 0) {
			outputFolderPath = null;
		}
		cf.setOutputFolder(outputFolderPath);

		String backupFolderPath = changeFileNameSeparator(txtBackupFolderPath.getText().trim());
		if (backupFolderPath.length() == 0) {
			backupFolderPath = null;
		}
		cf.setBackupFolder(backupFolderPath);

		try {
			cf.validateConfiguration();
		} catch (ConfigException ex) {
			throw new ConfigException(getPanelName() + " " + Messages.getString("SETTINGS_PANEL_SAYS") + " "   //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
					+ ex.getMessage());
		}

	}

	/**
	 * Returns panel name.
	 * @return Panel name.
	 */
	public String getPanelName() {

		return PANEL_NAME;
	}
}
