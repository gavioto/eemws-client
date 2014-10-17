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
import java.text.MessageFormat;

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
    private boolean findIsNonCaseSensitive = false;

    /** Main window. */
    private Editor mainWindow = null;

    /** Edit menu. */
    private JMenu editMenu =  new JMenu();

    /** Button bar. */
    private JToolBar buttonBar = new JToolBar();

    /**
     * Constructor. Create a new instance of Text Manager.
     * @param window Reference to main window.
     */
    public EditHandle(final Editor window) {

        mainWindow = window;
        documentHandle = mainWindow.getDocumentHandle();
        log = mainWindow.getLogHandle().getLog();
    }

    /**
     * Retrieve "Edit" menu.
     * @return Edition menu for Main Options bar.
     */
    public JMenu getMenu() {

        javax.swing.Action cut = documentHandle.getAction("cut-to-clipboard");
        JMenuItem cutMenuItem = new JMenuItem(cut);
        cutMenuItem.setAccelerator(KeyStroke.getKeyStroke('X', InputEvent.CTRL_MASK));
        cutMenuItem.setText(Messages.getString("kit.gui.editor.0"));
        cutMenuItem.setMnemonic('t');

        javax.swing.Action copy = documentHandle.getAction("copy-to-clipboard");
        JMenuItem copyMenuItem = new JMenuItem(copy);
        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke('C', InputEvent.CTRL_MASK));
        copyMenuItem.setText(Messages.getString("kit.gui.editor.1"));
        copyMenuItem.setMnemonic('C');

        javax.swing.Action paste = documentHandle.getAction("paste-from-clipboard");
        JMenuItem pasteMenuItem = new JMenuItem(paste);
        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke('V', InputEvent.CTRL_MASK));
        pasteMenuItem.setText(Messages.getString("kit.gui.editor.2"));
        pasteMenuItem.setMnemonic('P');

        JMenuItem findMenuItem = new JMenuItem(Messages.getString("kit.gui.editor.3"), new ImageIcon(getClass().getResource(Constants.ICON_FIND)));
        findMenuItem.setMnemonic('B');
        findMenuItem.setAccelerator(KeyStroke.getKeyStroke('F', InputEvent.CTRL_MASK));
        findMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                find();
            }
        });

        JMenuItem findNextMenuItem = new JMenuItem(Messages.getString("kit.gui.editor.4"), new ImageIcon(getClass().getResource(Constants.ICON_FIND)));
        findNextMenuItem.setMnemonic('z');
        findNextMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        findNextMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                findNext(true);
            }
        });

        JMenuItem replaceMenuItem = new JMenuItem();
        replaceMenuItem.setText(Messages.getString("kit.gui.editor.5"));
        replaceMenuItem.setMnemonic('R');
        replaceMenuItem.setAccelerator(KeyStroke.getKeyStroke('R', InputEvent.CTRL_MASK));
        replaceMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                replace();
            }
        });

        JMenuItem goToLineMenuItem = new JMenuItem(Messages.getString("kit.gui.editor.6"), new ImageIcon(getClass().getResource(Constants.ICON_GO)));
        goToLineMenuItem.setMnemonic('I');
        goToLineMenuItem.setAccelerator(KeyStroke.getKeyStroke('G', InputEvent.CTRL_MASK));
        goToLineMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                goToLine();
            }
        });

        JMenuItem undoMenuItem = new JMenuItem(mainWindow.getUndoRedoHandle().getUndoAction());
        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke('Z', InputEvent.CTRL_MASK));
        undoMenuItem.setText(Messages.getString("kit.gui.editor.7"));
        undoMenuItem.setMnemonic('D');

        JMenuItem redoMenuItem = new JMenuItem(mainWindow.getUndoRedoHandle().getRedoAction());
        redoMenuItem.setAccelerator(KeyStroke.getKeyStroke('Y', InputEvent.CTRL_MASK));
        redoMenuItem.setText(Messages.getString("kit.gui.editor.8"));
        redoMenuItem.setMnemonic('R');

        javax.swing.Action selectAll = documentHandle.getAction("select-all");
        JMenuItem selectAllMenuItem = new JMenuItem(selectAll);
        selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke('A', InputEvent.CTRL_MASK));
        selectAllMenuItem.setText(Messages.getString("kit.gui.editor.9"));
        selectAllMenuItem.setMnemonic('S');

        javax.swing.Action selectLine = documentHandle.getAction("select-line");
        JMenuItem selectLineMenuItem = new JMenuItem(selectLine);
        selectLineMenuItem.setText(Messages.getString("kit.gui.editor.10"));
        selectLineMenuItem.setMnemonic('l');

        editMenu.setText(Messages.getString("kit.gui.editor.11"));
        editMenu.setMnemonic('E');
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

        return editMenu;
    }

    /**
     * Retrieve Edition Button bar.
     * @return Edition Button bar.
     */
    public JToolBar getButtonBar() {

        buttonBar.setFloatable(true);

        JButton goToLineBtn = new JButton();
        goToLineBtn.setIcon(new ImageIcon(getClass().getResource(Constants.ICON_GO)));
        goToLineBtn.setToolTipText(Messages.getString("kit.gui.editor.6"));
        goToLineBtn.setBorderPainted(false);
        goToLineBtn.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                goToLine();
            }
        });

        JButton findBtn = new JButton();
        findBtn.setIcon(new ImageIcon(getClass().getResource(Constants.ICON_FIND)));
        findBtn.setToolTipText(Messages.getString("kit.gui.editor.3"));
        findBtn.setBorderPainted(false);
        findBtn.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                find();
            }
        });

        buttonBar.add(goToLineBtn, null);
        buttonBar.add(findBtn, null);

        return buttonBar;
    }

    /**
     * Enable disable graphic values.
     * @param activeValue <code>true</code> enable. <code>false</code> disable.
     */
    public void enable(final boolean activeValue) {

        Component[] botones = buttonBar.getComponents();
        for (int cont = 0; cont < botones.length; cont++) {

            botones[cont].setEnabled(activeValue);
        }

        Component[] subMenu = editMenu.getMenuComponents();
        for (int cont = 0; cont < subMenu.length; cont++) {

            subMenu[cont].setEnabled(activeValue);
        }
    }

    /**
     * Search successively in document the last term entered, starting from cursor position.
     * @param mustShowNotFoundDialogue Indicate whether the "not found" dialogue must be
     * displayed or not.
     * @return <code>true</code> If search term was found. <code>false</code> Otherwise.
     */
    private boolean findNext(final boolean mustShowNotFoundDialogue) {

        boolean retValue = false;
        if (!documentHandle.isEmpty()) {

            if (lastSearchTerm != null) {

                String busca = lastSearchTerm;
                String texto = documentHandle.getPlainText();
                if (findIsNonCaseSensitive) {

                    texto = texto.toLowerCase();
                    busca = busca.toLowerCase();
                }

                int caracterStart = documentHandle.getCursorPosition();
                int start = texto.indexOf(busca, caracterStart);
                if (start == -1) {

                    if (mustShowNotFoundDialogue) {

                        Object[] paramsText = {lastSearchTerm, caracterStart};
                        String msg = MessageFormat.format(Messages.getString("kit.gui.editor.13"), paramsText);
                        JOptionPane.showMessageDialog(mainWindow, msg, Messages.getString("kit.gui.editor.12"), JOptionPane.INFORMATION_MESSAGE);
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

            JOptionPane.showMessageDialog(mainWindow, Messages.getString("kit.gui.editor.14"), Messages.getString("kit.gui.editor.15"), JOptionPane.INFORMATION_MESSAGE);
            log.logMessage(Messages.getString("kit.gui.editor.16"));
        }

        return retValue;
    }

    /**
     * Search in document the entered term, starting from cursor position.
     */
    private void find() {

        if (!documentHandle.isEmpty()) {

            SearchAndReplaceDialogue dialogo = new SearchAndReplaceDialogue(mainWindow, false);
            dialogo.setVisible(true);
            if (!dialogo.isCanceled()) {

                lastSearchTerm = dialogo.getSearchTerm();
                findIsNonCaseSensitive = dialogo.isNonCaseSensitive();
                findNext(true);
            }

        } else {

            JOptionPane.showMessageDialog(mainWindow, Messages.getString("kit.gui.editor.17"), Messages.getString("kit.gui.editor.18"), JOptionPane.INFORMATION_MESSAGE);
            log.logMessage(Messages.getString("kit.gui.editor.19"));
        }
    }

    /**
     * Replace text according to values entered by user on dialogue.
     */
    private void replace() {

        if (!documentHandle.isEmpty()) {

            SearchAndReplaceDialogue dialogue = new SearchAndReplaceDialogue(mainWindow, true);
            dialogue.setVisible(true);
            if (!dialogue.isCanceled()) {

                lastSearchTerm = dialogue.getSearchTerm();
                String replace = dialogue.getReplaceTerm();
                findIsNonCaseSensitive = dialogue.isNonCaseSensitive();
                boolean replaceAll = dialogue.isReplaceAll();
                if (lastSearchTerm.equals(replace) || findIsNonCaseSensitive && lastSearchTerm.equalsIgnoreCase(replace)) {

                    JOptionPane.showMessageDialog(mainWindow, Messages.getString("kit.gui.editor.20"), Messages.getString("kit.gui.editor.21"), 1);

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

                        Object[] paramsText = {ocurrences};
                        String msg = MessageFormat.format(Messages.getString("kit.gui.editor.26"), paramsText);
                        JOptionPane.showMessageDialog(mainWindow, msg, Messages.getString("kit.gui.editor.22"), JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }

        } else {

            JOptionPane.showMessageDialog(mainWindow, Messages.getString("kit.gui.editor.23"), Messages.getString("kit.gui.editor.24"), JOptionPane.INFORMATION_MESSAGE);
            log.logMessage(Messages.getString("kit.gui.editor.25"));
        }
    }

    /**
     * Skips to the line entered on dialogue.
     */
    private void goToLine() {

        if (!documentHandle.isEmpty()) {

            String[] lines = documentHandle.getPlainText().split("\n");
            int numberOfLines = lines.length;
            boolean loop = true;
            while (loop) {

                Object[] paramsText = {numberOfLines};
                String msg = MessageFormat.format(Messages.getString("kit.gui.editor.28"), paramsText);

                String lineNumber = JOptionPane.showInputDialog(mainWindow, msg, Messages.getString("kit.gui.editor.27"), JOptionPane.QUESTION_MESSAGE);
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

            JOptionPane.showMessageDialog(mainWindow, Messages.getString("kit.gui.editor.23"), Messages.getString("kit.gui.editor.29"), JOptionPane.INFORMATION_MESSAGE);
            log.logMessage(Messages.getString("kit.gui.editor.19"));
        }
    }

    /**
     * Text replace dialogue.
     */
    class SearchAndReplaceDialogue extends JDialog {

        /** Class ID. */
        private static final long serialVersionUID = 0xae5fac8c05f1ca03L;

        /** Search term text box. */
        private JTextField searchTxt;

        /** Replace by Text box. */
        private JTextField replaceTxt;

        /** Checkbox to indicate if all elements must be replaced. */
        private JCheckBox replaceAll;

        /** Checkbox to indicate whether the search
         * must ignore capitalization. */
        private JCheckBox ignorarCase;

        /** Indicates if search was cancelled. */
        private boolean isCanceled;

        /**
         * Dialogue with search and replace parameters.
         * @param window Main window.
         * @param isReplace toggle between search and replace modes.
         */
        public SearchAndReplaceDialogue(final Frame window, final boolean isReplace) {

            super(window);
            setModal(true);
            isCanceled = false;
            searchTxt = new JTextField("");
            searchTxt.setBounds(new Rectangle(90, 10, 190, 20));
            searchTxt.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    accept();
                }
            });

            JLabel searchLbl = new JLabel(Messages.getString("kit.gui.editor.30"));
            searchLbl.setLabelFor(searchTxt);
            searchLbl.setBackground(Color.RED);
            searchLbl.setDisplayedMnemonic('F');
            searchLbl.setBounds(new Rectangle(10, 10, 50, 20));
            replaceTxt = new JTextField("");
            replaceTxt.setBounds(new Rectangle(90, 33, 190, 20));
            replaceTxt.setEnabled(isReplace);
            replaceTxt.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    accept();
                }
            });

            JLabel replaceLbl = new JLabel(Messages.getString("kit.gui.editor.31"));
            replaceLbl.setLabelFor(replaceTxt);
            replaceLbl.setDisplayedMnemonic('R');
            replaceLbl.setBounds(new Rectangle(10, 33, 100, 20));
            replaceLbl.setEnabled(isReplace);
            replaceAll = new JCheckBox();
            replaceAll.setText(Messages.getString("kit.gui.editor.32"));
            replaceAll.setMnemonic('a');
            replaceAll.setSelected(false);
            replaceAll.setBounds(new Rectangle(10, 66, 120, 20));
            replaceAll.setEnabled(isReplace);
            ignorarCase = new JCheckBox();
            ignorarCase.setText(Messages.getString("kit.gui.editor.33"));
            ignorarCase.setMnemonic('N');
            ignorarCase.setSelected(false);
            ignorarCase.setBounds(new Rectangle(10, 90, 270, 20));
            JButton findButton = new JButton();
            findButton.setMaximumSize(new Dimension(100, 26));
            findButton.setMinimumSize(new Dimension(100, 26));
            findButton.setPreferredSize(new Dimension(100, 26));
            findButton.setToolTipText(Messages.getString("kit.gui.editor.34"));
            findButton.setMnemonic('I');
            findButton.setText(Messages.getString("kit.gui.editor.3"));
            findButton.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    accept();
                }
            });

            JLabel separator = new JLabel();
            separator.setText("          ");
            JButton cancelBtn = new JButton();
            cancelBtn.setMaximumSize(new Dimension(100, 26));
            cancelBtn.setMinimumSize(new Dimension(100, 26));
            cancelBtn.setPreferredSize(new Dimension(100, 26));
            cancelBtn.setMnemonic('C');
            cancelBtn.setText(Messages.getString("kit.gui.configuration.6"));
            cancelBtn.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
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
            textPanel.add(ignorarCase, null);
            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(textPanel, BorderLayout.CENTER);
            getContentPane().add(buttonLabel, BorderLayout.SOUTH);
            setSize(new Dimension(300, 190));
            setResizable(false);
            if (isReplace) {
                setTitle(Messages.getString("kit.gui.editor.5"));
            } else {
                setTitle(Messages.getString("kit.gui.editor.3"));
            }
            Dimension screen = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
            setLocation(screen.width / 3, screen.height / 3);
            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                public void windowClosing(final WindowEvent e) {
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
        public boolean isNonCaseSensitive() {

            return ignorarCase.isSelected();
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
