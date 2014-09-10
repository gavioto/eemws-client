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

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import es.ree.eemws.core.utils.xml.XMLUtil;
import es.ree.eemws.kit.common.Messages;
import es.ree.eemws.kit.gui.applications.Logger;
import es.ree.eemws.kit.gui.common.Constants;

/**
 * XML display format related tasks.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 07/08/2014
 */
public final class XmlHandle {

    /** Text font to be used when XML format is selected. */
    private static final String GENERAL_FORMAT = "<font size=\"13pt\" color=\"#0000ff\" face=\"Arial,Verdana,SansSerif\">";

    /** Font color font to be applied on XML tags when XML format is selected. */
    private static final String LABEL_FORMAT = "<font color=\"#800000\">";

    /** Font color font to be applied on strings when XML format is selected. */
    private static final String STRING_FORMAT = "<font color=\"#000000\">";

    /** Font color font to be applied on attributes when XML format is selected. */
    private static final String ATTRIBUTE_FORMAT = "<font color=\"#FF0000\">";

    /** HTML space element. */
    private static final String HTML_SPACES = "&nbsp;";

    /** Closing tag for font tag element. */
    private static final String CLOSE_FORMAT = "</font>";

    /** Escaped double quotes. */
    private static final String DOUBLE_QUOTE_TOKEN = "\"";

    /** Escaped single quotes. */
    private static final String SINGLE_QUOTE_TOKEN = "\'";

    /** Number of spaces to compose a tab.  */
    private static final int TAB_SIZE = 5;

    /** Reference to Document handler. */
    private DocumentHandle documentHandle;

    /** Log window. */
    private Logger log;

    /** Main window. */
    private Editor mainWindow = null;

    /** Edit menu. */
    private JMenu menu = new JMenu();

    /** Button bar. */
    private JToolBar buttonBar = new JToolBar();

    /** Number of spaces used for a tab. */
    private String tabSpaces;

    /**
     * Create a new XML text formatter instance handler.
     * @param window Reference to main window.
     */
    public XmlHandle(final Editor window) {
        mainWindow = window;
        documentHandle = mainWindow.getDocumentHandle();
        log = mainWindow.getLogHandle().getLog();
        tabSpaces = "";
        StringBuffer buf = new StringBuffer();
        for (int cont = 0; cont < TAB_SIZE; cont++) {
            buf.append(" ");
        }
        tabSpaces = buf.toString();
    }

