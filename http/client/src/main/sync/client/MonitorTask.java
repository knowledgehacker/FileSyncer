package main.sync.client;

import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;

public class MonitorTask implements FileListener {
	private final int localDirLength;

	/*
	 * Is locking pair here equivalent to locking the referenced pair object?
	 */
	private FileDoubleBuffer fbuffer;

    private DefaultFileMonitor fileMonitor;

	public MonitorTask(String dir, long monitorDelay, FileDoubleBuffer fbuffer) {
		localDirLength = dir.length();

 		this.fbuffer = fbuffer;
	    
		FileObject dirObj = null;
        try {
            FileSystemManager fsManager = VFS.getManager(); 
			// dirObj actually has type of LocalFileanager();
            dirObj = fsManager.resolveFile(dir);  
        }catch(FileSystemException fse) {
            System.out.println(fse);
			return;
        }
        fileMonitor = new DefaultFileMonitor(this);
        fileMonitor.setRecursive(true);
        fileMonitor.setDelay(monitorDelay);
        fileMonitor.addFile(dirObj);
	}

	public void start() {
       fileMonitor.start();
	}

	public void setMonitorDelay(long delay) {
		fileMonitor.setDelay(delay);
	}

	public void stop() {
		fileMonitor.stop();
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
		synchronized(fbuffer) {
			String relPath = path.substring(localDirLength+1);
			System.out.println("relPath = " + relPath);
        	fbuffer.add(new FileOp(relPath, FileOp.CREATE));
		}
    }

    public void fileDeleted(FileChangeEvent event) {
        FileObject fileObj = event.getFile();
        String path = fileObj.getName().getPath();
		synchronized(fbuffer) {
			String relPath = path.substring(localDirLength+1);
			System.out.println("relPath = " + relPath);
	        fbuffer.add(new FileOp(relPath, FileOp.DELETE));
		}
    }
}
