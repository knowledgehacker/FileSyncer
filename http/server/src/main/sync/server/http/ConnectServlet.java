package main.sync.server.http;

import java.util.HashSet;

import java.io.File;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;

// servlet API in J2ee
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

/**
 * ConnectServlet is responsible for handling connect requests.
 */
public class ConnectServlet extends HttpServlet {
	private static final long serialVersionUID = 40299292994094L;

	//private HashSet<String> dirs;

	public ConnectServlet() {
		//dirs = new HashSet<String>();
	}
    
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	System.out.println("ConnectServlet.doPost() starts ...");

        boolean alreadyExists = true;

        BufferedReader ir = req.getReader();
        String user = ir.readLine();
        String dir = ir.readLine();
        ir.close();
        //System.out.println("user = " + user + ", dir = " + dir);
    
/*
        String root = user + File.separator + dir;
        if(!dirs.contains(root)) {
            alreadyExists = false;
                
        	File rootDir = new File(HttpServerSetting.REPOSITORY, root);
            rootDir.mkdir();

            dirs.add(root);
        }
*/

		File userDir = new File(HttpServerSetting.REPOSITORY, user);
		if(!userDir.exists())
			userDir.mkdir();
	
		File rootDir = new File(userDir, dir);
		if(!rootDir.exists()) {
			alreadyExists = false;

			rootDir.mkdir();
		}

		resp.setStatus(HttpServletResponse.SC_OK);
		DataOutputStream dos = new DataOutputStream(resp.getOutputStream());
		dos.writeBoolean(alreadyExists);
		dos.close();
    }
}
