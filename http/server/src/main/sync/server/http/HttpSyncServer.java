package main.sync.server.http;

import org.eclipse.jetty.util.thread.QueuedThreadPool;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
//import org.eclipse.jetty.server.Request;
//import org.eclipse.jetty.server.Handler;
//import org.eclipse.jetty.server.handler.AbstractHandler;
//import org.eclipse.jetty.server.handler.ContextHandler;
//import org.eclipse.jetty.server.handler.ContextHandlerCollection;
//import org.eclipse.jetty.webapp.WebAppContext;

import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

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
		if(args.length < 2) {
			System.err.println("Illegal argument number!");
			System.out.println("Usage: mvn exec:java -Dexec.mainClass=sync.server.http.HttpSyncServer host port");

			return;
		}
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		System.out.println("host = " + host + ", port = " + port);

		Server server = new Server();
		
		SelectChannelConnector scc = new SelectChannelConnector();
		//scc.setHost(HttpServerSetting.getProperty("server.host"));
		//scc.setPort(Integer.parseInt(HttpServerSetting.getProperty("server.port")));
		scc.setHost(host);
		scc.setPort(port);
		scc.setRequestHeaderSize(8192);
		int maxIdleTime = Integer.parseInt(HttpServerSetting.getProperty("server.idletime.max"));
		scc.setMaxIdleTime(maxIdleTime);
		int threadPoolSize = Integer.parseInt(HttpServerSetting.getProperty("server.threadpool.size"));
		scc.setThreadPool(new QueuedThreadPool(threadPoolSize));

		server.setConnectors(new Connector[]{scc});

		ServletContextHandler syncOpContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
		syncOpContext.setContextPath("/");	
		// "setResourceBase" specifies location of static resouces which are served using ResourceHandler.
		//syncOpContext.setResourceBase(HttpServerSetting.REPOSITORY + "../../src");
		/*
		syncOpContext.addServlet(new ServletHolder(new ConnectServlet()), "/index.html");
		syncOpContext.addServlet(new ServletHolder(new SyncOpServlet()), "/repos");
		*/
		syncOpContext.addServlet(main.sync.server.http.ConnectServlet.class, "/index.html");
		syncOpContext.addServlet(main.sync.server.http.SyncOpServlet.class, "/repos/*");

		server.setHandler(syncOpContext);

		/*
		String webapp = "/home/minglin/workspace/FileSyncer/server/webapps";
        WebAppContext syncOpContext = new WebAppContext();
        syncOpContext.setDescriptor(webapp+"/WEB-INF/web.xml");
        syncOpContext.setResourceBase(webapp+"../src");
        syncOpContext.setContextPath("/");
        syncOpContext.setParentLoaderPriority(true);

		server.setHandler(syncOpContext);
		*/

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
