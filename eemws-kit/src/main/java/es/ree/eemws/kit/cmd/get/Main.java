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
package es.ree.eemws.kit.cmd.get;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.ree.eemws.kit.cmd.ParentMain;
import es.ree.eemws.kit.config.Configuration;
import es.ree.eemws.client.exception.ClientException;
import es.ree.eemws.client.getmessage.GetMessage;
import es.ree.eemws.core.utils.file.FileUtil;


/**
 * Main class to get messages.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public final class Main extends ParentMain {

    /** Name of the command. */
    private static final String COMMAND_NAME = "get";

    /** Log messages. */
    private static Logger logger = Logger.getLogger(COMMAND_NAME);

    /**
     * Main. Execute the get action.
     *
     * @param args The arguments must be:
     *               - arg[0]: message type (mandatory)
     *               - arg[1]: -msgId (c1)
     *               - arg[2]: message identification (c1)
     *               - arg[3]: -msgVer (c1)
     *               - arg[4]: message version (c1)
     *               - arg[5]: -code (c2)
     *               - arg[6]: code (c2)
     *               - arg[7]: -queue (c3)
     *               - arg[8]: "NEXT" (c3)
     *               - arg[9]: -out (optional)
     *               - arg[10]: output file (optional)
     *               - arg[11]: -url (optional)
     *               - arg[12]: url (optional)
     */
    public static void main(final String[] args) {

        boolean okArgs = true;
        String messageType = null;
        String messageId = null;
        Integer iMessageVersion = null;
        Long lCode = null;
        String queue = null;
        String outputFile = null;
        String urlEndPoint = null;

        if (args.length < 1) {

            okArgs = false;

        } else {

            try {

                messageType = args[0];

                messageId = readParameter(args, "-msgId");
                String messageVersion = readParameter(args, "-msgVer");
                String code = readParameter(args, "-code");
                queue = readParameter(args, "-queue");

                if (code != null) {

                    if (queue != null || messageId != null || messageVersion != null) {

                        throw new ClientException("Incorrect parameters. [-code], [-queue] and [-msgId -msgVer] are exclusive");
                    }

                    try {
                        lCode = Long.valueOf(code);
                    } catch (NumberFormatException e) {
                        throw new ClientException("Incorrect parameters. [-code] must be a number");
                    }

                } else if (queue != null) {

                    if (code != null || messageId != null || messageVersion != null) {

                        throw new ClientException("Incorrect parameters. [-code], [-queue] and [-msgId -msgVer] are exclusive");
                    }

                    if (!"NEXT".equals(queue)) {

                        throw new ClientException("Incorrect parameters. [-queue] must be NEXT");
                    }

                } else {

                    if (queue != null || code != null) {

                        throw new ClientException("Incorrect parameters. [-code], [-queue] and [-msgId -msgVer] are exclusive");
                    }

                    if (messageId == null || messageVersion == null) {

                        throw new ClientException("Incorrect parameters. [-code], [-queue] or [-msgId -msgVer] are mandatory");
                    }

                    try {
                        iMessageVersion = Integer.valueOf(messageVersion);
                    } catch (NumberFormatException e) {
                        throw new ClientException("Incorrect parameters. [-msgVer] must be a number");
                    }
                }

                outputFile = readParameter(args, "-out");
                urlEndPoint = readParameter(args, "-url");

            } catch (ClientException e) {

                logger.log(Level.SEVERE, e.getMessage(), e);
                okArgs = false;
            }
        }

        if (okArgs) {

            try {

                urlEndPoint = setConfig(urlEndPoint);

                GetMessage get = new GetMessage();
                get.setSignRequest(System.getProperty(Configuration.SIGN_RESQUEST, "TRUE").toUpperCase().trim().equalsIgnoreCase("TRUE"));
                get.setVerifyResponse(System.getProperty(Configuration.VERIFY_SIGN_RESPONSE, "FALSE").toUpperCase().trim().equalsIgnoreCase("TRUE"));
                get.setEndPoint(urlEndPoint);

                long init = System.currentTimeMillis();

                String response = null;
                if (lCode != null) {

                    response = get.get(messageType, lCode);

                } else if (queue != null) {

                    response = get.get(messageType, queue);

                } else {

                    response = get.get(messageType, messageId, iMessageVersion);
                }

                if (outputFile == null) {

                    logger.info(response);

                } else {

                     FileUtil.writeUTF8(outputFile, response);
                }

                long end = System.currentTimeMillis();
                logger.info("Execution time: " + getPerformance(init, end));

            } catch (ClientException | IOException e) {

                logger.log(Level.SEVERE, e.getMessage(), e);
            }

        } else {

            StringBuffer buf = new StringBuffer();
            buf.append("Usage: get <message_type> <(-msgId msgId -msgVer msgVer) | (-code code) | (-queue NEXT)> [-out output_file] [-url url]");
            buf.append("\nExamples:");
            buf.append("\n  get message_type -msgId msgId_yyyymmdd -msgVer 3");
            buf.append("\n  get message_type -code code");
            buf.append("\n  get message_type -queue NEXT");
            buf.append("\n  get message_type -msgId msgId_yyyymmdd -msgVer 3 -out c:\\file.xml");
            buf.append("\n  get message_type -msgId msgId_yyyymmdd -msgVer 3 -url https:\\\\www.servicios\\servicioweb");

            logger.info(buf.toString());
        }
    }
}
