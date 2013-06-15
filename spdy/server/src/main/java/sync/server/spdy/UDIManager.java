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

/*
 * User Device Information Manager.
 */
public class UDIManager {
	public static final int USER_NOT_EXIST = 1;
	public static final int USERNAME_PASSWORD_NOT_MATCH = 2;
	public static final int DEVICE_NOT_EXIST = 3;
	public static final int DEVICE_ALREADY_EXIST = 4;

	private static final Map<String, UDI> uis = new HashMap<String, UDI>();

	/*
	 * If user "userName" doesn't exist, create a UDI instance for it.
     */
	public static synchronized int auth(String userName, String password, String deviceId, 
			short spdyVersion, Stream stream, HeadersBlock headers) {
		UDI udi = uis.get(userName);
		if(udi == null) {
			udi = new UDI(password);
			udi.add(deviceId, spdyVersion, stream, headers);
			uis.put(userName, udi);
			// create a directory for user "userName"
			File root = new File(ServerSettings.REPOSITORY, userName);
			root.mkdir();

			return USER_NOT_EXIST;
		}else {
			if(!password.equals(udi.getPassword()))
				return USERNAME_PASSWORD_NOT_MATCH;
			else {
				if(!udi.exists(deviceId))
					return DEVICE_NOT_EXIST;
				else
					return DEVICE_ALREADY_EXIST;
			}
		}
	}
	
	public static synchronized void syncToServer(String userName, String deviceId, String file) {
		UDI udi = uis.get(userName);
		UDI.DI di = udi.getDIS().get(deviceId);
		Vector<String> syncFiles = di.getSyncFiles();
		syncFiles.add(file);
	}

	// synchronize files from device "deviceid" of user "userName" to server 
	public static synchronized void syncToServer(String userName, String deviceId, Vector<String> files) {
		UDI ui = uis.get(userName);
		UDI.DI di = ui.getDIS().get(deviceId);
		Vector<String> syncFiles = di.getSyncFiles();
		syncFiles.addAll(files);
	}

	// synchronize files from other devices to device "deviceid" of user "userName"
	public static synchronized void syncToDevice(String userName, String deviceId) {
		UDI ui = uis.get(userName);
		Map<String, UDI.DI> dis = ui.getDIS();
		Set<Map.Entry<String, UDI.DI>> entries = dis.entrySet();
		for(Map.Entry<String, UDI.DI> entry: entries) {
			String key = entry.getKey();
			UDI.DI value = entry.getValue();
			if(!key.equals(deviceId)) {
				Vector<String> syncFiles = value.getSyncFiles();
				value.push(key, syncFiles);
			}
		}
	}
}
