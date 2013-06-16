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
import java.util.Vector;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import org.eclipse.jetty.spdy.api.Stream;
import org.eclipse.jetty.spdy.api.HeadersBlock;

import spdy.api.common.SpdyHttpHeader;
import spdy.api.common.SpdyHttpHeaderFactory;
import spdy.api.common.StreamUtils;

/*
 * User Device Information Manager.
 */
public class UDIManager {
	public static final int USER_NOT_EXIST = 1;
	public static final int USERNAME_PASSWORD_NOT_MATCH = 2;
	public static final int DEVICE_NOT_EXIST = 3;
	public static final int DEVICE_ALREADY_EXIST = 4;
	
	public static final short PUT_OP = 1;
	public static final short DELETE_OP = 2;

	private static final Map<String, UDI> uis = new HashMap<String, UDI>();

	// Just for test, add a ficitious device before any device connects to the sever
	static {
		/*
		String userName = "ml";
		String password = "123";
		String deviceId = "YYY";
		UDI udi = new UDI(password);
		udi.add(deviceId);
		uis.put(userName, udi);

		UDI.DI di = udi.get(deviceId);
		UDI.DI.SyncInfo si1 = new UDI.DI.SyncInfo("PUT", "ficitious/server_1.txt");
		UDI.DI.SyncInfo si2 = new UDI.DI.SyncInfo("PUT", "ficitious/server_2.txt");
		di.addSyncFile(si1);
		di.addSyncFile(si2);
		*/
	}

	/*
	 * If user "userName" doesn't exist, create a UDI instance for it.
     */
	public static synchronized int auth(String userName, String password, String deviceId) {
		UDI udi = uis.get(userName);
		if(udi == null) {
			udi = new UDI(password);
			udi.add(deviceId);
			uis.put(userName, udi);
			// create a directory for user "userName"
			File root = new File(ServerSettings.REPOSITORY, userName);
			if(!root.exists())
				root.mkdir();

			return USER_NOT_EXIST;
		}else {
			if(!password.equals(udi.getPassword()))
				return USERNAME_PASSWORD_NOT_MATCH;
			else {
				if(!udi.exists(deviceId)){
					udi.add(deviceId);
					
					return DEVICE_NOT_EXIST;
				}else
					return DEVICE_ALREADY_EXIST;
			}
		}
	}
	
	public static synchronized void syncToServer(String userName, String deviceId, String file, boolean isDir, String method) {
		//System.out.println("UDIManager.syncToServer: method = " + method);
		UDI udi = uis.get(userName);
		UDI.DI di = udi.get(deviceId);
		di.addSyncFile(new UDI.DI.SyncInfo(method, file, isDir));
	}

	// synchronize files from device "deviceid" of user "userName" to server 
	public static synchronized void syncToServer(String userName, String deviceId, Vector<UDI.DI.SyncInfo> files) {
		UDI udi = uis.get(userName);
		UDI.DI di = udi.get(deviceId);
		di.addSyncFiles(files);
	}

	// synchronize files from other devices to device "deviceId" of user "userName"
	public static synchronized void syncToDevice(String userName, String deviceId, short spdyVersion, Stream stream, HeadersBlock headers) {
		SpdyHttpHeader spdyHttpHeader = SpdyHttpHeaderFactory.getInstance(spdyVersion);
		String methodName = spdyHttpHeader.getMethod();
		
		UDI udi = uis.get(userName);
		UDI.DI di = udi.get(deviceId);
		Map<String, Integer> syncPoints = di.getSyncPoints();
		
		Map<String, UDI.DI> dis = udi.getDIS();
		Set<Map.Entry<String, UDI.DI>> entries = dis.entrySet();
		for(Map.Entry<String, UDI.DI> entry: entries) {
			String key = entry.getKey();
			UDI.DI value = entry.getValue();
			if(!key.equals(deviceId)) {
				Vector<UDI.DI.SyncInfo> syncFiles = value.getSyncFiles();
				if(syncPoints.containsKey(key)) {
					int syncPoint = syncPoints.get(key);
					int syncFileNum = syncFiles.size();
					for(int i = syncPoint; i < syncFileNum; ++i) {
						UDI.DI.SyncInfo si = syncFiles.get(i);
						
						String method = si.getMethod();
						headers.add(methodName, method);
						String isDir = (si.isDir() == true) ? "true" : "false";
						/* 
						 * Remember all headers in SPDY should be lower-case. 
						 * So we should use "isdir" instead of "isDir" here.
						 */
						headers.add("isdir", isDir);
						StreamUtils.push(spdyVersion, stream, headers, si.getPath());
					}
				}
			}
		}
	}
}
