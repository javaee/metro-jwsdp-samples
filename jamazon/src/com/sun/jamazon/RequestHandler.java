/**
 * @(#)RequestHandler.java	1.1 04/03/19
 *
 * Copyright 2004 Sun Microsystems, Inc. All Rights Reserved.
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.TimerTask;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import myamazonclient.AmazonClientGenClient.Details;

/**
 * Glue code which is a callback for an action. This will execute the web 
 * service query. 
 *
 * This code is the trickiest in the application and 
 * could use the most work. 
 */
public class RequestHandler implements ActionListener {

    private AmazonProxy proxy;
    private DetailsTableModel model;
    private JAmazon ui;
    private Timer timer;

    public RequestHandler(AmazonProxy proxy, DetailsTableModel model, 
			 JAmazon ui) {
	this.proxy = proxy;
	this.model = model;
	this.ui = ui;
    }

    public void actionPerformed(ActionEvent evt) {
	proxy.setKeyword(ui.getKeyword());
	proxy.setPage(1);
	    
	// This is a long operation and is executed on a new thread.
	setStatusMessage("Fetching results....");
	
	executeRequest2();
    }

    private void setStatusMessage(String message) {
	ui.setStatusMessage(message);
    }

    /**
     * Uses the SwingWorker. This works corectly but will only return 
     * 10 results.
     */
    private void executeRequest0() {
	SwingWorker worker = new SwingWorker() {
		public Object construct() {
		    if (proxy.executeRequest()) {
			return proxy.getDetails();
		    }
		    return null;
		}
		
		public void finished() {
		    Object results = get();
		    if (results != null) {
			model.setDetails((Details[])results);
			setStatusMessage(model.getRowCount() + " results retrieved");
		    } else {
			setStatusMessage("No results retrieved");
		    }			    
		}
	    };
	worker.start();
    }

    /**
     * Execute the request using java.util.Timer. There are threading issues with
     * this one but it will get all pages and load the model as needed.
     */
    private void executeRequest1() {
	final java.util.Timer timer = new java.util.Timer();

	TimerTask task = new TimerTask() {
		public void run() {
		    if (proxy.executeRequest()) {
			Runnable runnable;
			if (proxy.getNumberOfPages() > 0) {
			    // Must update the UI on an event dispatch thread
			    runnable = new Runnable() {
				    public void run() {
					if (proxy.getPage() == 1) {
					    model.setDetails(proxy.getDetails());
					} else {
					    model.addDetails(proxy.getDetails());
					}
					setStatusMessage(model.getRowCount() + 
							 " results retrieved");
					if (proxy.getPage() == proxy.getNumberOfPages()) {
					    timer.cancel();
					} else {
					    proxy.setPage(proxy.getPage() + 1);
					}
				    }
				};
			} else {
			    runnable = new Runnable() {
				    public void run() {
					setStatusMessage("No results retrieved");
				    }
				};
				
			}
			SwingUtilities.invokeLater(runnable);
		    } else {
			timer.cancel();
		    }
		}
	    };
	timer.schedule(task, 0L, 1150L);
    }


    /**
     * This version uses a Swing Timer.
     */
    private void executeRequest2() {
	timer = new javax.swing.Timer(1150, new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		    if (proxy.executeRequest()) {
			if (proxy.getNumberOfPages() > 0) {
			    if (proxy.getPage() == 1) {
				model.setDetails(proxy.getDetails());
			    } else {
				model.addDetails(proxy.getDetails());
			    }
			    setStatusMessage(model.getRowCount() + 
					     " results retrieved");
			    if (proxy.getPage() == proxy.getNumberOfPages()) {
				timer.stop();
			    } else {
				proxy.setPage(proxy.getPage() + 1);
			    }
			} else {
			    setStatusMessage("No results retrieved");
			}
		    } else {
			timer.stop();
		    }
		}
	    });
	timer.start();
    }
}
	
