package main.sync.client;

public class FileSyncBaseOpsException extends RuntimeException {
	private static final long serialVersionUID = -7523391676791081216L;
	
	private String msg;

	public FileSyncBaseOpsException(String msg) {
		this.msg = msg;
	}

	public String toString() {
		return "FileSyncBaseOpsException: " + msg;
	}
}
