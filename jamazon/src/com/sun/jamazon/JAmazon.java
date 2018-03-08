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

import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.text.NumberFormat;

import java.net.URL;

import java.util.Map;
import java.util.HashMap;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import javax.swing.*;

import javax.swing.table.JTableHeader;

/**
 * This class contains all the user interface code for the Amazon.com 
 * Java Desktop client for web services.
 *
 * @author Mark Davidson, Sun Microsystems, Inc.
 */
public class JAmazon extends JPanel {

    private AmazonProxy proxy;
    private DetailsTableModel model;

    // ui components
    private JTextField keywordField;
    private JComboBox modeCombo;
    private JTable table;
    private JLabel statusBar;
    private JEditorPane details;

    public final static Color amazonYellow = new Color(238, 238, 205);
    public final static Color amazonBlue = new Color(0, 52, 98);

    private int currentRow = -1; // holds the last row. a bit of a hack

    private static String HTML_HEAD = "<html><body>";
    private static String HTML_FOOT = "</body></html>";

    private static String HTML_TITLE = "<center><h3>Java Desktop Client Example for Amazon Web Services</h3></center>";
    private static String HTML_BODY = "<ul><li>Type search string, press enter or the Send button</li><li>Click on the column headings to sort <li>Select a row in the table to show details <li>Multiple row selection will show the Amazon price total</ul>";

    private static String INIT_TEXT = HTML_HEAD + HTML_TITLE + HTML_BODY + HTML_FOOT;

    private static Map imageCache = new HashMap();

    public JAmazon() {
	// Create the WS proxy and the model
	proxy = new AmazonProxy();
	model = new DetailsTableModel();

	initUI();
    }

    protected void initUI() {
	setLayout(new BorderLayout());
	add(createControlPanel(), BorderLayout.NORTH);

	JSplitPane splitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	splitpane.setTopComponent(createResultsTable());
	splitpane.setBottomComponent(createDetailPane());

	add(splitpane, BorderLayout.CENTER);
	add(createStatusBar(), BorderLayout.SOUTH);
	decorateComponent(this);
    }

    /**
     * Set the message in the status bar.
     */
    public void setStatusMessage(String message) {
	statusBar.setText("  " + message);
    }

    public String getKeyword() {
	return keywordField.getText();
    }

    private JComponent createStatusBar() {
	statusBar = new JLabel();
	decorateComponent(statusBar);

	FontMetrics fm = statusBar.getFontMetrics(statusBar.getFont());
	if (fm != null) {
	    Dimension pref = new Dimension(fm.stringWidth("    "), 
					   (int)(fm.getHeight() * 2));
	    statusBar.setPreferredSize(pref);
	}
	return statusBar;
    }

    private JComponent createResultsTable() {
	table = new JTable(model, new DetailsTableModel.DetailsColumnModel());
	table.setTableHeader(new SortableTableHeader(table.getColumnModel()));
	model.addTableModelListener(new TableModelListener() {
		public void tableChanged(TableModelEvent evt) {
		    // Notification that the data has changed. reset
		    if (evt.getType() == TableModelEvent.UPDATE) {
			currentRow = -1;
			details.setText(INIT_TEXT);
		    }
		}
	    });
				    
	// Clicking on a results item will load the details 
	// A range of selection will display a summary of the price.
	ListSelectionModel selectionModel = table.getSelectionModel();
	selectionModel.addListSelectionListener(new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent evt) {
		    ListSelectionModel lsm = (ListSelectionModel)evt.getSource();
		    if (!lsm.isSelectionEmpty()) {
			int firstrow = lsm.getMinSelectionIndex();
			int lastrow = lsm.getMaxSelectionIndex();

			if (currentRow != firstrow) {
			    details.setText(model.getProductDetails(firstrow));
			    details.setCaretPosition(0);
			    currentRow = firstrow;
			}

			// Compute the total price and send to the status bar
			float price = 0.0f;
			for (int i = firstrow; i <= lastrow; i++) {
			    price += model.getPrice(i);
			}
			NumberFormat nf = NumberFormat.getCurrencyInstance();
			setStatusMessage("Total Amazon price of selected items: " + nf.format(price));
		    }
		}
	    });

        // set our custom renderer on the JTable
	table.setOpaque(false);
        table.setDefaultRenderer(Object.class, new DetailsTableModel.AmazonTableCellRenderer());

	JScrollPane sp = new JScrollPane();
	final Image bgImage = getImage("resources/amazon.png", this);
	JViewport vp = new JViewport() {
		public void paintComponent(Graphics g) {
		    super.paintComponent(g);

		    int width = this.getWidth();
		    int height = this.getHeight();
		    int imageW = bgImage.getWidth(null);
		    int imageH = bgImage.getHeight(null);
		    
		    int x = (width - imageW)/2;
		    int y = (height - imageH)/2;

		    g.drawImage(bgImage, x, y, this);
		}
	    };
	vp.setBackground(Color.white);
	vp.setView(table);
	sp.setViewport(vp);
	sp.setPreferredSize(new Dimension(600, 200));

	return sp;
    }

    
    private JComponent createDetailPane() {
	details = new JEditorPane("text/html", INIT_TEXT);
	details.setEditable(false);
	details.setPreferredSize(new Dimension(600, 300));
	
	return new JScrollPane(details);
    }

    private JPanel createControlPanel() {
	JPanel panel = new JPanel();
	panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	// Set padding.
	int PAD = 5;

	panel.setBorder(BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));
	decorateComponent(panel);

	JLabel label = new JLabel("Search: ");
	decorateComponent(label);
	
	panel.add(label);
	panel.add(Box.createHorizontalStrut(PAD));
	panel.add(keywordField = new JTextField("Java Web Services"));
	panel.add(Box.createHorizontalStrut(PAD));
	panel.add(modeCombo = new JComboBox(AmazonProxy.MODE_KEYS));
	panel.add(Box.createHorizontalStrut(PAD));

	JButton button = new JButton("Send");
	decorateComponent(button);

	panel.add(button);

	ActionListener handler = new RequestHandler(proxy, model, this);

	keywordField.addActionListener(handler);
	button.addActionListener(handler);

	modeCombo.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		    proxy.setMode((String)modeCombo.getSelectedItem());
		}
	    });
	decorateComponent(modeCombo);

	return panel;
    }


    //  Utility methods. 
	
    public static Image getImage(String name, Component cmp) {
	Icon icon = getIcon(name, cmp);
	if (icon != null) {
	    return ((ImageIcon)icon).getImage();
	} else {
	    return null;
	}
    }

    public static Icon getIcon(String name, Component cmp) {
	Icon icon = null;
	
	if ((icon = (Icon)imageCache.get(name)) != null) {
	    return icon;
	}

	URL fileLoc = cmp.getClass().getResource(name);
	icon = new ImageIcon(fileLoc);
	if (icon != null) {
	    imageCache.put(name, icon);
	}
	return icon;
    }

    private void decorateComponent(JComponent comp) {
	comp.setBackground(amazonYellow);
	comp.setForeground(amazonBlue);
    }

    public static void main(String[] args) {
	JFrame frame = new JFrame("JAmazon.com - web services");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	frame.getContentPane().add(new JAmazon());
	frame.pack();
	frame.setVisible(true);
    }
}
