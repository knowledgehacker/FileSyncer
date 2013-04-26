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

package sync.client.http;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import java.io.File;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;

import sync.client.ClientSetting;
import sync.client.Path;
import sync.client.FileSyncOps;
import sync.client.SyncClient;

public class HttpSyncClient extends SyncClient implements Runnable {
	private final String localDir;
	private final String localDirName;
	private final String host;
	private final int port;

	public HttpSyncClient(String localDir, FileSyncOps syncOps, String host, int port) {
		super(localDir, syncOps);

		this.localDir = localDir;
		this.localDirName = localDir.substring(localDir.lastIndexOf(File.separator)+1);
		this.host = host;
		this.port = port;
	}

	public boolean connect() {
		boolean result = false;

        HttpURLConnection conn = null;
        try {
            URL url = new URL("http", host, port, "/index.html");
			System.out.println("url = " + url);
            conn = (HttpURLConnection)url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
	
			OutputStream os = conn.getOutputStream();
            PrintWriter out = new PrintWriter(os);
			out.println(System.getProperty("user.name"));
			out.println(localDirName);
			out.close();
			
			DataInputStream dis = new DataInputStream(conn.getInputStream());
			result = dis.readBoolean();
			dis.close();
        }catch(MalformedURLException ex) {
            System.err.println(ex);
        }catch (IOException ex) {
            System.err.println(ex);
        }
        conn.disconnect();
		
		return result;
	}

	public void disconnect() {
		// nothing to do ...
	}

	public void run() {
        initialize();

        try{
            Thread.sleep(Integer.parseInt(ClientSetting.getProperty("check.delay.per.run"))*20);
        }catch(InterruptedException ie) {
        }

        finalize();
    }

    public static void main(String args[]) {
        if(args.length < 2) {
            System.out.println("Usage: java -jar HttpSyncClient.jar localdir host:port");
            return;
        }

		String server = args[1];
		int colon = server.indexOf(':');
		String host = server.substring(0, colon);
		int port = Integer.parseInt(server.substring(colon+1));
		String localDir = Path.normalize(args[0]);
		FileSyncOpsOverURL syncOps = new FileSyncOpsOverURL(localDir, host, port);
		//FileSyncOpsOverURLCompressed syncOps = new FileSyncOpsOverURLCompressed(localDir, host, port);
        Thread syncThread = new Thread(new HttpSyncClient(localDir, syncOps, host, port));
        syncThread.start();
    }
}
