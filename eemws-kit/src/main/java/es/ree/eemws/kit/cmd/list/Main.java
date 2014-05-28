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
package es.ree.eemws.kit.cmd.list;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.ree.eemws.kit.cmd.ParentMain;
import es.ree.eemws.kit.config.Configuration;
import es.ree.eemws.client.exception.ClientException;
import es.ree.eemws.client.listmessages.ListMessages;
import es.ree.eemws.client.listmessages.MessageData;


/**
 * Main class to list messages.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public final class Main extends ParentMain {

    /** Name of the command. */
    private static final String COMMAND_NAME = "list";

    /** Log messages. */
    private static Logger logger = Logger.getLogger(COMMAND_NAME);

    /**
     * Main. Execute the list action.
     *
     * @param args The arguments must be:
     *               - arg[0]: -startTime (c1)
     *               - arg[1]: start time with format (dd-mm-yyyy) (c1)
     *               - arg[2]: -endTime (c1)
     *               - arg[3]: end time with format (dd-mm-yyyy) (c1)
     *               - arg[4]: -intervalType (c1)
     *               - arg[5]: "Application" or "Server" (c1)
     *               - arg[6]: -code (c2)
     *               - arg[7]: code (c2)
     *               - arg[8]: -msgId (optional)
     *               - arg[9]: message identification (optional)
     *               - arg[10]: -msgType (optional)
     *               - arg[11]: message type (optional)
     *               - arg[12]: -owner (optional)
     *               - arg[13]: owner (optional)
     *               - arg[14]: -url (optional)
     *               - arg[15]: url (optional)
     */
    public static void main(final String[] args) {

        boolean okArgs = true;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date dateStartTime = null;
        Date dateEndTime = null;
        String intervalType = null;
        Long lCode = null;
        String msgId = null;
        String msgType = null;
        String owner = null;
        String urlEndPoint = null;

        if (args.length < 1) {

            okArgs = false;

        } else {

            try {

                String startTime = readParameter(args, "-startTime");
                String endTime = readParameter(args, "-endTime");
                intervalType = readParameter(args, "-intervalType");

                String code = readParameter(args, "-code");


                if (code != null) {

                    if (startTime != null || endTime != null || intervalType != null) {

                        throw new ClientException("Incorrect parameters. [-code] and [-startTime -endTime -intervalType] are exclusive");
                    }

                    try {
                        lCode = Long.valueOf(code);
                    } catch (NumberFormatException e) {
                        throw new ClientException("Incorrect parameters. [-code] must be a number");
                    }


                } else {

                    if (startTime == null || endTime == null || intervalType == null) {

                        throw new ClientException("Incorrect parameters. [-code] or [-startTime -endTime -intervalType] are mandatory");
                    }

                    if (!"Application".equals(intervalType) && !"Server".equals(intervalType)) {

                        throw new ClientException("Incorrect parameters. [-intervalType] must be Application or Server");
                    }

                    try {
                        dateStartTime = sdf.parse(startTime);
                        dateEndTime = sdf.parse(endTime);
                    } catch (ParseException e) {
                        throw new ClientException("Incorrect parameters. [-startTime] and [-endTime] must be format dd-MM-yyyy");
                    }
                }

                msgId = readParameter(args, "-msgId");
                msgType = readParameter(args, "-msgType");
                owner = readParameter(args, "-owner");

                urlEndPoint = readParameter(args, "-url");

            } catch (ClientException e) {

                logger.log(Level.SEVERE, e.getMessage(), e);
                okArgs = false;
            }
        }

        if (okArgs) {

            try {

                urlEndPoint = setConfig(urlEndPoint);

                ListMessages list = new ListMessages();
                list.setSignRequest(System.getProperty(Configuration.SIGN_RESQUEST, "TRUE").toUpperCase().trim().equalsIgnoreCase("TRUE"));
                list.setVerifyResponse(System.getProperty(Configuration.VERIFY_SIGN_RESPONSE, "FALSE").toUpperCase().trim().equalsIgnoreCase("TRUE"));
                list.setEndPoint(urlEndPoint);

                long init = System.currentTimeMillis();

                List<MessageData> response = null;
                if (lCode != null) {

                    response = list.list(lCode, msgId, msgType, owner);

                } else {

                    response = list.list(dateStartTime, dateEndTime, intervalType, msgId, msgType, owner);
                }

                logger.info("Messages list: ");
                for (MessageData msgData : response) {

                    logger.info(msgData.toString());
                }

                long end = System.currentTimeMillis();
                logger.info("Execution time: " + getPerformance(init, end));

            } catch (ClientException e) {

                logger.log(Level.SEVERE, e.getMessage(), e);
            }

        } else {

            StringBuffer buf = new StringBuffer();
            buf.append("Usage: list  <(-code code) | (-startTime startTime -endTime endTime -intervalType intervalType)> [-msgId msgId] [-msgType msgType] [-owner owner] [-url url]");
            buf.append("\nExamples:");
            buf.append("\n  list -code 123456");
            buf.append("\n  list -startTime 01-01-2014 -endTime 02-01-2014 -intervalType Application");
            buf.append("\n  list -code 123456 -msgId idmensaj_yyyymmdd");
            buf.append("\n  list -code 123456 -owner eic_owner");
            buf.append("\n  list -code 123456 -url https:\\\\www.servicios\\servicioweb");

            logger.info(buf.toString());
        }
    }
}
