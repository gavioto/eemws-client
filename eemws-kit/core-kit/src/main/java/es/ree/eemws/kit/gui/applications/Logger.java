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
package es.ree.eemws.kit.gui.applications;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import es.ree.eemws.kit.gui.common.Constants;


/**
 * Class for logging tasks.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public final class Logger extends JFrame {

    /** Class iD. */
    private static final long serialVersionUID = -3840430150230723782L;

    /** Main window for which this class will work as log Panel. */
    private LoggerListener parentApp;

    /** Log text area. */
    private JTextArea textoLog = new JTextArea();

    /** Formatter for the Date to be shown next to Log entry. */
    private SimpleDateFormat sdfLog = new SimpleDateFormat("HH:mm:ss");

    /** Constructor.
     * @param listener Listener object.
     * */
    public Logger(final LoggerListener listener) {

        super("Log");
        parentApp = listener;
        jbInit();
    }

    /**
     * Sets visibility status.
     * @param bol visibility status true Visible. false Hidden
     */
    public void visibility(final boolean bol) {

        setVisible(bol);
    }

    /**
     * Close log window.
     */
    private void close() {

        parentApp.logWindowIsClosing();
        setVisible(false);
    }

    /**
     * Initialize logger panel.
     */
    private void jbInit() {

        JScrollPane panelScroll = new JScrollPane();
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource(Constants.ICON_PATH)));
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(final WindowEvent e) {
                close();
            }
        });
        textoLog.setText("");
        textoLog.setEditable(false);
        setSize(600, 100);
        getContentPane().add(panelScroll, BorderLayout.CENTER);
        panelScroll.getViewport().add(textoLog, null);
        panelScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        panelScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    /**
     * Delete log content.
     */
    public void deleteLog() {

        textoLog.setText("");
        update(getGraphics());
    }

    /**
     * Method the sets message to be shown as log entry.
     * @param msg Message to be shown as log entry.
     */
    public void logMessage(final String msg) {

        String time = sdfLog.format(new Date());
        String text = textoLog.getText();
        if (!text.endsWith("\n") && text.trim().length() != 0) {
            textoLog.append("\n");
        }
        textoLog.append(" [" + time + "] " + msg);
        update(getGraphics());
    }
}
