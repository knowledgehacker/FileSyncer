package main.sync.server;

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
	 * the existence of remote directory "dir" in the remote server.
	 * case 1: "dir" doesn't exist in the remote server.
	 * 	case 1.1: "dir" hasn't been created before.
	 * 	In such a case, the log file and log writer doesn't exist, create them.
	 *  case 1.2: "dir" has been created before, but deleted.
	 *  In such a case, the log file may already exists. Create the log writer 
	 *  in truncate mode no matter the log file already exists or not.
	 *
	 * case 2: "dir" already exist in the remote server.
	 * The log file should exist in such a case.
	 * Create the log writer in append mode if not exist.
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
	            logger.write(System.getProperty("line.separator"));
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