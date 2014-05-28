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
package es.ree.eemws.kit.cmd.put;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.ree.eemws.kit.cmd.ParentMain;
import es.ree.eemws.kit.config.Configuration;
import es.ree.eemws.client.exception.ClientException;
import es.ree.eemws.client.putmessage.PutMessage;
import es.ree.eemws.core.utils.file.FileUtil;


/**
 * Main class to put messages.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public final class Main extends ParentMain {

    /** Name of the command. */
    private static final String COMMAND_NAME = "put";

    /** Log messages. */
    private static Logger logger = Logger.getLogger(COMMAND_NAME);

    /**
     * Main. Execute the put action.
     *
     * @param args The arguments must be:
     *               - arg[0]: value  -attach or -in (mandatory)
     *               - arg[1]: input file (mandatory)
     *               - arg[2]: -out (optional)
     *               - arg[3]: output file (optional)
     *               - arg[4]: -url (optional)
     *               - arg[5]: url (optional)
     */
    public static void main(final String[] args) {

        boolean okArgs = true;
        String inputType = null;
        String inputFile = null;
        String outputFile = null;
        String urlEndPoint = null;

        if (args.length < 2) {

            okArgs = false;

        } else {

            try {

                inputType = args[0];
                inputFile = args[1];

                if (!"-attach".equals(inputType) && !"-in".equals(inputType)) {

                    throw new ClientException("Incorrect parameters. First argument must be -attach or -in");
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

                String content = FileUtil.readUTF8(inputFile);

                PutMessage put = new PutMessage();
                put.setSignRequest(System.getProperty(Configuration.SIGN_RESQUEST, "TRUE").toUpperCase().trim().equalsIgnoreCase("TRUE"));
                put.setVerifyResponse(System.getProperty(Configuration.VERIFY_SIGN_RESPONSE, "FALSE").toUpperCase().trim().equalsIgnoreCase("TRUE"));
                put.setEndPoint(urlEndPoint);

                long init = System.currentTimeMillis();

                String response = null;
                if ("-attach".equals(inputType)) {

                    String fileName = inputFile.substring(inputFile.lastIndexOf(File.separator) + 1);
                    response = put.put("binary", fileName, content.getBytes());

                } else {

                    response = put.put(content);
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
            buf.append("Usage: put <-attach|-in> <xml_input_file> [-out xml_output_file] [-url url]");
            buf.append("\nExamples:");
            buf.append("\n  put -in c:\\input.xml");
            buf.append("\n  put -in c:\\input.xml -out c:\\output.xml");
            buf.append("\n  put -in c:\\input.xml -url https:\\\\www.servicios\\servicioweb");
            buf.append("\n  put -attach c:\\input.xml");

            logger.info(buf.toString());
        }
    }
}
