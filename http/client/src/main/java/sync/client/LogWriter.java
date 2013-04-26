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

import java.io.File;
import java.io.RandomAccessFile;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

/**
 * Record log by clients.
 */
public class LogWriter {
    private final String logFilename = ".log.txt";
    private File logFile;
    private FileWriter logger;
    
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
	public LogWriter(String dir, boolean reconnect) throws FileNotFoundException, IOException {
		logFile = new File(dir, logFilename);    
		if(reconnect) {
			if(!logFile.exists()) {
				String msg = "LogWriter: Log file " + logFilename + " not found.";
				throw new FileNotFoundException(msg);
			}
		}

		try{
   		    logger = new FileWriter(logFile, reconnect);
	    }catch(IOException ioe) {
			System.out.println("LogWriter: Create a LogWriter failed.");
           throw ioe;
  		}
	}

	/**
	 * Read the disconnect time from .log.txt.
	 */
	public long readLastDisconnectTime(String disconMsg) throws FileNotFoundException, IOException {
		RandomAccessFile logRAF = null;
		try{
			logRAF= new RandomAccessFile(logFile, "r");
		}catch(FileNotFoundException fnfe) {
			System.out.println("initialize: Log file " + logFile.getName() 
				+ " not found - " + fnfe);
			throw fnfe;
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
			throw ioe;
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

		return disconTime;
	}
	
	public final long lastModified() {
		return logFile.lastModified();
	}

	public final void append(String logMsg, boolean timestamp) {
        try{
            logger.write(logMsg);
            logger.flush();
            if(timestamp) {
                logger.write(" - " + new Date(logFile.lastModified()));
	            logger.write(ClientSetting.NEWLINE);
			}
        }catch(IOException ioe) {
            System.out.println("append: Write to " + logFilename
				+ " failed - " + ioe);
            try{
                logger.close();
            }catch(IOException aioe) {
                System.out.println("append: Close the LogWriter failed - "
					+ aioe);
            }
        }
    }

	public void close() throws IOException{
		logger.close();
	} 
}
