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

import java.util.Vector;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;

/**
 * Detects the file create/delete operations in local filesystem,
 * and synchronize the operations with the files in remote server.
 * A pooling thread is running as a daemon monitoring file create/delete operations.
 * And a timer task is sheduled to synchronize the file create/delete operations 
 * with the files in remote server, and update the changed files in local filesystem 
 * to remote ones as well.
 */
public class FileSyncer implements Runnable, FileListener {
    private String ruser;
    private String rhost;
	private String lcwd;
	private String rcwd;
	private int lcwdLength;

    private Session session;
    private ChannelSftp channel;
    private boolean isConnected;

    private final String logFilename = ".log.txt";
    private File logFile;
    private FileWriter logWriter;
    private Date timestamp;
	private static final String disconMsg = "disconnect at ";

	// Delegate synchronization operations to object "syncOps".
	private SyncOps syncOps;

	/**
	 * Performance constraint of VFS implementation: 
	 * needs 1000 milliseconds to check every 1000 files in average.
	 */ 
	private long fileNum;
	private static final long checksPerRun = Long.parseLong(Config
		.getProperty("checks.per.run"));
	private static final long checkDelayPerRun = Long.parseLong(Config
		.getProperty("check.delay.per.run"));
	private DefaultFileMonitor fileMonitor;
	private long monitorDelay;
	private Timer syncTimer;
	private Thread syncThread;

	/**
	 * File monitor adds created files to inCreatedFiles, and 
	 * deleted files to inDeletedFiles between two runs.
	 * Time tasker retrieves the created files from outCreatedFiles, 
	 * and deleted files from outDeletedFiles.
	 * Each time the timer task is scheduled, inCreatedFiles and 
	 * outCreatedFiles, and inDeletedFiles and outDeletedFiles, 
	 * are swapped. 
	 */
	private Object syncObj;
	private Vector<String> inCreatedFiles;
	private Vector<String> inDeletedFiles;
	private Vector<String> outCreatedFiles;
	private Vector<String> outDeletedFiles;

	public FileSyncer(String lcwd, String uri) { 
		/*
		 * Initialize remote user name and host,
		 * and local/remote current working directories.
		 */
		int at = uri.indexOf("@");
		ruser = uri.substring(0, at);
		rhost = uri.substring(at+1);
		this.lcwd = lcwd;
		this.rcwd = Config.LOCAL_USERNAME + Config.PATH_SEPARATOR 
			+ lcwd.substring(lcwd.lastIndexOf("/")+1);
		lcwdLength = lcwd.length() + 1; //"+1" for "/" following lcwd

		/*
		 * Setup session object and logFile.
		 */
		try{
	        JSch jsch = new JSch();
    	    jsch.setKnownHosts(Config.getProperty("known.hosts.file"));
        	jsch.addIdentity(Config.getProperty("identity.file"));
	        session = jsch.getSession(ruser, rhost, 22);
    	    session.setUserInfo(new SpecificUserInfo());    
        }catch(JSchException jse) {
            System.out.println("connect: Setup session object failed - " + jse);
            System.exit(1);
        }
        logFile = new File(lcwd, logFilename);

		/*
		 * Calculate the number of files in current working direcotry,
		 * and corresponding monitor delay for the amount of files.
		 */ 
		fileNum = FileStatistics.countFiles(lcwd); 
		System.out.println("The number of files in \"" + lcwd +"\": " + fileNum + ".");
		monitorDelay = (fileNum / checksPerRun + 1) * checkDelayPerRun;
		System.out.println("monitorDelay = " + monitorDelay);

		syncObj = new Object();
		inCreatedFiles = new Vector<String>();
		inDeletedFiles = new Vector<String>();
		outCreatedFiles = new Vector<String>();
		outDeletedFiles = new Vector<String>();
	}

	public void connect(String ruser, String rhost) {
        try{
	        session.connect();
            channel = (ChannelSftp)session.openChannel("sftp");
            channel.connect();
        }catch(JSchException jse) {
            System.out.println("connect: Connect failed - " + jse);
            System.exit(1);
        }
        System.out.println("Connection established!");
        isConnected = true;
	}

