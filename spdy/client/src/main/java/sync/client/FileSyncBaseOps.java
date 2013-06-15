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

/**
 * Base operations for file synchronization.
 * Currently the synchronization is uni-directional, from local hosts to a centralized server.
 */
public abstract class FileSyncBaseOps {
	/**
	 * Upgrade the file in relative path "relPath" to remote server.
	 */
    public abstract void upgradeFile(String relPath) throws FileSyncBaseOpsException;

	/**
	 * Create a directory "relPath" in remote server.
	 */
    public abstract void mkdir(String relPath) throws FileSyncBaseOpsException;
	
	/**
	 * Delete the directory "relPath" in remote server.
	 */
    public abstract void rmdir(String relPath) throws FileSyncBaseOpsException;

	/**
	 * Create the file in relateive path "relPath" to remote server.
	 */
    public abstract void createFile(String relPath) throws FileSyncBaseOpsException;

	/**
	 * Delete the file "relPath" in remote server.
	 */
	public abstract void deleteFile(String relPath) throws FileSyncBaseOpsException;

	/**
	 * Check whether file or directory "relPath" exists in remote server or not.
	 */
    //public abstract boolean exists(String relPath) throws FileSyncBaseOpsException;
}
