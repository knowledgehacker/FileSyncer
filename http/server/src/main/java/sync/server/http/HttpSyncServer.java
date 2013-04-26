/**
 * Copyright (c) 2013 minglin. All rights reserved.

 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package sync.server.http;

import java.net.URL;
import org.xml.sax.SAXException;
import java.io.IOException;

import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.xml.XmlConfiguration;

import org.eclipse.jetty.server.Server;
/*
import org.eclipse.jetty.server.Connector;

import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
*/

//import main.sync.server.LogWriter;

/**
 * Currently we ignore user registration here.
 * I assume a directory with the user name "user" is created in remote server when a user registers.
 *
 * Afterwards, requests for a client are handled by two consecutive phases:
 * Phase 1: connect request, it is handled by servlet "ConnectServlet".
 * User connects to the server using syntax like "user/dir host:port". 
 * Upon connect request, servlet "ConnectServlet" checks whether the directory "user/dir" exists
 * or not, if not, creates a directory for it.
 * Phase 2: sync requests, they are handled by servlet "SyncOpServlet".
 * All subsequent sync requests directed to directory "user/dir" are handled by servlet "SyncOpServlet".
 *
 * Refer to sections "Creating a Server", "Configuring Connectors", "Setting Contexts", "Creating Servlets",
 * "Setting a ServletContext", and "Configuring a Context Handler Collection" in tutorial "Embedding Jetty" 
 * for more detailed information. http://wiki.eclipse.org/Jetty/Tutorial/Embedding_Jetty.
 */
public class HttpSyncServer {
	//private static UserDatabase userDb = new UserDatabase();

	//private static LogWriter logWriter;

	static {
		/*
		try {
			logWriter = new LogWriter(reposLoc, false);
		}catch(FileNotFoundException fnfe) {
			System.err.println(fnfe);
		}catch(IOException ioe) {
			System.err.println(ioe);
		}
		*/
	}

	public HttpSyncServer() {
	}
	
	public static void main(String[] args) {
		URL jettyConfig = HttpSyncServer.class.getResource("/jetty.xml");
        XmlConfiguration configuration = null;
		try{
			configuration = new XmlConfiguration(jettyConfig);
		}catch(SAXException asxe) {
			System.err.println(asxe);

			return;
		}catch(IOException ioe) {
			System.err.println(ioe);
	
			return;
		}

        Server server = null;
		try{
			server = (Server)configuration.configure();
		}catch(Exception e) {
			System.err.println(e);

			return;
		}

		try {
			server.start();
		}catch(Exception e) {
			System.err.println("Start server failed - " + e);
		}
		try {
			server.join();
		}catch(InterruptedException ie) {
			System.err.println("Wait for server to terminate failed - " + ie);
		}
	}
}
