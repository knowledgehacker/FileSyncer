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

import java.util.Vector;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

/*
 * User Device Information
 */
public class UDI {
	private final String password;
	private final Set<String> devices;
	private final Map<String, DI> dis;
	
	public UDI(String password) {
		this.password = password;
		devices = new HashSet<String>();
		dis = new HashMap<String, DI>();
	}

	public final String getPassword() {
		return password;
	}

	public final Map<String, DI> getDIS() {
		return dis;
	}

	public synchronized DI get(String deviceId) {
		return dis.get(deviceId);
	}

	public synchronized void add(String deviceId) {
		DI di = new DI(devices);
		devices.add(deviceId);
		dis.put(deviceId, di);
	}

	 // the device will be removed if expired
	public synchronized void remove(String deviceId) {
		devices.remove(deviceId);
		dis.remove(deviceId);
	}

	public synchronized boolean exists(String deviceId) {
		return devices.contains(deviceId);
	}

	/*
	 * Device Information
	 * syncFiles stores all the files sync from this device.
	 * syncPoints records the last sync points of other devices(excluding this device).
	 */
	public static class DI {
		private final Vector<SyncInfo> mySyncFiles;
		private final Map<String, Integer> syncPoints;
		
		public DI(Set<String> devices) {
			mySyncFiles = new Vector<SyncInfo>();
			syncPoints = new HashMap<String, Integer>();
			for(String device: devices)
				syncPoints.put(device, 0);
		}

		public final Vector<SyncInfo> getSyncFiles() {
			return mySyncFiles;
		}

		public final void addSyncFiles(Vector<SyncInfo> files) {
			mySyncFiles.addAll(files);
		}

		public final void addSyncFile(SyncInfo file) {
			mySyncFiles.add(file);
		}

		public final Map<String, Integer> getSyncPoints() {
			return syncPoints;
		}
		
		public static class SyncInfo {
			private final String method;
			private final String path;
			private final boolean isDir;
			
			public SyncInfo(String method, String path, boolean isDir) {
				this.method = method;
				this.path = path;
				this.isDir = isDir;
			}
			
			public final String getMethod() {
				return method;
			}
			
			public final String getPath() {
				return path;
			}

			public final boolean isDir() {
				return isDir;
			}
		}
	}
}
