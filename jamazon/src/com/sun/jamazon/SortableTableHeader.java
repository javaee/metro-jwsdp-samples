/**
 * @(#)SortableTableHeader.java	1.4 02/12/02
 *
 * Copyright 2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials
 * provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY
 * DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT OF OR
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE OR
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF
 * THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 * 
 */

package com.sun.jamazon;

import java.awt.Component;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * A replacement table header which enables sorting on the 
 * table columns by clicking on the header. This class tracks the ordering of
 * sorted columns such that clicking on other column headers will 
 * lead to correct subsorting. This table header
 * will also render arrow buttons on the current sorted column to
 * indicate the sort direction.
 * <p>
 * This class must be used in conjunction with the <code>SortableTableModel</code>
 *
 * @see SortableTableModel
 */
public class SortableTableHeader extends JTableHeader {
    
    private Icon descendingIcon;
    private Icon ascendingIcon;
	

    /**
     * This constructor <i>must</i> be used as a workaround for bug 4776303
     * in JTable that will not register the TableColumnModel on the 
     * JTableHeader when the table header is changed. The code to construct
     * this table header may be as simple as:
     * <pre>
     *  table.setTableHeader(new SortableTableHeader(table.getColumnModel()));
     * </pre>
     *
     * @param model the table columm model associated with the table
     */
    public SortableTableHeader(TableColumnModel model) {
	super(model);

	setDefaultRenderer(new SortHeaderCellRenderer());
	addMouseListener(new SortHeaderMouseAdapter());
    }


    /**
     * Sets the icon to use that indicates the sort order is descending.
     * By default this will use the JLF GR Down16.gif Icon.
     *
     * @param descend the icon to use for descending
     */
    public void setDescendingIcon(Icon descend) {
	this.descendingIcon = descend;
    }

    /**
     * Sets the icon to use that indicates the sort order is ascending.
     * By default this will use the JLF GR Up16.gif Icon.
     *
     * @param ascend the icon to use for ascending
     */
    public void setAscendingIcon(Icon ascend) {
	this.ascendingIcon = ascend;
    }


    /**
     * A cell renderer for the JTableHeader which understands the sorted
     * column state and renders arrow buttons to indicated the sorted column
     * and order
     */
    class SortHeaderCellRenderer extends DefaultTableCellRenderer {

	public SortHeaderCellRenderer() {
	    if (descendingIcon == null) {
		descendingIcon = JAmazon.getIcon("resources/Down16.gif", this);
	    }
	    if (ascendingIcon == null) {
		ascendingIcon = JAmazon.getIcon("resources/Up16.gif", this);
	    }
	    setFont(SortableTableHeader.this.getFont());

	    setBorder(UIManager.getBorder("TableHeader.cellBorder"));
	    setHorizontalAlignment(JLabel.CENTER);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, 
						       boolean isSelected, boolean hasFocus,
						       int row, int column)  {
	    setText((value == null) ? "" : value.toString());
	    
	    SortableTableModel model = (SortableTableModel)table.getModel();

	    Icon icon = null;
	    if (table.convertColumnIndexToModel(column) == model.getColumn()) {
		if (model.isAscending()) {
		    icon = ascendingIcon;
		} else {
		    icon = descendingIcon;
		}
	    }
	    setIcon(icon);
	    
	    return this;
	}
    } // end SortHeaderCellRenderer

    /**
     * A mouse adapater which is attached to the header of a JTable. It listens
     * for mouse clicks on a column and sorts that column.
     */
    class SortHeaderMouseAdapter extends MouseAdapter {
	
	public void mouseClicked(MouseEvent evt) {
	    JTableHeader header = (JTableHeader)evt.getSource();
	    JTable table = header.getTable();
	
	    TableColumnModel columnModel = table.getColumnModel();
	    int viewColumn = columnModel.getColumnIndexAtX(evt.getX()); 
	    int column = table.convertColumnIndexToModel(viewColumn); 
	    if (evt.getClickCount() == 1 && column != -1) {
		SortableTableModel model = (SortableTableModel)table.getModel();

		// Reverse the sorting direction.
		model.sortByColumn(column, !model.isAscending()); 
	    }
	}
    } // end SortHeaderMouseAdapter
}
