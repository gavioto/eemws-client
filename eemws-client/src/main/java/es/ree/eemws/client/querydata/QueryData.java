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

package es.ree.eemws.client.querydata;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Element;

import ch.iec.tc57._2011.schema.message.PayloadType;
import ch.iec.tc57._2011.schema.message.RequestMessage;
import ch.iec.tc57._2011.schema.message.ResponseMessage;
import es.ree.eemws.client.common.ParentClient;
import es.ree.eemws.core.utils.error.EnumErrorCatalog;
import es.ree.eemws.core.utils.iec61968100.EnumFilterElement;
import es.ree.eemws.core.utils.iec61968100.EnumNoun;
import es.ree.eemws.core.utils.iec61968100.EnumVerb;
import es.ree.eemws.core.utils.iec61968100.MessageUtil;
import es.ree.eemws.core.utils.operations.HandlerException;
import es.ree.eemws.core.utils.operations.query.QueryOperationException;
import es.ree.eemws.core.utils.operations.query.QueryRequestMessageValidator;
import es.ree.eemws.core.utils.xml.XMLElementUtil;

/**
 * Query Data Service can be used by clients to request specific data from the server using different query parameters.
 * 
 * @author Red Eléctrica de España S.A.U.
 * @version 1.1 10/01/2016
 */
public final class QueryData extends ParentClient {

    /** QueryData request messages are not signed. */
    private static final boolean SIGN_REQUEST = false;

    /** QueryData response messages signature are validated. */
    private static final boolean VERIFY_RESPONSE_SIGNATURE = true;

    /**
     * Constructor.
     */
    public QueryData() {

        setSignRequest(SIGN_REQUEST);
        setVerifyResponse(VERIFY_RESPONSE_SIGNATURE);
    }

    /**
     * Invokes the QueryData operation with no parameters (just the query identification).
     * @param dataType Indicates the type of data being requested.
     * @return String with the server's response (only payload content).
     * @throws QueryOperationException If the retrieved message has an invalid format or the application cannot handle it.
     * or if the retrieved message has invalid signature or is not valid (has no header, invalid verb, etc.)
     */
    public String query(final String dataType) throws QueryOperationException {

        return query(dataType, null, null, null);
    }

    /**
     * Invokes the QueryData operation with start time parameter.
     * @param dataType Indicates the type of data being requested.
     * @param startTime Specifies that the returned message should only include data whose Application Date is after the
     * given date.
     * @return String with the server's response (only payload content).
     * @throws QueryOperationException If the retrieved message has an invalid format or the application cannot handle it.
     * or if the retrieved message has invalid signature or is not valid (has no header, invalid verb, etc.)
     */
    public String query(final String dataType, final Date startTime) throws QueryOperationException {

        return query(dataType, startTime, null, null);
    }

    /**
     * Invokes the QueryData operation with start and end time parameters.
     * @param dataType Indicates the type of data being requested.
     * @param startTime Specifies that the returned message should only include data whose Application Date is after the
     * given date
     * @param endTime Specifies that the returned message should only include data whose Application Date is before the
     * given date.
     * @return String with the server's response (only payload content).
     * @throws QueryOperationException If the retrieved message has an invalid format or the application cannot handle it.
     * or if the retrieved message has invalid signature or is not valid (has no header, invalid verb, etc.)
     */
    public String query(final String dataType, final Date startTime, final Date endTime) throws QueryOperationException {

        return query(dataType, startTime, endTime, null);
    }

    
    /**
     * Invokes the QueryData operation with the given parameters.
     * @param msgOptions List options as a Map which key must be on the EnumFilterElement list.
     * @return String with the server's response (only payload content).
     * @throws QueryOperationException If the retrieved message has an invalid format or the application cannot handle it.
     * or if the retrieved message has invalid signature or is not valid (has no header, invalid verb, etc.)
     */
    public String query(final Map<String, String> msgOptions) throws QueryOperationException {

        String retValue = null;

        try {
            RequestMessage requestMessage = MessageUtil.createRequestWithOptions(EnumVerb.GET, EnumNoun.QUERY_DATA, msgOptions);
            QueryRequestMessageValidator.validate(requestMessage);
            ResponseMessage response = sendMessage(requestMessage);
            validateResponse(response, EnumNoun.QUERY_DATA.toString());
                        
            retValue = responsePayload2String(response);

        } catch (TransformerException | ParserConfigurationException e) {
            throw new QueryOperationException(EnumErrorCatalog.ERR_QRY_012, e, e.getMessage());
        } catch (HandlerException e) {
            throw new QueryOperationException(e);
        }

        return retValue;
    }

    /**
     * Invokes the QueryData operation with the given parameters.
     * @param dataType Indicates the type of data being requested.
     * @param startTime Specifies that the returned message should only include data whose Application Date is after the
     * given date. (Can be <code>null</code>).
     * @param endTime Specifies that the returned message should only include data whose Application Date is before the
     * given date. (Can be <code>null</code>).
     * @param others Specifies others parameters to the query. The parameters are expressed as a key-value pairs, where
     * the value is optional.
     * @return String with the server's response (only payload content).
     * @throws QueryOperationException If the retrieved message has an invalid format or the application cannot handle it.
     * or if the retrieved message has invalid signature or is not valid (has no header, invalid verb, etc.)
     */
    public String query(final String dataType, final Date startTime, final Date endTime, final Map<String, String> others) throws QueryOperationException {

        Map<String, String> msgOptions = new HashMap<>(others);

        msgOptions.put(EnumFilterElement.DATA_TYPE.toString(), dataType);

        if (startTime != null) {
            DateFormat df = DateFormat.getInstance();
            msgOptions.put(EnumFilterElement.START_TIME.toString(), df.format(startTime));
        }

        if (endTime != null) {
            DateFormat df = DateFormat.getInstance();
            msgOptions.put(EnumFilterElement.END_TIME.toString(), df.format(endTime));
        }

        return query(msgOptions);
    }

    /**
     * Returns the operation response's payload content as a String.
     * @param responseMessage Response message received from server.
     * @return String with the server's response (only payload content).
     * @throws TransformerException If the method cannot create a string from the received response.
     * @throws ParserConfigurationException If the method cannot create a string from the received response.
     */
    private String responsePayload2String(final ResponseMessage responseMessage) throws TransformerException, ParserConfigurationException {
        PayloadType payload = responseMessage.getPayload();

        Element message = payload.getAnies().get(0);

        return XMLElementUtil.element2String(message);
    }
}
