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

package es.ree.eemws.kit.gui.applications.listing;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.MenuElement;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.kit.common.Messages;
import es.ree.eemws.kit.config.Configuration;
import es.ree.eemws.kit.gui.applications.Logger;
import es.ree.eemws.kit.gui.applications.editor.ServiceMenu;
import es.ree.eemws.kit.gui.common.Constants;
import es.ree.eemws.kit.gui.common.ServiceMenuListener;

/**
 * Implements graphic interface for message listing and retrieval.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 09/05/2014
 *
 */
public final class Lists extends JFrame implements ServiceMenuListener {

    /** Class ID. */
    private static final long serialVersionUID = 3814363426669389598L;

    /** Default window width. */
    private static final int DEFAULT_WINDOW_WIDTH = 520;

    /** Default window height. */
    private static final int DEFAULT_WINDOW_HEIGHT = 500;

    /** Key to store window height as preference. */
    private static final String WINDOW_HEIGHT_KEY = "WINDOW_HEIGHT_KEY";

    /** Key to store window width as preference. */
    private static final String WINDOW_WIDTH_KEY = "WINDOW_WIDTH_KEY";

    /** Key to store horizontal position of window preference. */
    private static final String WINDOW_LEFT_KEY = "WINDOW_LEFT_KEY";

    /** Key to store vertical position of window preference. */
    private static final String WINDOW_TOP_KEY = "WINDOW_TOP_KEY";

    /** Log window. */
    private Logger log;

    /** Log window manager. */
    private LogHandle logHandle = new LogHandle();

    /** File options manager. */
    private FileHandle fileHandle = null;

    /** Status bar. */
    private StatusBar status = new StatusBar();

    /** Filter area. */
    private Filter filter = null;

    /** Main menu. */
    private JMenuBar menuBar = new JMenuBar();

    /** Service end point. */
    private String endPoint;

    /** Listing class. */
    private ListSend listSend = null;

    /** Request class. */
    private RequestSend requestSend = null;

    /** Service menu. */
    private ServiceMenu serviceMenu = null;

    /** Column visibility Handler. */
    private ColumnVisibilityHandle columnVisibilityHandler = null;

    /** Data table. */
    private DataTable dataTable;

    /** Settings for this class. */
    private Preferences preferences;

    /**
     * Main program, initializes listing application and display on screen.
     * @param args Arguments -ignored-
     */
    public static void main(final String[] args) {

        Locale.setDefault(Locale.ENGLISH);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            Lists b = new Lists();
            b.setVisible(true);
        } catch (Exception ex) {

            ex.printStackTrace();

            /*
             * ClassNotFoundException, InstantiationException,
             * IllegalAccessException, UnsupportedLookAndFeelException
             *
             * Theoretically unreachable, implementing "Look&Feel" native class
             * should be always supported.
             *
             */
            JOptionPane.showMessageDialog(null,
                    "Cannot initialize graphic elements.\n",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

    }

    /**
     * Constructor. Read settings and invoke the method which arranges graphic
     * elements.
     * <b>Important:</b> If object cannot be created application will exit.
     */
    public Lists() {
        super("List and retrieve messages");

        try {
            preferences = Preferences.userNodeForPackage(getClass());
            Configuration cf = new Configuration();
            cf.readConfiguration();
            if (!cf.hasMinimumConfiguration()) {
                throw new ConfigException(Messages.getString("kit.gui.configuration.67"));
            }
            endPoint = cf.getUrlEndPoint().toString();
            requestSend = new RequestSend(cf.getUrlEndPoint(), this);
            listSend = new ListSend(cf.getUrlEndPoint(), this);
            jbInit();

        } catch (ConfigException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    /**
     * Configure and create application graphic elements.
     */
    private void jbInit() {

        fileHandle = new FileHandle(this);
        dataTable = new DataTable(this);
        columnVisibilityHandler = new ColumnVisibilityHandle(dataTable.getModel());
        filter = new Filter(this);

        log = logHandle.getLog();

        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource(Constants.ICON_PATH)));

        /* Menu construction. */
        JMenu mnService = getServiceMenu();

        JMenu mnView = new JMenu("View");
        mnView.setMnemonic('V');
        columnVisibilityHandler.getMenu(mnView);
        filter.getMenu(mnView);
        dataTable.getVisualizationMenu(mnView);

        menuBar.add(fileHandle.getMenu());
        menuBar.add(mnView);
        menuBar.add(dataTable.getSelectionMenu());
        menuBar.add(logHandle.getMenu());
        menuBar.add(mnService);

        JLabel lblLeftMargin = new JLabel("  ");
        JLabel lblRightMargin = new JLabel("  ");

        JPanel pnlCenterPanel = new JPanel();
        pnlCenterPanel.setLayout(null);
        pnlCenterPanel.add(filter.getFilterCanvas());
        pnlCenterPanel.add(dataTable.getPanelScroll());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(menuBar, BorderLayout.NORTH);
        getContentPane().add(lblRightMargin, BorderLayout.EAST);
        getContentPane().add(lblLeftMargin, BorderLayout.WEST);
        getContentPane().add(status.getPanel(), BorderLayout.SOUTH);
        getContentPane().add(pnlCenterPanel, BorderLayout.CENTER);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setResizable(true);

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(final WindowEvent e) {
                fileHandle.exitApplication();
            }
        });