	/**
	 * Create the log writer, which is created in different ways according to 
	 * the existence of "rcwd" in the remote server.
	 * case 1: "rcwd" doesn't exist in the remote server.
	 * 	case 1.1: "rcwd" hasn't been created before.
	 * 	In such a case, the log file and log writer doesn't exist, create them.
	 *  case 1.2: "rcwd" has been created before, but deleted.
	 *  In such a case, the log file may already exists. Create the log writer 
	 *  in truncate mode no matter the log file already exists or not.
	 *
	 * case 2: "rcwd" already exist in the remote server.
	 * The log file should exist in such a case, so application exits if not.
	 * Otherwise create the log writer in append mode.
	 */
	private void createLogWriter(boolean reconnect) {
		if(reconnect) {
			if(!logFile.exists()) {
				System.out.println("createLogWriter: Log file " + logFile.getName() 
					+ " does not exist.");
				session.disconnect();
				System.exit(1);
			}
		}

		try{
   		    logWriter = new FileWriter(logFile, reconnect);
	    }catch(IOException ioe) {
           	System.out.println("createLogWriter: Create logWriter failed - " + ioe);
			session.disconnect();
			System.exit(1);
  		}
	}

	public void initialize() {
		connect(ruser, rhost);

		syncOps = new SyncOps(lcwd, rcwd, channel);
		try{
			// Create a directory for local user in remote server.
			String userName = Config.LOCAL_USERNAME;
			if(!syncOps.exists(userName)) {
				try{
					channel.mkdir(userName);
					System.out.println("Directory \"" + userName + "\" created.");
				}catch(SftpException sfe) {
					System.out.println("initialize: Create user remote directory for \"" 
						+ userName + "\" failed - " + sfe);
					session.disconnect();
					System.exit(1);
				}
			}

			// Upload all directories and files in "lcwd".
			if(!syncOps.exists(rcwd)) {
				try{
        		    channel.mkdir(rcwd);
		        }catch(SftpException sfe) {
	        	    System.out.println("initialize: Create top remote directory \""
     	       	    	+ rcwd + "\" failed - " + sfe);
		            session.disconnect();
					System.exit(1);
    		    }
        		System.out.println("Directory \"" + rcwd + "\" created.");

				syncOps.upload();
				
				// Create a log file if it doesn't exist, otherwise truncate it.
				createLogWriter(false);
				writeLog("uploaded", true);
          	
				// Record the time all files in "lcwd" are uploaded.
	            timestamp = new Date(logFile.lastModified());
			}else {
				/*
				 * The log file should exist at this time. Application exits if not.
				 * Otherwise create the log writer in append mode.
				 * Thus subsequent logs will be written to the end of the log file.
				 */
				createLogWriter(true);

				// Read the disconnect time from .log.txt and record it in timestamp.
				RandomAccessFile logRAF = null;
				try{
					logRAF= new RandomAccessFile(logFile, "r");
				}catch(FileNotFoundException fnfe) {
					System.out.println("initialize: Log file " + logFile.getName() 
						+ " not found - " + fnfe);
					session.disconnect();
					System.exit(1);
				}

				String lastLine = null;
				try{
					long logRAFLength = logRAF.length();
					if(logRAFLength > 256)
						logRAF.seek(logRAFLength - 256);

					String line = logRAF.readLine();
					while(line != null) {
						lastLine = line;
						line = logRAF.readLine();
					}
					logRAF.close();
				}catch(IOException ioe) {
					System.out.println("initialize: Read timestamp from log file " 
						+ logFile.getName() + " failed - " + ioe);
					session.disconnect();
					System.exit(1);
				}

				long disconTime = 0;
				for(int i = lastLine.indexOf(disconMsg) + disconMsg.length(); 
					i < lastLine.length(); ++i) {
					char c = lastLine.charAt(i);
					if((c >= '0') && (c <= '9'))
						disconTime = disconTime*10 + c - '0';
					else
						break;
				}
				timestamp = new Date(disconTime);
			}
		}catch(SftpException sfe) {
			System.out.println("initialize: " + sfe);
		}

		FileObject dirObj = null;
		try {
			FileSystemManager fsManager = VFS.getManager();
			dirObj = fsManager.resolveFile(lcwd);	// dirObj actually has type of LocalFile
		}catch(FileSystemException fse) {
			System.out.println(fse);
			session.disconnect();
			System.exit(1);
		}
		fileMonitor = new DefaultFileMonitor(this); 
		fileMonitor.setRecursive(true); 
		fileMonitor.setDelay(monitorDelay);
		fileMonitor.addFile(dirObj); 
		fileMonitor.start();

		syncTimer = new Timer("Sync Timer");
		syncTimer.scheduleAtFixedRate(new SyncTask(), monitorDelay*3/2, monitorDelay);
		System.out.println("SyncTimer is created at " + new Date() + ".");
	}

