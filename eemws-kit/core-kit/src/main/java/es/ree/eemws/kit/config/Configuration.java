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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.core.utils.config.ConfigManager;
import es.ree.eemws.core.utils.file.FileUtil;
import es.ree.eemws.kit.common.Messages;


/**
 * Class for settings management.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 09/05/2014
 */
public class Configuration {

    /** Mapping key to {@link #url}. */
    public static final String WEBSERVICE_URL_KEY = "WEBSERVICES.URL";

    /** Mapping key to {@link #signResquest}. */
    public static final String SIGN_RESQUEST = "SIGN_RESQUEST";

    /** Mapping key to {@link #verifySignResponse}. */
    public static final String VERIFY_SIGN_RESPONSE = "VERIFY_SIGN_RESPONSE";

    /** Mapping key to {@link #keyStoreFile}. */
    private static final String KEY_STORE_FILE_KEY = "javax.net.ssl.keyStore";

    /** Mapping key to {@link #keyStorePassword}. */
    private static final String KEY_STORE_PASSWORD_KEY = "javax.net.ssl.keyStorePassword";

    /** Mapping key to {@link #proxyHost}. */
    private static final String PROXY_HOST_KEY = "https.proxyHost";

    /** Mapping key to {@link #proxyPort}. */
    private static final String PROXY_PORT_KEY = "https.proxyPort";

    /** Mapping key to {@link #proxyUser}. */
    private static final String PROXY_USER_KEY = "https.proxyUser";

    /** Mapping key to {@link #proxyPassword}. */
    private static final String PROXY_PASSWORD_KEY = "https.proxyPassword";

    /** URL for the end point. */
    private String url = null;

    /** Indicate whether Service call is signed. */
    private boolean signResquest = true;

    /** Indicate whether Service call is signed. */
    private boolean verifySignResponse = true;

    /** Path to Key store file. */
    private String keyStoreFile;

    /** Path to Access Key store. */
    private String keyStorePassword;

    /** Host name for proxy server. */
    private String proxyHost;

    /** Listening port for proxy server. */
    private String proxyPort;

    /** User name for proxy server.*/
    private String proxyUser;

    /** User password for proxy server.*/
    private String proxyPassword;

    /** Path to configuration file. */
    public static final String CONFIG_FILE = "config.properties";

    /**
     * Read system settings.
     * @throws ConfigException If cannot load settings.
     */
    public void readConfiguration() throws ConfigException {

        ConfigManager cm = new ConfigManager();
        cm.readConfigFile(CONFIG_FILE);

        url = cm.getValue(WEBSERVICE_URL_KEY);

        keyStoreFile = cm.getValue(KEY_STORE_FILE_KEY);
        keyStorePassword = cm.getValue(KEY_STORE_PASSWORD_KEY);
        
        proxyHost = cm.getValue(PROXY_HOST_KEY);
        proxyPort = cm.getValue(PROXY_PORT_KEY);
        proxyUser = cm.getValue(PROXY_USER_KEY);
        proxyPassword = cm.getValue(PROXY_PASSWORD_KEY);
        
        signResquest = cm.getValue(SIGN_RESQUEST, "TRUE").equalsIgnoreCase("TRUE");
        verifySignResponse = cm.getValue(VERIFY_SIGN_RESPONSE, "FALSE").equalsIgnoreCase("TRUE");

        if (!hasMinimumConfiguration()) {
            throw new ConfigException(Messages.getString("kit.gui.editor.35"));
        }

    }

