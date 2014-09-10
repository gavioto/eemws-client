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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import es.ree.eemws.kit.common.Messages;


/**
 * Management of undo/redo actions.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 09/05/2014
 */
public final class UndoRedoHandle implements UndoableEditListener {

    /**
     * Reference to main window.
     * Used to show warning messages only.
     */
    private Editor mainWindow;

    /** Edit changes parameter. */
    private UndoManager undo = null;

    /** "Undo" action. */
    private UndoAction undoAction = null;

    /** "Redo" action. */
    private RedoAction redoAction = null;

    /**
     * Creates new instance of Undo/redo manager.
     * @param window Reference to main window.
     */
    public UndoRedoHandle(final Editor window) {

        mainWindow = window;
        undo = new UndoManager();
        undoAction = new UndoAction();
        redoAction = new RedoAction();
    }

    /**
     * Retrieves "Undo" action.
     * @return "Undo" action
     */
    public UndoAction getUndoAction() {

        return undoAction;
    }

    /**
     * Retrieves "Redo" action.
     * @return "Redo" action
     */
    public RedoAction getRedoAction() {

        return redoAction;
    }

    /**
     * Invoked when Edit window changes.
     * @param e Event which triggered action.
     */
    public void undoableEditHappened(final UndoableEditEvent e) {

        undo.addEdit(e.getEdit());
        undoAction.update();
        redoAction.update();
    }

    /**
     * Resets history status.
     */
    public void reset() {

        undo.discardAllEdits();
        undoAction.update();
        redoAction.update();
    }

    /**
     * Implements an "Undo" action to Undo changes on editor.
     */
    class UndoAction extends AbstractAction {

        /** Class ID. */
        private static final long serialVersionUID = 0x16221b67e6a25842L;

        /**
         * Invoked when "undo" Action is activated, Undoes the last available
         * edition on History.
         * @param e Event which triggered action.
         */
        public void actionPerformed(final ActionEvent e) {

            try {

                undo.undo();

            } catch (CannotUndoException ex) {

                JOptionPane.showMessageDialog(mainWindow, Messages.getString("kit.gui.editor.87"), Messages.getString("kit.gui.editor.88"), JOptionPane.INFORMATION_MESSAGE);
            }

            update();
            redoAction.update();
        }

        /**
         * Enable / disable action depending on
         * whether undo is possible.
         */
        protected void update() {

            setEnabled(undo.canUndo());
            String title = mainWindow.getTitle();
            if (undo.canUndo()) {

                if (title.indexOf("*") == -1) {

                    mainWindow.setTitle(title + "*");
                }

            } else {

                int pos = title.indexOf("*");
                if (pos != -1) {

                    mainWindow.setTitle(title.substring(0, pos));
                }
            }
        }

        /**
         * Undoes action.
         */
        public UndoAction() {

            super(Messages.getString("kit.gui.editor.7"));
            update();
        }
    }

    /**
     * Implement the "Redo" action on editor to switch back changes.
     *
     */
    class RedoAction extends AbstractAction {

        /** Class ID. */
        private static final long serialVersionUID = 0x6a8a34eae470edf8L;

        /**
         * Invoked when "redo" Action is activated, Redoes the last undo action
         * found on History.
         * @param e Event which triggered action.
         */
        public void actionPerformed(final ActionEvent e) {

            try {

                undo.redo();

            } catch (CannotRedoException ex) {

                JOptionPane.showMessageDialog(mainWindow, Messages.getString("kit.gui.editor.89"), Messages.getString("kit.gui.editor.88"), JOptionPane.INFORMATION_MESSAGE);
            }

            update();
            undoAction.update();
        }

        /**
         * Enable / disable action depending on whether
         * redo is possible.
         */
        protected void update() {

            setEnabled(undo.canRedo());
        }

        /**
         * Redoes un undone action.
         */
        public RedoAction() {

            super(Messages.getString("kit.gui.editor.8"));
            update();
        }
    }
}
