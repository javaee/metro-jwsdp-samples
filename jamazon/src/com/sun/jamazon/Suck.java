/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;

import java.util.Timer;
import java.util.TimerTask;

import myamazonclient.AmazonClientGenClient.ProductInfo;
import myamazonclient.AmazonClientGenClient.Details;
import myamazonclient.AmazonClientGenClient.Reviews;

/**
 * Sucks all the info from Amazon for a Keyword search and dumps it into a file.
 * Quick and dirty.
 */
public class Suck {

    private AmazonProxy proxy;

    private int pages;
    private int total;
    private int currentPage;
    
    private Timer timer;

    private PrintWriter writer;

    private ProductInfo info;

    private String filename;

    public Suck(String filename) {
	this.filename = filename;
	proxy = new AmazonProxy();
	proxy.setKeyword("java");
	//	proxy.setType("lite");

	if (proxy.executeRequest()) {
	    info = (ProductInfo)proxy.getResults();

	    total = proxy.getNumberOfResults();
	    pages = proxy.getNumberOfPages();

	    System.out.println("Request - total: " + total + " pages: " + pages);
	    
	    // XXX For testing
	    // pages = 5;

	    if (pages > 0 && total != 0) {
		init();
		addDetails(info.getDetails());
		currentPage = 2;

		TimerTask task = new TimerTask() {
			public void run() {
			    proxy.setPage(Integer.toString(currentPage, 10));
			    
			    synchronized (info) {
				if (proxy.executeRequest()) {
				    info = (ProductInfo)proxy.getResults();
				    addDetails(info.getDetails());
				}
			    }
			    currentPage++;

			    if (currentPage > pages) {
				timer.cancel();
				finish();
			    }
			}
		    };
		timer = new Timer();
		timer.schedule(task, 0L, 1050L);
	    }
	}
    }

    private void init() {
	if (writer == null) {
	    try {
		File file = new File(filename);
		FileOutputStream fos = new FileOutputStream(file);
		writer = new PrintWriter(new BufferedOutputStream(fos), true);
		System.out.println(file.getAbsolutePath() + " opened for output...");
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	}
    }

    public void finish() {
	if (writer != null) {
	    writer.close();
	    System.out.println("Finished. Data in " + filename);
	}
    }

    public synchronized void addDetails(Details[] dets) {
	Details[] details = new Details[dets.length];
	System.arraycopy(dets, 0, details, 0, dets.length);
	if (writer == null) {
	    init();
	}

	System.out.println("added more entries...");

	for (int i = 0; i < details.length; i++) {
	    Details d = details[i];
	    if (d != null && writer != null) {

		String[] authors = d.getAuthors();
		String author = "";
		if (authors != null) {
		    author = authors[0];
		    if (authors.length > 1) {
			author += ", et al";
		    }
		}

		writer.println(d.getAsin() + "\t" +
			       d.getProductName() + "\t" +
			       //  d.getUrl() + "\t" +
			       //d.getImageUrlSmall()  + "\t" +
			       author + "\t" +
			       // d.getManufacturer()  + "\t" +
			       d.getReleaseDate()  + "\t" +
			       d.getListPrice()  + "\t" +
			       d.getOurPrice() + "\t" +
			       d.getSalesRank() + "\t" +
			       (d.getReviews() == null ? "0.0\t0" :
				d.getReviews().getAvgCustomerRating()));// + "\t" +
		//d.getReviews().getTotalCustomerReviews()));
	    }
	}
    }

    public static void main(String[] args) {
	Suck suck;
	if (args.length > 0) {
	    suck = new Suck(args[0]);
	} else {
	    suck = new Suck("java-books.txt");
	}
    }
}
