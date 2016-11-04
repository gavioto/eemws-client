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

package es.ree.eemws.client.get;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import ch.iec.tc57._2011.schema.message.ReplyType.ID;
import ch.iec.tc57._2011.schema.message.ResponseMessage;
import es.ree.eemws.core.utils.error.EnumErrorCatalog;
import es.ree.eemws.core.utils.file.GZIPUtil;
import es.ree.eemws.core.utils.iec61968100.EnumMessageFormat;
import es.ree.eemws.core.utils.iec61968100.EnumNoun;
import es.ree.eemws.core.utils.iec61968100.MessageUtil;
import es.ree.eemws.core.utils.operations.get.GetOperationException;
import es.ree.eemws.core.utils.xml.XMLUtil;

/**
 * Payload response message wrapper. 
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.3 11/08/2016
 */

public final class RetrievedMessage {

    /** Identification - Version character separator. Used to create a file name with the format id.version */
    private static final String VERSION_SEPARATOR = "."; //$NON-NLS-1$

    /** Binary content as byte[]. */
    private byte[] binaryContent;

    /** Xml content as String. */
    private String xmlContent;
   
    /** This message format (default = XML). */
    private EnumMessageFormat format = EnumMessageFormat.XML;

    /** This message fileName. */
    private String fileName;
    
    /** Response message. */
    private ResponseMessage respMess;

    /**
     * Returns whether the message is binary.
     * Note that XML that are received as binary are internally converted to String.
     * @return <code>true</code> for binary messages.
     */
    public boolean isBinary() {
        return format.equals(EnumMessageFormat.BINARY);
    }

    /**
     * Returns this message file name. If the message was retrieved as binary, the filename
     * will be the value of the reply's ID value. Otherwise, the get's filter parameters will be used.
     * @return This message file name. <code>null</code> if the QUEUE method was used and the returned 
     * message has no ID value.
     */
    public String getFileName() {
        return fileName;
    }

    /** 
     * Sets this message identification in order to create a proper file name identification.
     * @param messageIdentification Get filter's message identification.
     */
    public void setMsgIdentification(final String messageIdentification) {
        if (messageIdentification != null) {
            fileName = messageIdentification;
        }
    }

    /**
     * Sets this message version in order to create a proper file name identification. 
     * This method is called after {@link #setMsgIdentification(String)} only if the version applies.  
     * @param messageVersion Get filter's message version.
     */
    public void setMsgVersion(final String messageVersion) {
        if (fileName != null) {
            fileName += VERSION_SEPARATOR + messageVersion;
        }
    }

    /**
     * Returns this message binary content.
     * @return This message binary content. <code>null</code> if there is no binary content.
     */
    public byte[] getBinaryPayload() {
        return binaryContent;
    }

    /**
     * Returns this message string (xml) content.
     * @return This message string content. <code>null</code> if there is no payload.
     */
    public String getStringPayload() {
        return xmlContent;
    }

    /**
     * Returns a pretty print version of this message string (xml) content.
     * Note: If the System property <code>USE_PRETTY_PRINT_OUTPUT</code> is set, the output is already pretty printed.
     * @return A pretty print (formatted) version of this message string content. <code>null</code> if there is no string payload. 
     */
    public String getPrettyPayload() {
        String retValue = null;

        if (xmlContent != null) {
            retValue = XMLUtil.prettyPrint(XMLUtil.removeNameSpaces(xmlContent).toString()).toString();
        }

        return retValue;
    }
    
    /**
     * Returns the response message (whole reply from the get operation).
     * @return Reply of the get operation.
     */
    public ResponseMessage getResponseMessage() {
        return respMess;
    }

    /**
     * Sets this class payload or binary values from a ResponseMessage object.
     * @param response ResponseMessage object with payload or binary values.
     * @throws GetOperationException If the BINARY response has an invalid format or if it cannot be unzipped.  
     */
    public void setMessage(final ResponseMessage response) throws GetOperationException {
        
        respMess = response;
        
        try {

            if (response.getHeader().getNoun().equals(EnumNoun.COMPRESSED.toString())) {

                String msgFormat = response.getPayload().getFormat();
                if (msgFormat != null) {
                    format = EnumMessageFormat.fromString(msgFormat);

                    if (format == null) {
                        throw new GetOperationException(EnumErrorCatalog.ERR_GET_014, msgFormat, EnumMessageFormat.getList());
                    }
                }

                binaryContent = DatatypeConverter.parseBase64Binary(response.getPayload().getCompressed());

                /* Hide binary content if the payload is xml. */
                if (msgFormat == null || format.equals(EnumMessageFormat.XML)) {
                    xmlContent = new String(GZIPUtil.uncompress(binaryContent), StandardCharsets.UTF_8);
                    binaryContent = null;
                }

                List<ID> lsIds = response.getReply().getIDS();
                if (lsIds != null) {
                    boolean loop = true;
                    Iterator<ID> ids = lsIds.iterator();
                    while (ids.hasNext() && loop) {
                        ID id = ids.next();
                        if (EnumMessageFormat.BINARY_FILENAME_ID.equals(id.getIdType())) {
                            fileName = id.getValue();
                            loop = false;
                        }
                    }
                }

                if (isBinary() && fileName == null) {
                    throw new GetOperationException(EnumErrorCatalog.ERR_GET_018);
                }

            } else {
                xmlContent = MessageUtil.responsePayload2String(response);
            }

        } catch (IOException e) {
            throw new GetOperationException(EnumErrorCatalog.ERR_GET_015, e);
        } catch (TransformerException | ParserConfigurationException e) {
            throw new GetOperationException(EnumErrorCatalog.ERR_GET_015, e);
        }
    }

}
