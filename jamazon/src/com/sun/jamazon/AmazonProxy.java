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

import java.net.URL;
import java.net.MalformedURLException; 

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import java.rmi.RemoteException; 

import javax.xml.rpc.ServiceException; 

// Connection and request classes
import myamazonclient.AmazonClientGenClient.AmazonSearchService;
import myamazonclient.AmazonClientGenClient.AmazonSearchService_Impl;
import myamazonclient.AmazonClientGenClient.AmazonSearchPort;
import myamazonclient.AmazonClientGenClient.AmazonSearchService;
import myamazonclient.AmazonClientGenClient.KeywordRequest;

// Results classes
import myamazonclient.AmazonClientGenClient.ProductInfo;
import myamazonclient.AmazonClientGenClient.Details;

/**
 * A class that interfaces with the Amazon Webservices.
 *
 * @author Mark Davidson, Sun Microsystems, Inc.
 */
public class AmazonProxy {

    private Map params;  // Request parameters

    private Object results; // The results object. May be a ProductInfo 

    private static AmazonSearchPort port = null;

    // The mode keys that are used for the request
    public  static String[] MODE_KEYS = {
	"books", "dvd", "music", "videogames"
    };
    
    // Human readable strings that should map to the mode keys.
    public static String[] MODE_STRINGS = {
	"Books", "DVD", "Music", "Computer & Video Games"
    };

    public AmazonProxy() {
	try {
	    init();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    public void setParams(Map params) {
	this.params = params;
    }

    public void setParam(String name, String value) {
	if (params == null) {
	    params = new HashMap();
	}
	params.put(name, value);
    }

    /** 
     * Return the value corresponding to name or null if it doesn't exist.
     */
    public String getParam(String name) {
	return params == null ? null : (String)params.get(name);
    }

    // Returns null if there are no params.
    public Set getParamNames() {
	return params == null ? null : params.keySet();
    }

    /**
     * Will initialize the port, Must be called before a request is issued.
     */
    protected void init()  {
	setPage("1");
	setMode("books");
	setType("heavy");
	
	// Required information for the web service.
	setParam("Dev-Tag","D2F3W1N33AZV9P");
	setParam("Tag","webservices-20");

	// XXX workaround for a Java WebStart bug 4852968. JWS uses the deprecated 
	// proxyHost, proxyPort when it should use the http prefix.
	String p = System.getProperty("http.proxyHost");
	if (p != null) {
	    System.setProperty("proxyHost", p);
	} else if ((p = System.getProperty("proxyHost")) != null) {
	    System.setProperty("http.proxyHost", p);
	}

	if ((p = System.getProperty("http.proxyPort")) != null) {
	    System.setProperty("proxyPort", p);
	} else if ((p = System.getProperty("proxyPort")) != null) {
	    System.setProperty("http.proxyPort", p);
	}

	if (port == null) {
	    try {
		AmazonSearchService service = new AmazonSearchService_Impl();
		port = service.getAmazonSearchPort();
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	}
	
    }

    /**
     * Set the keyword for the search
     */
    public void setKeyword(String keyword) {
	setParam("Keyword", keyword);
    }

    /**
     * @param type "heavy" or "lite"
     */
    public void setType(String type) {
	setParam("Type", type);
    }

    public void setPage(int page) {
	try {
	    setPage(Integer.toString(page, 10));
	} catch (NumberFormatException ex) {
	    // get the first page
	    setPage("1");
	}
    }

    /** 
     * Sets the page number. 
     */
    public void setPage(String page) {
	setParam("Page",page);
    }

    /** 
     * Sets the page number. 
     */
    public int getPage() {
	String param = getParam("Page");
	Integer iValue = null;
	try {
	    iValue = Integer.decode(param);
	} catch (NumberFormatException ex) {
	    iValue = new Integer(0);
	}
	return iValue.intValue();
    }

    /**
     * Set the mode for the Search. The product mode is one of MODE_KEYS.
     */
    public void setMode(String mode) {
	setParam("Mode", mode);
    }

    /**
     * Executes the current request. If this returns true then getResults()
     * will contain the ProductInfo data structure.
     * 
     * @return true if successful; false otherwise
     */
    public boolean executeRequest()  {
	KeywordRequest request = new KeywordRequest();

	request.setKeyword((String)params.get("Keyword"));
	request.setPage((String)params.get("Page"));
	request.setMode((String)params.get("Mode"));
	request.setTag((String)params.get("Tag"));
	request.setType((String)params.get("Type"));
	request.setDevtag((String)params.get("Dev-Tag"));

	results = null;
	try {
	    results = port.keywordSearchRequest(request);
	} catch (RemoteException ex) {
	    ex.printStackTrace();
	    if (ex.getCause() != null) {
		System.err.println("Caused by: ");
		ex.getCause().printStackTrace();
	    }
	    return false;
	} catch (Exception ex2) {
	    ex2.printStackTrace();
	    return false;
	}
	return true;
    }

    /**
     * Returns the results from the request.
     */
    public Object getResults() {
	return results;
    }

    public Details[] getDetails() {
	if (results != null) {
	    return ((ProductInfo)results).getDetails();
	} else {
	    return null;
	}
    }

    /**
     * Returns the number of pages.
     */
    public int getNumberOfPages() {
	int pages = 0;

	if (results != null) {
	    ProductInfo info = (ProductInfo)results;
	    try {
		pages = Integer.valueOf(info.getTotalPages()).intValue();
	    } catch (NumberFormatException ex) {
		// drop through, pages will be 0
	    }
	}
	return pages;
    }

    public int getNumberOfResults() {
	int num = 0;
	
	if (results != null) {
	    ProductInfo info = (ProductInfo)results;
	    try {
		num = Integer.valueOf(info.getTotalResults()).intValue();
	    } catch (NumberFormatException ex) {
		// drop through
	    }
	}
	return num;
    }

}
