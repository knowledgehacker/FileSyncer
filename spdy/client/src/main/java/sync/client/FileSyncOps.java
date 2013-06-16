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

import java.util.Date;
import java.util.Vector;
import java.util.Set;
import java.io.File;

/**
 * Synchronized file operations such as upload, add/remove, and update to remote server.
 */
public abstract class FileSyncOps {
	private final String root = ClientSettings.SYNC_DIR;

	// Record the files changed each run of update.
	private Vector<String> changedFiles;

	public FileSyncOps() {
		changedFiles = new Vector<String>();
	}

	/**
 	 * Upload all files in "root" to remote server.
 	 */
	public void upload() {
		File dir = new File(root);
		File[] dfs = dir.listFiles();
		for(int i = 0; i < dfs.length; ++i) {
			File df = dfs[i];
			if(df.isFile())
				addFile(df.getName());

			if(df.isDirectory())
				addDir(df.getName(), true);
		}
	}

	/**
	 * Add the file "relPath" to remote server.
	 */
	public void addFile(String relPath) {
		try{
			createFile(relPath);
		}catch(FileSyncOpException fsoe) {
			System.out.println("add: Create remote file " + relPath 
				+ " failed - " + fsoe);
			return;
		}
		System.out.println("File \"" + relPath + "\" created.");
	}

	/**
	 * Add the directory "relPath" to remote server.
	 * Add all directories/files in "relPath" recursively if passing true as "recursive".
	 */
	public void addDir(String relPath, boolean recursive) {
		try{
			mkdir(relPath);
		}catch(FileSyncOpException fsoe) {
			System.out.println("add: Create remote directory " + relPath 
				+ " failed - " + fsoe);
			return;
		}
		System.out.println("Directory \"" + relPath + "\" created.");

		File dir = new File(root, relPath);
		if(recursive) {
			File[] dfs = dir.listFiles();
			for(int i = 0; i < dfs.length; ++i) {
				File df = dfs[i];
				String dfRelPath = relPath + File.separator + df.getName();

				if(df.isFile()) 
					addFile(dfRelPath);

				if(df.isDirectory())
					addDir(dfRelPath, true);
			}
		}
	}

	/**
	 * Remove file or directory "relPath" in remote server
	 */
	public void remove(String relPath) {
		try{
			delete(relPath);
		}catch(FileSyncOpException fsoe) {
           System.out.println("removeFile: Remove remote file " + relPath
                + " failed - " + fsoe);
		}
	}
	
	/**
	 * Remove the file "relPath" in remote server.
	 */
	/*
	public void removeFile(String relPath) {
		try{
			deleteFile(relPath);
		}catch(FileSyncOpException fsoe) {
           System.out.println("removeFile: Remove remote file " + relPath
                + " failed - " + fsoe);
		}
	}
	*/

	/**
	 * Remove the directory "relPath" in remote server.
	 * Remove all directories/files in "relPath" recursively if passing true as "recursive".
	 */
	/*
	public void removeDir(String relPath, boolean recursive) {
		if(recursive) {
			File dir = new File(relPath);
			File[] dfs = dir.listFiles();
			for(int i = 0; i < dfs.length; ++i) {
				File df = dfs[i];
				String dfRelPath = relPath + File.separator + df.getName();

				if(df.isFile())
					removeFile(dfRelPath);

				if(df.isDirectory())
					removeDir(dfRelPath, true);
			}
		}

		try{
			rmdir(relPath);
		}catch(FileSyncOpException fsoe) {
           System.out.println("removeDir: Remove remote directory " + relPath
                + " failed - " + fsoe);
		}
	}
	*/
	
	/**
	 * Update all the directories or files modified since last check in "root".
	 * The last modified time of the files created since last check will be smaller than the last check time.
	 * So I pass the list of files created since last check here.
	 */ 
	public Vector<String> update(Date timestamp, Set<String> filesCreated) {
		changedFiles.clear();

		File dir = new File(root);
		File[] dfs = dir.listFiles();
		for(int i = 0; i < dfs.length; ++i) {
			File df = dfs[i];
			if(df.isFile()) {
				String fileName = df.getName();
				updateFile(fileName, timestamp, filesCreated);
			}

			if(df.isDirectory())
				updateDir(df.getName(), true, timestamp, filesCreated);
		}

		return changedFiles;
	}
	
	/**
	 * Update the file "relPath" in remote server.
	 */
	private void updateFile(String relPath, Date timestamp, Set<String> filesCreated) {
		if(filesCreated.contains(relPath))
			return;

		File file = new File(root, relPath);
		long lastModifiedTime = file.lastModified();
		if((lastModifiedTime <= timestamp.getTime()) 
				|| FileStore.removeIfPresent(relPath))
			return;

		try {
			upgradeFile(relPath);
		}catch(FileSyncOpException fsoe) {
			System.out.println("updateFile: Update remote file " + relPath + " failed.");
			return;
		}
		changedFiles.add(relPath);
		System.out.println("File \"" + relPath + "\" updated.");
	}
	
	/**
	 * Update the directory "relPath" in remote server.
	 * Update all directories/files in "relPath" recursively if passing true as "recursive".
	 */
	private void updateDir(String relPath, boolean recursive, Date timestamp, Set<String> filesCreated) {
		File dir = new File(root, relPath);
		long lastModifiedTime = dir.lastModified();
		if((lastModifiedTime <= timestamp.getTime())
			|| FileStore.removeIfPresent(relPath))
			return;

		if(recursive) {
			File[] dfs = dir.listFiles();
			for(int i = 0; i < dfs.length; ++i) {
				File df = dfs[i];
				String dfRelPath = relPath + File.separator + df.getName();
				
				if(df.isFile())
					updateFile(dfRelPath, timestamp, filesCreated);

				if(df.isDirectory())
					updateDir(dfRelPath, true, timestamp, filesCreated);
			}
		}
	}
	
	/**
	 * Upgrade the file in relative path "relPath" to remote server.
	 */
    public abstract void upgradeFile(String relPath) throws FileSyncOpException;

	/**
	 * Create a directory "relPath" in remote server.
	 */
    public abstract void mkdir(String relPath) throws FileSyncOpException;
	
	/**
	 * Create the file in relateive path "relPath" to remote server.
	 */
    public abstract void createFile(String relPath) throws FileSyncOpException;

	/**
	 * Delete the file or directory "relPath" in remote server.
	 */
    public abstract void delete(String relPath) throws FileSyncOpException;

	/**
	 * Check whether file or directory "relPath" exists in remote server or not.
	 */
    //public abstract boolean exists(String relPath) throws FileSyncOpException;
}