        addComponentListener(new ComponentAdapter() {
            public void componentResized(final ComponentEvent e) {
                modifySize();
            }
        });

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
     * Modify size of filter panel and table to fit in the main window when it is resized.
     * This method is also invoked in case of modifying visibility status of panel to
     * rearrange the size of the data grid.
     */
    public void modifySize() {
        int width = getWidth();
        int height = getHeight();

        filter.setSize(width);
        dataTable.adjustTableSize(width, height, filter.isVisible());

        preferences.putInt(WINDOW_WIDTH_KEY, width);
        preferences.putInt(WINDOW_HEIGHT_KEY, height);

        Point p = getLocation();
        preferences.putInt(WINDOW_TOP_KEY, p.x);
        preferences.putInt(WINDOW_LEFT_KEY, p.y);
    }

    /**
     * Return the Service menu.
     * @return Menu entry containing settings for server and service.
     */
    private JMenu getServiceMenu() {
        JMenu mnServiceMenu = new JMenu();
        mnServiceMenu.setText("Service");
        mnServiceMenu.setMnemonic('S');
        serviceMenu = new ServiceMenu(this);
        serviceMenu.addServiceElements(mnServiceMenu);

        return mnServiceMenu;
    }

    /**
     * Set the application end point. If point set is settlement and 'incremental mode' is
     * selected. The change will be rejected.
     * @param endp point to which messages are sent (environment + service).
     */
    @Override
    public void setEndPoint(final String endp) {

        try {
            listSend.setEndPoint(new URL(endp));
            requestSend.setEndPoint(new URL(endp));
            endPoint = endp;
            log.logMessage("Target service set to: " + endPoint);

        } catch (MalformedURLException mue) {
            JOptionPane.showMessageDialog(null, "Entered URL is not correct.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Enable / disable elements on screen.
     * @param enableValue <code>true</code> enable,
     * <code>false</code> disable.
     */
    public void enableScreen(final boolean enableValue) {
        filter.enable(enableValue);
        enableMenu(enableValue);
        dataTable.setEnabled(enableValue);
        if (enableValue) {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        } else {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        update(getGraphics());
    }

    /**
     * Enable / Disable main menu elements.
     * @param enableValue <code>true</code> enable,
     * <code>false</code> disable.
     */
    private void enableMenu(final boolean enableValue) {
        MenuElement[] me = menuBar.getSubElements();
        int len = me.length;

        for (int cont = 0; cont < len; cont++) {
            me[cont].getComponent().setEnabled(enableValue);
        }
    }

    /**
     * Get data table.
     * @return Data table.
     */
    public DataTable getDataTable() {
        return dataTable;
    }

    /**
     * Get File handler.
     * @return File handler.
     */
    public FileHandle getFileHandle() {
        return fileHandle;
    }

    /**
     * Get Filter elements.
     * @return Filter elements.
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * Get status bar.
     * @return Status bar.
     */
    public StatusBar getStatusBar() {
        return status;
    }

    /**
     * Get Log window.
     * @return Log window.
     */
    public LogHandle getLogHandle() {
        return logHandle;
    }

    /**
     * Get message request object.
     * @return Message request object.
     */
    public RequestSend getRequestSend() {
        return requestSend;
    }

    /**
     * Return List sending object.
     * @return list sending object.
     */
    public ListSend getListSend() {
        return listSend;
    }
}
