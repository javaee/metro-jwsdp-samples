/**
 * @(#)DetailsTableModel.java	1.7 03/07/08
 *
 * Copyright 2003 Sun Microsystems, Inc. All Rights Reserved.
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
 */
package com.sun.jamazon;

import java.awt.*;

import java.net.URL;
import java.net.MalformedURLException; 

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.ParseException;

import javax.swing.*;
import javax.swing.table.*;

import myamazonclient.AmazonClientGenClient.Details;
import myamazonclient.AmazonClientGenClient.Reviews;
import myamazonclient.AmazonClientGenClient.CustomerReview;

/**
 * A table model which encapsulates the details returned
 * from the Amazon.com web service.
 *
 * @author Mark Davidson, Sun Microsystems, Inc.
 */
public class DetailsTableModel extends SortableTableModel {

    private static final Float FLOAT_ZERO = new Float(0f);

    private List details;

    public DetailsTableModel() {
	details = null;
    }

    public DetailsTableModel(Details[] details) {
	setDetails(details);
    }

    public void setDetails(Details[] details) {
	if (details == null) {
	    return;
	}
	
	this.details = new ArrayList();
	this.details.addAll(Arrays.asList(details)); 

	// Required for the SortableTableModel
	setRowElements(this.details);
	
	fireTableDataChanged();
    }

    public void addDetails(Details[] newDetails) {
	if (newDetails == null) {
	    return;
	}

	int firstRow = details.size();
	
	for (int i = 0; i < newDetails.length; i++) {
	    details.add(newDetails[i]);
	}

	fireTableRowsInserted(firstRow, firstRow + newDetails.length);
    }

    /**
     * Returns the number of columns.
     */ 
    public int getColumnCount() {
	return DetailsColumnModel.NUM_COLUMNS;
    }

    public int getRowCount() {
	return (details != null ? details.size() : 0);
    }

    /**
     * Get text value for cell of table
     * @param row table row
     * @param col table column
     */
    public Object getValueAt(int row, int col) {
	return getValueForColumn(getDetails(row), col);
    }

    /**
     * Overridden to sorting of the rows of details.
     */
    public Object getValueForColumn(Object obj, int column) {
	if (obj instanceof Details) {
	    Details details = (Details)obj;

	    switch (column) {

	    case DetailsColumnModel.IDX_TITLE:
		return details.getProductName();

	    case DetailsColumnModel.IDX_AUTHOR:
		String author = "[ no author ]";
		String[] authors = details.getAuthors();
		if (authors != null) {
		    author = authors[0];
		    if (authors.length > 1) {
			// multiple authors
			author += ", et al";
		    }
		}
		return author;

	    case DetailsColumnModel.IDX_LIST_PRICE:
		return getFloatFromPrice(details.getListPrice());

	    case DetailsColumnModel.IDX_AMAZ_PRICE:
		return getFloatFromPrice(details.getOurPrice());

	    case DetailsColumnModel.IDX_RATING:
		Reviews reviews = details.getReviews();
		return (reviews == null ? "" : reviews.getAvgCustomerRating());
	    }
	}
	return null;
    }

    public float getPrice(int row) {
	Float f = (Float)getValueAt(row, DetailsColumnModel.IDX_AMAZ_PRICE);
	if (f != null) {
	    return f.floatValue();
	}
	return 0f;
    }

    /**
     * Create a page with the product details
     * TODO: hand coding html in Java is tedious and unmainatinable. Should
     * probably use a template and define tags which can be replaced.
     */
    public String getProductDetails(int row) {
	Details details = getDetails(row);
	if (details == null) {
	    return "<html><body><i>No product details</i></body></html>";
	}

	// Html header
	StringBuffer buffer = new StringBuffer("<html><head></head><body>");
	
	buffer.append("<table><tr><td><img src=\"").append(details.getImageUrlMedium());
	buffer.append("\"></td><td style=\"vertical-align: top;\"");
	buffer.append("<h2>").append(details.getProductName()).append("</h2>");
	buffer.append("<p><b>Publisher: </b>").append(details.getManufacturer());
	buffer.append("; (").append(details.getReleaseDate());
	buffer.append(")<br><b>ISBN: </b>").append(details.getIsbn());
	buffer.append("<br><b>Sales Rank: </b>").append(details.getSalesRank());
	buffer.append("</td></tr></table>");
	
	Reviews reviews = details.getReviews();
	if (reviews != null) {
	    buffer.append("<b>Average Customer Review: </b>");
	    buffer.append(reviews.getAvgCustomerRating());
	    buffer.append(" Based on ").append(reviews.getTotalCustomerReviews());
	    buffer.append(" reviews<p><hr>");
	    
	    CustomerReview[] crevs = reviews.getCustomerReviews();
	    for (int i = 0; i < crevs.length; i++) {
		CustomerReview rev = crevs[i];
		buffer.append("<b>Rating: ").append(rev.getRating()).append(" ");
		buffer.append(rev.getSummary()).append("</b><p>");
		buffer.append(rev.getComment()).append("<p>");
	    }
	}
	buffer.append("</body></html>");

	return buffer.toString();
    }

    public URL getProductURL(int row) {
	Details details = getDetails(row);
	URL url = null;
	if (details != null) {
	    try {
		url = new URL(details.getUrl());
	    } catch (MalformedURLException ex) {
		// drop through
	    }
	}
	return url;
    }

    private Details getDetails(int row) {
	if (details == null) {
	    return null;
	}
	if (row > details.size()) {
	    return null;
	}
	return (Details)details.get(row);
    }