    /**
     * Updates configuration file with the current values.
     * @throws ConfigException If cannot modify settings file (Error accessing file).
     */
    public void writeConfiguration() throws ConfigException {

        String configPath = CONFIG_FILE;
        ConfigManager cm = new ConfigManager();
        cm.readConfigFile(configPath);

        try {

            createBackup(configPath);

            String fileContent = FileUtil.read(FileUtil.getFullPathOfResoruce(configPath));
            fileContent = writeValue(url, cm.getValue(WEBSERVICE_URL_KEY), WEBSERVICE_URL_KEY, fileContent);

            fileContent = writeValue(proxyHost, cm.getValue(PROXY_HOST_KEY), PROXY_HOST_KEY, fileContent);
            fileContent = writeValue(proxyPort, cm.getValue(PROXY_PORT_KEY), PROXY_PORT_KEY, fileContent);
            fileContent = writeValue(proxyUser, cm.getValue(PROXY_USER_KEY), PROXY_USER_KEY, fileContent);
            fileContent = writeValue(proxyPassword, cm.getValue(PROXY_PASSWORD_KEY), PROXY_PASSWORD_KEY, fileContent);

            fileContent = writeValue(keyStoreFile, cm.getValue(KEY_STORE_FILE_KEY), KEY_STORE_FILE_KEY, fileContent);
            fileContent = writeValue(keyStorePassword, cm.getValue(KEY_STORE_PASSWORD_KEY), KEY_STORE_PASSWORD_KEY, fileContent);

            fileContent = writeValue(Boolean.toString(signResquest), cm.getValue(SIGN_RESQUEST), SIGN_RESQUEST, fileContent);
            fileContent = writeValue(Boolean.toString(verifySignResponse), cm.getValue(VERIFY_SIGN_RESPONSE), VERIFY_SIGN_RESPONSE, fileContent);

            FileUtil.write(FileUtil.getFullPathOfResoruce(configPath), fileContent);

        } catch (IOException ex) {

            throw new ConfigException(ex);
        }
    }

    /**
     * Create new backup from settings file.
     * @param configPath Path to settings file.
     * @throws IOException If cannot create backup file.
     * @throws ConfigException If cannot create backup file.
     */
    protected final void createBackup(final String configPath) throws IOException, ConfigException {

        String fullPath = FileUtil.getFullPathOfResoruce(configPath);
        String contenido = FileUtil.read(fullPath);

        FileUtil.createBackup(fullPath);
        FileUtil.write(fullPath, contenido);
    }

    /**
     * Update contents of settings file, modifying value mapped to key providing this new value
     * is different from existing value.
     * @param newValue New configuration value.
     * @param current Current configuration value.
     * @param key Mapping key for this value.
     * @param settings Full content of the settings file as a string.
     * @return Full content of settings file modified if appropriate.
     */
    protected final String writeValue(final String newValue,
            final String current,
            final String key,
            final String settings) {

        String content = settings;

        if ((newValue != null && current != null && !newValue.equals(current))
                || (newValue == null && current != null)
                || (newValue != null && current == null)) {

            int pos = content.indexOf(key);
            int pos4;
            int pos2;
            boolean hasChanged = false;

            while (pos != -1 && !hasChanged) {

                int pos3 = pos;
                pos4 = pos + 1;

                /* Position of the character previous to key. */
                while (content.charAt(pos3) != '\n') {

                    pos3--;
                }

                pos3++;

                /* Position of symbol '=' for current key. */
                pos = content.indexOf('=', pos);

                /* Last position for the line of current key.  */
                pos2 = content.indexOf('\n', pos);

                /* Case 1: Content is an uncommented key. */
                if (content.charAt(pos3) != '#') {

                    if (newValue != null) {

                        content = content.substring(0, pos + 1) + newValue + content.substring(pos2);

                    } else {

                        content = content.substring(0, pos3) + "#" + key + content.substring(pos2);
                    }

                    hasChanged = true;

                } else {

                    /* Case 2: Content is a commented key. */
                    if (content.charAt(pos3) == '#' && content.charAt(pos3 + 1) != '#') {

                        if (newValue != null) {

                            content = content.substring(0, pos3) + key + "=" + newValue
                                    + content.substring(pos2);
                        }

                        hasChanged = true;
                    }
                }

                pos = content.indexOf(key, pos4);
            }

            /* If file does not contain the key is added at the end of the file*/
            if (!hasChanged) {

                content = content + "\n" + key + "=" + newValue + "\n";
            }
        }

        return content;
    }

    /**
     * Indicate whether a document must be signed.
     * @return status.
     */
    public final boolean isSignResquest() {

        return signResquest;
    }

    /**
     * Set whether document must be signed.
     * @param aSignResquest status
     */
    public final void setSignResquest(final boolean aSignResquest) {

        signResquest = aSignResquest;
    }

