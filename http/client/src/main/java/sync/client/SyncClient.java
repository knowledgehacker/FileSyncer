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
import java.util.Timer;
import java.util.TimerTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Detects the file operations such as created, changed, and deleted in a specified 
 * local directory, and synchronize the operations with the files in remote server.
 * A pooling thread is running as a daemon monitoring file create/delete operations.
 * And a timer task is scheduled to synchronize the file create/delete operations 
 * with the files in remote server, and update the changed files in the specified 
 * local directory to remote ones as well.
 */

/**
 * TODO: Introduce user authentication!!!
 */
public abstract class SyncClient {
	private String localDir;
	
	// Delegate synchronization operations to object "syncOps".
	private FileSyncOps syncOps;
	
	private boolean isConnected;

	private LogWriter logWriter;
	private Date timestamp;
    private static final String disconMsg = "disconnect at ";

	private FileDoubleBuffer fbuffer;

   /**
    * Performance constraint of VFS implementation: 
    * Check every 1000 files takes about 1 second.
    */
    private static final long checksPerRun = Integer.parseInt(ClientSetting.getProperty("checks.per.run"));
    private static final long checkDelayPerRun = Integer.parseInt(ClientSetting.getProperty("check.delay.per.run"));
    private int fileNum;
    private long monitorDelay;
	private MonitorTask monitorTask;

	private Timer syncTimer;
	private Thread syncThread;

	public SyncClient(String localDir, FileSyncOps syncOps) {
		this.localDir = localDir;

		this.syncOps = syncOps;

		isConnected = false;
	}

	/*
	 * TODO: define the exceptions will be thrown during connecting and disconnecting!!!
	 */
	public abstract boolean connect();
    public abstract void disconnect();

	public void initialize() {
		boolean alreadyExists = false;
		//try {
			/**
			 * connect here will check the loalDir to synchronize already exists or not.
			 * If not exist, create one. Otherwise nothing has to do.
			 * connect returns a boolean to indicate the localDir to synchronize already exists or not.
			 */
			alreadyExists = connect();
		//}catch(...) {
		//}
		isConnected = true;
		System.out.println("Connection established!");

		try{
			/**
			 * Create a directory for local user in remote server.
			 * It should be done when user registering, ignore it for the moment.
			 */
			/*
			String user = ClientSetting.LOCALUSER;
			if(!syncOps.exists(user)) {
				try{
					syncOps.mkdir(user);
					System.out.println("initialize: Directory \"" + user + "\" created.");
				}catch(FileSyncBaseOpsException fsboe) {
					System.out.println("initialize: Create remote directory for \"" 
						+ user + "\" failed - " + fsboe);
					disconnect();
				}
			}
			*/

			/*
			if(!syncOps.exists(remoteDir)) {
				try{
        		    syncOps.mkdir(remoteDir);
		        }catch(FileSyncBaseOpsException fsboe) {
	        	    System.out.println("initialize: Create remote directory \""
     	       	    	+ remoteDir + "\" failed - " + fsboe);
		            disconnect();
    		    }
        		System.out.println("initialize: Directory \"" + remoteDir + "\" created.");
			*/
			if(!alreadyExists) {
				long currentTime = System.currentTimeMillis();
				syncOps.upload();
				System.out.println("Upload takes " + (System.currentTimeMillis() - currentTime) + " milliseconds.");

				System.out.println("testing ...");
				//syncOps.createFile("TestSyncOps.java");
				//syncOps.createFile("HttpSyncClient.jar");
				//syncOps.deleteFile("HttpSyncClient.jar");
				//syncOps.mkdir("kkk");
				//syncOps.exists("kkk");
				//syncOps.rmdir("kkk");
				
				// Create a log file if it doesn't exist, otherwise truncate it.
				try {
					logWriter = new LogWriter(localDir, false);
					logWriter.append("uploaded", true);
				}catch(IOException ioe) {
					System.err.println("initialize: " + ioe);
				}	
          	
	            timestamp = new Date(logWriter.lastModified());
			}else {
				/*
				 * The log file should exist at this time.
				 * Create the log writer in append mode.
				 */
				long disconnTime = 0;
				try {
					logWriter = new LogWriter(localDir, true);
					disconnTime = logWriter.readLastDisconnectTime(disconMsg);
				}catch(FileNotFoundException fnfe) {
					System.err.println("initialize: " + fnfe);
				}catch(IOException ioe) {
					System.err.println("initialize: " + ioe);
				}

				timestamp = new Date(disconnTime);
			}
		}catch(FileSyncBaseOpsException fsboe) {
			System.out.println("initialize: " + fsboe);
		}

		fbuffer = new FileDoubleBuffer();

        /*
         * Calculate the number of files in local directory "localDir",
         * and corresponding monitor delay for the amount of files.
         */
        fileNum = FileStatistics.countFiles(localDir);
        System.out.println("The number of files in \"" + localDir +"\": " + fileNum + ".");
        monitorDelay = (fileNum / checksPerRun + 1) * checkDelayPerRun;
        System.out.println("monitorDelay = " + monitorDelay);
		monitorTask = new MonitorTask(localDir, monitorDelay, fbuffer);
		if(monitorTask == null) {
			/*
			 * TODO: Eliminate all the operations up to now:
			 * Creating the user and specified directories in remote server.
			 * Create the LogWriter.
			 */
			disconnect();

			return;
		}
		monitorTask.start();

		syncTimer = new Timer("Sync Timer");
		syncTimer.scheduleAtFixedRate(new SyncTask(), monitorDelay*3/2, monitorDelay);
		System.out.println("SyncTimer is created at " + new Date() + ".");
	}


