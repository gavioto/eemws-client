/*
 * Copyright 2016 Red Eléctrica de España, S.A.U.
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
package es.ree.eemws.client.list;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Calendar;

import es.ree.eemws.core.utils.error.EnumErrorCatalog;
import es.ree.eemws.core.utils.operations.list.ListOperationException;

/**
 * Keeps the values of a list element.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.1 10/01/2016
 */
public final class MessageListEntry implements Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = 1592261739373083411L;

    /** Message's code. */
    private BigInteger code = null;

    /** Message's identification. */
    private String messageIdentification = null;

    /** Message's version. */
    private BigInteger version = null;

    /** Message's status. */
    private String status = null;

    /** Message's application start time. */
    private Calendar applicationStartTime = null;

    /** Message's application end time. */
    private Calendar applicationEndTime = null;

    /** Message's server date. */
    private Calendar serverTimestamp = null;

    /** Message's type. */
    private String type = null;

    /** Message's owner. */
    private String owner = null;
    
    /** Join message's identification and version. */    
    private static final char NAME_VERSION_SEPARATOR = '.';

    /**
     * Gets this message's code.
     * @return This message's code.
     */
    public BigInteger getCode() {
        return code;
    }
    
    /**
     * Sets this message's code.
     * @param cod This message's code.
     */
    public void setCode(final BigInteger cod) {
        code = cod;
    }

    /**
     * Gets this message's identification.
     * @return This message's identification.
     */
    public String getMessageIdentification() {
        return messageIdentification;
    }

    /**
     * Sets this message's identification.
     * @param id This message's identification.
     */
    public void setMessageIdentification(final String id) {
        messageIdentification = id;
    }
    
    /**
     * Gets this message's version.
     * @return This message's version.
     */
    public BigInteger getVersion() {
        return version;
    }

    /**
     * Sets this message's version.
     * @param ver This message's version.
     */
    public void setVersion(final BigInteger ver) {
        version = ver;
    }
    
    /**
     * Gets this message's status. 
     * @return This message's status.
     */
    public String getStatus() {
        return status;
    }
    
    /**
     * Sets this message's status. 
     * @param stat This message's status.
     */
    public void setStatus(final String stat) {
        status = stat;
    }

    /**
     * Gets this message's application start time.
     * @return This message's application start time.
     */
    public Calendar getApplicationStartTime() {
        return applicationStartTime;
    }
    
    /**
     * Sets this message's application start time.
     * @param time This message's application start time.
     */
    public void setApplicationStartTime(final Calendar time) {

        applicationStartTime = time;
    }

    /**
     * Gets this message's application end time.
     * @return This message's application end time.
     */
    public Calendar getApplicationEndTime() {
        return applicationEndTime;
    }
    
    /**
     * Sets this message's application end time.
     * @param time This message's application end time.
     */
    public void setApplicationEndTime(final Calendar time) {

        applicationEndTime = time;
    }

    /**
     * Gets this message's server date.
     * @return This message's server date.
     */
    public Calendar getServerTimestamp() {
        return serverTimestamp;
    }
    
    /**
     * Sets this message's server date.
     * @param time This message's server date.
     */
    public void setServerTimestamp(final Calendar time) {
        serverTimestamp = time;
    }

    /**
     * Gets this message's type.
     * @return This message's type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets this message's type.
     * @param typ This message's type.
     */
    public void setType(final String typ) {

        type = typ;
    }
    
    /**
     * Gets this message's owner.
     * @return This message's owner.
     */
    public String getOwner() {
        return owner;
    }   

    /**
     * Sets this message's owner.
     * @param own This message's owner.
     */
    public void setOwner(final String own) {
        owner = own;
    }

    /**
     * Checks that the entry has all its mandatory elements.
     * Note it is faster to check only one element entry that the whole list against schema.
     * This method is called only when one entry causes exception (usually because a mandatory element was not provided) 
     * @throws ListOperationException If the entry has at least one mandatory element set as <code>null</code>.
     */
    public void checkMandatoryElements() throws ListOperationException {

        if (getCode() == null) {
            throw new ListOperationException(EnumErrorCatalog.ERR_LST_012);
        }

        String codeStr = code.toString();

        if (getMessageIdentification() == null) {
            throw new ListOperationException(EnumErrorCatalog.ERR_LST_013);
        }

        String idMsg = getMessageIdentification();

        if (getVersion() == null) {
            idMsg = getMessageIdentification();
        } else {
            idMsg = getMessageIdentification() + NAME_VERSION_SEPARATOR + getVersion();
        }

        if (getType() == null) {
            throw new ListOperationException(EnumErrorCatalog.ERR_LST_014, codeStr, idMsg);
        }

        if (getApplicationStartTime() == null) {
            throw new ListOperationException(EnumErrorCatalog.ERR_LST_015, codeStr, idMsg, getType());
        }

        if (getServerTimestamp() == null) {
            throw new ListOperationException(EnumErrorCatalog.ERR_LST_016, codeStr, idMsg, getType());
        }

        if (getOwner() == null) {
            throw new ListOperationException(EnumErrorCatalog.ERR_LST_017, codeStr, idMsg, getType());
        }
    }

}