    public void disconnect() {
        if(isConnected == true) {
			// Write disconnect timestamp to .log.txt for later reconnect.
			writeLog(disconMsg + logFile.lastModified() + Config.NEWLINE, false);

            try{
                logWriter.close();
            }catch(IOException ioe) {
                System.out.println("disconnect: Close logWriter failed - " + ioe);
            }

            session.disconnect();
            isConnected = false;
        }
    }

	public void finalize() {
		fileMonitor.stop();

		syncTimer.cancel();
		try{
			syncThread.join();
		}catch(InterruptedException ie) {
			System.out.println("Thread " + syncThread.getName() + " - "
				+ syncThread.getId() + " interrupted - " + ie);
		}
	
		disconnect();
	}

    private final void writeLog(String logMsg, boolean timestamp) {
        try{
            logWriter.write(logMsg);
            logWriter.flush();
            if(timestamp) {
                logWriter.write(" - " + new Date(logFile.lastModified()));
	            logWriter.write(Config.NEWLINE);
			}
        }catch(IOException ioe) {
            System.out.println("writeLog: Write to " + logFilename
				+ " failed - " + ioe);
            try{
                logWriter.close();
            }catch(IOException aioe) {
                System.out.println("writeLog: Close logWriter failed - "
					+ aioe);
            }
        }
    }

	public void fileChanged(FileChangeEvent event) {
		// We do not care about the files changed here.
	}

	public void fileCreated(FileChangeEvent event) {
		/*
		 * fileObj actually is of type LocalFile.
		 * path is the absolute path of the file.
		 */
		FileObject fileObj = event.getFile();
		String path = fileObj.getName().getPath();
		synchronized(syncObj) {
			inCreatedFiles.add(path.substring(lcwdLength));
		}
	}
	
	public void fileDeleted(FileChangeEvent event) {
		FileObject fileObj = event.getFile();
		String path = fileObj.getName().getPath();
		synchronized(syncObj) {
			inDeletedFiles.add(path.substring(lcwdLength));
		}
	}

	private class SyncTask extends TimerTask {
		public void run() {
			System.out.println(System.currentTimeMillis()
				+ ": SyncTask.run starts.");

			if(syncThread == null)
				syncThread = Thread.currentThread();

			String logMsg = "";
			Vector<String> changedFiles = syncOps.update(timestamp);
			if(changedFiles.size() > 1) {
				for(int i = 0; i < changedFiles.size(); ++i)
					logMsg += "File " + changedFiles.get(i) + " changed."
						+ Config.NEWLINE;
			}

			long fileAdded = 0;
			if(in2Out()) {
				int fn;
				fn = outCreatedFiles.size();
				if(fn > 0) {
					for(int i = 0; i < fn; ++i) {
						String createdFile = outCreatedFiles.get(i);
						syncOps.add(createdFile, false);
						logMsg += "File " + createdFile + " added."
							+ Config.NEWLINE;
					}
					fileAdded += fn;
				}

				fn = outDeletedFiles.size();
				if(fn > 0) {
					for(int i = 0; i < fn; ++i) {
						String deletedFile = outDeletedFiles.get(i);
						syncOps.remove(deletedFile, true);
						logMsg += "File " + deletedFile + " deleted."
							+ Config.NEWLINE;
					}
					fileAdded -= fn;
				}
		
				outCreatedFiles.clear();
				outDeletedFiles.clear();
			}
			if(logMsg != "") {
				writeLog(logMsg, false);
				writeLog("updated", true);
			}
			timestamp = new Date(logFile.lastModified());

			// Adjust monitor delay if needed
			adjustMonitorDelay(fileAdded);	
		}

