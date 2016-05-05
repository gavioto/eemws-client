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

package es.ree.eemws.kit.gui.applications.sender;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.kit.common.Messages;
import es.ree.eemws.kit.config.Configuration;
import es.ree.eemws.kit.gui.common.Constants;
import es.ree.eemws.kit.gui.common.LogHandle;
import es.ree.eemws.kit.gui.common.Logger;

/**
 * Sends a file using a simple graphical application.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 01/03/2016
 */
public final class Sender extends JFrame {

    /** Serial verion UID. */
    private static final long serialVersionUID = 3441762408609317161L;

    /** Default window width. */
    private static final int DEFAULT_WINDOW_WIDTH = 520;

    /** Default window height. */
    private static final int DEFAULT_WINDOW_HEIGHT = 500;

    /** Key to store window height as preference. */
    private static final String WINDOW_HEIGHT_KEY = "SENDER_WINDOW_HEIGHT_KEY"; //$NON-NLS-1$

    /** Key to store window width as preference. */
    private static final String WINDOW_WIDTH_KEY = "SENDER_WINDOW_WIDTH_KEY"; //$NON-NLS-1$

    /** Key to store horizontal position of window preference. */
    private static final String WINDOW_LEFT_KEY = "SENDER_WINDOW_LEFT_KEY"; //$NON-NLS-1$

    /** Key to store vertical position of window preference. */
    private static final String WINDOW_TOP_KEY = "SENDER_WINDOW_TOP_KEY"; //$NON-NLS-1$

    /** Background color for sending status. */
    private static final Color SENDING_BG_COLOR = new Color(245, 255, 171);

    /** Background color for failed status. */
    private static final Color SENT_FAILED_COLOR = new Color(255, 128, 128);

    /** Background color for accepted (OK) status. */
    private static final Color SENT_OK_COLOR = new Color(192, 255, 192);

    /** Status text. */
    private JLabel statusLbl;
    
    /** Settings for this class. */
    private Preferences preferences;

    /** Log handler. */
    private LogHandle logHandle = new LogHandle();
    
    /** Logger. */
    private Logger logger = logHandle.getLog();

    
    /**
     * Main program, initializes  application and display on screen.
     * @param args Arguments -ignored-
     */
    public static void main(final String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            Sender b = new Sender();
            b.setVisible(true);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            JOptionPane.showMessageDialog(null, Messages.getString("SETTINGS_NO_GUI"), //$NON-NLS-1$
                    Messages.getString("SETTINGS_NO_GUI"), //$NON-NLS-1$
                    JOptionPane.ERROR_MESSAGE);
        }

    }

  
    /**
     * Constructor. Reads settings and invoke the method which arranges graphic elements.
     * <b>Important:</b> If object cannot be created application will exit.
     */
    public Sender() {
        super(Messages.getString("SENDER_TITLE")); //$NON-NLS-1$

        try {
            preferences = Preferences.userNodeForPackage(getClass());
            Configuration cf = new Configuration();
            cf.readConfiguration();
            if (!cf.hasMinimumConfiguration()) {
                throw new ConfigException(Messages.getString("SETTINGS_NO_CONFIGURATION")); //$NON-NLS-1$
            }

            jbInit();

        } catch (ConfigException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), Messages.getString("SETTINGS_NO_CONFIGURATION"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
            System.exit(1); //NOSONAR We want to force application to exit.
        }
    }

    /**
     * Changes status label to "sending" status.
     * @param fileName Name of the file to be sent.
     */
    public void setSending(final String fileName) {
        statusLbl.setBackground(SENDING_BG_COLOR);
        String msg = Messages.getString("SENDER_SENDING", fileName); //$NON-NLS-1$
        statusLbl.setText(msg);
        logger.logMessage(msg);
    }
    
    /**
     * Changes status label to "failed" status.
     * @param fileName Name of the file sent.
     */
    public void setSentFailed(final String fileName) {
        statusLbl.setBackground(SENT_FAILED_COLOR);
        String msg = Messages.getString("SENDER_FILE_FAILED", fileName); //$NON-NLS-1$
        statusLbl.setText(msg);
        logger.logMessage(msg);
    }
    
    /**
     * Changes status label to "ok" status.
     * @param fileName Name of the file sent.
     */
    public void setSentOk(final String fileName) {
        statusLbl.setBackground(SENT_OK_COLOR);
        String msg = Messages.getString("SENDER_FILE_OK", fileName); //$NON-NLS-1$
        statusLbl.setText(msg);
        logger.logMessage(msg);
    }
    
    
    /**
     * Configures and creates application graphic elements.
     */
    private void jbInit() {

        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource(Constants.ICON_PATH)));

        
        JMenuBar barraMenu = new JMenuBar();
                
        FileHandler fileHandle = new FileHandler(this, logHandle);
        
        barraMenu.add(fileHandle.getMenu());
        barraMenu.add(logHandle.getMenu());

        getContentPane().add(barraMenu, BorderLayout.NORTH);

        statusLbl = new JLabel(Messages.getString("SENDER_DRAG_FILE_HERE")); //$NON-NLS-1$
        statusLbl.setHorizontalAlignment(SwingConstants.CENTER);
        statusLbl.setOpaque(true);
        getContentPane().add(statusLbl, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(final ComponentEvent e) { // NOSONAR event is not used.
                modifySize();
            }

            @Override
            public void componentResized(final ComponentEvent e) { // NOSONAR event is not used.
                modifySize();
            }
        });

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) { // NOSONAR event is not used.
                System.exit(0);
            }
        });

        setResizable(true);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle maxSize = ge.getMaximumWindowBounds();
        setMaximizedBounds(maxSize);

        int width = preferences.getInt(WINDOW_WIDTH_KEY, DEFAULT_WINDOW_WIDTH);
        int height = preferences.getInt(WINDOW_HEIGHT_KEY, DEFAULT_WINDOW_HEIGHT);
        setSize(width, height);

        int top = preferences.getInt(WINDOW_TOP_KEY, (maxSize.width - width) / 2);
        int left = preferences.getInt(WINDOW_LEFT_KEY, (maxSize.width - width) / 2);
        setLocation(top, left);
    }

    /**
     * Stores size and location when modified.
     */
    private void modifySize() {
        int width = getWidth();
        int height = getHeight();

        preferences.putInt(WINDOW_WIDTH_KEY, width);
        preferences.putInt(WINDOW_HEIGHT_KEY, height);

        Point p = getLocation();
        preferences.putInt(WINDOW_TOP_KEY, p.x);
        preferences.putInt(WINDOW_LEFT_KEY, p.y);
    }

}
