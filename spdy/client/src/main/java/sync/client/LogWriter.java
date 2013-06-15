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
import java.io.FileWriter;
import java.io.IOException;

/**
 * Record log by clients.
 */
public class LogWriter {
    private final String logFilename = ".log.txt";
    private File logFile;
    private FileWriter logger;
    
	public LogWriter() {
		try{
			logFile = new File(ClientSettings.SYNC_DIR, logFilename);
			if(!logFile.exists())
				logFile.createNewFile();
   		    logger = new FileWriter(logFile, true);
	    }catch(IOException ioe) {
			System.err.println("LogWriter: Create a LogWriter failed.");
  		}
	}
	
	public final long lastModified() {
		return logFile.lastModified();
	}

	public final void append(String logMsg, boolean timestamp) {
        try{
            logger.write(logMsg);
            if(timestamp)
                logger.write(" - " + new Date(logFile.lastModified()) + ClientSettings.NEWLINE);
			logger.flush();
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
