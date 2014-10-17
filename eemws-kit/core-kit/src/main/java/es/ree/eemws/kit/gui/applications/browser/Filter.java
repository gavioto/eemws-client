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
package es.ree.eemws.kit.gui.applications.browser;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import es.ree.eemws.client.list.IntervalTimeType;
import es.ree.eemws.kit.common.Messages;

/**
 * Graphical filter options.
 * 
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 02/06/2014
 */
public final class Filter {

	/** Date format for components related to date on filter. */
	public static final String DATE_FORMAT = "dd/MM/yyyy"; //$NON-NLS-1$

	/** Default value for code list. */
	private static final String ZERO_CODE = "0"; //$NON-NLS-1$
	
	/** Type of filter. */
	private JComboBox<String> cbFilterType = null;

	/** Label for {@link #txtCode}. */
	private JLabel lblCode = new JLabel();

	/** 'List' button. */
	private JButton btList = new JButton();

	/** 'Get' button. */
	private JButton btGet = new JButton();

	/** Date format. */
	private SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

	/** Label for {@link #txtStartDate}. */
	private JLabel lblStartDate = new JLabel();

	/** Start date. */
	private JFormattedTextField txtStartDate = new JFormattedTextField(sdf);

	/** Label for {@link #txtEndDate}. */
	private JLabel lblEndDate = new JLabel();

	/** End date. */
	private JFormattedTextField txtEndDate = new JFormattedTextField(sdf);

	/** Code. */
	private JTextField txtCode = new JTextField();

	/** ID. */
	private JTextField txtID = new JTextField();

	/** Message type. */
	private JTextField txtMessageType = new JTextField();

	/** Owner. */
	private JTextField txtOwner;

	/** Filter panel. */
	private JPanel pnlFilter = null;

	/** Reference to main window. */
	private Browser mainWindow;

	/**
	 * Constructor. Creates an instance of filter.
	 * @param mainW Reference to main class.
	 */
	public Filter(final Browser mainW) {
		mainWindow = mainW;
	}

	/**
	 * Sets current filtering type. Switch between code and date based filtering.
	 */
	private void switchFilterType() {

		boolean isDateType = cbFilterType.getSelectedIndex() != 0;

		lblStartDate.setVisible(isDateType);
		txtStartDate.setVisible(isDateType);
		lblEndDate.setVisible(isDateType);
		txtEndDate.setVisible(isDateType);
		lblCode.setVisible(!isDateType);
		txtCode.setVisible(!isDateType);
	}

