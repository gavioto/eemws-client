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

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import _504.iec62325.wss._1._0.MsgFaultMsg;
import ch.iec.tc57._2011.schema.message.RequestMessage;
import ch.iec.tc57._2011.schema.message.ResponseMessage;
import es.ree.eemws.client.common.ClientException;
import es.ree.eemws.client.common.ParentClient;
import es.ree.eemws.core.utils.iec61968100.EnumFilterElement;
import es.ree.eemws.core.utils.iec61968100.EnumNoun;
import es.ree.eemws.core.utils.iec61968100.EnumVerb;
import es.ree.eemws.core.utils.iec61968100.MessageUtil;

/**
 * Query Data Service can be used by clients to request specific data from the server using different query parameters.
 * 
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public final class QueryData extends ParentClient {

	/** QueryData request messages are not signed. */
	private static final boolean SIGN_REQUEST = false;

	/** QueryData response messages signature are validated. */
	private static final boolean VERIFY_RESPONSE = true;

	/**
	 * Constructor.
	 */
	public QueryData() {

		setSignRequest(SIGN_REQUEST);
		setVerifyResponse(VERIFY_RESPONSE);
	}

	/**
	 * Invokes the QueryData operation with no parameters (just the query identification).
	 * @param dataType Indicates the type of data being requested.
	 * @return String with the payload message.
	 * @throws ClientException Exception with the error.
	 */
	public String query(final String dataType) throws ClientException {

		return query(dataType, null, null, null);
	}

	/**
	 * Invokes the QueryData operation with start time parameter.
	 * @param dataType Indicates the type of data being requested.
	 * @param startTime Specifies that the returned message should only include data whose Application Date is after the
	 * given date.
	 * @return String with the payload message.
	 * @throws ClientException Exception with the error.
	 */
	public String query(final String dataType, final Date startTime) throws ClientException {

		return query(dataType, startTime, null, null);
	}

	/**
	 * Invokes the QueryData operation with start and end time parameters.
	 * @param dataType Indicates the type of data being requested.
	 * @param startTime Specifies that the returned message should only include data whose Application Date is after the
	 * given date
	 * @param endTime Specifies that the returned message should only include data whose Application Date is before the
	 * given date.
	 * @return String with the payload message.
	 * @throws ClientException Exception with the error.
	 */
	public String query(final String dataType, final Date startTime, final Date endTime) throws ClientException {

		return query(dataType, startTime, endTime, null);
	}

	/**
	 * This method can be used by clients to request specific data from the server using different query parameters.
	 * @param dataType Indicates the type of data being requested.
	 * @param startTime Specifies that the returned message should only include data whose Application Date is after the
	 * given date. (Can be <code>null</code>).
	 * @param endTime Specifies that the returned message should only include data whose Application Date is before the
	 * given date. (Can be <code>null</code>).
	 * @param others Specifies others parameters to the query. The parameters are expressed as a key-value pairs, where
	 * the value is optional.
	 * @return String with the XML message.
	 * @throws ClientException Exception with the error.
	 */
	public String query(final String dataType, final Date startTime, final Date endTime, final Map<String, String> others) throws ClientException {

		try {

		    Map<String, String> options = new HashMap<>(others);
		    
		    options.put(EnumFilterElement.DATA_TYPE.toString(), dataType);
		    
		    if (startTime != null) {
		        DateFormat df = DateFormat.getInstance();
		        options.put(EnumFilterElement.START_TIME.toString(), df.format(startTime));
		    }
		    
		    if (endTime != null) {
		        DateFormat df = DateFormat.getInstance();
		        options.put(EnumFilterElement.END_TIME.toString(), df.format(endTime));
		    }		    
		    
			RequestMessage requestMessage = MessageUtil.createRequestWithOptions(EnumVerb.GET, EnumNoun.QUERY_DATA, options);
			ResponseMessage responseMessage = sendMessage(requestMessage);
			return getPrettyPrintPayloadMessage(responseMessage);

		} catch (MsgFaultMsg e) {

			throw new ClientException(e);
		}
	}
}
