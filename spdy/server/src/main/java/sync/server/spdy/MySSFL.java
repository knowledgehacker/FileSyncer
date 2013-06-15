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

import java.io.File;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import org.eclipse.jetty.spdy.api.Stream;
import org.eclipse.jetty.spdy.api.StreamFrameListener;
import org.eclipse.jetty.spdy.api.SynInfo;
import org.eclipse.jetty.spdy.api.HeadersBlock;

import spdy.api.common.SpdyHttpHeaderFactory;
import spdy.api.common.SpdyHttpHeader;
import spdy.api.common.StreamUtils;
import spdy.api.server.MTServerSessionFrameListener;
import spdy.api.server.ServerPutStreamFrameListener;

public class MySSFL extends MTServerSessionFrameListener {
	private final Logger LOG = Log.getLogger(MySSFL.class);

	public MySSFL(short spdyVersion) {
		super(spdyVersion);
	}

	// Handle login and user authentication in "POST" request.
	protected StreamFrameListener handlePostRequest(Stream stream, SynInfo synInfo) {
		SpdyHttpHeader spdyHttpHeader = SpdyHttpHeaderFactory.getInstance(spdyVersion);
        HeadersBlock headers = synInfo.getHeaders();
        String path = headers.get(spdyHttpHeader.getPath());
        LOG.info("handlePostRequest - path: " + path);
	
		if(path.equals("/login.html")) {
			// Send back reply
			StreamUtils.sendReply(spdyVersion, stream, HeadersBlock.HTTP_STATUS_OK, false);
			
			return new MySPSFL(spdyVersion, headers);
		}else
			return null;
	}
	
	// Handle create file operation, query string "userName=xxx&deviceId=xxx&isDir=false"
	protected StreamFrameListener handlePutRequest(Stream stream, SynInfo synInfo) {
		SpdyHttpHeader spdyHttpHeader = SpdyHttpHeaderFactory.getInstance(spdyVersion);
        HeadersBlock headers = synInfo.getHeaders();
        String path = headers.get(spdyHttpHeader.getPath());
        LOG.info("handlePutRequest - path: " + path);

		StreamUtils.sendReply(spdyVersion, stream, HeadersBlock.HTTP_STATUS_OK, true);

        UDT udt = parse(path);
        String userName = udt.getUserName();
        String deviceId = udt.getDeviceId();
        String realPath = path.substring(0, path.indexOf('?'));
        UDIManager.syncToServer(userName, deviceId, realPath);
        /*
		String userName = path.substring(path.indexOf('=')+1, path.indexOf('&'));
		boolean isDir = path.substring(path.lastIndexOf('=')+1).equals("true") ? true : false;
		*/
        File resource = new File(ServerSettings.REPOSITORY + File.separatorChar + userName + realPath);
        boolean isDir = udt.isDir();
		if(isDir) {
			if(!resource.exists())
				resource.mkdir();
			
			// client sends some meaningless data to put stream in half-closed state, server discard the data simply
			return null;
		}else
			return new ServerPutStreamFrameListener(resource);
	}
	
	// Handle delete file operation, query string "userName=xxx&deviceId=xxx&isDir=false"
	protected StreamFrameListener handleDeleteRequest(Stream stream, SynInfo synInfo) {
		SpdyHttpHeader spdyHttpHeader = SpdyHttpHeaderFactory.getInstance(spdyVersion);
        HeadersBlock headers = synInfo.getHeaders();
        String path = headers.get(spdyHttpHeader.getPath());
        LOG.info("handleDeleteRequest - path: " + path);

        String userName = path.substring(path.indexOf('=')+1, path.indexOf('&'));
        File resource = new File(ServerSettings.REPOSITORY + File.separatorChar + userName
        		+ path.substring(0, path.indexOf('?')));
		if(resource.exists())
			resource.delete();
		StreamUtils.sendReply(spdyVersion, stream, HeadersBlock.HTTP_STATUS_OK, true);

		return null;
	}
	
	private UDT parse(String path) {
		String userName = null;
		String deviceId = null;
		boolean isDir = false;
		
		String queryStr = path.substring(path.indexOf('?')+1);
	
		boolean done = false;
		String segment = null;
		int start = 0;
		int end = queryStr.indexOf('&');
		while(!done) {
			if(end == -1) {
				segment = queryStr.substring(start);
				done = true;
			}else
				segment = queryStr.substring(start, end);
			int equalidx = segment.indexOf('=');
			String key = segment.substring(0, equalidx);
			String value = segment.substring(equalidx+1);
			if(key.equals("userName"))
				userName = value;
			else if(key.equals("deviceId"))
				deviceId = value;
			else if(key.equals("isDir"))
				isDir = value.equals("true") ? true : false;
			
			start = end+1;
			end = queryStr.indexOf('&', start);
		}
		
		return new UDT(userName, deviceId, isDir);
	}
	
	private class UDT {
		private final String userName;
		private final String deviceId;
		private final boolean isDir;
		
		public UDT(String userName, String deviceId, boolean isDir) {
			this.userName = userName;
			this.deviceId = deviceId;
			this.isDir = isDir;
		}
		
		public final String getUserName() {
			return userName;
		}
		
		public final String getDeviceId() {
			return deviceId;
		}
		
		public final boolean isDir() {
			return isDir;
		}
	}
}
