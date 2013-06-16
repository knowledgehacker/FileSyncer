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

package sync.client.spdy;

import java.io.Console;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.spdy.api.SPDY;

import sync.client.FileSyncOps;
import sync.client.SyncClient;
//import sync.client.spdy.LoginUI;

import spdy.api.client.SPDYClientHelper;

public class SPDYSyncClient extends SyncClient {
	//private LoginUI loginUI;
	private String userName;
	private String password;
	private String deviceId;
	
	private final short spdyVersion;
	private final String hostname;
	private final int port;

	private SPDYClientHelper helper;
	
	private boolean isConnected;

	public SPDYSyncClient(short spdyVersion, QueuedThreadPool threadpool, String hostname, int port) {
		this.spdyVersion = spdyVersion;
		this.hostname = hostname;
		this.port = port;

		helper = new SPDYClientHelper(spdyVersion, threadpool);
        isConnected = false;
	}

	public boolean connect() {
        helper.connect(hostname, port, SPDYClientSettings.IDLE_TIMEOUT);
        
		// TODO: check whether connect successfully
        isConnected = true;
        
		return true;
	}

	public void disconnect() {
		// Nothing has to do
	}

	public FileSyncOps newFileSyncOps() {
		if(!isConnected) {
			// TODO: throw an exception
		}

		//return new FileSyncOpsOverSPDY(helper, loginUI.getUserName(), loginUI.getDeviceId());
		return new FileSyncOpsOverSPDY(helper, userName, deviceId);
	}

	public boolean login() {
		//loginUI = new LoginUI(spdyVersion, hostname, port);
		//loginUI.show();
		LoginUtils loginUtils = new LoginUtils(helper, spdyVersion, hostname, port);

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
       System.out.print("User name: ");
       try {
    	   userName = br.readLine();
       } catch (IOException e) {
    	   // TODO Auto-generated catch block
    	   e.printStackTrace();
        }
      
		/*
       System.out.print("Password: ");
       try {
    	   password = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		Console console = null;
 		char[] passwd;
 		if ((console = System.console()) != null &&(passwd = console.readPassword("%s", "Password: ")) != null) {
			password = new String(passwd);
     		java.util.Arrays.fill(passwd, ' ');
 		}
       
       System.out.print("Device id: ");
		try {
			deviceId = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		loginUtils.auth(userName, password, deviceId);
		synchronized(LoginSyncObj.kick) {
			try {
				LoginSyncObj.kick.wait();
			}catch(InterruptedException ie) {
				// TODO: handle exception "ie"
			}
		}
		System.out.println("Login succeeded!");
		
		// TODO: check user authentication succeeds or fails
		return true;
	}

	public void logout() {
		// nothing to do ...
	}


    public static void main(String args[]) {
    	short spdyVersion = SPDY.V3;
    	
    	/*
		int argNum = args.length;
        if(argNum < 2) {
			System.err.println("Invalid argument number: " + argNum);
            System.out.println("Usage: java -jar HttpSyncClient.jar hostname port");

            return;
        }
		
		String hostname = args[0];
		int port = Integer.parseInt(args[1]);
		*/
		
		String hostname = System.getProperty("hostname");
		int port = Integer.parseInt(System.getProperty("port"));
		
		QueuedThreadPool threadpool = new QueuedThreadPool(200, 20);
        SPDYSyncClient client = new SPDYSyncClient(spdyVersion, threadpool, hostname, port);
        client.start();
    }
}
