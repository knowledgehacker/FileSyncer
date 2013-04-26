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

import java.net.URLConnection;

import java.io.File;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.BufferedInputStream;
//import java.io.InputStreamReader;
//import java.io.BufferedReader;
import java.util.zip.GZIPInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

// servlet API in J2ee
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

/**
 * Servlet "SyncOpServlet" is responsible for handling all sync requests.
 */
public class SyncOpServlet extends HttpServlet {
	private static final long serialVersionUID = 30299292994094L;
		
	private final int bufferSize;

	public SyncOpServlet() {
		bufferSize = 128*1024; // 128K
	}

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("SyncOpServlet.doGet() starts ...");

		boolean exists = false;

		String uri = req.getRequestURI();
		String relPath = ".." + File.separator + URI2Path(uri);
		File df = new File(HttpServerSetting.REPOSITORY, relPath);
		if(df.exists())
			exists = true;
		resp.setStatus(HttpServletResponse.SC_OK);
		DataOutputStream dos = new DataOutputStream(resp.getOutputStream());
		dos.writeBoolean(exists);
		dos.close();
    }
		
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("SyncOpServlet.doDelete() starts ...");
		
		String uri = req.getRequestURI();
		String relPath = ".." + File.separator + URI2Path(uri);
        File df = new File(HttpServerSetting.REPOSITORY, relPath);
		df.delete();
				
		/**
		 * We must send the response to client upon each request, otherwise doPost will block!!!
		 * Since request/response is synchronous, specified in HTTP protocol specification.
		 */
		resp.setStatus(HttpServletResponse.SC_OK);
		DataOutputStream dos = new DataOutputStream(resp.getOutputStream());
		dos.writeBoolean(true);
		dos.close();
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//System.out.println("SyncOpServlet.doPost() starts ...");

		String relPath = ".." + File.separator + URI2Path(req.getRequestURI());
		File df = new File(HttpServerSetting.REPOSITORY, relPath);

		InputStream is = req.getInputStream();
		DataInputStream dis = new DataInputStream(is);
		boolean isDir = dis.readBoolean();

		if(isDir)
			df.mkdir();
		else {
			/*
			String mime = URLConnection.guessContentTypeFromName(req.getRequestURI());
			if(mime == null)
				mime = "content/unkown";

			if(mime.startsWith("text/")) {
				BufferedReader ir = new BufferedReader(new InputStreamReader(is));
				FileWriter fw = new FileWriter(df);
                char[] buffer = new char[bufferSize];
                int charsRead = ir.read(buffer);
                while(charsRead != -1) {
                    fw.write(buffer, 0, charsRead);
                    charsRead = ir.read(buffer);
                }

				ir.close();
                fw.flush();
                fw.close();
			}else {
				BufferedInputStream bis = new BufferedInputStream(is);
                FileOutputStream fos = new FileOutputStream(df);
                byte[] buffer = new byte[bufferSize];
                int bytesRead = bis.read(buffer);
                while(bytesRead != -1) {
                    fos.write(buffer, 0, bytesRead);
                    bytesRead = bis.read(buffer);
                }

				bis.close();
                fos.flush();
                fos.close();
			}
			*/
			
			InputStream bis = new BufferedInputStream(is);
            FileOutputStream fos = new FileOutputStream(df);
            byte[] buffer = new byte[bufferSize];
            int bytesRead = bis.read(buffer);
            while(bytesRead != -1) {
                fos.write(buffer, 0, bytesRead);
                bytesRead = bis.read(buffer);
            }

			bis.close();
            fos.flush();
            fos.close();
		
	/*
			GZIPInputStream gis = new GZIPInputStream(is);
            FileOutputStream fos = new FileOutputStream(df);
            byte[] buffer = new byte[bufferSize];
            int bytesRead = gis.read(buffer);
            while(bytesRead != -1) {
                fos.write(buffer, 0, bytesRead);
                bytesRead = gis.read(buffer);
            }

			gis.close();
            fos.flush();
            fos.close();
	*/
		}

		dis.close();

		/**
		 * We must send the response to client upon each request, otherwise doPost will block!!!
		 * Since request/response is synchronous, specified in HTTP protocol specification.
		 */
		resp.setStatus(HttpServletResponse.SC_OK);
		DataOutputStream dos = new DataOutputStream(resp.getOutputStream());
		dos.writeBoolean(true);
		dos.close();
	}

	private String URI2Path(String uri) {
		char separator = File.separatorChar;
        StringBuilder path = new StringBuilder(uri);
        int index = uri.indexOf('/');
        while(index != -1) {
			path.setCharAt(index, separator);
            index = uri.indexOf('/', index+1);
        }

        return path.toString();
	}
}
