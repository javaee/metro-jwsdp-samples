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
