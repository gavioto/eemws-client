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

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import es.ree.eemws.client.exception.ClientException;
import es.ree.eemws.client.getmessage.GetMessage;
import es.ree.eemws.kit.config.Configuration;
import es.ree.eemws.kit.gui.applications.Logger;


/**
 * Send 'Get' message to server.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 09/05/2014
 */
public final class RequestSend {

    /** Message request object. */
    private GetMessage get;

    /** Reference to main window. */
    private Lists mainWindow;

    /** Reference to status bar. */
    private StatusBar status;

    /** Log object. */
    private Logger logger;

    /**
     * Constructor, create new instance of request messages handler.
     * @param url URL to which requests are sent.
     * @param principal Reference to main window.
     */
    public RequestSend(final URL url, final Lists principal) {

        get = new GetMessage();
        mainWindow = principal;

        status = principal.getStatusBar();
        logger = principal.getLogHandle().getLog();


        setEndPoint(url);
    }

    /**
     * Set URL of the system to which requests are made.
     * @param url URL of the system to which requests are made.
     */
    public void setEndPoint(final URL url) {

        get.setEndPoint(url);
    }

    /**
     * Retrieve currently selected message/s.
     */
    public void retrieve() {
        DataTable dataTable = mainWindow.getDataTable();
        int[] selectedRows = dataTable.getSelectedRows();
        int len = selectedRows.length;

        if (len == 0) {
            String msg;
            if (dataTable.getModel().getRowCount() == 0) {
                msg = "No messages to retrieve.";
            } else {
                msg = "Select any file to retrive messages.";
            }
            logger.logMessage(msg);
            status.setStatus(msg);
            JOptionPane.showMessageDialog(mainWindow, msg,
                    "Information", JOptionPane.INFORMATION_MESSAGE);
        } else {
            if (len > 1) {
                logger.logMessage(" Request " + len + " messages");
                mainWindow.enableScreen(false);
                for (int cont = 0; cont < len; cont++) {
                     retieveWhenDisabled(dataTable.getSelectedRows()[cont]);
                }
                mainWindow.enableScreen(true);
            } else {
            	mainWindow.enableScreen(false);
                int row = dataTable.getSelectedRow();
                String id = (String) dataTable.getModel().getAbsoluteValueAt(row, ColumnsId.ID.ordinal());

                int answer = JOptionPane.showConfirmDialog(mainWindow,
                        "Retrieve message " + id + "?",
                        "Confirm:", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (answer == JOptionPane.OK_OPTION) {
                	retieveWhenDisabled(row);
                }
                mainWindow.enableScreen(true);
            }
        }
    }

    /**
     * Retrieve message relative to a row index.
     * @param row Row index.
     */
    private void retrieve(final int row) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                retieveWhenDisabled(row);
            }
        });
    }

    /**
     * Perform the message retrieval once the graphic components are disabled.
     * @param row Row index.
     * @see {@link #retrieve(int)}.
     */
    private void retieveWhenDisabled(final int row) {

        DataTable dataTable = mainWindow.getDataTable();

        String idMensaje = (String) dataTable.getModel().getAbsoluteValueAt(row, ColumnsId.ID.ordinal());
        String msgType = (String) dataTable.getModel().getAbsoluteValueAt(row, ColumnsId.TYPE.ordinal());
        Long codigo = ((BigInteger) dataTable.getModel().getAbsoluteValueAt(row, ColumnsId.CODE.ordinal())).longValue();

        String response = null;
        String traceMsg = "[" + idMensaje + " (" + codigo + ")]";

        logger.logMessage(traceMsg + " Requesting....");
        status.setStatus("Requesting " + traceMsg);

        try {
            response = get.get(codigo);

            logger.logMessage(traceMsg + " Retrieving.");
            status.setStatus("Retrieved " + traceMsg);

            FileHandle ficheroHandle = mainWindow.getFileHandle();
            ficheroHandle.saveFile(idMensaje, response);
        } catch (ClientException e) {
            Throwable cause = e.getCause();
            String detalle = "";
            if (cause != null) {
                detalle = cause.getMessage();
            }
            String msg = "Cannot retrieve message! " + traceMsg;
            status.setStatus(msg);
            logger.logMessage(msg + " " + e.getMessage() + detalle);
            JOptionPane.showMessageDialog(mainWindow, msg + "\n" + detalle, "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            String msg = "Cannot save file!. Cause: " + e.getMessage();
            status.setStatus(msg);
            logger.logMessage(msg);
            JOptionPane.showMessageDialog(mainWindow, msg, "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            // mainWindow.enableScreen(true);
        }
    }
}
