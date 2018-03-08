/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
