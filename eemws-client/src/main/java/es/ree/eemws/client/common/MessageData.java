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
package es.ree.eemws.client.common;

import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.Calendar;


/**
 * Class with the data of the message.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public final class MessageData implements Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = -4335411655916552644L;

    /** Date when the message is received. */
    private Calendar dateReceived = null;

    /** X.509 Certificate. */
    private X509Certificate certificate = null;

    /** XML with the complete message. */
    private StringBuilder xmlMessage = null;

    /** Verb of the message. */
    private String verb = null;

    /** Noun of the message. */
    private String noun = null;

    /** Payload of the message. */
    private StringBuilder payload = null;

    /**
     * This method gets the date when the message is received.
     * @return Date when the message is received.
     */
    public Calendar getDateReceived() {

        return dateReceived;
    }

    /**
     * This method sets the date when the message is received.
     * @param date Date when the message is received.
     */
    public void setDateReceived(final Calendar date) {

        dateReceived = date;
    }

    /**
     * This method gets the X.509 Certificate.
     * @return X.509 Certificate.
     */
    public X509Certificate getCertificate() {

        return certificate;
    }

    /**
     * This method sets the X.509 Certificate.
     * @param x509Certificate X.509 Certificate.
     */
    public void setCertificate(final X509Certificate x509Certificate) {

        certificate = x509Certificate;
    }

    /**
     * This method gets the XML with the complete message.
     * @return XML with the complete message.
     */
    public StringBuilder getXmlMessage() {

        return xmlMessage;
    }

    /**
     * This method sets the XML with the complete message.
     * @param xml XML with the complete message.
     */
    public void setXmlMessage(final StringBuilder xml) {

        xmlMessage = xml;
    }

    /**
     * This method get the verb of the message.
     * @return Verb of the message.
     */
    public String getVerb() {

        return verb;
    }

    /**
     * This method set the verb of the message.
     * @param inVerb Verb of the message.
     */
    public void setVerb(final String inVerb) {

        verb = inVerb;
    }

    /**
     * This method get the noun of the message.
     * @return Noun of the message.
     */
    public String getNoun() {

        return noun;
    }

    /**
     * This method set the noun of the message.
     * @param inNoun Noun of the message.
     */
    public void setNoun(final String inNoun) {

        noun = inNoun;
    }

    /**
     * This method gets the payload of the message.
     * @return Payload of the message.
     */
    public StringBuilder getPayload() {

        return payload;
    }

    /**
     * This method sets the payload of the message.
     * @param xml Payload of the message.
     */
    public void setPayload(final StringBuilder xml) {

        payload = xml;
    }
}