    /**
     * Indicate whether the response must be signed.
     * @return status.
     */
    public final boolean isVerifySignResponse() {

        return verifySignResponse;
    }

    /**
     * Set whether the response must be signed.
     * @param aVerifySignResponse status.
     */
    public final void setVerifySignResponse(final boolean aVerifySignResponse) {

        verifySignResponse = aVerifySignResponse;
    }



    /**
     * Return host name for the proxy server.
     * @return host name for the proxy server
     */
    public final String getProxyHost() {

        return proxyHost;
    }

    /**
     * Set host name for the proxy server.
     * @param host Host name
     */
    public final void setProxyHost(final String host) {

        proxyHost = host;
    }

    /**
     * Return listening port for proxy server.
     * If port is not set or incorrect (not a number) returns <b>-1</b>.
     * @return Listening port for proxy server. -1 if is not set
     * or in a non-numeric value.
     */
    public final int getProxyPort() {

        int port;
        try {

            port = Integer.parseInt(proxyPort);

        } catch (NumberFormatException ex) {

            port = -1;
        }

        return port;
    }

    /**
     * Set value for proxy listening port.
     * @param port Proxy port number.
     * @see #setProxyPort(int)
     */
    public final void setProxyPort(final String port) {

        proxyPort = port;
    }

    /**
     * Set value for proxy listening port.
     * @param port Proxy port number.
     * @see #setProxyPort(String)
     */
    public final void setProxyPort(final int port) {

        if (port != -1) {

            proxyPort = String.valueOf(port);

        } else {

            proxyPort = null;
        }
    }

    /**
     * Retorna user name for proxy server.
     * @return User name for proxy server.
     */
    public final String getProxyUser() {

        return proxyUser;
    }

    /**
     * Set user name for proxy server.
     * @param user user name for proxy server.
     */
    public final void setProxyUser(final String user) {

        proxyUser = user;
    }

    /**
     * Return password for proxy server.
     * @return Password for proxy server.
     */
    public final String getProxyPassword() {

        return proxyPassword;
    }

    /**
     * Set password for proxy server.
     * @param passwd Password for proxy server.
     */
    public final void setProxyPassword(final String passwd) {

        proxyPassword = passwd;
    }

    /**
     * Return password for key store. If password is not crypted correctly will
     * return an empty string.
     * @return Access key. An empty string if value is empty or not crypted correctly.
     */
    public final String getKeyStorePassword() {

        return keyStorePassword;
    }

    /**
     * Set password for key store.
     * @param passwd Password for key store.
     */
    public final void setKeyStorePassword(final String passwd) {

        keyStorePassword = passwd;
    }

    /**
     * Absolute path to keystore file.
     * <code>null</code> null if is not correctly set.
     * @return Absolute path to keystore file.
     */
    public final String getKeyStoreFile() {

        return keyStoreFile;
    }

    /**
     * Set Absolute path to keystore file.
     * @param keyFile Absolute path to keystore file.
     */
    public final void setKeyStoreFile(final String keyFile) {

        keyStoreFile = keyFile;
    }

    /**
     * Return access URL.
     * @return Access URL. <code>null</code>
     * If is not correctly entered or is not a valid URL.
     */
    public final URL getUrlEndPoint() {

        URL urlEndPoint;

        try {

            urlEndPoint = new URL(url);

        } catch (MalformedURLException e) {

            urlEndPoint = null;
        }

        return urlEndPoint;
    }

    /**
     * Sets URL to which System will connect.
     * @param aUrl to which System will connect.
     */
    public final void setUrl(final String aUrl) {

        url = aUrl;
    }

    /**
     * Ensure a minimum set of parameters is set before allow start.
     * @return true if a minimum set of parameters is set to run
     * GUI applications.
     */
    public final boolean hasMinimumConfiguration() {

        return (!isEmpty(url) && !isEmpty(keyStoreFile) && !isEmpty(keyStorePassword));
    }

    /**
     * Determine whether an attribute is null or an empty string.
     * @param st String object
     * @return true if String is null or empty
     */
    private boolean isEmpty(final String st) {

        return (st == null || "".equals(st.trim()));
    }
}
