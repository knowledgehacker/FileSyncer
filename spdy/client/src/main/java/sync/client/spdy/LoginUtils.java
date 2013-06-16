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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.eclipse.jetty.spdy.api.HeadersInfo;
import org.eclipse.jetty.spdy.api.Stream;
import org.eclipse.jetty.spdy.api.StreamFrameListener;
import org.eclipse.jetty.spdy.api.ReplyInfo;
import org.eclipse.jetty.spdy.api.DataInfo;
import org.eclipse.jetty.spdy.api.PushInfo;
import org.eclipse.jetty.spdy.api.HeadersBlock;

import spdy.api.common.Constants;
import spdy.api.common.DataUtils;
import spdy.api.common.SpdyHttpHeader;
import spdy.api.common.SpdyHttpHeaderFactory;
import spdy.api.client.SPDYClientHelper;
import sync.client.FileStore;

//import sync.client.ClientSettings;

public class LoginUtils {
	private final SPDYClientHelper helper;

	private final short spdyVersion;
	private final String hostname;
	private final int port;
	
	public LoginUtils(SPDYClientHelper helper, short spdyVersion, String hostname, int port) {
		this.helper = helper;

		this.spdyVersion = spdyVersion;
		this.hostname = hostname;
		this.port = port;
	}
	
	public void auth(String userName, String password, String deviceId) {
		new AuthThread(userName, password, deviceId).start();
	}
	
	private class AuthThread extends Thread {
		private final String userName;
		private final String password;
		private final String deviceId;
		
		public AuthThread(String userName, String password, String deviceId) {
			this.userName = userName;
			this.password = password;
			this.deviceId = deviceId;
		}
		
		public void run() {
			// Authenticate should use SSL, to improvement.
			helper.connect(hostname, port, SPDYClientSettings.IDLE_TIMEOUT);

			String path = "/login.html";
			// TODO: generate a unique id to identify the device
			String data = "userName=" + userName + "&password=" + password + "&deviceId=" + deviceId;
			StreamFrameListener sfl = new MyPostStreamFrameListener();
			// TO IMPROVE: Know when the post request has been processed completely and get the result. 
			Stream stream = helper.createStream("POST", path, sfl);
			helper.sendData(stream, data, true);
		}
	}

	private class MyPostStreamFrameListener extends StreamFrameListener.Adapter {
		private final String methodName;
		private final String pathName;
		
		public MyPostStreamFrameListener() {
			SpdyHttpHeader spdyHttpHeader = SpdyHttpHeaderFactory.getInstance(spdyVersion);
			methodName = spdyHttpHeader.getMethod();
			pathName = spdyHttpHeader.getPath();
		}

		public void onReply(Stream stream, ReplyInfo replyInfo) {
			//System.out.println("MyPostStreamFrameListener.onReply...");
    	}

    	public void onData(Stream stream, DataInfo dataInfo) {
        	int dataLength = dataInfo.length();
        	byte[] data = new byte[dataLength];
        	dataInfo.consumeInto(data, 0, dataLength);
			String charsetName = "UTF-8";
			Charset charset = Charset.forName(charsetName);
			String response = new String(data, charset);
			//System.out.println("response: " + response);

        	if(response.startsWith("login status:")) {
				// notify SPDYSyncClient that "POST" request has been handled
        		synchronized(LoginSyncObj.kick) {
        			LoginSyncObj.kick.notify();
        		}
        	}
    	}
    	
    	public StreamFrameListener onPush(Stream stream, PushInfo pushInfo) {
            HeadersBlock headers = pushInfo.getHeaders();
            String method = headers.get(methodName);
            String path = headers.get(pathName);
			boolean isDir = headers.get("isdir").equals("true") ? true : false;

			String relPath = path.substring(1);
            File resource = new File(Constants.PUSH_DST_ROOT_DIR, relPath);
            System.out.println("LoginUtils.MyPostStreamFrameListener.onPush: resource = " + resource.getPath());
            if(method.equals("DELETE")) {
            	if(resource.exists())
            		resource.delete();
      
            	return null;
            }
            
            if(!resource.exists()) {
				if(isDir)
            		resource.mkdirs();
            	else {
            		File parent = resource.getParentFile();
            		if(!parent.exists())
            			parent.mkdirs();

				 	try {
						resource.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
            	FileStore.add(relPath);
              }
            return new MyPushStreamFrameListener(resource);
    	}
	}
	
	public class MyPushStreamFrameListener extends StreamFrameListener.Adapter {
		private static final int DEFAULT_BUFFER_SIZE = 65536;	// 64KB

		//private final File resource;
		private FileOutputStream fos;
		private byte[] data;
		private int pos;

		public MyPushStreamFrameListener(File resource) {
			//this.resource = resource; 
  
			try {
				fos = new FileOutputStream(resource, true);
			}catch(FileNotFoundException fnfe) {
				// TODO: handle exception "fnfe"
			}

			data = new byte[DEFAULT_BUFFER_SIZE];
			pos = 0;
		}

		// In response to a "PUT" request, the server stores the data into the specified file 
		public void onData(Stream stream, DataInfo dataInfo) {
			// Buffer all the data frames read in "data"
	        int dataLength = dataInfo.length();
			if((pos + dataLength) > DEFAULT_BUFFER_SIZE) {
				DataUtils.storeDataToFile(fos, data, pos);
				pos = 0;
			}
	        dataInfo.consumeInto(data, pos, dataLength);
	        pos += dataLength;
	        if(dataInfo.isClose() == true) {
				if((pos > 0) && (pos <= DEFAULT_BUFFER_SIZE))
					DataUtils.storeDataToFile(fos, data, pos);
				try {
					fos.close();
				}catch(IOException ioe) {
					// TODO: handle exception ioe
				}
			}
		}
		
		public void onHeaders(Stream stream, HeadersInfo headersInfo) {

	    }
	}
}
