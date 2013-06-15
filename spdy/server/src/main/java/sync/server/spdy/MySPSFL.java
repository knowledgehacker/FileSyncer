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

package sync.server.spdy;

import java.nio.charset.Charset;

import org.eclipse.jetty.spdy.api.Stream;
import org.eclipse.jetty.spdy.api.StreamFrameListener;
import org.eclipse.jetty.spdy.api.DataInfo;
import org.eclipse.jetty.spdy.api.HeadersBlock;

import spdy.api.common.StreamUtils;

public class MySPSFL extends StreamFrameListener.Adapter {
	private static final int DEFAULT_BUFFER_SIZE = 65536;

	private final short spdyVersion;
	private final HeadersBlock headers;
	//private final File resource;
	private byte[] data;
	private int pos;

	public MySPSFL(short spdyVersion, HeadersBlock headers) {
		this.spdyVersion = spdyVersion;
		this.headers = headers;
		//this.resource = resource;

		data = new byte[DEFAULT_BUFFER_SIZE];
		pos = 0;
	}

	public void onData(Stream stream, DataInfo dataInfo) {
		// Buffer all the data frames read in "data"
        int dataLength = dataInfo.length();
        dataInfo.consumeInto(data, pos, dataLength);
        pos += dataLength;
        if(dataInfo.isClose() == true) {
			// TODO: handle posted data
			UPD upd = parsePostedData(data, pos);
			String userName = upd.getUserName();
			String password = upd.getPassword();
			String deviceId = upd.getDeviceId();
			//System.out.println("username: " + userName);
			//System.out.println("password: " + password);
			//System.out.println("deviceid: " + deviceId);

			String response = "login status: ";
			int result = UDIManager.auth(userName, password, deviceId, spdyVersion, stream, headers);	
			switch(result) {
				case UDIManager.USERNAME_PASSWORD_NOT_MATCH:
					// authenticate failed
					response += "user name and password do not match";
					StreamUtils.sendData(stream, response, true);
					break;

				case UDIManager.USER_NOT_EXIST:
					response += "user " + userName + " does not exist";
					StreamUtils.sendData(stream, response, true);
					break;

				case UDIManager.DEVICE_NOT_EXIST:
				case UDIManager.DEVICE_ALREADY_EXIST:
					response += "needs sync from other devices, starts...";
					StreamUtils.sendData(stream, response, false);
					
					UDIManager.syncToDevice(userName, deviceId);
					
					// TODO: listens on sync operations, push files sync from other devices immediately
					break;

				default:
					System.err.println("Invalid status code!");
			}
		}
	}

	// Parse posted data in format "userName=xxx&password=xxx&deviceId=xxx"
	private UPD parsePostedData(byte[] data, int length) {
		String userName = null;
		String password = null;
		String deviceId = null;
		
        String charsetName = "UTF-8";
        Charset charset = Charset.forName(charsetName);
        String str = new String(data, 0, length, charset);
	
		boolean done = false;
		String segment = null;
		int start = 0;
		int end = str.indexOf('&');
		while(!done) {
			if(end == -1) {
				segment = str.substring(start);
				done = true;
			}else
				segment = str.substring(start, end);
			int equalidx = segment.indexOf('=');
			String key = segment.substring(0, equalidx);
			String value = segment.substring(equalidx+1);
			if(key.equals("userName"))
				userName = value;
			else if(key.equals("password"))
				password = value;
			else if(key.equals("deviceId"))
				deviceId = value;

			start = end+1;
			end = str.indexOf('&', start);
		}
		
		return new UPD(userName, password, deviceId);
	}
	
	private class UPD {
		private final String userName;
		private final String password;
		private final String deviceId;
		
		public UPD(String userName, String password, String deviceId) {
			this.userName = userName;
			this.password = password;
			this.deviceId = deviceId;
		}
		
		public final String getUserName() {
			return userName;
		}
		
		public final String getPassword() {
			return password;
		}
		
		public final String getDeviceId() {
			return deviceId;
		}
	}
}
