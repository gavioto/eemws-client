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

import java.awt.Font;

import javax.accessibility.AccessibleEditableText;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;


/**
 * Document management.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public final class DocumentHandle {

    /** Scrollable container for Document. */
    private JScrollPane scrollableDocumentContainer = new JScrollPane();

    /** Identifies the text area content as XML. */
    private static final String XML_TYPE = "text/html"; //$NON-NLS-1$

    /** Identifies the text area content as plain text. */
    private static final String PLAIN_TEXT_TYPE = "text/plain"; //$NON-NLS-1$

    /** Font used to display text. */
    private static final Font FONT = new Font("Monospaced", Font.PLAIN, 13); //$NON-NLS-1$

    /** Edition panel for document. */
    private JEditorPane document = new JEditorPane(PLAIN_TEXT_TYPE, ""); //$NON-NLS-1$

    /** Editable text on document. */
    private AccessibleEditableText editableText = document.getAccessibleContext().getAccessibleEditableText();

    /** Main window. */
    private Editor mainWindow = null;

    /**
     * Constructor. Creates a new instance of text area Manager.
     * @param window Reference to main window.
     */
    public DocumentHandle(final Editor window) {

        mainWindow = window;
        document.getDocument().addUndoableEditListener(window.getUndoRedoHandle());
        scrollableDocumentContainer.getViewport().add(document, null);
    }

    /**
     * Retrieves a reference to edit area.
     * @return Reference to edit area.
     */
    public JEditorPane getDocument() {
        return document;
    }

    /**
     * Returns the document area.
     * @return Scrollable pane containing edition area.
     */
    public JScrollPane getDocumentArea() {

        return scrollableDocumentContainer;
    }

    /**
     * Displays the String content using the most proper formatting.
     * @param content Content to display on edit area.
     * @param contentType Content type.
     * @param cannotUndo Indicate whether undo can be done after text loading.
     */
    private void open(final StringBuilder content, final String contentType, final boolean cannotUndo) {

        if (cannotUndo) {

            mainWindow.getUndoRedoHandle().reset();
            document.getDocument().removeUndoableEditListener(mainWindow.getUndoRedoHandle());
        }

        document.setFont(FONT);
        document.setContentType(contentType);
        document.setText(content.toString());
        document.setSelectionStart(0);
        document.setSelectionEnd(0);
        document.setCaretPosition(0);
        document.requestFocus();

        if (cannotUndo) {

            document.getDocument().addUndoableEditListener(mainWindow.getUndoRedoHandle());
        }
    }

    /**
     * Displays content applying an XML-convenient formatting.
     * @param content Content to be displayed.
     */
    public void openXml(final StringBuilder content) {

        open(content, XML_TYPE, false);
    }

    /**
     * Displays content using plain text formatting. Content opened
     * this way cannot be undone.
     * @param content Text to be displayed on text area.
     */
    public void openIrreversible(final StringBuilder content) {

        open(content, PLAIN_TEXT_TYPE, true);
    }

    /**
     * Displays content using plain text formatting. Content opened
     * this way can be undone.
     * @param content Text to be displayed/edited on text area.
     */
    public void openReversible(final StringBuilder content) {

        open(content, PLAIN_TEXT_TYPE, false);
    }
 
    /**
     * Retrieves the action bound to document.
     * @param accionName Name of the action to retrieve.
     * @return Action bound to the document with a given key.
     */
    public Action getAction(final String accionName) {

        return document.getActionMap().get(accionName);
    }

    /**
     * Returns the position of the cursor on text.
     * @return Position of cursor on text.
     */
    public int getCursorPosition() {

        return document.getCaretPosition();
    }

    /**
     * Sets cursor at text beginning.
     */
    public void setCursorToBeginning() {

        document.setCaretPosition(0);
    }

    /**
     * Marks (select) the text between the start and end positions.
     * @param start Start position to be selected.
     * @param end End position to be selected.
     */
    public void markText(final int start, final int end) {

        document.setCaretPosition(start);
        document.moveCaretPosition(end);
        document.requestFocus();
        mainWindow.update(mainWindow.getGraphics());
    }

    /**
     * Indicates whether the text area is empty.
     * @return <code>true</code>If text area is empty.
     * <code>false</code> otherwise.
     */
    public boolean isEmpty() {

        return editableText.getCharCount() == 0;
    }

    /**
     * Returns the visible text on document area.
     * If document is color-formatted, removal of spaces and reformat
     * will be required.
     * @return Visible text on document area.
     */
    public String getPlainText() {

        String retText = editableText.getTextRange(0, editableText.getCharCount());
        return retText;
    }

    /**
     * Replaces selected text by the one passed as argument.
     * @param string Replace string.
     */
    public void replace(final String string) {

        document.replaceSelection(string);
    }

    /**
     * Enebles / disables graphic values.
     * @param activationValue <code>true</code> Enable.
     * <code>false</code> Disable.
     */
    public void enable(final boolean activationValue) {

        document.setEnabled(activationValue);
    }
}
