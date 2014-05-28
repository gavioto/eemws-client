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
package es.ree.eemws.kit.cmd;

import java.text.MessageFormat;

import es.ree.eemws.client.common.ErrorText;
import es.ree.eemws.client.exception.ClientException;
import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.kit.config.Configuration;


/**
 * Parent class of the line command actions.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public abstract class ParentMain {

    /**
     * This method get the parameter of the prefix.
     * @param args List of arguments.
     * @param prefix Prefix of the argument.
     * @return Value of the argument.
     * @throws ClientException Exception with the error.
     */
    protected static String readParameter(final String[] args, final String prefix) throws ClientException {

        String retValue = null;
        int len = args.length;
        for (int cont = 0; (retValue == null) && (cont < len); cont++) {

            if (args[cont].equals(prefix)) {

                cont++;
                if (cont < len) {

                    retValue = args[cont];

                } else {

                    Object[] paramsText = {prefix};
                    String errorText = MessageFormat.format(ErrorText.ERROR_TEXT_005, paramsText);
                    throw new ClientException(errorText);
                }
            }
        }

        return retValue;
    }

    /**
     * This method set the configuration client.
     * @param endPoint URL end point.
     * @return URL end point.
     * @throws ClientException Exception with the error.
     */
    protected static String setConfig(final String endPoint) throws ClientException {

        Configuration config = new Configuration();

        try {

            config.readConfiguration();

        } catch (ConfigException ex) {

            Object[] paramsText = {ex.getMessage()};
            String errorText = MessageFormat.format(ErrorText.ERROR_TEXT_006, paramsText);
            throw new ClientException(errorText, ex);
        }

        String endPnt = endPoint;
        if (endPnt == null) {

            endPnt = config.getUrl().toString();
        }

        return endPnt;
    }

    /**
     * This method get the execution time.
     * @param init Initial time.
     * @param end End time.
     * @return String with the format [yy'zz".mmm].
     */
    protected static String getPerformance(final long init, final long end) {

        StringBuilder sb = new StringBuilder();

        long dif = end - init;
        int seg = (int) dif / 1000;

        int minutes = seg / 60;
        if (minutes > 0) {
            sb.append(minutes);
            sb.append("'");
        }

        seg = seg % 60;
        String sSeconds = "00" + seg;
        sSeconds = sSeconds.substring(sSeconds.length() - 2);
        sb.append(sSeconds);

        long milliseconds  = (int) dif % 1000;
        String sMilliseconds = "000" + milliseconds;
        sMilliseconds = sMilliseconds.substring(sMilliseconds.length() - 3);
        sb.append("\".");
        sb.append(sMilliseconds);

        return sb.toString();
    }
}
