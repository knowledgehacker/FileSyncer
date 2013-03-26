package main.sync.client;

public class FileOp {
	public static final short CREATE = 0;
	public static final short DELETE = 1;

	private String relPath;
	private short op;

	public FileOp(String relPath, short op) {
		this.relPath = relPath;
		this.op = op;
	}

	public final String getRelPath() {
		return relPath;
	}

	public final short getOp() {
		return op;
	}
}		
