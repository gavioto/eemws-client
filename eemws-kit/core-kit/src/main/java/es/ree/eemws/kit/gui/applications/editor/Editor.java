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
package es.ree.eemws.kit.gui.applications.editor;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.kit.common.Messages;


/**
 * Implements a simple editor for edit and send XML Messages.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 09/05/2014
 */
public final class Editor extends JFrame {

    /** Class ID. */
    private static final long serialVersionUID = 7898627141226851450L;

    /** Editor size when "Restore" button is pressed. */
    private static final Dimension DEFAULT_SIZE_ON_RESTORE = new Dimension(617, 417);

    /** File actions manager. */
    private FileHandle fileHandle = null;

    /** Editing actions handler. */
    private EditHandle editHandle = null;

    /** Handler of currently editing document. */
    private DocumentHandle documentHandle = null;

    /** Document send handler. */
    private SendHandle sendHandle = null;

    /** XML formating. */
    private XmlHandle xmlHandle = null;

    /** Undo / Redo handler. */
    private UndoRedoHandle undoRedoHandle = null;

    /** Log window handler. */
    private LogHandle logHandle = null;

    /**
     * Main program.
     * @param args Command line arguments (ignored).
     */
    public static void main(final String[] args) {

        Exception excep = null;

        try {

            /* Keep text of native components (as default dialogue buttons) text in English */
            Locale.setDefault(Locale.ENGLISH);

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            Editor a = new Editor();
            a.setVisible(true);

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {

            /*
             * Theoretically unreachable, Native "L&F" implementing class must
             * be always instantiable.
             */
            excep = ex;

        } catch (ConfigException ex) {

            JOptionPane.showMessageDialog(null, ex.getMessage(), Messages.getString("kit.gui.editor.36"), JOptionPane.ERROR_MESSAGE);

        }

        if (excep != null) {

            JOptionPane.showMessageDialog(null, Messages.getString("kit.gui.editor.37") + excep.getMessage(), Messages.getString("kit.gui.configuration.12"), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Creates new instance of graphic module. Read settings files (send only).
     * Creates and orders all graphic elements.
     * @throws ConfigException If module settings are incorrect.
     */
    public Editor() throws ConfigException {

        super();
        jbInit();
    }

    /**
     * Creates and arranges all graphic interface elements.
     * @throws ConfigException If module settings are incorrect.
     */
    private void jbInit() throws ConfigException   {

        /* Construction of handlers, constructors order should be kept. */
        logHandle = new LogHandle();
        undoRedoHandle = new UndoRedoHandle(this);
        documentHandle = new DocumentHandle(this);
        fileHandle = new FileHandle(this);
        editHandle = new EditHandle(this);
        sendHandle = new SendHandle(this);
        xmlHandle = new XmlHandle(this);

        /* Button bar constructors */
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(fileHandle.getButtonBar());
        buttonPanel.add(editHandle.getButtonBar());
        buttonPanel.add(sendHandle.getButtonBar());
        buttonPanel.add(xmlHandle.getButtonBar());

        /* Menu construction. */
        JMenuBar barraMenu = new JMenuBar();
        barraMenu.add(fileHandle.getMenu());
        barraMenu.add(editHandle.getMenu());
        barraMenu.add(logHandle.getMenu());
        barraMenu.add(sendHandle.getMenu());
        barraMenu.add(xmlHandle.getMenu());

        getContentPane().add(barraMenu, BorderLayout.NORTH);

        /* Graphic elements arrangement. */
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BorderLayout());
        getContentPane().add(panelPrincipal, BorderLayout.CENTER);
        panelPrincipal.add(documentHandle.getDocumentArea(), BorderLayout.CENTER);
        panelPrincipal.add(buttonPanel, BorderLayout.NORTH);

        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource(es.ree.eemws.kit.gui.common.Constants.ICON_PATH)));

        /* When "Close" button is pressed triggers close action in file handler. */
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(final WindowEvent e) {
                fileHandle.exitProgram();
            }
        });

        /* Sets an average size for the window size and centers it in screen when "Restore" button is pressed for the first time. */
        setSize(DEFAULT_SIZE_ON_RESTORE);
        Dimension screen = new Dimension(java.awt.Toolkit.getDefaultToolkit().getScreenSize());
        setLocation(screen.width / 2 - getSize().width / 2, screen.height / 2 - getSize().height / 2);

        /* Starts editor maximized. */
        setExtendedState(MAXIMIZED_BOTH);
    }

    /**
     * Returns file actions manager.
     * @return File actions manager.
     */
    public FileHandle getFicheroHandle() {

        return fileHandle;
    }

    /**
     * Returns Handler of currently editing document.
     * @return Handler of currently editing document.
     */
    public DocumentHandle getDocumentHandle() {

        return documentHandle;
    }

    /**
     * Returns Log window handler.
     * @return log window handler.
     */
    public LogHandle getLogHandle() {

        return logHandle;
    }

    /**
     * Returns reference to Undo/Redo handler.
     * @return reference to Undo/Redo handler.
     */
    public UndoRedoHandle getUndoRedoHandle() {

        return undoRedoHandle;
    }

    /**
     * Enables/Disables screen elements.
     * @param activeValue <code>true</code> to enable.
     * <code>false</code> to disable.
     */
    public void enableScreen(final boolean activeValue) {

        sendHandle.enable(activeValue);
        editHandle.enable(activeValue);
        documentHandle.enable(activeValue);
        fileHandle.enable(activeValue);
        logHandle.enable(activeValue);

        if (activeValue) {

            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        } else {

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }

        update(getGraphics());
    }
}