    /**
     * Obtain XML menu.
     * @return XML Menu option on Menu Bar.
     */
    public JMenu getMenu() {

        JMenuItem miFormatMenu = new JMenuItem();
        miFormatMenu.setText(Messages.getString("kit.gui.editor.91"));
        miFormatMenu.setToolTipText(Messages.getString("kit.gui.editor.92"));
        miFormatMenu.setMnemonic('F');
        miFormatMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                applyFormat();
            }
        });

        JMenuItem miDisplayMenu = new JMenuItem();
        miDisplayMenu.setText(Messages.getString("kit.gui.editor.93"));
        miDisplayMenu.setToolTipText(Messages.getString("kit.gui.editor.93"));
        miDisplayMenu.setMnemonic('c');
        miDisplayMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                visualizar();
            }
        });

        menu.setText(Messages.getString("kit.gui.editor.94"));
        menu.setMnemonic('X');
        menu.add(miFormatMenu);
        menu.add(miDisplayMenu);

        return menu;
    }

    /**
     * Return button bar related to document handling.
     * @return Button bar related to document handling.
     */
    public JToolBar getButtonBar() {

        buttonBar.setFloatable(true);

        JButton btApplyFormat = new JButton();
        btApplyFormat.setIcon(new ImageIcon(getClass().getResource(Constants.ICON_FORMAT)));
        btApplyFormat.setToolTipText(Messages.getString("kit.gui.editor.92"));
        btApplyFormat.setBorderPainted(false);
        btApplyFormat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                applyFormat();
            }
        });

        buttonBar.add(btApplyFormat, null);

        return buttonBar;
    }

    /**
     * Enable / disable graphical values.
     * @param activeValue <code>true</code> enable. <code>false</code> disable.
     */
    public void enable(final boolean activeValue) {
        Component[] buttons = buttonBar.getComponents();
        for (int cont = 0; cont < buttons.length; cont++) {
            buttons[cont].setEnabled(activeValue);
        }

        Component[] subMenu = menu.getMenuComponents();
        for (int cont = 0; cont < subMenu.length; cont++) {
            subMenu[cont].setEnabled(activeValue);
        }
    }


    /**
     * Apply format (tabs, spaces, etc.) on current document. 
     */
    private void applyFormat() {
        if (!documentHandle.isEmpty()) {
            try {
                mainWindow.enableScreen(false);
                documentHandle.openReversible(XMLUtil.prettyPrint(documentHandle.getPlainText()));
                log.logMessage(Messages.getString("kit.gui.editor.95"));
            } catch (RuntimeException ex) {
                String msg = Messages.getString("kit.gui.editor.96");
                log.logMessage(msg);
                JOptionPane.showMessageDialog(mainWindow, msg, Messages.getString("kit.gui.editor.97"), JOptionPane.ERROR_MESSAGE);
            } finally {
                mainWindow.enableScreen(true);
            }
        } else {
            JOptionPane.showMessageDialog(mainWindow, Messages.getString("kit.gui.editor.98"), Messages.getString("kit.gui.editor.98"),
                    JOptionPane.INFORMATION_MESSAGE);
            log.logMessage(Messages.getString("kit.gui.editor.100"));
        }
    }

    /**
     * Display document applying colors and indentation.
     */
    private void visualizar() {
        if (!documentHandle.isEmpty()) {
            try {
                mainWindow.enableScreen(false);
                String[] text = XMLUtil.prettyPrint(documentHandle.getPlainText()).toString().split("<");
                StringBuffer output = new StringBuffer();
                StringBuffer current;

                output.append(GENERAL_FORMAT);

                for (int cont = 1; cont < text.length; cont++) {
                    current = new StringBuffer(text[cont].replaceAll(" ", HTML_SPACES).replaceFirst("\\n", "\n<br>"));

                    int posToken = applyLabelFormat(current, 0);
                    int len = current.length();
                    while (posToken < len) {
                        char ch = current.charAt(posToken);
                        switch (ch) {
                        case '"':
                            posToken = applyStringFormat(current, posToken, DOUBLE_QUOTE_TOKEN);
                            len = current.length();
                            break;

                        case '\'':
                            posToken = applyStringFormat(current, posToken, SINGLE_QUOTE_TOKEN);
                            len = current.length();
                            break;

                        case '=':
                            posToken = applyAttributeFormat(current, posToken);
                            len = current.length();
                            break;

                        case '&':
                            /* White spaces are skipped. &nbsp; */
                            if (current.indexOf(HTML_SPACES, posToken) == posToken) {
                                posToken += HTML_SPACES.length();
                            } else {
                                posToken++;
                            }
                            break;

                        default:
                            posToken++;
                            break;
                        }
                    }
                    output.append("&lt;");
                    output.append(current);
                }
                output.append(CLOSE_FORMAT);

                documentHandle.openXml(output);
                log.logMessage(Messages.getString("kit.gui.editor.95"));
            } catch (RuntimeException ex) {
                String msg = Messages.getString("kit.gui.editor.96");
                log.logMessage(msg);
                JOptionPane.showMessageDialog(mainWindow, msg, Messages.getString("kit.gui.editor.97"), JOptionPane.ERROR_MESSAGE);
            } finally {
                mainWindow.enableScreen(true);
            }
        } else {
            JOptionPane.showMessageDialog(mainWindow, Messages.getString("kit.gui.editor.98"), Messages.getString("kit.gui.editor.99"),
                    JOptionPane.INFORMATION_MESSAGE);
            log.logMessage(Messages.getString("kit.gui.editor.100"));
        }
    }

    /**
     * Apply format to XML tag.
     * @param current Current string to be formatted.
     * @param tokenPosition Format will be applied starting from this position.
     * @return New position to be formatted.
     */
    private int applyLabelFormat(final StringBuffer current, final int tokenPosition) {
        int spacePosition = current.indexOf("&", tokenPosition);
        int closePosition = current.indexOf(">", tokenPosition);
        int position;
        int startPosition = tokenPosition;
        if (current.indexOf("/") == 0) {
            startPosition++;
        }
        if (spacePosition != -1 || closePosition != -1) {

            if (spacePosition == -1) {
                spacePosition = Integer.MAX_VALUE;
            }

            if (closePosition == -1) {
                closePosition = Integer.MAX_VALUE;
            }

            position = Math.min(spacePosition, closePosition);
            current.insert(startPosition, LABEL_FORMAT);
            position += LABEL_FORMAT.length();
            current.insert(position, CLOSE_FORMAT);
            position += CLOSE_FORMAT.length();
        } else {
            position = tokenPosition + 1;
        }

        return position;
    }

    /**
     * Format current attribute.
     * @param current Buffer content to be formatted.
     * @param tokenPosition Position of the token to be formated.
     * @return New position for token search.
     */
    private int applyAttributeFormat(final StringBuffer current, final int tokenPosition) {

        int returnPosition = tokenPosition;
        int position = returnPosition;
        boolean found = false;
        while (!found && position > 0) {
            if (current.charAt(position) != ';') {
                position--;
            } else {
                found = true;
                current.insert(position + 1, ATTRIBUTE_FORMAT);
                returnPosition += ATTRIBUTE_FORMAT.length();
                current.insert(returnPosition, CLOSE_FORMAT);
                returnPosition += CLOSE_FORMAT.length();
            }
        }

        return returnPosition + 1;
    }

    /**
     * Format quoted position.
     * @param current Buffer content to be formatted.
     * @param tokenPosition Position of the token to be formated.
     * @param quoteToken Token value to be highlighted.
     * @return New position for token search.
     */
    private int applyStringFormat(final StringBuffer current, final int tokenPosition, final String quoteToken) {
        int position = current.indexOf(quoteToken, tokenPosition + 1) + 1;

        current.insert(tokenPosition, STRING_FORMAT);
        position += STRING_FORMAT.length();
        current.insert(position, CLOSE_FORMAT);
        position += CLOSE_FORMAT.length();

        return position;
    }
}
