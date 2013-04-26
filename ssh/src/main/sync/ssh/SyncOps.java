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

package main.sync.ssh;

import java.util.Date;
import java.util.Vector;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

/**
 * Synchronized file operations such as upload, add/remove,
 * and update from local filesystem to remote server.
 */
public class SyncOps {
	private final String localDir;
	private final String remoteDir;	
	private final ChannelSftp channel;

	// Record the files changed each run of update.
	private Vector<String> changedFiles;

	public SyncOps(String localDir, String remoteDir, ChannelSftp channel) {
		this.localDir = localDir;
		this.remoteDir = remoteDir;
		this.channel = channel;

		changedFiles = new Vector<String>();
	}

	/**
 	 * Upload all files from "localDir" in local filesystem to "remoteDir" in remote filesystem.
 	 */
	public void upload() {
		File dir = new File(localDir);
		File[] files = dir.listFiles();
		for(int i = 0; i < files.length; ++i)
			upload(files[i].getName());
	}

	/*
	 * Upload a local directory or file "dfName" relative to "localDir" to 
	 * corresponding remote location relative to "remoteDir".
	 */
	private void upload(String dfName) {
		String relPath = remoteDir + Config.PATH_SEPARATOR + dfName;
		File df = new File(localDir, dfName);
		if(df.isFile() && uploadFile(df, relPath))
			System.out.println("File \"" + relPath + "\" created.");
			
		if(df.isDirectory()) {
			try{
				channel.mkdir(relPath);
			}catch(SftpException sfe) {
				System.out.println("FileUploader.uploadRecursively: Create remote directory " 
					+ relPath + " failed - " + sfe);
				return;
			}
			System.out.println("Directory \"" + relPath + "\" created.");

			String[] files = df.list();
			for(int i = 0; i < files.length; ++i) {
				String file = files[i];
				upload(dfName + Config.PATH_SEPARATOR + file.substring(file.lastIndexOf('/')+1));
			}
		}
	}

	private boolean uploadFile(File file, String relPath) {
		FileInputStream fis = null;
		try{
			fis = new FileInputStream(file);
		}catch(FileNotFoundException fnfe) {
			System.out.println("uploadFile: upload file " + file.getAbsolutePath() 
				+ " failed - " + fnfe);
			return false;
		}

		try{
			channel.put(fis, relPath);
		}catch(SftpException sfe) {
			System.out.println("uploadFile: Upload file " + file.getAbsolutePath() 
				+ " failed - " + sfe);
			return false;
		}finally {
			try{
				fis.close();
			}catch(IOException ioe) {
				System.out.println("uploadFile: Close fis failed - " + ioe);
			}			
		}

		return true;
	}

	/**
	 * Add direcotry or file represented by "dfName" to remote filesystem.
	 * @dfName: directory or file to add.
	 * @recursive: if it is set to true, and "dfName" represents a directory,
	 * then directories and files in directory "dfName" will be added recursively.
	 * otherwise only the directory or file represented by "dfName" itself is added.
	 */
	public void add(String dfName, boolean recursive) {
		String relPath = createRelPath(dfName);
		try {
			if(exists(relPath))
				return;
		}catch(SftpException sfe) {
			System.out.println("update: Check whether remote directory or file "
				+ relPath + " exists or not failed - " + sfe);
		}

		File df = new File(localDir, dfName);
		if(df.isDirectory()) {
			if(recursive)
				upload(dfName);
			else{
				try{
					channel.mkdir(relPath);
				}catch(SftpException sfe) {
					System.out.println("add: Create directory " + relPath + " failed - "
						+ sfe);
					return;
				}
			}
			//System.out.println("Directory \"" + relPath + "\" added.");
		}

		if(df.isFile() && uploadFile(df, relPath)) {
			//System.out.println("File \"" + relPath + "\" added.");
		}
	}

	public void remove(String dfName, boolean recursive) {
		String relPath = createRelPath(dfName);
		try {
			if(!exists(relPath))
				return;
		}catch(SftpException sfe) {
			System.out.println("update: Check whether remote directory or file "
				+ relPath + " exists or not failed - " + sfe);
		}

		if(isRemoteDir(relPath)) {
			if(recursive)
				removeRecursively(dfName);
			else
				try{
					channel.rmdir(relPath);
				}catch(SftpException sfe) {
					System.out.println("remove: Remove remote directory "
						+ relPath + " failed - " + sfe);
					return;
				}
			//System.out.println("Directory \"" + relPath + "\" removed.");
		}else {
			try{
				channel.rm(relPath);
			}catch(SftpException sfe) {
				System.out.println("remove: Remove remote file " + relPath + " failed - "
					+ sfe);
				return;
			}
			//System.out.println("File \"" + relPath + "\" removed.");	
		}
	}

