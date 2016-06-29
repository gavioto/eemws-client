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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent; 

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import es.ree.eemws.core.utils.xml.XMLUtil;
import es.ree.eemws.kit.common.Messages;
import es.ree.eemws.kit.gui.common.Logger;
import es.ree.eemws.kit.gui.common.Constants;


/**
 * Edition management.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 09/05/2014
 */
public final class EditHandle {

    /** Reference to document Handle. */
    private DocumentHandle documentHandle;

    /** Log window. */
    private Logger log;

    /** Last search term. */
    private String lastSearchTerm = null;

    /** Indicate whether search must ignores capitalization. */
    private boolean findIsCaseSensitive = false;

    /** Main window. */
    private Editor mainWindow = null;

    /** Edit menu. */
    private JMenu editMenu =  new JMenu();

    /** Button bar. */
    private JToolBar buttonBar = new JToolBar();

    /**
     * Constructor. Creates a new instance of Text Manager.
     * @param window Reference to main window.
     */
    public EditHandle(final Editor window) {

        mainWindow = window;
        documentHandle = mainWindow.getDocumentHandle();
        log = mainWindow.getLogHandle().getLog();
    }

    /**
     * Retrieves "Edit" menu.
     * @return Edition menu for Main Options bar.
     */
    public JMenu getMenu() {

        javax.swing.Action cut = documentHandle.getAction("cut-to-clipboard"); //$NON-NLS-1$
        JMenuItem cutMenuItem = new JMenuItem(cut);
        cutMenuItem.setAccelerator(KeyStroke.getKeyStroke('X', InputEvent.CTRL_MASK));
        cutMenuItem.setText(Messages.getString("EDITOR_CUT")); //$NON-NLS-1$
        cutMenuItem.setMnemonic(Messages.getString("EDITOR_CUT_HK").charAt(0)); //$NON-NLS-1$

        javax.swing.Action copy = documentHandle.getAction("copy-to-clipboard"); //$NON-NLS-1$
        JMenuItem copyMenuItem = new JMenuItem(copy);
        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke('C', InputEvent.CTRL_MASK));
        copyMenuItem.setText(Messages.getString("EDITOR_COPY")); //$NON-NLS-1$
        copyMenuItem.setMnemonic(Messages.getString("EDITOR_COPY_HK").charAt(0)); //$NON-NLS-1$

