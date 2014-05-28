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
package es.ree.eemws.client.listmessages;

import java.io.Serializable;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * Data of the message.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public final class MessageData implements Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = 6056968994966130764L;

    /** Code. */
    private BigInteger code = null;

    /** Message identification. */
    private String messageIdentification = null;

    /** Message version. */
    private BigInteger version = null;

    /** Status. */
    private String status = null;

    /** Application start time. */
    private Calendar applicationStartTime = null;

    /** Application end time. */
    private Calendar applicationEndTime = null;

    /** Server date. */
    private Calendar serverTimestamp = null;

    /** Type. */
    private String type = null;

    /** Owner. */
    private String owner = null;

    /** Format of the date. */
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    /** Format of the date. */
    private SimpleDateFormat sdfTimestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    /**
     * This method get the code.
     * @return Code.
     */
    public BigInteger getCode() {

        return code;
    }

    /**
     * This method get the message identification.
     * @return Message identification.
     */
    public String getMessageIdentification() {

        return messageIdentification;
    }

    /**
     * This method get the message version.
     * @return Message version.
     */
    public BigInteger getVersion() {

        return version;
    }

    /**
     * This method get the status.
     * @return Status.
     */
    public String getStatus() {

        return status;
    }

    /**
     * This method get the application start time.
     * @return Application start time.
     */
    public Calendar getApplicationStartTime() {

        return applicationStartTime;
    }

    /**
     * This method get the application end time.
     * @return Application end time.
     */
    public Calendar getApplicationEndTime() {

        return applicationEndTime;
    }

    /**
     * This method get the server date.
     * @return Server date.
     */
    public Calendar getServerTimestamp() {

        return serverTimestamp;
    }

    /**
     * This method get the type.
     * @return Type.
     */
    public String getType() {

        return type;
    }

    /**
     * This method get the owner.
     * @return Owner.
     */
    public String getOwner() {

        return owner;
    }

    /**
     * This method set the code.
     * @param inCode Code.
     */
    public void setCode(final BigInteger inCode) {

        code = inCode;
    }

    /**
     * This method set the message identification.
     * @param inMessageIdentification Message identification.
     */
    public void setMessageIdentification(final String inMessageIdentification) {

        messageIdentification = inMessageIdentification;
    }

    /**
     * This method set the message version.
     * @param inVersion Message version.
     */
    public void setVersion(final BigInteger inVersion) {

        version = inVersion;
    }

    /**
     * This method set the status.
     * @param inStatus Status.
     */
    public void setStatus(final String inStatus) {

        status = inStatus;
    }

    /**
     * This method set the application start time.
     * @param inApplicationStartTime Application start time.
     */
    public void setApplicationStartTime(final Calendar inApplicationStartTime) {

        applicationStartTime = inApplicationStartTime;
    }

    /**
     * This method set the application end time.
     * @param inApplicationEndTime Application end time.
     */
    public void setApplicationEndTime(final Calendar inApplicationEndTime) {

        applicationEndTime = inApplicationEndTime;
    }

    /**
     * This method set the server date.
     * @param inServerTimestamp Server date.
     */
    public void setServerTimestamp(final Calendar inServerTimestamp) {

        serverTimestamp = inServerTimestamp;
    }

    /**
     * This method set the type.
     * @param inType Type.
     */
    public void setType(final String inType) {

        type = inType;
    }

    /**
     * This method set the owner.
     * @param inOwner Owner.
     */
    public void setOwner(final String inOwner) {

        owner = inOwner;
    }

    /**
     * This method show the message data.
     * @return Message data.
     */
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("Code[").append(code).append("] ");
        sb.append("MessageIdentification[").append(messageIdentification).append("] ");

        if (version != null) {

            sb.append("MessageVersion[").append(version).append("] ");
        }

        if (status != null) {

            sb.append("Status[").append(status).append("] ");
        }

        sb.append("ApplicationTimeInterval - start[").append(sdf.format(applicationStartTime.getTime())).append("] ");

        if (applicationEndTime != null) {

            sb.append("ApplicationTimeInterval - end[").append(sdf.format(applicationEndTime.getTime())).append("] ");
        }

        sb.append("ServerTimestamp[").append(sdfTimestamp.format(serverTimestamp.getTime())).append("] ");
        sb.append("Type[").append(type).append("] ");
        sb.append("Owner[").append(owner).append("] ");

        return sb.toString();
    }
}