	/**
	 * Here we still pass path relative to "remoteDir" as the paramter for consistency.
	 * It is more convenient to handle removing directly in remote filesystem
	 * by passing path relative to remote home directory.
	 */
	@SuppressWarnings("unchecked")
	private void removeRecursively(String dirName) {
		String relPath = createRelPath(dirName);
		Vector<ChannelSftp.LsEntry> lsEntries = null;
		try{
			lsEntries = channel.ls(relPath);
		}catch(SftpException sfe) {
			System.out.println("removeRecursively: List in remote directory \"" 
				+ relPath + "\" failed - " + sfe);
			return;
		}

		for(int i = 0; i < lsEntries.size(); ++i) {
			ChannelSftp.LsEntry lsEntry = lsEntries.get(i);
			String filename = lsEntry.getFilename();
			if(filename.equals(".") || filename.equals(".."))
				continue;
			if(channel.isRemoteDir(relPath + Config.PATH_SEPARATOR + filename))
				removeRecursively(dirName + Config.PATH_SEPARATOR + filename);
			else
				try{
					channel.rm(relPath + Config.PATH_SEPARATOR + filename);
				}catch(SftpException sfe) {
					System.out.println("removeRecursively: Remove remote file \""
						+ filename + "\" in directory " + relPath + " failed - " + sfe);
				}
		}

		try {
			channel.rmdir(relPath);
		}catch(SftpException sfe) {
			System.out.println("removeRecursively: Remove remote directory \"" 
				+ relPath + "\" failed - " + sfe);
			return;
		}
	}

	/**
	 * Update all the directories or files modified since last update in "localDir".
	 * As to adding/removing the directories or files, call "add" or "remove" instead.
	 */ 
	public Vector<String> update(Date timestamp) {
		changedFiles.clear();

		File dir = new File(localDir);
		String[] files = dir.list();
		for(int i = 0; i < files.length; ++i) {
			String file = files[i];
			update(file.substring(file.lastIndexOf('/')+1), timestamp);
		}

		return changedFiles;
	}

	/**
	 * Update a directory or file represented by "dfName".
	 * @dfName: path relative to "localDir" in local filesystem.
	 * If "dfName" represents a directory, update all directories and files in it.
	 * If "dfName" represents a file, update it.
	 */
	public void update(String dfName, Date timestamp) {
		String relPath = createRelPath(dfName);
		try {
			if(!exists(relPath))
				return;
		}catch(SftpException sfe) {
			System.out.println("update: Check whether remote directory or file " + relPath 
				+ " exists or not failed - " + sfe);
		}

		File df = new File(localDir, dfName);
		long lastModifiedTime = df.lastModified();
		if(df.isDirectory()) {
			if(lastModifiedTime <= timestamp.getTime())
				return;

			File[] files = df.listFiles();
			for(int i = 0; i < files.length; ++i)
				update(dfName + Config.PATH_SEPARATOR + files[i].getName(), timestamp);
		}

		if(df.isFile()) {
			if(lastModifiedTime <= timestamp.getTime())
				return;

			if(uploadFile(df, relPath)) {
				changedFiles.add(relPath);
				//System.out.println("File \"" + relPath + "\" updated.");
			}
		}
	}

	/**
	 * Check whether the directory or file represented by "relPath" exists
	 * in the remote filesystem or not.
	 * @relPath: path relative to home directory in remote filesystem.
	 * @return: true if exists, false otherwise.
	 */
	public boolean exists(String relPath) throws SftpException {
		try {
			channel.stat(relPath);
		}catch(SftpException sfe) {
			if(sfe.getId() == ChannelSftp.SSH_FX_NO_SUCH_FILE)
				return false;
			throw sfe;
		}

		return true;
	}

	/**
	 * Check whether directory or file represented by "relPath" is a remote directory or not.
	 * @relPath: path relative to home directory in remote filesystem.
	 * @return: true if it is, false otherwise.
	 */
	private boolean isRemoteDir(String relPath) {
		SftpATTRS attrs	= null;
		try{
			attrs = channel.stat(relPath);
		}catch(SftpException sfe) {
			System.out.println("isRemoteDir: Check whether " + relPath + 
				" is a remote directory or not failed - " + sfe);
			return false;
		}

		return attrs.isDir();
	}

	/*
	// remoteDir/parent/filename
	private String createRelPath(String parent, String filename) {
		return remoteDir + Config.PATH_SEPARATOR + parent + Config.PATH_SEPARATOR + filename;
	}
	*/
	
	// remoteDir/filename
	private String createRelPath(String filename) {
		return remoteDir + Config.PATH_SEPARATOR + filename;
	}
}