    /**
     * Converts the Amazon string representation of a price "$99.99" 
     * To a float type.
     */
    public static Float getFloatFromPrice(String value) {
	Float fval = FLOAT_ZERO;

	if (value != null && !"".equals(value)) {
	    if (value.startsWith("$")) {
		value = value.substring(1);
	    }
	    try {
		Number nval = DecimalFormat.getInstance().parse(value);
		fval = new Float(nval.floatValue());
	    } catch (ParseException ex) {
		//
	    }
	}
	return fval;
    }

    /**
     * The renderer component must be non-opaque and the selection is semi-transparant.
     */
    public static class AmazonTableCellRenderer extends DefaultTableCellRenderer {

	protected boolean isSelected = false;
        protected Color selectionColor;

	public AmazonTableCellRenderer() {
	    setOpaque(false);
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value,
						       boolean isSelected, boolean hasFocus, 
						       int row, int column) {
	    if (selectionColor == null) {
                // we'll use a translucent version of the table's default
                // selection color to paint selections

                Color oldCol = table.getSelectionBackground();
                selectionColor = new Color(oldCol.getRed(), oldCol.getGreen(), 
					   oldCol.getBlue(), 128);
	    }

	    // save the selected state since we'll need it when painting
	    this.isSelected = isSelected;
	    return super.getTableCellRendererComponent(table, value, 
						       isSelected, hasFocus, 
						       row, column);
	}

	// since DefaultTableCellRenderer is really just a JLabel, we can override
	// paintComponent to paint the translucent selection when necessary
	public void paintComponent(Graphics g) {
	    if (isSelected) {
		g.setColor(selectionColor);
		g.fillRect(0, 0, getWidth(), getHeight());
	    }
	    super.paintComponent(g);
	}
    }

    public static class DetailsColumnModel extends DefaultTableColumnModel {
	private final static String COL_LABEL_IMAGE = "Image";
	private final static String COL_LABEL_TITLE = "Title";
	private final static String COL_LABEL_AUTHOR = "Author";
	private final static String COL_LABEL_LIST_PRICE = "List Price";
	private final static String COL_LABEL_AMAZ_PRICE = "Amazon Price";
	private final static String COL_LABEL_RATING = "Rating";

	public static final int NUM_COLUMNS = 5;

	// Column indexes
	public final static int IDX_TITLE = 0;
	public final static int IDX_AUTHOR = 1;
	public final static int IDX_LIST_PRICE = 2;
	public final static int IDX_AMAZ_PRICE = 3;
	public final static int IDX_RATING = 4;

	private static final int SMALL_WIDTH = 100;
	private static final int MED_WIDTH = 200;
	private static final int LARGE_WIDTH = 400;

	public DetailsColumnModel() {
	    // Configure the columns and add them to the model
	    addColumn(IDX_TITLE, COL_LABEL_TITLE, LARGE_WIDTH, null);
	    addColumn(IDX_AUTHOR, COL_LABEL_AUTHOR, MED_WIDTH, null);
	    addColumn(IDX_LIST_PRICE, COL_LABEL_LIST_PRICE, SMALL_WIDTH, new PriceRenderer());
	    addColumn(IDX_AMAZ_PRICE, COL_LABEL_AMAZ_PRICE, SMALL_WIDTH, new PriceRenderer());
	    addColumn(IDX_RATING, COL_LABEL_RATING, SMALL_WIDTH, new RatingRenderer());
	}

	private void addColumn(int index, String header, 
			       int width, TableCellRenderer renderer) {
	    
	    TableColumn column = new TableColumn(index, width, renderer, null);
	    column.setHeaderValue(header);
	    addColumn(column);
	}

	/**
	 * A renderer which can correctly render prices
	 */ 
	class PriceRenderer extends AmazonTableCellRenderer {
	    protected void setValue(Object value) {
		Float f = (Float)value;
		NumberFormat nf = NumberFormat.getCurrencyInstance();
		setText(nf.format(f.doubleValue()));
		setHorizontalAlignment(SwingConstants.CENTER);
	    }
	}
	
	/**
	 * Renders the rating value as an ImageIcon of stars. 
	 */
	class RatingRenderer extends AmazonTableCellRenderer {

	    private float mult;

	    private Image img0;
	    private Image img5;

	    private int width;
	    private int height;

	    public RatingRenderer() {
		img0 = JAmazon.getImage("resources/stars-0-0.png", this);
		img5 = JAmazon.getImage("resources/stars-5-0.png", this);
		width = img5.getWidth(null);
		height = img5.getHeight(null);
	    }

	    protected void setValue(Object value) {
		setToolTipText((String)value);

		// Calculate multiple;
		try {
		    float fval  = (new Float((String)value)).floatValue();
		    mult = fval/5f;
		} catch (NumberFormatException ex) {
		    mult = 0f;
		    super.setValue(value);
		}
	    }

	    public void paintComponent(Graphics g) {
		super.paintComponent(g);

		int w2 = (int)(width * mult);

		// Calculate the starting position based on the alignment
		int x = 0;
		int y = 0;

		// TODO: default is center, use the alignment attributes.
		Dimension size = this.getSize();
		x = (int)((size.width - width)/2);
		y = (int)((size.height - height)/2);

		g.translate(x, y);
		g.drawImage(img0, 0, 0, this);
		g.drawImage(img5, 0, 0, w2, height, 0, 0, w2, height, this);
		g.translate(-x, -y);
	    }
	    
	}

    }
}
