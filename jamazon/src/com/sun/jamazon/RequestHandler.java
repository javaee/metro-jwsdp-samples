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
	
