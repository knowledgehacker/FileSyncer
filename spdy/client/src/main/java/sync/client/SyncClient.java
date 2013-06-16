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
//import java.util.Set;
//import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import java.io.File;

/**
 * Detects the file operations such as created, changed, and deleted in a specified 
 * local directory, and synchronize the operations with the files in remote server.
 * A pooling thread is running as a daemon monitoring file create/delete operations.
 * And a timer task is scheduled to synchronize the file create/delete operations 
 * with the files in remote server, and update the changed files in the specified 
 * local directory to remote ones as well.
 */

public abstract class SyncClient {
	protected FileSyncOps syncOps;

	private FileDoubleBuffer fbuffer;

	private LogWriter logWriter;
	private Date timestamp;

    /**
     * Performance constraint of VFS implementation: 
     * Check every 1000 files takes about 1 second.
     */
    private static final long checksPerRun = Integer.parseInt(ClientSettings.getProperty("checks.per.run"));
    private static final long checkDelayPerRun = Integer.parseInt(ClientSettings.getProperty("check.delay.per.run"));
    private int fileNum;
    private long monitorDelay;
	private MonitorTask monitorTask;

	private Timer syncTimer;
	private Thread syncThread;

	public SyncClient() {
		
	}

	public void start() {	
        initialize();

        try{
            Thread.sleep(Integer.parseInt(ClientSettings.getProperty("check.delay.per.run"))*3600);
        }catch(InterruptedException ie) {
        	// TODO: handle exception ie
        }

        finalize();
    }
	
	public abstract boolean login();
	public abstract void logout();
	public abstract boolean connect();
	public abstract void disconnect();
	public abstract FileSyncOps newFileSyncOps();

	/*
	 * The work flow is like this:
	 * a. login, user name and device id is stored for later use when authenticate completed.
	 * b. connect to server.
	 * c. create an concrete FileSyncOps instance, and assign it to "syncOps".
	 * d. Start monitor task, and create log writer as well.
	 */
	public void initialize() {
		File syncDir = new File(ClientSettings.SYNC_DIR);
		if(!syncDir.exists())
			syncDir.mkdir();

		// login and user authentication
		login();
		
		// connect to server
		connect();
        // create a FileSyncOps instance
        syncOps = newFileSyncOps();
        /* 
         * TODO: upload the existing files in root directory if it is the first time 
         * for the device to connect to the server.
         * Currently we assume there is no existing files in root directory 
         * before a device connect to the server for the first time.
         */
        //syncOps.upload();

		// create an log writer
		logWriter = new LogWriter();
		timestamp = new Date(logWriter.lastModified());

		// start monitor task
		startMonitorTask();
	}

	public void finalize() {
		// stop monitor task
		stopMonitorTask();
		
		// disconnect from server
		disconnect();
		
		// logout
		logout();
	}
	
	private final void startMonitorTask() {
        fileNum = FileStatistics.countFiles(ClientSettings.SYNC_DIR);
        System.out.println("The number of files in \"" + ClientSettings.SYNC_DIR +"\": " + fileNum + ".");
        monitorDelay = (fileNum / checksPerRun + 1) * checkDelayPerRun;
        System.out.println("monitorDelay = " + monitorDelay);
		fbuffer = new FileDoubleBuffer();
		monitorTask = new MonitorTask(monitorDelay, fbuffer);
		if(monitorTask == null) {
			disconnect();
	
			return;
		}
		monitorTask.start();

		syncTimer = new Timer("Sync Timer");
		syncTimer.scheduleAtFixedRate(new SyncTask(), monitorDelay*3/2, monitorDelay);
		System.out.println("SyncTimer is created at " + new Date() + ".");
	}
	
	private final void stopMonitorTask() {
		monitorTask.stop();

		syncTimer.cancel();
		try{
			syncThread.join();
		}catch(InterruptedException ie) {
			System.out.println("Thread " + syncThread.getName() + " - "
				+ syncThread.getId() + " interrupted - " + ie);
		}
	}

	private class SyncTask extends TimerTask {
		public void run() {
			System.out.println(System.currentTimeMillis()
				+ ": SyncTask.run starts.");

			if(syncThread == null)
				syncThread = Thread.currentThread();

			synchronized(fbuffer) {
				if(!fbuffer.empty()) 
					fbuffer.swap();
			}
			
			/**
		 	 * We don't need to synchronize on FileDoubleBuffer.outFiles after FileDoubleBuffer.swap is called.
			 * Since it will only be accessed in the single monitor thread from then on each run.
			 */
			String logMsg = "";
			//Set<String> filesCreated = new HashSet<String>();

			long fileAdded = 0;
			Vector<FileOp> fops = fbuffer.get();
			int fn = fops.size();
			for(int i = 0; i < fn; ++i) {
				FileOp fop = fops.get(i);
				String relPath = fop.getRelPath();
				File df = new File(ClientSettings.SYNC_DIR, relPath);
				short op = fop.getOp();
				if(op == FileOp.CREATE) {
					//filesCreated.add(relPath);
					
					if(df.isFile())
						syncOps.addFile(relPath);

					if(df.isDirectory())
						syncOps.addDir(relPath, false);

					logMsg += "File " + relPath + " added."
						+ ClientSettings.NEWLINE;
					fileAdded += 1;
				}

				if(op == FileOp.DELETE) {
					/*
					if(fileType == FileOp.FILE) {
						System.out.println("SyncClient: call syncOps.removeFile(" + relPath + ")");
						syncOps.removeFile(relPath);
					}

					if(fileType == FileOp.DIR)
						syncOps.removeDir(relPath, true);
					*/
					syncOps.remove(relPath);
					
					logMsg += "File " + relPath + " deleted."
						+ ClientSettings.NEWLINE;
					fileAdded -= 1;
				}
			}
			fops.clear();

			/*
			 * Check modified files since last round.
			 * But the implementation here doesn't work.
			 * If some files are added after fbuffer is swapped, but before update begins.
			 * These new files will be treated as modified ones, since they do not exist in filesCreated.
			 * We don't handle files modified for the moment here.
			 */
			/*
			Vector<String> changedFiles = syncOps.update(timestamp, filesCreated);
			if(!changedFiles.isEmpty()) {
				int changedFileNum = changedFiles.size();
				for(int i = 0; i < changedFileNum; ++i)
					logMsg += "File " + changedFiles.get(i) + " changed."
						+ ClientSettings.NEWLINE;
			}
			*/

			if(!logMsg.equals("")) {
				logWriter.append(logMsg, false);
				logWriter.append("updated", true);
				timestamp = new Date(logWriter.lastModified());
			}
			//timestamp = new Date();

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
