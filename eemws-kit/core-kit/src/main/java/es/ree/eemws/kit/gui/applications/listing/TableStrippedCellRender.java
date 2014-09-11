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

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;

/**
 * Class to format table cells.
 * Table is is shown in striped table format, status values are shown in green / red.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 02/06/2014
 */

public final class TableStrippedCellRender extends TableCellRender {

    /** Class ID. */
    private static final long serialVersionUID = 1637294076635324355L;

    /** Selected background color. */
    private static final Color BACKGROUND_COLOR_SELECTED_ITEM = new Color(49, 106, 197);

    /** Selected text color. */
    private static final Color TEXT_COLOR_SELECTED_ITEM = Color.WHITE;

    /** Even cell background color. */
    private static final Color BACKGROUND_COLOR_EVEN_CELL = Color.WHITE;

    /** Even cell text color. */
    private static final Color TEXT_COLOR_EVEN_CELL = Color.BLACK;

    /** Odd cell background color. */
    private static final Color BACKGROUND_COLOR_ODD_CELL = new Color(229, 229, 229);

    /** Odd cell text color. */
    private static final Color TEXT_COLOR_ODD_CELL = Color.BLACK;

    /** Correct message background color. */
    private static final Color BACKGROUND_COLOR_CORRECT_MESSAGE = new Color(85, 198, 84);

    /** Correct message text color. */
    private static final Color TEXT_COLOR_CORRECT_MESSAGE = new Color(255, 255, 128);

    /** Incorrect message background color. */
    private static final Color BACKGROUND_COLOR_INCORRECT_MESSAGE = new Color(255, 105, 108);

    /** Incorrect message text color. */
    private static final Color TEXT_COLOR_INCORRECT_MESSAGE = new Color(255, 255, 128);

    /** Background color for selected correct status cell. */
    private static final Color BACKGROUND_COLOR_CORRECT_ITEM_SELECTED = new Color(84, 193, 198);

    /** Text color for selected correct status cell. */
    private static final Color TEXT_COLOR_CORRECT_ITEM_SELECTED = new Color(255, 255, 128);

    /** Background color for selected incorrect status cell. */
    private static final Color TEXT_COLOR_INCORRECT_ITEM_SELECTED = new Color(255, 255, 128);

    /** Text color for selected incorrect status cell. */
    protected static final Color BACKGROUND_COLOR_INCORRECT_ITEM_SELECTED = new Color(208, 44, 216);

    /**
     * Retrieve a cell formatted according to entered values.
     * @param table Table containing the cell.
     * @param obj Type of the object contained in cell.
     * @param isSelected Indicate whether the cell is selected.
     * @param hasFocus Indicate whether the cell is focused.
     * @param row Row to which cell belongs.
     * @param column Row to which cell belongs.
     * @return Cell formatted according to entered values.
     */
    public Component getTableCellRendererComponent(final JTable table, final Object obj, final boolean isSelected,
            final boolean hasFocus, final int row, final int column) {

        Component cell = super.getTableCellRendererComponent(table, obj, isSelected, hasFocus, row, column);

        if (obj != null && obj.getClass() == MessageStatus.class) {
            if (isSelected) {
                if (((MessageStatus) obj).isOk()) {
                    cell.setBackground(BACKGROUND_COLOR_CORRECT_ITEM_SELECTED);
                    cell.setForeground(TEXT_COLOR_CORRECT_ITEM_SELECTED);
                } else {
                    cell.setBackground(BACKGROUND_COLOR_INCORRECT_ITEM_SELECTED);
                    cell.setForeground(TEXT_COLOR_INCORRECT_ITEM_SELECTED);
                }
            } else {
                if (((MessageStatus) obj).isOk()) {
                    cell.setBackground(BACKGROUND_COLOR_CORRECT_MESSAGE);
                    cell.setForeground(TEXT_COLOR_CORRECT_MESSAGE);
                } else {
                    cell.setBackground(BACKGROUND_COLOR_INCORRECT_MESSAGE);
                    cell.setForeground(TEXT_COLOR_INCORRECT_MESSAGE);
                }
            }
        } else {
            if (isSelected) {
                cell.setBackground(BACKGROUND_COLOR_SELECTED_ITEM);
                cell.setForeground(TEXT_COLOR_SELECTED_ITEM);
            } else {
                if (row % 2 == 0) {
                    cell.setBackground(BACKGROUND_COLOR_EVEN_CELL);
                    cell.setForeground(TEXT_COLOR_EVEN_CELL);
                } else {
                    cell.setBackground(BACKGROUND_COLOR_ODD_CELL);
                    cell.setForeground(TEXT_COLOR_ODD_CELL);
                }
            }
        }

        return cell;
    }

}
