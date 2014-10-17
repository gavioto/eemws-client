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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import es.ree.eemws.kit.common.Messages;
import es.ree.eemws.client.list.IntervalTimeType;


/**
 * Object Data Value that represents the list's filter data.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 02/06/2014
 */
public final class FilterData {
	
	/** Value for "no code". */
	public static final int NO_CODE = -1;

    /** Date format for cell values and filters. */
    private SimpleDateFormat sdf = new SimpleDateFormat(Filter.DATE_FORMAT);

    /** Message code. */
    private long code = NO_CODE;

    /** Interval start date. */
    private Date startDate = null;

    /** Interval end date. */
    private Date endDate = null;

    /** Type of message to filter by. */
    private String messageType = null;

    /** Message ID to filter by. */
    private String messageID = null;

    /** Filter owner.*/
    private String owner = null;

    /** Defines type of interval. */
    private IntervalTimeType msgInterval = null;

    /**
     * Sets list filter code.
     * @param strCode Message code filter value.
     */
    public void setCode(final String strCode) {
        try {
            code = Long.parseLong(strCode);
        } catch (NumberFormatException ex) {
            throw new FilterException(Messages.getString("INCORRECT_CODE", strCode)); //$NON-NLS-1$
        }
    }
 
    /**
     * Returns <code>true</code> if the current filter configuration is by code.
     * @return <code>true</code> if the current filter configuration is by code. <code>false</code> otherwise.
     */
    public boolean isFilterByCode() {
		return code != NO_CODE;
	}
    
    /**
     * Returns the message code filter value.
     * @return The message code filter value.
     */
    public long getCode() {
        return code;
    }

    /**
     * Gets the start time interval filter value.
     * @return Start time interval filter value.
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Sets start time interval filter value.
     * @param strStartDate Start time interval filter value.
     */
    public void setStartDate(final String strStartDate) {
    	String msg = Messages.getString("BROWSER_FILTER_START_DATE"); //$NON-NLS-1$
    	msg = msg.substring(0, msg.length() - 1); // remove last ":"
        startDate = getDateFromStr(strStartDate, msg);
    }

    /**
     * Gets the end time interval filter value.
     * @return End time interval filter value.
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Sets end time interval filter value.
     * The hour, minutes and seconds values of the filter are set to the end of the day.
     * @param strEndDate End time interval filter value.
     */
    public void setEndDate(final String strEndDate) {

    	String msg = Messages.getString("BROWSER_FILTER_END_DATE"); //$NON-NLS-1$
    	msg = msg.substring(0, msg.length() - 1); // remove last ":"
    	
        endDate = getDateFromStr(strEndDate, msg);
        if (endDate != null) {
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(endDate);
            calEnd.set(Calendar.HOUR_OF_DAY, 23);
            calEnd.set(Calendar.MINUTE, 59);
            calEnd.set(Calendar.SECOND, 59);
            endDate = calEnd.getTime();
        }
    }

    /**
     * Returns a Date object from a formatted string.
     * @param date Date entered by user with dd/MM/yyyy format.
     * @param dateName Name of the date (for error information)
     * @return Date object containing date given as a <code>String</string>.
     */
    private Date getDateFromStr(final String date, final String dateName) {
        Date retValue = null;

        if (date != null) {
            try {
                retValue = sdf.parse(date);
            } catch (ParseException e) {
            	throw new FilterException(Messages.getString("INCORRECT_DATE_FORMAT", dateName, date, Filter.DATE_FORMAT)); //$NON-NLS-1$
            }

            String originDate = sdf.format(retValue);

            if (!originDate.equals(date)) {
            	throw new FilterException(Messages.getString("INCORRECT_DATE_FORMAT", dateName, date, Filter.DATE_FORMAT)); //$NON-NLS-1$
            }
        }

        return retValue;
    }

    /**
     * Sets the type of message to filter by.
     * @param msgType Type of message to filter by.
     */
    public void setType(final String msgType) {
        if (msgType.trim().isEmpty()) {
            messageType = null;
        } else {
            messageType = msgType.trim();
        }
    }

    /**
     * Gets the type of message to filter by.
     * @return Type of message to filter by.
     */
    public String getType() {
        return messageType;
    }

    /**
     * Gets Message ID filter value.
     * @return Message ID filter value.
     */
    public String getMessageID() {
        return messageID;
    }

    /**
     * Sets the message ID filter value.
     * @param msgID Message ID filter value.
     */
    public void setMessageID(final String msgID) {
        if (msgID.trim().isEmpty()) {
            messageID = null;
        } else {
            messageID = msgID.trim();
        }

    }

    /**
     * Sets 'owner' filter value.
     * @param ownr Owner filter value.
     */
    public void setOwner(final String ownr) {
        if (ownr.trim().isEmpty()) {
        	owner = null;
        } else {
        	owner = ownr.trim();
        }
    }

    /**
     * Gets 'owner' filter value.
     * @return 'owner' filter value.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Gets 'interval' filtering field.
     * @return 'interval' filtering field.
     */

    public IntervalTimeType getMsgInterval() {
        return msgInterval;
    }

    /**
     * Sets 'interval' filtering field.
     * @param pMsgInterval Owner by which query will be filtered.
     */
    public void setMsgInterval(final IntervalTimeType pMsgInterval) {
        msgInterval = pMsgInterval;
    }

	
}
