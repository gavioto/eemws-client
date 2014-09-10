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
package es.ree.eemws.client.exception;

import java.util.List;

import ch.iec.tc57._2011.schema.message.ErrorType;
import es.ree.eemws.client.common.Messages;
import _504.iec62325.wss._1._0.MsgFaultMsg;


/**
 * General exception used by the client of the web service.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public final class ClientException extends Exception {

	/** Serial version UID. */
	private static final long serialVersionUID = -4884537802173142975L;
	
	/** Fault message cause. Can be <code>null</code>. */
    private MsgFaultMsg faultCause;
    
    /**
     * Constructor.
     */
    public ClientException() {

        super();
    }

    /**
     * Constructor.
     * @param cause Cause of the exception.
     */
    public ClientException(final String cause) {

        super(cause);
    }

    /**
     * Constructor.
     * @param cause Fault received from server.
     */
    public ClientException(final MsgFaultMsg cause) {
    	super(cause);
    	faultCause = cause;
    }
    
    /**
     * Constructor.
     * @param cause Exception.
     */
    public ClientException(final Throwable cause) {

        super(cause);
    }

    /**
     * Constructor.
     * @param errorMessage Error message.
     * @param cause Exception.
     */
    public ClientException(final String errorMessage, final Throwable cause) {

        super(errorMessage, cause);
    }

    /**
     * Returns this exception error message.
     * In case that the exception was produced by a fault message,
     * a string version of such fault will be also returned.
     * @return A string message with this exception details. 
     */
    @Override
    public String getMessage() {
    	String retValue;
    	if (faultCause == null) {
    		retValue = super.getMessage();
    	} else {
    		StringBuilder sb = new StringBuilder();
    		sb.append(Messages.getString("SERVER_FAULT", faultCause.getMessage(), faultCause.getFaultInfo().getReply().getResult())); //$NON-NLS-1$
    		
    		List<ErrorType> lstErr = faultCause.getFaultInfo().getReply().getErrors();
    		if (lstErr != null && !lstErr.isEmpty()) {
    			
    			sb.append("["); //$NON-NLS-1$
    			sb.append(Messages.getString("SERVER_FAULT_ERRORS")); //$NON-NLS-1$
    			sb.append("="); //$NON-NLS-1$
    			for (ErrorType error : lstErr) {
    				sb.append("{"); //$NON-NLS-1$
    				String str;
    				str = error.getCode();
    				if (str != null) {
    					sb.append("["); //$NON-NLS-1$
    					sb.append(Messages.getString("SERVER_FAULT_CODE")); //$NON-NLS-1$
    					sb.append("="); //$NON-NLS-1$
    					sb.append(str);
    					sb.append("]"); //$NON-NLS-1$
    				}
    				
    				str = error.getReason();
    				if (str != null) {
    					sb.append("["); //$NON-NLS-1$
    					sb.append(Messages.getString("SERVER_FAULT_REASON")); //$NON-NLS-1$
    					sb.append("="); //$NON-NLS-1$
    					sb.append(str);
    					sb.append("]"); //$NON-NLS-1$
    				}
    				
    				str = error.getDetails();
    				if (str != null) {
    					sb.append("["); //$NON-NLS-1$
    					sb.append(Messages.getString("SERVER_FAULT_DETAILS")); //$NON-NLS-1$
    					sb.append("="); //$NON-NLS-1$
    					sb.append(str);
    					sb.append("]"); //$NON-NLS-1$
    				}
    				sb.append("},"); //$NON-NLS-1$
    			}
    			sb.setLength(sb.length() - 1);
    			sb.append("]"); //$NON-NLS-1$
    		}
    		
    		retValue = sb.toString();    		
    	}
    	
    	return retValue;
    }

}
