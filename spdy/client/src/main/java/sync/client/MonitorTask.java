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

	public MonitorTask(long monitorDelay, FileDoubleBuffer fbuffer) {
		String root = ClientSettings.SYNC_DIR;
		localDirLength = root.length();

 		this.fbuffer = fbuffer;
	    
		FileObject dirObj = null;
        try {
            FileSystemManager fsManager = VFS.getManager(); 
			// dirObj actually has type of LocalFileanager();
            dirObj = fsManager.resolveFile(root);  
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
       String relPath = path.substring(localDirLength+1);
		if(!FileStore.removeIfPresent(relPath)) {
			System.out.println("fileCreated: relPath = " + relPath);
			synchronized(fbuffer) {
				fbuffer.add(new FileOp(relPath, FileOp.CREATE));
			}
		}
    }

    public void fileDeleted(FileChangeEvent event) {
        FileObject fileObj = event.getFile();
        String path = fileObj.getName().getPath();
		String relPath = path.substring(localDirLength+1);
		synchronized(fbuffer) {
			System.out.println("fileDeleted: relPath = " + relPath);
	        fbuffer.add(new FileOp(relPath, FileOp.DELETE));
		}
    }
}
