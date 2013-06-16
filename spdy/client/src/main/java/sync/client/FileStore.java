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

package sync.client;

import java.util.Set;
import java.util.HashSet;

/*
 * FileStore records all files synchronized from other devices
 * Each time before we try to synchronize a file in this device to the server,
 * we check whether it exists in the FileStore or not, if it is, it means this file 
 * is a file synchronized from other devices, not a new file, we should ignore it.
 * 
 * FileStore is in memory, so when the client terminates, all the files in FileStore
 * will be gone. So if the client terminates abnormally after some files have been 
 * synchronized from other devices, but before the next round of Monitor task begins.
 * Then all the files in FileStore and sync operations such as changed, created, and 
 * deleted are also gone.
 * 
 * In other way, this implementation of file syncer works only in the case that all
 * clients terminate after all file sync operations completed.
 */
public class FileStore {
	public static final Set<String> alienFiles = new HashSet<String>();
	
	public static final synchronized void add(String alienFile) {
		alienFiles.add(alienFile);
	}
	
	public static final synchronized boolean removeIfPresent(String alienFile) {
		boolean isPresent = alienFiles.contains(alienFile);
		if(isPresent)
			alienFiles.remove(alienFile);
		
		return isPresent;
	}
}
