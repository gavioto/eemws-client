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
package es.ree.eemws.kit.gui.applications.listing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;


/**
 * Filtering components for listing application.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 02/06/2014
 */
public final class Filter {

    /** Date format for components related to date on filter. */
    public static final String DATE_FORMAT = "dd/MM/yyyy";

    /** Type of filter. */
    private JComboBox cbFilterType = null;

    /** Label for {@link #txtCode}. */
    private JLabel lblCode = new JLabel();

    /** 'Query' button. */
    private JButton btQuery = new JButton();

    /** 'Retrieve' button. */
    private JButton btRetrieve = new JButton();

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
    private Lists mainWindow;

    /**
     * Constructor. Creates an instance of listing filters.
     * @param listador Reference to main class.
     */
    public Filter(final Lists listador) {
        mainWindow = listador;
    }

    /**
     * Sets current filtering type. Switch between incremental and date based filtering.
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
     * Return filter panel with graphical components (textboxes, labels, etc.).
     * @return Filter panel containing graphical elements.
     */
    public JPanel getFilterCanvas() {

        JLabel tipoFiltroLbl = new JLabel("Type of filter:", SwingConstants.RIGHT);
        tipoFiltroLbl.setLabelFor(cbFilterType);
        tipoFiltroLbl.setDisplayedMnemonic('i');
        tipoFiltroLbl.setBounds(16, 28, 75, 16);

        cbFilterType = new JComboBox();
        cbFilterType.setEditable(false);

        /* Must be added in the same order as in {@link ListMessages} */
        cbFilterType.addItem("Message code");
        cbFilterType.addItem("Registration day");
        cbFilterType.addItem("Application date");

        cbFilterType.setSelectedIndex(2);
        cbFilterType.setBounds(100, 28, 130, 19);
        cbFilterType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                switchFilterType();
            }
        });

        lblStartDate.setDisplayedMnemonic('S');
        lblStartDate.setLabelFor(txtStartDate);
        lblStartDate.setText("Start date:");
        lblStartDate.setHorizontalAlignment(SwingConstants.RIGHT);
        lblStartDate.setBounds(16, 53, 75, 16);

        txtStartDate.setText(sdf.format(new Date()));
        txtStartDate.setFocusLostBehavior(JFormattedTextField.PERSIST);
        txtStartDate.setBounds(100, 53, 92, 19);

        lblEndDate.setDisplayedMnemonic('E');
        lblEndDate.setLabelFor(txtEndDate);
        lblEndDate.setText("End date:");
        lblEndDate.setHorizontalAlignment(SwingConstants.RIGHT);
        lblEndDate.setBounds(16, 78, 75, 16);

        txtEndDate.setText(sdf.format(new Date()));
        txtEndDate.setFocusLostBehavior(JFormattedTextField.PERSIST);
        txtEndDate.setBounds(100, 78, 92, 19);

        lblCode.setDisplayedMnemonic('C');
        lblCode.setLabelFor(txtCode);
        lblCode.setText("Code:");
        lblCode.setHorizontalAlignment(SwingConstants.RIGHT);
        lblCode.setBounds(16, 53, 75, 16);
        lblCode.setVisible(false);

        txtCode.setText("0");
        txtCode.setBounds(100, 53, 92, 19);
        txtCode.setVisible(false);

        JLabel lblID = new JLabel("ID:", SwingConstants.RIGHT);
        lblID.setLabelFor(txtID);
        lblID.setDisplayedMnemonic('I');
        lblID.setBounds(200, 28, 130, 16);

        txtID.setText("");
        txtID.setBounds(340, 28, 130, 19);

        JLabel lblMessageType = new JLabel("Type:", SwingConstants.RIGHT);
        lblMessageType.setDisplayedMnemonic('T');
        lblMessageType.setLabelFor(txtMessageType);
        lblMessageType.setBounds(200, 53, 130, 16);

        txtMessageType.setText("");
        txtMessageType.setBounds(340, 53, 130, 19);

        JLabel lblOwner = new JLabel("Owner", SwingConstants.RIGHT);
        lblOwner.setLabelFor(txtOwner);
        lblOwner.setDisplayedMnemonic('O');
        lblOwner.setBounds(200, 78, 130, 16);

        txtOwner = new JTextField("");
        txtOwner.setBounds(340, 78, 130, 19);

        btQuery.setBounds(90, 112, 121, 26);
        btQuery.setMnemonic('Q');
        btQuery.setText("Query");
        btQuery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                mainWindow.getListSend().retrieveList();
            }
        });

        btRetrieve.setBounds(251, 112, 121, 26);
        btRetrieve.setMnemonic('R');
        btRetrieve.setText("Retrieve");
        btRetrieve.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                mainWindow.getRequestSend().retrieve();
            }
        });

        pnlFilter = new JPanel();
        pnlFilter.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), " Query filter "));
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
        pnlFilter.add(btQuery);
        pnlFilter.add(btRetrieve);
        pnlFilter.add(lblMessageType);
        pnlFilter.add(txtMessageType);
        pnlFilter.add(lblOwner);
        pnlFilter.add(txtOwner);
        pnlFilter.setVisible(true);


        pnlFilter.setBounds(0, 0, 500, 160);

        return pnlFilter;
    }

    /**
     * Resize filter box size according to the value passed as parameter.
     * @param width Width of the parent window.
     */
    public void setSize(final int width) {
        pnlFilter.setBounds(0, 0, width - 20, 160);
    }


    /**
     * Enable / disable graphical values.
     * @param activeValue <code>true</code> Enable.
     * <code>false</code> disable.
     */
    public void enable(final boolean activeValue) {
        Component[] component = pnlFilter.getComponents();
        for (Component comp : component) {
            comp.setEnabled(activeValue);
        }
    }

    /**
     * Return data entered in filter.
     * @return Data in filter.
     */
    public FilterData getFilterData() {
        FilterData df = new FilterData();

        int listType = cbFilterType.getSelectedIndex();

        df.setFilterType(listType);

        df.setMsgInterval(FilterData.SERVER);
        if (listType == 2) {
            df.setMsgInterval(FilterData.APPLICATION);
        }

        if (listType == 0) {
            df.setStartDate(null);
            df.setEndDate(null);
            df.setCode(txtCode.getText().trim());
        } else {
            df.setStartDate(txtStartDate.getText());
            df.setEndDate(txtEndDate.getText());
            df.setCode("-1");
        }

        df.setType(txtMessageType.getText());
        df.setMessageID(txtID.getText());

        String strOwner = txtOwner.getText().trim();
        if (strOwner.length() > 0) {
            df.setOwner(strOwner);
        }

        return df;
    }


    /**
     * Show / hide filter according to value passed as parameter.
     * @param visibility <code>true</code> to show filter. <code>false</code> to hide it.
     */
    private void setVisible(final boolean visibility) {
        pnlFilter.setVisible(visibility);
        mainWindow.modifySize();
    }

    /**
     * Indicate whether filter is visible (useful for adjust size of other elements) .
     * @return <code>true</code> if the filter is visible
     * <code>false</code> otherwise.
     */
    public boolean isVisible() {
        return pnlFilter.isVisible();
    }

    /**
     * Return menu containing filtering options.
     * @param mnView Menu containing filtering options.
     */
    public void getMenu(final JMenu mnView) {
        JMenu visibilityFilter = new JMenu("Filter");

        mnView.add(visibilityFilter);

        JRadioButtonMenuItem miShowFilter = new JRadioButtonMenuItem("Show filter");
        miShowFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                setVisible(true);
            }
        });
        miShowFilter.setSelected(true);

        JRadioButtonMenuItem miHideFilter = new JRadioButtonMenuItem("Hide filter");
        miHideFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                setVisible(false);
            }
        });

        ButtonGroup bg = new ButtonGroup();
        bg.add(miShowFilter);
        bg.add(miHideFilter);

        visibilityFilter.add(miShowFilter);
        visibilityFilter.add(miHideFilter);
    }

}
