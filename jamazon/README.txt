JAmazon.com - Rich Client Interface to Amazons Web Service
==========================================================
Mark Davidson
Java Client Group
Sun Microsystems, Inc

March 2004

This application is a demostration of building a Java Desktop client to the 
Amazon.com's web service using JAX-RPC.

For more information about Amazon's Web Services program see 
http://amazon.com/webservices

The WSDL for Amazon.com's web service is: 
http://soap.amazon.com/schemas3/AmazonWebServices.wsdl

Requirements
------------
Apache Ant
http://ant.apache.org

Java Web Services Developer Pack 1.3 (or greater):
http://java.sun.com/webservices/downloads/webservicespack.html

Java 2 Platform, Standard Edition SDK 1.4.2
http://java.sun.com/j2se/1.4.2/download.html

build.xml and build.properties
------------------------------
An Ant (http://ant.apache.org) build file has been included to run and compile 
the client. 

The default target is "run" which will compile any out of date classes before 
executing.

Before the application can be built, the build.properties file should be changed
to reflect your development environment.

jwsdp.home: Should point to the root of the JWSDP 1.3 directory
proxy.host: The name of the http proxy server (if behind a firewall)
proxy.port: The port of the http proxy server (if behind a firewall)

The "generate-stubs" target will generate the static stubs from the Amazon WSDL. This target
is independent and should be executed first.

Just so the out of the box experience will be better, the default "run" target depends
on "generate-stubs". You may wish to remove this dependency if you intend to do iterative
development.

Firewall Issues
---------------
If you get an UnknownHostException: "soap.amazon.com" then it is likely that you
are behind a firewall and you will have to use a proxy. Set this in build.properties.

The genrate-stubs target will reference the Amazon WSDL at amazon.com. The wscompile
task contains the attribute httpproxy. If a firewall doesn't exist then you may
have to remove that attribute.

Deployment
----------
This application can be deployed with Java WebStart.

All jar deployed jar files must be signed with the same certificate using 
jarsigner or a similar tool.

Here is an ant target that will generate a self certificate and and sign the jar files:

  <target name="deploy" depends="dist" description="creates the deployment bundle">

    <genkey alias="mark" storepass="amazon" keystore="${dir-lib}/keystore"
	    dname="CN=Java Client Group, OU=Java Software Division, O=Sun Microsystems Inc, C=US"/>

    <signjar alias="mark" storepass="amazon" keystore="${dir-lib}/keystore">
      <fileset dir="${dir-dist}" includes="**/*.jar"/>
    </signjar>
    ...

Note that the key only has to be generated once. If genkey is excuted on the same 
keystore that contains an existing key then it will fail.

The jamazon.jnlp is an example of a Java WebStart deployment file. The @deploy.host@ 
tags will be replaced by the value of deploy.host in the build.properties file.

The runtime for JAX-RPC clients must include the following jar files from the JWSDP 1.3:

jaxrpc/lib:
	jaxrpc-impl.jar
	jaxrpc-api.jar
	jaxrpc-spi.jar

saaj/lib:
	saaj-impl.jar
	saaj-api.jar

jwsdp-shared/lib:
	activation.jar
	mail.jar
	jax-qname.jar 

NOTE: There are some deployment issues which make it prohibitive to deploy
      the application over the internet. The major issue is that JAX-RPC 1.1 
      uses the xerces parser in the JWSDP_HOME/jaxp/lib/endorsed directory. 

Architecture
============
There are 4 main classes in the implementaion of this client:

AmazonProxy:	 
   Encapsualtes the stub classes to the web service.

DetailsTableModel: 
   An adapter for the web service data. Also contains the renderers
   and column specifications.

JAmazon:
   The main class. Constructs models, initiates web service connection,
   and constructs and lays out ui. 

RequestHandler: 
   Invokes the web service call and retrieves data on threads.

The other classes are some utility classes to enable table sorting and 
threading. 
     
Comments
========
Send comments to: mark.davidson@sun.com

