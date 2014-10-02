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

    /** Number of characters required to determine whether the text content is XML or plain text. */
    private static final int MESSAGE_HEADER = 100;

    /** Identifies the text area content as XML. */
    private static final String XML_TYPE = "text/html";

    /** Identifies the text area content as plain text. */
    private static final String PLAIN_TEXT_TYPE = "text/plain";

    /** Font used to display text. */
    private static final Font FONT = new Font("Monospaced", Font.PLAIN, 13);

    /** Edition panel for document. */
    private JEditorPane document = new JEditorPane(PLAIN_TEXT_TYPE, "");

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
     * Retrieve reference to edit area.
     * @return Reference to edit area.
     */
    public JEditorPane getDocument() {
        return document;
    }

    /**
     * Creates the pane for Edition area.
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
     * Display content applying an XML-convenient formatting.
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
     * Check whether the content in plain text or XML.
     * @param buf String to check.
     * @return <code>true</code> if is XML. <code>false</code> otherwise.
     */
    private boolean isXML(final StringBuilder buf) {

        String header;
        if (buf.length() < MESSAGE_HEADER) {

            header = buf.toString();

        } else {

            header = buf.substring(0, MESSAGE_HEADER);
        }

        int semicolonPosition = header.split(";").length;
        int openerTagPosition = header.split("<").length;

        return semicolonPosition < openerTagPosition;
    }

    /**
     * Indicate whether the content is XML or CSV.
     * @return <code>true</code> if is a XML document
     * <code>false</code> otherwise.
     */
    public boolean isXml() {

        String header;
        if (document.getText().length() < MESSAGE_HEADER) {

            header = document.getText();

        } else {

            header = document.getText().substring(0, MESSAGE_HEADER);
        }

        return isXML(new StringBuilder(header));
    }

    /**
     * Retrieve the action bound to document.
     * @param accionName Name of the action to retrieve.
     * @return Action bound to the document with a given key.
     */
    public Action getAction(final String accionName) {

        return document.getActionMap().get(accionName);
    }

    /**
     * Return the position of cursor on text.
     * @return  Position of cursor on text.
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
     * Selects between the positions set on an interval.
     * @param begin Interval begin.
     * @param end Interval end.
     */
    public void markText(final int begin, final int end) {

        document.setCaretPosition(begin);
        document.moveCaretPosition(end);
        document.requestFocus();
        mainWindow.update(mainWindow.getGraphics());
    }

    /**
     * Indicate whether the text area is empty.
     * @return <code>true</code>If text area is empty.
     * <code>false</code> otherwise.
     */
    public boolean isEmpty() {

        return editableText.getCharCount() == 0;
    }

    /**
     * Return visible text on document area.
     * If document is color-formatted, removal of spaces and reformat
     * will be required.
     * @return Visible text on document area.
     */
    public String getPlainText() {

        String retText = editableText.getTextRange(0, editableText.getCharCount());
        return retText;
    }

    /**
     * Replace selected text by the one passed as argument.
     * @param string Replace string.
     */
    public void replace(final String string) {

        document.replaceSelection(string);
    }

    /**
     * Eneble / disable graphic values.
     * @param activationValue <code>true</code> Enable.
     * <code>false</code> Disable.
     */
    public void enable(final boolean activationValue) {

        document.setEnabled(activationValue);
    }
}
