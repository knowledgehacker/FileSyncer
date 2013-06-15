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

import org.eclipse.jetty.spdy.api.Stream;

import spdy.api.client.SPDYClientHelper;

import sync.client.ClientSettings;
import sync.client.FileSyncOps;
import sync.client.FileSyncBaseOpsException;

public class FileSyncOpsOverSPDY extends FileSyncOps {
	private final SPDYClientHelper helper;
	
	private final String userName;
	private final String deviceId;

	public FileSyncOpsOverSPDY(SPDYClientHelper helper, String userName, String deviceId) {
		this.helper = helper;
		
		this.userName = userName;
		this.deviceId = deviceId;
	}

	// TODO: get user id, the user id is retrieved from server
	private final String decoratePath(String path, boolean isDir) {
		return '/' + path + "?userName=" + userName + "&deviceId=" + deviceId + "&isDir=" + (isDir ? "true" : "false");
	}

	private final void create(String path, boolean isDir) throws FileSyncBaseOpsException {	
		String fatPath = decoratePath(path, isDir);
		MyPutStreamFrameListener sfl = new MyPutStreamFrameListener(path, isDir);
		Stream stream = helper.createStream("PUT", fatPath, sfl);
		if(!isDir) {
			File file = new File(ClientSettings.SYNC_DIR, path);
			helper.sendFile(stream, file);
		}else {
			// TODO: as to directory, what can we send?
			// If we do not send something, the client side of the stream will stay open.
			// As a workaround, some data that is meaningless is sent, server can discard it.
			String data = "nonsense";
			helper.sendData(stream, data, true);
		}
	}
	
	private final void remove(String path, boolean isDir) {
		String fatPath = decoratePath(path, isDir);
		MyDeleteStreamFrameListener sfl = new MyDeleteStreamFrameListener(path, isDir);
		helper.createStream("DELETE", fatPath, sfl);		
	}

	public void createFile(String path) throws FileSyncBaseOpsException {
		System.out.println("createFile starts ...");
		create(path, false);
	}

	public void upgradeFile(String path) throws FileSyncBaseOpsException {
		System.out.println("upgradeFile starts ...");
		// TO IMPROVE: only update the part of the file changed
		create(path, false);
	}

	public void mkdir(String path) throws FileSyncBaseOpsException {
		System.out.println("mkdir starts ...");
		create(path, true);	
	}
	
	public void deleteFile(String path) throws FileSyncBaseOpsException {
		System.out.println("deleteFile starts ...");
		remove(path, false);
	}
	
	public void rmdir(String path) throws FileSyncBaseOpsException {
		System.out.println("rmdir starts");
		remove(path, true);
	}
/*
	public boolean exists(String relPath) throws FileSyncBaseOpsException {
		System.out.println("exists starts ...");

		return res;
	}
*/
}
