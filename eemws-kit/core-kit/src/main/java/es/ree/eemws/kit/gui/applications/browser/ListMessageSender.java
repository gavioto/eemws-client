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

import java.net.URL;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import es.ree.eemws.client.common.ClientException;
import es.ree.eemws.client.list.ListMessages;
import es.ree.eemws.client.list.MessageListEntry;
import es.ree.eemws.kit.common.Messages;
import es.ree.eemws.kit.gui.common.Logger;

/**
 * Class responsible for sending list messages to server.
 * This class sends List message sending class
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 02/06/2014
 *
 */
public final class ListMessageSender {

    /** Listing object. */
    private ListMessages list;

    /** Reference to main window. */
    private Browser mainWindow;

    /** Reference to status bar. */
    private StatusBar status;

    /** Logging object. */
    private Logger logger;

    /**
     * Constructor, create a new instance of List Messages handler.
     * @param url Access URL to the target system.
     * @param main Reference to main class.
     */
    public ListMessageSender(final URL url, final Browser main) {

        list = new ListMessages();
        list.setEndPoint(url);
        mainWindow = main;
        status = main.getStatusBar();
        logger = main.getLogHandle().getLog();
    }

    /**
     * Set access URL to system to which connect.
     * @param url access URL to system to which connect.
     */
    public void setEndPoint(final URL url) {
        list.setEndPoint(url);
    }

    /**
     * Run service calling.
     */
    public void retrieveList() {
        mainWindow.enableScreen(false);
        mainWindow.getDataTable().setData(new Object[0][ColumnsId.values().length]);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                retrieveListWhenDisabled();
            }
        });

    }

    /**
     * Starts listing tasks once screen elements are disabled
     * and the wait cursor is shown.
     * @see #retrieveList()
     */
    private void retrieveListWhenDisabled() {
        Object[][] data = getDataList();
        if (data != null) {
            mainWindow.getDataTable().setData(data);
        }
        mainWindow.enableScreen(true);
    }

    /**
     * Gets the filter settings to be used for listing.
     * @return Filter settings to be used for listing.
     */
    private FilterData getFilterData() {
        FilterData filterData = null;
        try {
            filterData = mainWindow.getFilter().getFilterData();
        } catch (FilterException ex) {
            JOptionPane.showMessageDialog(mainWindow, Messages.getString("BROWSER_CHECK_FILTER_ERROR_MSG", ex.getMessage()),  //$NON-NLS-1$
            		Messages.getString("MSG_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ 
        }

        return filterData;
    }

    /**
     * Send the 'List' message and return response as a two-dimensional array.
     * @return Two-dimensional array containing retrieved list data.
     */
    private Object[][] getDataList() {

        Object[][] data = null;

        FilterData filterData = getFilterData();

        if (filterData != null) {

            List<MessageListEntry> messageList = null;

            try {
                if (filterData.isFilterByCode()) {
                    messageList = list.list(filterData.getCode(),
                            filterData.getMessageID(),
                            filterData.getType(),
                            filterData.getOwner());
                } else {
                    messageList = list.list(filterData.getStartDate(),
                            filterData.getEndDate(),
                            filterData.getMsgInterval(),
                            filterData.getMessageID(),
                            filterData.getType(),
                            filterData.getOwner());
                }

                int len = messageList.size();
                data = toArray(messageList);

                if (len == 0) {
                    String msg = Messages.getString("BROWSER_STATUS_NO_MESSAGES_RETRIEVED"); //$NON-NLS-1$
                    logger.logMessage(msg);
                    status.setStatus(msg);
                    JOptionPane.showMessageDialog(mainWindow, msg, Messages.getString("BROWSER_NO_MESSAGES_TITLE"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
                } else {                    
                	String msg = Messages.getString("BROWSER_STATUS_MESSAGES_RETRIEVED", len); //$NON-NLS-1$
                	status.setStatus(msg);
                	logger.logMessage(msg);
                }

            } catch (ClientException ex) {
                JOptionPane.showMessageDialog(mainWindow,
                        Messages.getString("BROWSER_UNABLE_TO_LIST", ex.getMessage()), //$NON-NLS-1$
                        Messages.getString("MSG_ERROR_TITLE"),  //$NON-NLS-1$
                        JOptionPane.ERROR_MESSAGE);
                logger.logMessage(Messages.getString("BROWSER_UNABLE_TO_LIST", ex.getMessage())); //$NON-NLS-1$
            } catch (Exception e) {
            	JOptionPane.showMessageDialog(mainWindow,
                        Messages.getString("BROWSER_UNABLE_TO_BROWSER_UNKNOW"), //$NON-NLS-1$
                        Messages.getString("MSG_ERROR_TITLE"),  //$NON-NLS-1$
                        JOptionPane.ERROR_MESSAGE);
            	logger.logException(Messages.getString("BROWSER_UNABLE_TO_BROWSER_UNKNOW"), e); //$NON-NLS-1$
            }
        }

        return data;
    }

    /**
     * Transform service response into a two-dimensional array
     * containing generic objects.
     * @param msgList The response retrieved from List service.
     * @return Two-dimensional array containing this objects.
     */
    private Object[][] toArray(final List<MessageListEntry> msgList) {
        int len = msgList.size();
        Object[][] data = new Object[len][ColumnsId.values().length];

        int row = 0;
        for (MessageListEntry message : msgList) {

            data[row][ColumnsId.CODE.ordinal()] = message.getCode();
            data[row][ColumnsId.ID.ordinal()] = message.getMessageIdentification();
            data[row][ColumnsId.TYPE.ordinal()] = message.getType();
            data[row][ColumnsId.VERSION.ordinal()] = message.getVersion();
            
            String status = message.getStatus();
            
            if (status == null) {
                data[row][ColumnsId.STATUS.ordinal()] = null;
            } else {
                data[row][ColumnsId.STATUS.ordinal()] = new MessageStatus(status);
            }
            
            data[row][ColumnsId.APPLICATION_ST_TIME.ordinal()] = message.getApplicationStartTime();
            data[row][ColumnsId.APPLICATION_END_TIME.ordinal()] = message.getApplicationEndTime();
            data[row][ColumnsId.SERVER_TIMESTAMP.ordinal()] = message.getServerTimestamp();
            data[row][ColumnsId.OWNER.ordinal()] = message.getOwner();

            row++;
        }

        return data;
    }

}
