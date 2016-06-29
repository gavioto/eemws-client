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

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;

import javax.swing.JOptionPane;

import es.ree.eemws.client.get.GetMessage;
import es.ree.eemws.client.get.RetrievedMessage;
import es.ree.eemws.core.utils.operations.get.GetOperationException;
import es.ree.eemws.kit.common.Messages;
import es.ree.eemws.kit.gui.common.Logger;


/**
 * Sends 'Get' message to server.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 09/05/2014
 */
public final class GetMessageSender {

    /** Message request object. */
    private GetMessage get;

    /** Reference to main window. */
    private Browser mainWindow;

    /** Reference to status bar. */
    private StatusBar status;

    /** Log object. */
    private Logger logger;

    /**
     * Constructor. Creates new instance of request messages handler.
     * @param url URL to which requests are sent.
     * @param principal Reference to main window.
     */
    public GetMessageSender(final URL url, final Browser principal) {

        get = new GetMessage();
        mainWindow = principal;

        status = principal.getStatusBar();
        logger = principal.getLogHandle().getLog();


        setEndPoint(url);
    }

    /**
     * Sets URL of the system to which requests are made.
     * @param url URL of the system to which requests are made.
     */
    public void setEndPoint(final URL url) {

        get.setEndPoint(url);
    }

    /**
     * Retrieves currently selected message/s.
     */
    public void retrieve() {
        DataTable dataTable = mainWindow.getDataTable();
        int[] selectedRows = dataTable.getSelectedRows();
        int len = selectedRows.length;

        if (len == 0) {
            String msg;
            if (dataTable.getModel().getRowCount() == 0) {
                msg = Messages.getString("BROWSER_NO_MESSAGES_TO_GET"); //$NON-NLS-1$
            } else {
            	msg = Messages.getString("BROWSER_SELECT_MESSAGES_TO_GET"); //$NON-NLS-1$
            }
            logger.logMessage(msg);
            status.setStatus(msg);
            JOptionPane.showMessageDialog(mainWindow, msg,
            		Messages.getString("MSG_INFO_TITLE"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
        } else {
            if (len > 1) {
            	String msg = Messages.getString("BROWSER_RETRIEVING_SEVERAL_MESSAGES", len); //$NON-NLS-1$
                logger.logMessage(msg);
                status.setStatus(msg);
                mainWindow.enableScreen(false);
                for (int cont = 0; cont < len; cont++) {
                     retieveWhenDisabled(dataTable.getSelectedRows()[cont]);
                }
                mainWindow.enableScreen(true);
            } else {
            	mainWindow.enableScreen(false);
                int row = dataTable.getSelectedRow();
                Long codigo = ((BigInteger) dataTable.getModel().getAbsoluteValueAt(row, ColumnsId.CODE.ordinal())).longValue();

                String idMensaje = getMessageId(row, dataTable);
                
                int answer = JOptionPane.showConfirmDialog(mainWindow,
                        Messages.getString("BROWSER_RETRIEVE_MESSAGE_CONFIRMATION", idMensaje, codigo), //$NON-NLS-1$
                        Messages.getString("MSG_CONFIRM_TITLE"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); //$NON-NLS-1$

                if (answer == JOptionPane.OK_OPTION) {
                	retieveWhenDisabled(row);
                }
                mainWindow.enableScreen(true);
            }
        }
    }

    /**
     * Performs the message retrieval once the graphic components are disabled.
     * @param row Row index.
     * @see #retrieve().
     */
    private void retieveWhenDisabled(final int row) {

        DataTable dataTable = mainWindow.getDataTable();

        String idMensaje = getMessageId(row, dataTable);
        
        Long codigo = ((BigInteger) dataTable.getModel().getAbsoluteValueAt(row, ColumnsId.CODE.ordinal())).longValue();
        
        String msg = Messages.getString("BROWSER_RETRIEVING_FILE", idMensaje, codigo); //$NON-NLS-1$
        
        logger.logMessage(msg);
        status.setStatus(msg);

        try {
            RetrievedMessage response = get.get(codigo);
            
            msg = Messages.getString("BROWSER_RETRIEVED_FILE", idMensaje, codigo); //$NON-NLS-1$
            
            logger.logMessage(msg);
            status.setStatus(msg);

            FileHandle ficheroHandle = mainWindow.getFileHandle();
            ficheroHandle.saveFile(idMensaje, response);
        } catch (GetOperationException e) {
        	msg = Messages.getString("BROWSER_UNABLE_TO_GET", e.getMessage()); //$NON-NLS-1$
            status.setStatus(msg);
            logger.logMessage(msg);
            JOptionPane.showMessageDialog(mainWindow, msg, Messages.getString("MSG_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
        } catch (IOException e) {
            msg = Messages.getString("UNABLE_TO_WRITE", idMensaje); //$NON-NLS-1$
            status.setStatus(msg);
            logger.logMessage(msg);
            JOptionPane.showMessageDialog(mainWindow, msg, Messages.getString("MSG_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
        } 
    }
    
    /**
     * Returns message identification as <id> + "." + <version>. If the message has no version only the <id>
     * part will be returned.
     * @param row Table view row number.
     * @param dataTable Data table.
     * @return Identification of the message that appears at row <code>row</code>
     */
	private String getMessageId(final int row, final DataTable dataTable) {
		String idMensaje = (String) dataTable.getModel().getAbsoluteValueAt(row, ColumnsId.ID.ordinal());
        BigInteger version = (BigInteger) dataTable.getModel().getAbsoluteValueAt(row, ColumnsId.VERSION.ordinal());
        
        if (version != null) {
        	idMensaje = idMensaje + "." + version.toString(); //$NON-NLS-1$
        }
		return idMensaje;
	}
}
