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
package es.ree.eemws.client.querydata;

import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import _504.iec62325.wss._1._0.MsgFaultMsg;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.OptionType;
import ch.iec.tc57._2011.schema.message.RequestMessage;
import ch.iec.tc57._2011.schema.message.RequestType;
import ch.iec.tc57._2011.schema.message.ResponseMessage;
import es.ree.eemws.client.common.ParentClient;
import es.ree.eemws.client.exception.ClientException;
import es.ree.eemws.core.utils.xml.XMLGregorianCalendarFactory;


/**
 * Query Data Service can be used by clients to request specific data from the server using different query parameters.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public final class QueryData extends ParentClient {

    /** Verb of the action. */
    private static final String VERB = "get";

    /** Noun of the action. */
    private static final String NOUN = "QueryData";

    /** Name of the DataType option. */
    private static final String DATA_TYPE_OPTION = "DataType";

    /** Name of the AreaCode option. */
    private static final String AREA_CODE_OPTION = "AreaCode";

    /**
     * Constructor.
     */
    public QueryData() {

        setSignRequest(false);
        setVerifyResponse(true);
    }

    /**
     * This method can be used by clients to request specific data from the server using different query parameters.
     * @param dataType Indicates the type of data being requested.
     * @return String with the XML message.
     * @throws ClientException Exception with the error.
     */
    public String query(final String dataType) throws ClientException {

        return query(dataType, null, null, null);
    }

    /**
     * This method can be used by clients to request specific data from the server using different query parameters.
     * @param dataType Indicates the type of data being requested.
     * @param startTime Specifies that the returned message should only include data whose Application Date is after the provided.
     * @return String with the XML message.
     * @throws ClientException Exception with the error.
     */
    public String query(final String dataType, final Date startTime)
            throws ClientException {

        return query(dataType, startTime, null, null);
    }

    /**
     * This method can be used by clients to request specific data from the server using different query parameters.
     * @param dataType Indicates the type of data being requested.
     * @param startTime Specifies that the returned message should only include data whose Application Date is after the provided.
     * @param endTime Specifies that the returned message should only include data whose Application Date is before the provided date.
     * @return String with the XML message.
     * @throws ClientException Exception with the error.
     */
    public String query(final String dataType, final Date startTime, final Date endTime)
            throws ClientException {

        return query(dataType, startTime, endTime, null);
    }

    /**
     * This method can be used by clients to request specific data from the server using different query parameters.
     * @param dataType Indicates the type of data being requested.
     * @param startTime Specifies that the returned message should only include data whose Application Date is after the provided.
     * @param endTime Specifies that the returned message should only include data whose Application Date is before the provided date.
     * @param areaCode Specifies that the returned data should be relevant to the provided Area.
     * @return String with the XML message.
     * @throws ClientException Exception with the error.
     */
    public String query(final String dataType, final Date startTime, final Date endTime, final String areaCode)
            throws ClientException {

        try {

            RequestMessage requestMessage = createRequest(dataType, startTime, endTime, areaCode);
            ResponseMessage responseMessage = sendMessage(requestMessage);
            return getPrettyPrintPayloadMessage(responseMessage);

        } catch (MsgFaultMsg e) {

            throw new ClientException(e);
        }
    }

    /**
     * This method create the request message.
     * @param dataType Indicates the type of data being requested.
     * @param startTime Specifies that the returned message should only include data whose Application Date is after the provided.
     * @param endTime Specifies that the returned message should only include data whose Application Date is before the provided date.
     * @param areaCode Specifies that the returned data should be relevant to the provided Area.
     * @return Request message.
     */
    private RequestMessage createRequest(final String dataType, final Date startTime, final Date endTime, final String areaCode) {

        RequestMessage message = new RequestMessage();

        HeaderType header = createHeader(VERB, NOUN);
        message.setHeader(header);

        RequestType resquest = new RequestType();

        if (startTime != null) {

            XMLGregorianCalendar xmlStartTime = XMLGregorianCalendarFactory.getInstance(startTime);
            resquest.setStartTime(xmlStartTime);
        }

        if (endTime != null) {

            XMLGregorianCalendar xmlEndTime = XMLGregorianCalendarFactory.getInstance(endTime);
            resquest.setEndTime(xmlEndTime);
        }

        List<OptionType> options = resquest.getOptions();
        OptionType option = createOption(DATA_TYPE_OPTION, dataType);
        options.add(option);

        if (areaCode != null) {

            option = createOption(AREA_CODE_OPTION, areaCode);
            options.add(option);
        }

        message.setRequest(resquest);
        return message;
    }
}