	/**
	 * Returns filter panel with graphical components (textboxes, labels, etc.).
	 * @return Filter panel containing graphical elements.
	 */
	public JPanel getFilterCanvas() {

		JLabel tipoFiltroLbl = new JLabel(Messages.getString("BROWSER_FILTER_TYPE"), SwingConstants.RIGHT); //$NON-NLS-1$
		tipoFiltroLbl.setDisplayedMnemonic(Messages.getString("BROWSER_FILTER_TYPE_HK").charAt(0)); //$NON-NLS-1$
		tipoFiltroLbl.setLabelFor(cbFilterType);
		tipoFiltroLbl.setBounds(16, 28, 75, 16);

		cbFilterType = new JComboBox<>();
		cbFilterType.setEditable(false);

		/* Must be added in the same order as in {@link ListMessages} */
		cbFilterType.addItem(Messages.getString("BROWSER_FILTER_TYPE_CODE")); //$NON-NLS-1$
		cbFilterType.addItem(Messages.getString("BROWSER_FILTER_TYPE_SERVER")); //$NON-NLS-1$
		cbFilterType.addItem(Messages.getString("BROWSER_FILTER_TYPE_APPLICATION")); //$NON-NLS-1$

		cbFilterType.setSelectedIndex(2);
		cbFilterType.setBounds(100, 28, 130, 19);
		cbFilterType.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				switchFilterType();
			}
		});

		lblStartDate.setDisplayedMnemonic(Messages.getString("BROWSER_FILTER_START_DATE_HK").charAt(0)); //$NON-NLS-1$
		lblStartDate.setLabelFor(txtStartDate);
		lblStartDate.setText(Messages.getString("BROWSER_FILTER_START_DATE")); //$NON-NLS-1$
		lblStartDate.setHorizontalAlignment(SwingConstants.RIGHT);
		lblStartDate.setBounds(16, 53, 75, 16);

		txtStartDate.setText(sdf.format(new Date()));
		txtStartDate.setFocusLostBehavior(JFormattedTextField.PERSIST);
		txtStartDate.setBounds(100, 53, 92, 19);

		lblEndDate.setDisplayedMnemonic(Messages.getString("BROWSER_FILTER_END_DATE_HK").charAt(0)); //$NON-NLS-1$
		lblEndDate.setLabelFor(txtEndDate);
		lblEndDate.setText(Messages.getString("BROWSER_FILTER_END_DATE")); //$NON-NLS-1$
		lblEndDate.setHorizontalAlignment(SwingConstants.RIGHT);
		lblEndDate.setBounds(16, 78, 75, 16);

		txtEndDate.setText(sdf.format(new Date()));
		txtEndDate.setFocusLostBehavior(JFormattedTextField.PERSIST);
		txtEndDate.setBounds(100, 78, 92, 19);

		lblCode.setDisplayedMnemonic(Messages.getString("BROWSER_FILTER_CODE_HK").charAt(0)); //$NON-NLS-1$
		lblCode.setLabelFor(txtCode);
		lblCode.setText(Messages.getString("BROWSER_FILTER_CODE")); //$NON-NLS-1$
		lblCode.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCode.setBounds(16, 53, 75, 16);
		lblCode.setVisible(false);

		txtCode.setText(ZERO_CODE);
		txtCode.setBounds(100, 53, 92, 19);
		txtCode.setVisible(false);

		JLabel lblID = new JLabel(Messages.getString("BROWSER_FILTER_ID"), SwingConstants.RIGHT); //$NON-NLS-1$
		lblID.setLabelFor(txtID);
		lblID.setDisplayedMnemonic(Messages.getString("BROWSER_FILTER_ID_HK").charAt(0)); //$NON-NLS-1$
		lblID.setBounds(200, 28, 130, 16);

		txtID.setText(""); //$NON-NLS-1$
		txtID.setBounds(340, 28, 130, 19);

		JLabel lblMessageType = new JLabel(Messages.getString("BROWSER_FILTER_MSG_TYPE"), SwingConstants.RIGHT); //$NON-NLS-1$
		lblMessageType.setDisplayedMnemonic(Messages.getString("BROWSER_FILTER_MSG_TYPE_HK").charAt(0)); //$NON-NLS-1$
		lblMessageType.setLabelFor(txtMessageType);
		lblMessageType.setBounds(200, 53, 130, 16);

		txtMessageType.setText(""); //$NON-NLS-1$
		txtMessageType.setBounds(340, 53, 130, 19);

		JLabel lblOwner = new JLabel(Messages.getString("BROWSER_FILTER_OWNER"), SwingConstants.RIGHT); //$NON-NLS-1$
		lblOwner.setLabelFor(txtOwner);
		lblOwner.setDisplayedMnemonic(Messages.getString("BROWSER_FILTER_OWNER_HK").charAt(0)); //$NON-NLS-1$
		lblOwner.setBounds(200, 78, 130, 16);

		txtOwner = new JTextField(""); //$NON-NLS-1$
		txtOwner.setBounds(340, 78, 130, 19);

		btList.setBounds(90, 112, 121, 26);
		btList.setMnemonic(Messages.getString("BROWSER_FILTER_BROWSER_BUTTON_HK").charAt(0)); //$NON-NLS-1$
		btList.setText(Messages.getString("BROWSER_FILTER_BROWSER_BUTTON")); //$NON-NLS-1$
		btList.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				mainWindow.getListSend().retrieveList();
			}
		});

		btGet.setBounds(251, 112, 121, 26);
		btGet.setMnemonic(Messages.getString("BROWSER_FILTER_GET_BUTTON_HK").charAt(0)); //$NON-NLS-1$
		btGet.setText(Messages.getString("BROWSER_FILTER_GET_BUTTON")); //$NON-NLS-1$
		btGet.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				mainWindow.getRequestSend().retrieve();
			}
		});

		pnlFilter = new JPanel();
		pnlFilter.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("BROWSER_FILTER_LEGEND"))); //$NON-NLS-1$
		pnlFilter.setLayout(null);

		pnlFilter.add(tipoFiltroLbl);
		pnlFilter.add(cbFilterType);
		pnlFilter.add(lblStartDate);
		pnlFilter.add(txtStartDate);
		pnlFilter.add(txtEndDate);
		pnlFilter.add(lblEndDate);
		pnlFilter.add(lblCode);
		pnlFilter.add(txtCode);
		pnlFilter.add(lblID);
		pnlFilter.add(txtID);
		pnlFilter.add(btList);
		pnlFilter.add(btGet);
		pnlFilter.add(lblMessageType);
		pnlFilter.add(txtMessageType);
		pnlFilter.add(lblOwner);
		pnlFilter.add(txtOwner);
		pnlFilter.setVisible(true);

		pnlFilter.setBounds(0, 0, 500, 160);

		return pnlFilter;
	}

	/**
	 * Resizes filter box size according to the value passed as parameter.
	 * @param width Width of the parent window.
	 */
	public void setSize(final int width) {
		pnlFilter.setBounds(0, 0, width - 20, 160);
	}

	/**
	 * Enables / disables graphical values.
	 * @param activeValue <code>true</code> Enable. <code>false</code> disable.
	 */
	public void enable(final boolean activeValue) {
		Component[] component = pnlFilter.getComponents();
		for (Component comp : component) {
			comp.setEnabled(activeValue);
		}
	}

	/**
	 * Builts <code>FilterData</code> object value with the current filter options.
	 * @return Object with the current filter options.
	 */
	public FilterData getFilterData() {
		FilterData df = new FilterData();

		int listType = cbFilterType.getSelectedIndex();

		if (listType == 0) {
			df.setStartDate(null);
			df.setEndDate(null);
			df.setCode(txtCode.getText().trim());
		} else {

			if (listType == 2) {
				df.setMsgInterval(IntervalTimeType.Application);
			} else {
				df.setMsgInterval(IntervalTimeType.Server);
			}
			df.setStartDate(txtStartDate.getText());
			df.setEndDate(txtEndDate.getText());
		}

		df.setType(txtMessageType.getText());
		df.setMessageID(txtID.getText());
		df.setOwner(txtOwner.getText());

		return df;
	}

	/**
	 * Shows / hides filters according to the parameter.
	 * @param visibility <code>true</code> to show filter. <code>false</code> to hide it.
	 */
	private void setVisible(final boolean visibility) {
		pnlFilter.setVisible(visibility);
		mainWindow.modifySize();
	}

	/**
	 * Indicates whether filter is visible (useful for adjust size of other elements) .
	 * @return <code>true</code> if the filter is visible <code>false</code> otherwise.
	 */
	public boolean isVisible() {
		return pnlFilter.isVisible();
	}

	/**
	 * Returns menu containing filtering options.
	 * @param mnView Menu containing filtering options.
	 */
	public void getMenu(final JMenu mnView) {

		final JCheckBoxMenuItem miShowFilter = new JCheckBoxMenuItem(Messages.getString("BROWSER_FILTER_SHOW_FILTER_MENU_ENTRY")); //$NON-NLS-1$
		miShowFilter.setMnemonic(Messages.getString("BROWSER_FILTER_SHOW_FILTER_MENU_ENTRY_HK").charAt(0)); //$NON-NLS-1$
		miShowFilter.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				setVisible(miShowFilter.isSelected());
			}
		});
		miShowFilter.setSelected(true);

		mnView.add(miShowFilter);
	}

}
