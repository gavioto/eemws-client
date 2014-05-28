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
package es.ree.eemws.kit.cmd.query;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.ree.eemws.kit.cmd.ParentMain;
import es.ree.eemws.kit.config.Configuration;
import es.ree.eemws.client.exception.ClientException;
import es.ree.eemws.client.querydata.QueryData;
import es.ree.eemws.core.utils.file.FileUtil;


/**
 * Main class to get messages.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public final class Main extends ParentMain {

    /** Name of the command. */
    private static final String COMMAND_NAME = "query";

    /** Log messages. */
    private static Logger logger = Logger.getLogger(COMMAND_NAME);

    /**
     * Main. Execute the query action.
     *
     * @param args The arguments must be:
     *               - arg[0]: dataType (mandatory)
     *               - arg[1]: -startTime (optional)
     *               - arg[2]: start time with format (dd-mm-yyyy) (optional)
     *               - arg[3]: -endTime (optional)
     *               - arg[4]: end time with format (dd-mm-yyyy) (optional)
     *               - arg[5]: -areaCode (optional)
     *               - arg[6]: EIC area code (optional)
     *               - arg[7]: -out (optional)
     *               - arg[8]: output file (optional)
     *               - arg[9]: -url (optional)
     *               - arg[10]: url (optional)
     */
    public static void main(final String[] args) {

        boolean okArgs = true;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String dataType = null;
        Date dateStartTime = null;
        Date dateEndTime = null;
        String areaCode = null;
        String outputFile = null;
        String urlEndPoint = null;

        if (args.length < 1) {

            okArgs = false;

        } else {

            try {

                dataType = args[0];

                String startTime = readParameter(args, "-startTime");
                String endTime = readParameter(args, "-endTime");
                areaCode = readParameter(args, "-areaCode");

                if (startTime != null) {
                    try {
                        dateStartTime = sdf.parse(startTime);
                    } catch (ParseException e) {
                        throw new ClientException("Incorrect parameters. [-startTime] must be format dd-MM-yyyy");
                    }
                }

                if (endTime != null) {
                    try {
                        dateEndTime = sdf.parse(endTime);
                    } catch (ParseException e) {
                        throw new ClientException("Incorrect parameters. [-endTime] must be format dd-MM-yyyy");
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

                QueryData query = new QueryData();
                query.setSignRequest(System.getProperty(Configuration.SIGN_RESQUEST, "TRUE").toUpperCase().trim().equalsIgnoreCase("TRUE"));
                query.setVerifyResponse(System.getProperty(Configuration.VERIFY_SIGN_RESPONSE, "FALSE").toUpperCase().trim().equalsIgnoreCase("TRUE"));
                query.setEndPoint(urlEndPoint);

                long init = System.currentTimeMillis();

                String response = query.query(dataType, dateStartTime, dateEndTime, areaCode);

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
            buf.append("Usage: query <dataType> [-startTime startTime] [-endTime endTime] [-areaCode areaCode] [-out output_file] [-url url]");
            buf.append("\nExamples:");
            buf.append("\n  query dataType");
            buf.append("\n  query dataType -startTime 01-01-2014 -endTime 02-01-2014 -areaCode area_code");
            buf.append("\n  query dataType -startTime 01-01-2014");
            buf.append("\n  query dataType -out c:\\file.xml");
            buf.append("\n  query dataType -areaCode area_code -url https:\\\\www.servicios\\servicioweb");

            logger.info(buf.toString());
        }
    }
}
