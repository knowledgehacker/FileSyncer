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
