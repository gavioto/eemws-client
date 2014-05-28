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
package es.ree.eemws.kit.config;

import java.net.MalformedURLException;
import java.net.URL;

import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.core.utils.config.ConfigManager;


/**
 * Class to manage the client configuration.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public final class Configuration {

    /** Configuration file. */
    public static final String CONFIG_FILE = "config/config.properties";

    /** Key of the URL of the web service. */
    public static final String WEBSERVICE_URL_KEY = "WEBSERVICES.URL";

    /** Key of the check to sign the request. */
    public static final String SIGN_RESQUEST = "SIGN_RESQUEST";

    /** Key of the check to verify the sign response. */
    public static final String VERIFY_SIGN_RESPONSE = "VERIFY_SIGN_RESPONSE";

    /** URL of the web service. */
    private String url = null;

    /** Check to sign the request. */
    private boolean signResquest = true;

    /** Check to verify the sign response. */
    private boolean verifySignResponse = true;

    /**
     * This method read the configuration.
     * @throws ConfigException Exception with the error.
     */
    public void readConfiguration() throws ConfigException {

        ConfigManager cm = new ConfigManager();
        cm.readConfigFile(CONFIG_FILE);

        url = cm.getValue(WEBSERVICE_URL_KEY);
        signResquest = cm.getValue(SIGN_RESQUEST, "TRUE").equalsIgnoreCase("TRUE");
        verifySignResponse = cm.getValue(VERIFY_SIGN_RESPONSE, "FALSE").equalsIgnoreCase("TRUE");
    }

    /**
     * This method get the URL end point.
     * @return URL end point.
     */
    public URL getUrl() {

        URL urlEndPoint;

        try {

            urlEndPoint = new URL(url);

        } catch (MalformedURLException e) {

            urlEndPoint = null;
        }

        return urlEndPoint;
    }

    /**
     * This method set the URL end point.
     * @param aUrl URL end point.
     */
    public void setUrl(final String aUrl) {

        url = aUrl;
    }

    /**
     * This method get the check to sign the request.
     * @return Check to sign the request.
     */
    public boolean isSignResquest() {

        return signResquest;
    }

    /**
     * This method set the check to sign the request.
     * @param aSignResquest Check to sign the request.
     */
    public void setSignResquest(final boolean aSignResquest) {

        signResquest = aSignResquest;
    }

    /**
     * This method get the check to verify the sign response.
     * @return Check to verify the sign response.
     */
    public boolean isVerifySignResponse() {

        return verifySignResponse;
    }

    /**
     * This method set the check to verify the sign response.
     * @param aVerifySignResponse Check to verify the sign response.
     */
    public void setVerifySignResponse(final boolean aVerifySignResponse) {

        verifySignResponse = aVerifySignResponse;
    }
}
