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
package es.ree.eemws.kit.gui.common;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import es.ree.eemws.kit.common.Messages;

/**
 * Grafical log window. Implements a simple text area where the application can show messages.
 * The application should implement <code>LoggerListener</code> in order to know whether the log window
 * is visible.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public final class Logger extends JFrame {

    /** Class Id. */
	private static final long serialVersionUID = -5015462579525663589L;

	/** Window width. */	
	private static final int LOGGER_WINDOW_X_SIZE = 600;
	
	/** Window height. */
	private static final int LOGGER_WINDOW_Y_SIZE = 100;

	/** New line character. */ 
	private static final String NEW_LINE = "\n"; //$NON-NLS-1$
  
    /** Main window for which this class will work as log Panel. */
    private LoggerListener parentApp;

    /** Log text area. */
    private JTextArea logTextArea = new JTextArea();

    /** Formatter for the Date to be shown next to Log entry. */
    private SimpleDateFormat sdfLog = new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$

    /** 
     * Constructor.
     * @param listener Reference to the owner of this log window. Can be <code>null</code>
     * */
    public Logger(final LoggerListener listener) {
        super(Messages.getString("LOG_FRAME_TITLE")); //$NON-NLS-1$
        parentApp = listener;
        jbInit();
    }

    /**
     * Sets visibility status.
     * @param bol visibility status <code>true</code> Visible. <code>false</code> Hidden
     */
    public void visibility(final boolean bol) {

        setVisible(bol);
    }

    /**
     * Close log window. Notifies to the application that the log is not visible anymore.
     */
    private void close() {
    	if (parentApp != null) {
    		parentApp.logWindowIsClosing();
    	}
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
            public void windowClosing(final WindowEvent e) {  // NOSONAR event is not used.
                close();
            }
        });
        logTextArea.setText(""); //$NON-NLS-1$
        logTextArea.setEditable(false);
        setSize(LOGGER_WINDOW_X_SIZE, LOGGER_WINDOW_Y_SIZE);
        getContentPane().add(panelScroll, BorderLayout.CENTER);
        panelScroll.getViewport().add(logTextArea, null);
        panelScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        panelScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    }

    /**
     * Delete log area.
     */
    public void deleteLog() {

    	logTextArea.setText(""); //$NON-NLS-1$
        update(getGraphics());
    }

    /**
     * Add a new message to the log window.
     * @param msg Message to log.
     */
    public void logMessage(final String msg) {

        String time = sdfLog.format(new Date());
        
        logTextArea.append(" [" + time + "] " + msg);  //$NON-NLS-1$ //$NON-NLS-2$
        
        if (!msg.endsWith(NEW_LINE)) {
        	logTextArea.append(NEW_LINE);
        }
        
        update(getGraphics());
    }

    /**
     * Logs an exception detail.
     * @param msg Descritive message.
     * @param e Exception.
     */
	public void logException(final String msg, final Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));  //NOSONAR - Here we are dumping the stack trace to a writer not to the console.
		String exceptionDetails = sw.toString();
		
		StringBuilder sb = new StringBuilder();
		sb.append(msg);
		sb.append("\n"); //$NON-NLS-1$
		sb.append(Messages.getString("LOG_DETAIL_EXCEPTION")); //$NON-NLS-1$
		sb.append("\n"); //$NON-NLS-1$
		sb.append(exceptionDetails);
		
		logMessage(sb.toString());
	}	
}
