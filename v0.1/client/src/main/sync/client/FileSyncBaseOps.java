package main.sync.client;

/**
 * Base operations for file synchronization.
 * Currently the synchronization is uni-directional, from local hosts to a centralized server.
 */
public abstract class FileSyncBaseOps {
	/**
	 * Upgrade the file in relative path "relPath" to remote server.
	 */
    public abstract void upgradeFile(String relPath) throws FileSyncBaseOpsException;

	/**
	 * Create a directory "relPath" in remote server.
	 */
    public abstract void mkdir(String relPath) throws FileSyncBaseOpsException;
	
	/**
	 * Delete the directory "relPath" in remote server.
	 */
    public abstract void rmdir(String relPath) throws FileSyncBaseOpsException;

	/**
	 * Create the file in relateive path "relPath" to remote server.
	 */
    public abstract void createFile(String relPath) throws FileSyncBaseOpsException;

	/**
	 * Delete the file "relPath" in remote server.
	 */
	public abstract void deleteFile(String relPath) throws FileSyncBaseOpsException;

	/**
	 * Check whether file or directory "relPath" exists in remote server or not.
	 */
    public abstract boolean exists(String relPath) throws FileSyncBaseOpsException;
}
