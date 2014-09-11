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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * Filtering data set by user.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 02/06/2014
 */
public final class FilterData {

    /** Length of subject EIC code. */
    private static final int EIC_CODE_LENGTH = 16;

    /** Constant for 'Application' interval.
     * @see #msgInterval. */
    public static final String APPLICATION = "Application";

    /** Constant for 'Server' interval.
     * @see #msgInterval. */
    public static final String SERVER = "Server";

    /** Date format for cell values and filters. */
    private SimpleDateFormat sdf = new SimpleDateFormat(Filter.DATE_FORMAT);

    /** Message code (incremental lists). */
    private long code = -1;

    /** Type of filter to apply.*/
    private int filterType = -1;

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
    private String msgInterval = null;

    /**
     * Set list code.
     * @param strCode Message code to filter by.
     */
    public void setCode(final String strCode) {
        try {
            code = Long.parseLong(strCode);
        } catch (NumberFormatException ex) {
            throw new FilterException("Code value must be a numeric value.");
        }
    }

    /**
     * This method create a string to show the data.
     * @return String to show the data.
     */
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        if (filterType == -1) {

            sb.append("No filter");

        } else {

            if (filterType == 0) {

                sb.append("[code:");
                sb.append(code);
                sb.append("]");

            } else {

                if (filterType == 1) {
                    sb.append("[REGDAY]");
                } else if (filterType == 2) {
                    sb.append("[APPDATE]");
                }
                sb.append("[StartDate:");
                sb.append(sdf.format(startDate));
                sb.append("][EndDate:");
                sb.append(sdf.format(endDate));
                sb.append("]");
            }

            if (messageType != null) {
                sb.append("[Type:");
                sb.append(messageType);
                sb.append("]");
            }

            if (messageID != null) {
                sb.append("[ID:");
                sb.append(messageID);
                sb.append("]");
            }

            if (owner != null) {
                sb.append("[Owner:");
                sb.append(owner);
                sb.append("]");
            }
        }

        return sb.toString();
    }

    /**
     * Return list code to filter by.
     * @return List code to filter by (incremental list).
     */
    public long getCode() {
        return code;
    }

    /**
     * Return type of list to apply (Application, horizon, incremental).
     * @return Type of list to apply.
     */
    public int getFilterType() {
        return filterType;
    }

    /**
     * Set type of list to apply.
     * @param tipo type of list to apply (application, etc.)
     */
    public void setFilterType(final int tipo) {
        filterType = tipo;
    }

    /**
     * Get interval start date for filter.
     * @return Interval start date for filter.
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Set interval start date for filter.
     * @param strStartDate Horizon interval start date with 'dd/MM/yyyy' format.
     */
    public void setStartDate(final String strStartDate) {
        startDate = getDateFromStr(strStartDate, "inicio");
    }

    /**
     * Get interval end date for filter.
     * @return Interval end date for filter.
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Set interval end date for filter.
     * @param strEndDate Horizon interval start date with 'dd/MM/yyyy' format.
     */
    public void setEndDate(final String strEndDate) {

        endDate = getDateFromStr(strEndDate, "fin");
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
     * Return a Date object from a string.
     *
     * @param date Date entered by user with dd/MM/yyyy format.
     * @param dateType Type of date to process (start or end).
     * @return Date object containing date given as a string.
     */
    private Date getDateFromStr(final String date, final String dateType) {
        Date retValue = null;

        if (date != null) {
            try {
                retValue = sdf.parse(date);
            } catch (ParseException e) {
                throw new FilterException("Date " + dateType + " must match " + Filter.DATE_FORMAT + " format.");
            }

            String originDate = sdf.format(retValue);

            if (!originDate.equals(date)) {
                throw new FilterException("Date " + dateType + " contains out of range values.");
            }
        }

        return retValue;
    }

    /**
     * Set the type of message to filter by.
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
     * Get the type of message to filter by.
     * @return Type of message to filter by.
     */
    public String getType() {
        return messageType;
    }

    /**
     * Get Message ID to filter by.
     * @return Message ID to filter by.
     */
    public String getMessageID() {
        return messageID;
    }

    /**
     * Set the message ID to filter by..
     * @param msgID Message ID to filter by..
     */
    public void setMessageID(final String msgID) {
        if (msgID.trim().isEmpty()) {
            messageID = null;
        } else {
            messageID = msgID.trim();
        }

    }

    /**
     * Set 'owner' filtering field.
     * @param ownr Owner by which query will be filtered.
     */
    public void setOwner(final String ownr) {
        if (ownr.length() != EIC_CODE_LENGTH) {
            throw new FilterException("Owner code must be "
                    + EIC_CODE_LENGTH + " characters long.");
        }
        owner = ownr;
    }

    /**
     * Get 'owner' filtering field.
     * @return 'owner' filtering field.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Get 'interval' filtering field.
     * @return 'interval' filtering field.
     */

    public String getMsgInterval() {
        return msgInterval;
    }

    /**
     * Set 'interval' filtering field.
     * @param pMsgInterval Owner by which query will be filtered.
     */
    public void setMsgInterval(final String pMsgInterval) {
        this.msgInterval = pMsgInterval;
    }
}