        javax.swing.Action paste = documentHandle.getAction("paste-from-clipboard"); //$NON-NLS-1$
        JMenuItem pasteMenuItem = new JMenuItem(paste);
        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke('V', InputEvent.CTRL_MASK));
        pasteMenuItem.setText(Messages.getString("EDITOR_PASTE")); //$NON-NLS-1$
        pasteMenuItem.setMnemonic(Messages.getString("EDITOR_PASTE_HK").charAt(0)); //$NON-NLS-1$

        JMenuItem findMenuItem = new JMenuItem(Messages.getString("EDITOR_FIND"), new ImageIcon(getClass().getResource(Constants.ICON_FIND))); //$NON-NLS-1$
        findMenuItem.setMnemonic(Messages.getString("EDITOR_FIND_HK").charAt(0)); //$NON-NLS-1$
        findMenuItem.setAccelerator(KeyStroke.getKeyStroke(Messages.getString("EDITOR_FIND_HK").charAt(0), InputEvent.CTRL_MASK)); //$NON-NLS-1$
        findMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) { // NOSONAR
                find();
            }
        });

        JMenuItem findNextMenuItem = new JMenuItem(Messages.getString("EDITOR_FIND_NEXT"), new ImageIcon(getClass().getResource(Constants.ICON_FIND))); //$NON-NLS-1$
        findNextMenuItem.setMnemonic(Messages.getString("EDITOR_FIND_NEXT").charAt(0)); //$NON-NLS-1$
        findNextMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        findNextMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) { // NOSONAR
                findNext(true);
            }
        });

        JMenuItem replaceMenuItem = new JMenuItem();
        replaceMenuItem.setText(Messages.getString("EDITOR_REPLACE")); //$NON-NLS-1$
        replaceMenuItem.setMnemonic(Messages.getString("EDITOR_REPLACE_HK").charAt(0)); //$NON-NLS-1$
        replaceMenuItem.setAccelerator(KeyStroke.getKeyStroke(Messages.getString("EDITOR_REPLACE_HK").charAt(0), InputEvent.CTRL_MASK)); //$NON-NLS-1$
        replaceMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) { // NOSONAR
                replace();
            }
        });

        JMenuItem goToLineMenuItem = new JMenuItem(Messages.getString("EDITOR_GO_TO_LINE"), new ImageIcon(getClass().getResource(Constants.ICON_GO))); //$NON-NLS-1$
        goToLineMenuItem.setMnemonic(Messages.getString("EDITOR_GO_TO_LINE_HK").charAt(0)); //$NON-NLS-1$
        goToLineMenuItem.setAccelerator(KeyStroke.getKeyStroke(Messages.getString("EDITOR_GO_TO_LINE_HK").charAt(0), InputEvent.CTRL_MASK)); //$NON-NLS-1$
        goToLineMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) { // NOSONAR
                goToLine();
            }
        });

        JMenuItem undoMenuItem = new JMenuItem(mainWindow.getUndoRedoHandle().getUndoAction());
        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke('Z', InputEvent.CTRL_MASK));
        undoMenuItem.setText(Messages.getString("EDITOR_UNDO")); //$NON-NLS-1$
        undoMenuItem.setMnemonic(Messages.getString("EDITOR_UNDO_HK").charAt(0)); //$NON-NLS-1$

        JMenuItem redoMenuItem = new JMenuItem(mainWindow.getUndoRedoHandle().getRedoAction());
        redoMenuItem.setAccelerator(KeyStroke.getKeyStroke('Y', InputEvent.CTRL_MASK));
        redoMenuItem.setText(Messages.getString("EDITOR_REDO")); //$NON-NLS-1$
        redoMenuItem.setMnemonic(Messages.getString("EDITOR_REDO").charAt(0)); //$NON-NLS-1$

        Action selectAll = documentHandle.getAction("select-all"); //$NON-NLS-1$
        JMenuItem selectAllMenuItem = new JMenuItem(selectAll);
        selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke('A', InputEvent.CTRL_MASK));
        selectAllMenuItem.setText(Messages.getString("EDITOR_SELECT_ALL")); //$NON-NLS-1$
        selectAllMenuItem.setMnemonic(Messages.getString("EDITOR_SELECT_ALL_HK").charAt(0)); //$NON-NLS-1$

        Action selectLine = documentHandle.getAction("select-line"); //$NON-NLS-1$
        JMenuItem selectLineMenuItem = new JMenuItem(selectLine);
        selectLineMenuItem.setText(Messages.getString("EDITOR_SELECT_LINE")); //$NON-NLS-1$
        selectLineMenuItem.setMnemonic(Messages.getString("EDITOR_SELECT_LINE_HK").charAt(0)); //$NON-NLS-1$

        JMenuItem xmlFormatMenuItem = new JMenuItem();
        xmlFormatMenuItem.setText(Messages.getString("EDITOR_MENU_ITEM_XML_FORMAT")); //$NON-NLS-1$
        xmlFormatMenuItem.setMnemonic(Messages.getString("EDITOR_MENU_ITEM_XML_FORMAT_HK").charAt(0)); //$NON-NLS-1$
        xmlFormatMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) { // NOSONAR event is not used.
                applyFormat();
            }
        });
       
        editMenu.setText(Messages.getString("EDITOR_EDIT_MENU_ENTRY")); //$NON-NLS-1$
        editMenu.setMnemonic(Messages.getString("EDITOR_EDIT_MENU_ENTRY_HK").charAt(0)); //$NON-NLS-1$
        editMenu.add(cutMenuItem);
        editMenu.add(copyMenuItem);
        editMenu.add(pasteMenuItem);
        editMenu.addSeparator();
        editMenu.add(findMenuItem);
        editMenu.add(findNextMenuItem);
        editMenu.add(replaceMenuItem);
        editMenu.add(goToLineMenuItem);
        editMenu.addSeparator();
        editMenu.add(undoMenuItem);
        editMenu.add(redoMenuItem);
        editMenu.addSeparator();
        editMenu.add(selectAllMenuItem);
        editMenu.add(selectLineMenuItem);
        editMenu.addSeparator();
        editMenu.add(xmlFormatMenuItem);

        return editMenu;
    }

    /**
     * Applies format (tabs, spaces, etc.) on current document. 
     */
    private void applyFormat() {
        if (documentHandle.isEmpty()) {
            JOptionPane.showMessageDialog(mainWindow, Messages.getString("EDITOR_DOCUMENT_EMPTY"),  //$NON-NLS-1$
                    Messages.getString("MSG_INFO_TITLE"), //$NON-NLS-1$
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            mainWindow.enableScreen(false);
            documentHandle.openReversible(XMLUtil.prettyPrint(documentHandle.getPlainText()));
            mainWindow.enableScreen(true);
        }
    }   
    
    /**
     * Retrieves Edition Button bar.
     * @return Edition Button bar.
     */
    public JToolBar getButtonBar() {

        buttonBar.setFloatable(true);

        JButton goToLineBtn = new JButton();
        goToLineBtn.setIcon(new ImageIcon(getClass().getResource(Constants.ICON_GO)));
        goToLineBtn.setToolTipText(Messages.getString("EDITOR_GO_TO_LINE")); //$NON-NLS-1$
        goToLineBtn.setBorderPainted(false);
        goToLineBtn.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) { // NOSONAR event is not used.
                goToLine();
            }
        });

        JButton findBtn = new JButton();
        findBtn.setIcon(new ImageIcon(getClass().getResource(Constants.ICON_FIND)));
        findBtn.setToolTipText(Messages.getString("EDITOR_FIND")); //$NON-NLS-1$
        findBtn.setBorderPainted(false);
        findBtn.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) { // NOSONAR event is not used.
                find();
            }
        });
        
        JButton btApplyFormat = new JButton();
        btApplyFormat.setIcon(new ImageIcon(getClass().getResource(Constants.ICON_FORMAT)));
        btApplyFormat.setToolTipText(Messages.getString("EDITOR_MENU_ITEM_XML_FORMAT")); //$NON-NLS-1$
        btApplyFormat.setBorderPainted(false);
        btApplyFormat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {  // NOSONAR event is not used.
                applyFormat();
            }
        });

        buttonBar.add(goToLineBtn, null);
        buttonBar.add(findBtn, null);
        buttonBar.add(btApplyFormat, null);

        return buttonBar;
    }

    /**
     * Enables disable graphic values.
     * @param activeValue <code>true</code> enable. <code>false</code> disable.
     */
    public void enable(final boolean activeValue) {
        for (Component component : buttonBar.getComponents()) {
            component.setEnabled(activeValue);
        }
        
        for (Component menu : editMenu.getMenuComponents()) {
            menu.setEnabled(activeValue);
        }
    }

    /**
     * Searches successively in document the last term entered, starting from cursor position.
     * @param mustShowNotFoundDialogue Indicate whether the "not found" dialogue must be
     * displayed or not.
     * @return <code>true</code> If search term was found. <code>false</code> Otherwise.
     */
    private boolean findNext(final boolean mustShowNotFoundDialogue) {

        boolean retValue = false;
        if (!documentHandle.isEmpty()) {

            if (lastSearchTerm != null) {

                String search = lastSearchTerm;
                String text = documentHandle.getPlainText();
                if (!findIsCaseSensitive) {

                    text = text.toLowerCase();
                    search = search.toLowerCase();
                }

                int chrStart = documentHandle.getCursorPosition();
                int start = text.indexOf(search, chrStart);
                if (start == -1) {

                    if (mustShowNotFoundDialogue) {
                        String msg = Messages.getString("EDITOR_SEARCH_NOT_FOUND_DETAIL", lastSearchTerm, chrStart); //$NON-NLS-1$
                        JOptionPane.showMessageDialog(mainWindow, msg, Messages.getString("EDITOR_SEARCH_NOT_FOUND"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
                        log.logMessage(msg);
                    }

                } else {

                    int end = lastSearchTerm.length() + start;
                    documentHandle.markText(start, end);
                    retValue = true;
                }

            } else {

                find();
            }

        } else {

            JOptionPane.showMessageDialog(mainWindow, Messages.getString("EDITOR_DOCUMENT_EMPTY"), Messages.getString("EDITOR_DOCUMENT_EMPTY"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
            log.logMessage(Messages.getString("EDITOR_DOCUMENT_EMPTY")); //$NON-NLS-1$
        }

        return retValue;
    }

    /**
     * Searches in document the entered term, starting from cursor position.
     */
    private void find() {

        if (!documentHandle.isEmpty()) {

            SearchAndReplaceDialogue dialog = new SearchAndReplaceDialogue(mainWindow, false);
            dialog.setVisible(true);
            if (!dialog.isCanceled()) {

                lastSearchTerm = dialog.getSearchTerm();
                findIsCaseSensitive = dialog.isCaseSensitive();
                findNext(true);
            }

        } else {

            JOptionPane.showMessageDialog(mainWindow, Messages.getString("EDITOR_DOCUMENT_EMPTY"), Messages.getString("EDITOR_DOCUMENT_EMPTY"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
            log.logMessage(Messages.getString("EDITOR_DOCUMENT_EMPTY")); //$NON-NLS-1$
        }
    }

    /**
     * Replaces text according to values entered by user on dialogue.
     */
    private void replace() {

        if (!documentHandle.isEmpty()) {

            SearchAndReplaceDialogue dialogue = new SearchAndReplaceDialogue(mainWindow, true);
            dialogue.setVisible(true);
            if (!dialogue.isCanceled()) {
                
                lastSearchTerm = dialogue.getSearchTerm();
                String replace = dialogue.getReplaceTerm();
                findIsCaseSensitive = dialogue.isCaseSensitive();
                boolean replaceAll = dialogue.isReplaceAll();
                if (lastSearchTerm.equals(replace) || !findIsCaseSensitive && lastSearchTerm.equalsIgnoreCase(replace)) {

                    JOptionPane.showMessageDialog(mainWindow, Messages.getString("EDITOR_REPLACE_THE_SAME"), Messages.getString("EDITOR_REPLACE_THE_SAME_NOTHING_TO_REPLACE"), 1); //$NON-NLS-1$ //$NON-NLS-2$

                } else {

                    int ocurrences = 0;
                    if (replaceAll) {

                        documentHandle.setCursorToBeginning();
                        while (findNext(ocurrences == 0)) {

                            documentHandle.replace(replace);
                            ocurrences++;
                        }

                    } else {

                        if (findNext(ocurrences == 0)) {

                            documentHandle.replace(replace);
                            ocurrences++;
                        }
                    }

                    if (ocurrences > 0) {
                        JOptionPane.showMessageDialog(mainWindow, Messages.getString("EDITOR_REPLACE_NUM_REPLACEMENTS", ocurrences),  //$NON-NLS-1$
                                Messages.getString("MSG_INFO_TITLE"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
                    }
                }
            }

        } else {

            JOptionPane.showMessageDialog(mainWindow, Messages.getString("EDITOR_DOCUMENT_EMPTY"), Messages.getString("EDITOR_DOCUMENT_EMPTY"), JOptionPane.INFORMATION_MESSAGE);  //$NON-NLS-1$//$NON-NLS-2$
            log.logMessage(Messages.getString("EDITOR_DOCUMENT_EMPTY")); //$NON-NLS-1$
        }
    }

    /**
     * Skips to the line entered on dialogue.
     */
    private void goToLine() {

        if (!documentHandle.isEmpty()) {

            String[] lines = documentHandle.getPlainText().split("\n"); //$NON-NLS-1$
            int numberOfLines = lines.length;
            boolean loop = true;
            while (loop) {

                String lineNumber = JOptionPane.showInputDialog(mainWindow, Messages.getString("EDITOR_GO_TO_LINE_NUMBER", numberOfLines), //$NON-NLS-1$
                        Messages.getString("MSG_QUESTION_TITLE"), JOptionPane.QUESTION_MESSAGE); //$NON-NLS-1$
                
                if (lineNumber != null) {

                    try {

                        int numLine = Integer.parseInt(lineNumber);
                        if (numLine > 0 && numLine <= numberOfLines) {

                            loop = false;

                            int start = 0;
                            for (int cont = 0; cont < numLine - 1; cont++) {

                                start += lines[cont].length() + 1;
                            }
                            int end = start + lines[numLine - 1].length();
                            documentHandle.markText(start, end);
                        }

                    } catch (NumberFormatException e) {

                        /* Non numeric value entered. */
                        loop = true;
                    }

                } else {

                    /* Cancel */
                    loop = false;
                }
            }

        } else {

            JOptionPane.showMessageDialog(mainWindow, Messages.getString("EDITOR_DOCUMENT_EMPTY"),  //$NON-NLS-1$
                    Messages.getString("EDITOR_DOCUMENT_EMPTY"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$

        }
    }

    /**
     * Text replace dialogue.
     */
    class SearchAndReplaceDialogue extends JDialog {

        /** Class ID. */
        private static final long serialVersionUID = -8621170710549253308L;

        /** Search term text box. */
        private JTextField searchTxt;

        /** Replace by Text box. */
        private JTextField replaceTxt;

        /** Checkbox to indicate if all elements must be replaced. */
        private JCheckBox replaceAll;

        /** Checkbox to indicate whether the search
         * must ignore capitalization. */
        private JCheckBox caseSensitive;

        /** Indicates if search was cancelled. */
        private boolean isCanceled;

        /**
         * Dialogue with search and replace parameters.
         * @param window Main window.
         * @param isReplace toggle between search and replace modes.
         */
        SearchAndReplaceDialogue(final Frame window, final boolean isReplace) {

            super(window);
            setModal(true);
            isCanceled = false;
            searchTxt = new JTextField(""); //$NON-NLS-1$
            searchTxt.setBounds(new Rectangle(90, 10, 190, 20));
            searchTxt.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {  // NOSONAR event is not used.
                    accept();
                }
            });

            JLabel searchLbl = new JLabel(Messages.getString("EDITOR_FIND_LBL")); //$NON-NLS-1$
            searchLbl.setLabelFor(searchTxt);
            searchLbl.setBackground(Color.RED);
            searchLbl.setDisplayedMnemonic(Messages.getString("EDITOR_FIND_LBL_HK").charAt(0)); //$NON-NLS-1$
            searchLbl.setBounds(new Rectangle(10, 10, 50, 20));
            replaceTxt = new JTextField(""); //$NON-NLS-1$
            replaceTxt.setBounds(new Rectangle(90, 33, 190, 20));
            replaceTxt.setEnabled(isReplace);
            replaceTxt.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {  // NOSONAR event is not used.
                    accept();
                }
            });

            JLabel replaceLbl = new JLabel(Messages.getString("EDITOR_REPLACE_LBL")); //$NON-NLS-1$
            replaceLbl.setLabelFor(replaceTxt);
            replaceLbl.setDisplayedMnemonic(Messages.getString("EDITOR_REPLACE_LBL_HK").charAt(0)); //$NON-NLS-1$
            replaceLbl.setBounds(new Rectangle(10, 33, 100, 20));
            replaceLbl.setEnabled(isReplace);
            replaceAll = new JCheckBox();
            replaceAll.setText(Messages.getString("EDITOR_REPLACE_ALL_LBL")); //$NON-NLS-1$
            replaceAll.setMnemonic(Messages.getString("EDITOR_REPLACE_ALL_LBL_HK").charAt(0)); //$NON-NLS-1$
            replaceAll.setSelected(false);
            replaceAll.setBounds(new Rectangle(10, 66, 120, 20));
            replaceAll.setEnabled(isReplace);
            caseSensitive = new JCheckBox();
            caseSensitive.setText(Messages.getString("EDITOR_REPLACE_CASE_SENSITIVE")); //$NON-NLS-1$
            caseSensitive.setMnemonic(Messages.getString("EDITOR_REPLACE_CASE_SENSITIVE_HK").charAt(0)); //$NON-NLS-1$
            caseSensitive.setSelected(true);
            caseSensitive.setSelected(false);
            caseSensitive.setBounds(new Rectangle(10, 90, 270, 20));
            JButton findButton = new JButton();
            findButton.setMaximumSize(new Dimension(100, 26));
            findButton.setMinimumSize(new Dimension(100, 26));
            findButton.setPreferredSize(new Dimension(100, 26));
            findButton.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) { // NOSONAR event is not used.
                    accept();
                }
            });

            JLabel separator = new JLabel();
            separator.setText("          "); //$NON-NLS-1$
            JButton cancelBtn = new JButton();
            cancelBtn.setMaximumSize(new Dimension(100, 26));
            cancelBtn.setMinimumSize(new Dimension(100, 26));
            cancelBtn.setPreferredSize(new Dimension(100, 26));
            cancelBtn.setMnemonic(Messages.getString("EDITOR_CANCEL_BUTTON_HK").charAt(0)); //$NON-NLS-1$
            cancelBtn.setText(Messages.getString("EDITOR_CANCEL_BUTTON")); //$NON-NLS-1$
            cancelBtn.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {  // NOSONAR event is not used.
                    cancel();
                }
            });

            JPanel buttonLabel = new JPanel();
            buttonLabel.add(findButton, null);
            buttonLabel.add(separator, null);
            buttonLabel.add(cancelBtn, null);
            JPanel textPanel = new JPanel();
            textPanel.setLayout(null);
            textPanel.add(searchLbl, null);
            textPanel.add(searchTxt, null);
            textPanel.add(replaceLbl, null);
            textPanel.add(replaceTxt, null);
            textPanel.add(replaceAll, null);
            textPanel.add(caseSensitive, null);
            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(textPanel, BorderLayout.CENTER);
            getContentPane().add(buttonLabel, BorderLayout.SOUTH);
            setSize(new Dimension(300, 190));
            setResizable(false);
            if (isReplace) {
                setTitle(Messages.getString("EDITOR_SEARCH_AND_REPLACE")); //$NON-NLS-1$
                findButton.setMnemonic(Messages.getString("EDITOR_SEARCH_AND_REPLACE_HK").charAt(0)); //$NON-NLS-1$
                findButton.setText(Messages.getString("EDITOR_SEARCH_AND_REPLACE")); //$NON-NLS-1$
            } else {
                setTitle(Messages.getString("EDITOR_FIND")); //$NON-NLS-1$
                findButton.setMnemonic(Messages.getString("EDITOR_FIND_HK").charAt(0)); //$NON-NLS-1$
                findButton.setText(Messages.getString("EDITOR_FIND")); //$NON-NLS-1$
                
            }
            Dimension screen = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
            setLocation(screen.width / 3, screen.height / 3);
            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                public void windowClosing(final WindowEvent e) {  // NOSONAR event is not used.
                    cancel();
                }
            });
        }

        /**
         * Close Search dialogue.
         */
        private void accept() {

            isCanceled = false;
            setVisible(false);
        }

        /**
         * Close dialogue without making changes.
         */
        private void cancel() {

            isCanceled = true;
            setVisible(false);
        }

        /**
         * Indicate whether the search was cancelled.
         * @return <code>true</code> if cancelled, <code>false</code> otherwise.
         */
        public boolean isCanceled() {

            return isCanceled;
        }

        /**
         * Indicate whether the capitalization must be ignored.
         * @return <code>true</code> If capitalization is ignored<code>false</code> otherwise.
         */
        public boolean isCaseSensitive() {

            return caseSensitive.isSelected();
        }

        /**
         * Indicate if all elements must be replaced.
         * @return <code>true</code> replace all<code>false</code>
         * otherwise.
         */
        public boolean isReplaceAll() {

            return replaceAll.isSelected();
        }

        /**
         * Retrieve search term entered by user.
         * @return search term entered by user.
         */
        public String getSearchTerm() {

            return searchTxt.getText();
        }

        /**
         * Retrieve replace term entered by user.
         * @return Replace term .
         */
        public String getReplaceTerm() {

            return replaceTxt.getText();
        }
    }
}