	public void finalize() {
		monitorTask.stop();

		syncTimer.cancel();
		try{
			syncThread.join();
		}catch(InterruptedException ie) {
			System.out.println("Thread " + syncThread.getName() + " - "
				+ syncThread.getId() + " interrupted - " + ie);
		}
	
	    if(isConnected == true) {
			// Write disconnect timestamp to .log.txt for later reconnect.
			logWriter.append(disconMsg + logWriter.lastModified() + ClientSetting.NEWLINE, false);

            try{
                logWriter.close();
            }catch(IOException ioe) {
                System.out.println("finalize: Close logWriter failed - " + ioe);
            }

			//try {
	            disconnect();
			//}catch(...) {
			//}
            isConnected = false;
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
						+ ClientSetting.NEWLINE;
			}

			synchronized(fbuffer) {
				if(fbuffer.empty() == true)
					return;
				else
					fbuffer.swap();
			}

			/**
			 * We don't need to synchronize on FileDoubleBuffer.outFiles after FileDoubleBuffer.swap is called.
			 * Since it will only be accessed in the single monitor thread from then on each run.
			 */
			long fileAdded = 0;

			Vector<FileOp> fops = fbuffer.get();
			int fn = fops.size();
			for(int i = 0; i < fn; ++i) {
				FileOp fop = fops.get(i);
				String relPath = fop.getRelPath();
				File df = new File(localDir, relPath);
				short op = fop.getOp();
				if(op == FileOp.CREATE) {
					if(df.isFile())
						syncOps.addFile(relPath);

					if(df.isDirectory())
						syncOps.addDir(relPath, false);

					logMsg += "File " + relPath + " added."
						+ ClientSetting.NEWLINE;
					fileAdded += 1;
				}

				if(op == FileOp.DELETE) {
					if(df.isFile())
						syncOps.addFile(relPath);

					if(df.isDirectory())
						syncOps.removeDir(relPath, true);
						
					logMsg += "File " + relPath + " deleted."
						+ ClientSetting.NEWLINE;
					fileAdded -= 1;
				}
			}

			fops.clear();

			if(logMsg != "") {
				logWriter.append(logMsg, false);
				logWriter.append("updated", true);
			}
			timestamp = new Date(logWriter.lastModified());

			// reschedule monitor and sync tasks if needed
			rescheduleTasks(fileAdded);	
		}

		/**
		 * To correct: if newFileNum < 0, we should decrease monitor dealy.
		 */
		private void rescheduleTasks(long newFileNum) {
			if((newFileNum > 0) && ((fileNum % checksPerRun + newFileNum) >= checksPerRun)) {
				fileNum += newFileNum;
				monitorDelay += (fileNum / checksPerRun + 1) * checkDelayPerRun;
				monitorTask.setMonitorDelay(monitorDelay);
				
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

    private static class FileStatistics {
        public static int countFiles(String dfName) {
            int num = 1;
            return fileNum(new File(dfName), num);
        }

        private static int fileNum(File df, int num) {
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
}