		private final boolean in2Out() {
			synchronized(syncObj) {
				if((inCreatedFiles.size() == 0) && (inDeletedFiles.size() == 0))
					return false;

				Vector<String> tmp = null;
				tmp = inCreatedFiles;
				inCreatedFiles = outCreatedFiles;
				outCreatedFiles = tmp;

				tmp = inDeletedFiles;
				inDeletedFiles = outDeletedFiles;
				outDeletedFiles = tmp;
			}

			return true;
		}
	
		private void adjustMonitorDelay(long newFileNum) {
			if((newFileNum > 0) && ((fileNum % checksPerRun + newFileNum) >= checksPerRun)){
				fileNum += newFileNum;
				monitorDelay += (fileNum / checksPerRun + 1) * checkDelayPerRun;
				fileMonitor.setDelay(monitorDelay);
				System.out.println("monitor delay: " + fileMonitor.getDelay());
				this.cancel();
				syncTimer.purge();
				/*
				long ret = System.currentTimeMillis() - this.scheduledExecutionTime();
				syncTimer.scheduleAtFixedRate(new SyncTask(), ret, monitorDelay);
				*/
				syncTimer.scheduleAtFixedRate(new SyncTask(), 0, monitorDelay);
			}
		}
	}


	/**
	 * Calculate the number of directories and files in directory
	 * represented by "df", "df" included.
	 */	
	private static class FileStatistics {
		public static long countFiles(String dfName) {
			int num = 1;
			return fileNum(new File(dfName), num);
		}

		private static long fileNum(File df, long num) {
			num += 1;

			if(df.isFile())
				return num;
			if(df.isDirectory()) {
				File files[] = df.listFiles();	
				for(int i = 0; i < files.length; ++i) {
					int subnum = 0;
					num += fileNum(files[i], subnum);
				}
			}

			return num;
		}
	}

	private static String parsePath(String path) {
        String res = path;

		// path begins with '~'.
        if(path.charAt(0) == '~')
            res = System.getProperty("user.home") + path.substring(1);
        else if(path.charAt(0) == '.') {
			if(path.length() == 1)
				res = System.getProperty("user.dir");
			else if(path.charAt(1) != '.')
	            res = System.getProperty("user.dir") + path.substring(1);
			else {
				// path begins with ".."
				if((path.charAt(0) == '.') && (path.charAt(1) == '.')) {
	        		String userDir = System.getProperty("user.dir");
					int i = 0;
        			while((path.charAt(i) == '.') && (path.charAt(i+1) == '.')) {
        				int lastSlash = userDir.lastIndexOf('/');
			            if(lastSlash == -1)
    			        	return null;

        	    		userDir = userDir.substring(0, lastSlash);
		            	i += 2;
						if(i >= path.length()-1)
							break;
						if(path.charAt(i) != '/')
							return null;
						++i;	
    	    		}
		            String postfix = path.substring(i, path.length());
        		    if(postfix.equals("/"))
                		res = userDir;
		            else
        		        res = userDir + Config.PATH_SEPARATOR + postfix;
				}
			}
		}

		// Take the last '/' if exists away.
		if(res.charAt(res.length()-1) == '/')
			res = res.substring(0, res.length()-1);

        System.out.println("res = " + res);
		return res;
    }
	
	public void run() {
		while(true) {
			initialize();

			try{
				Thread.sleep(Long.parseLong(Config.getProperty("check.delay.per.run"))*3600*24);
			}catch(InterruptedException ie) {
				// ...
			}

			finalize();
		}
	}

	public static void main(String args[]) {
		if(args.length < 2) {
			System.out.println("Usage: java -jar FileSyncer.jar localdir user@remotehost");
			return;
		}

		Thread syncThread = new Thread(new FileSyncer(parsePath(args[0]), args[1]));
		syncThread.start();
	}
}
