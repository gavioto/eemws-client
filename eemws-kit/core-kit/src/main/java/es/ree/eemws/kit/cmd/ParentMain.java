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

import java.util.List;

import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.kit.config.Configuration;


/**
 * Parent class of the line command actions.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public abstract class ParentMain {

    /** Configuration options. */
    private static Configuration config = null;
    
    /** Date format. */
	protected static final String DATE_FORMAT_PATTERN = "dd-MM-yyyy"; //$NON-NLS-1$


    /**
     * Returns the value of a command line parameter given the list of the parameters and the prefix.
     * If list of parameters contain the prefix, the method will return the value n+1 where n is the 
     * position of the prefix in the list. Then both key and value are removed form the list.
     * The class that invokes this method can check that there is no remain values in the list, if
     * so, that means that the users puts some "unknown" value, then execution must be finished. 	
     * @param args List of the command line arguments.
     * @param prefix Prefix of the argument.
     * @return Value of the argument, <code>null</code> if the parameter was not specified.
     */
    protected static String readParameter(final List<String> args, final String prefix) {
    	
        String retValue = null;
        
        int len = args.size() - 1;
        
        for (int cont = 0; retValue == null && cont < len; cont++) {
        	if (args.get(cont).equals(prefix)) {
        		retValue = args.get(cont + 1);
        		args.remove(cont); // Remove key.
        		args.remove(cont); // Then value.
        	}
        }

        return retValue;
    }

    /**
     * This method set the configuration client.
     * @param endPoint URL end point if <code>null</code> will overrides the one set up in the configuration.
     * @return URL end point.
     * @throws ConfigException Exception with the error.
     */
    protected static String setConfig(final String endPoint) throws ConfigException {

        config = new Configuration();
        config.readConfiguration();

        String endPnt = endPoint;
        if (endPnt == null) {

            endPnt = config.getUrlEndPoint().toString();
        }

        return endPnt;
    }

    /**
     * This method gets the configuration options.
     * @return Configuration options.
     */
    protected static Configuration getConfig() {

        return config;
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
            sb.append("'"); //$NON-NLS-1$
        }

        seg = seg % 60;
        String sSeconds = "00" + seg; //$NON-NLS-1$
        sSeconds = sSeconds.substring(sSeconds.length() - 2);
        sb.append(sSeconds);

        long milliseconds  = (int) dif % 1000;
        String sMilliseconds = "000" + milliseconds; //$NON-NLS-1$
        sMilliseconds = sMilliseconds.substring(sMilliseconds.length() - 3);
        sb.append("\"."); //$NON-NLS-1$
        sb.append(sMilliseconds);

        return sb.toString();
    }
}
