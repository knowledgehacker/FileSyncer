package main.sync.client;

import java.util.Vector;

/**
 * FileDoubleBuffer here is not thread-safe.
 * So the classes has FileDoubleBuffer component should provide mechanism to ensure 
 * thread-safety if the Pair component will be accessed by multiple threads.
 */
public class FileDoubleBuffer {

    //private Object syncObj;
	private Vector<FileOp> inFiles;
	private Vector<FileOp> outFiles;

	@SuppressWarnings("unchecked")
	public FileDoubleBuffer() {
		//syncObj = new Object();
		inFiles = new Vector<FileOp>();
		outFiles = new Vector<FileOp>();
	}

	/**
	 * The following methods operate on inFiles before inFiles and outFiles are swapped each run.
	 */
	public final void add(FileOp fop) {
		//synchronized(syncObj) {
			inFiles.add(fop);
		//}
	}	

	public final boolean empty() {
		//synchronized(syncObj) {
			return inFiles.isEmpty();
		//}
	}

	public final void swap() {
		//synchronized(syncObj) {
			Vector<FileOp> tmp = inFiles;
			inFiles = outFiles;
			outFiles = tmp;
		//}
	}

	/**
	 * The following methods operate on outFiles after inFiles and outFiles are swapped each run.
	 * We return the private Vector "outFiles" directly, it is dangerous to do so. We should return a copy of it.
	 * But it will introduce copy overhead, since we it is used internally, we choose to return reference here.
	 */
	public final Vector<FileOp> get() {
		return outFiles;
	}
}
