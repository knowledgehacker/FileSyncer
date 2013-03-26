package main.sync.client.http;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import java.io.File;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.util.zip.GZIPOutputStream;
import java.io.IOException;

import main.sync.client.FileSyncOps;
import main.sync.client.FileSyncBaseOpsException;

/**
 * Possible improvement.
 * 1) Can all the requests from a client share the same HttpURLConnection?
 * Each time a request opens an Stream out of the shared HttpURLConnection, the request will get a new Stream???
 * 2) Compress the data to transmit.
 * 3) Make requests using HttpClient asynchronous I/O.
 */
public class FileSyncOpsOverURLCompressed extends FileSyncOps {
	private final String localDir;
	private final String localDirName;
	private final String host;
	private final int port;

	private final int bufferSize;

	public FileSyncOpsOverURLCompressed(String localDir, String host, int port) {
		super(localDir);

		this.localDir = localDir;
		this.localDirName = localDir.substring(localDir.lastIndexOf(File.separatorChar)+1);
		this.host = host;
		this.port = port;

		bufferSize = 128*1024;	// 128KB
	}

	/**
	 * Absolute path of the file to send in local filesystem is "localDir/relPath".
	 * While server will get the path "locaDirName/relPath".
	 */
	public void createFile(String relPath) throws FileSyncBaseOpsException {
		System.out.println("createFile starts ...");

        HttpURLConnection conn = null;
        try {
            String target = "/repos" + "/" + System.getProperty("user.name") + "/" + localDirName + "/" + relPath;;
            URL url = new URL("http", host, port, target);
            //System.out.println("url = " + url);
            conn = (HttpURLConnection)url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            OutputStream os = conn.getOutputStream();
            DataOutputStream  dos = new DataOutputStream(os);
            dos.writeBoolean(false);
            dos.flush();    // flush is very important here!!!
            
            File file = new File(localDir, relPath);
			GZIPOutputStream gos = new GZIPOutputStream(os);
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[bufferSize];
            int bytesRead = fis.read(buffer);
            while(bytesRead != -1) {
                //System.out.println("bytesRead = " + bytesRead);
                gos.write(buffer, 0, bytesRead);
                bytesRead = fis.read(buffer);
            }

            fis.close();
            gos.finish();
			gos.close();

            dos.close();

			DataInputStream dis = new DataInputStream(conn.getInputStream());
			boolean res = dis.readBoolean();
			dis.close();
        }catch(MalformedURLException ex) {
            System.err.println(ex);
        }catch (IOException ex) {
           System.err.println(ex);
        }

        conn.disconnect();
	}

	public void mkdir(String relPath) throws FileSyncBaseOpsException {
		System.out.println("mkdir starts ...");

        HttpURLConnection conn = null;
        try {
            String target = "/repos" + "/" + System.getProperty("user.name") + "/" + localDirName + "/" + relPath;;
            URL url = new URL("http", host, port, target);
            System.out.println("url = " + url);
            conn = (HttpURLConnection)url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

			OutputStream os = conn.getOutputStream();
			DataOutputStream dos = new DataOutputStream(os);
			dos.writeBoolean(true);
			dos.flush();	// flush is very important here!!!
			dos.close();

			DataInputStream dis = new DataInputStream(conn.getInputStream());
			boolean res = dis.readBoolean();
			dis.close();
        }catch(MalformedURLException ex) {
            System.err.println(ex);
        }catch (IOException ex) {
           System.err.println(ex);
        }

        conn.disconnect();		
	}
	
	public void rmdir(String relPath) throws FileSyncBaseOpsException {
		System.out.println("rmdir starts");

		submitRequest(relPath, "DELETE");
	}

	public void upgradeFile(String relPath) throws FileSyncBaseOpsException {
		// TO IMPROVE

		createFile(relPath);
	}

	public void deleteFile(String relPath) throws FileSyncBaseOpsException {
		System.out.println("deleteFile starts ...");

		submitRequest(relPath, "DELETE");
	}

	public boolean exists(String relPath) throws FileSyncBaseOpsException {
		System.out.println("exists starts ...");

		boolean res = submitRequest(relPath, "GET");

		return res;
	}

	private boolean submitRequest(String relPath, String method) {
		System.out.println("submitRequest : relPath = " + relPath + ", method = " + method);

		boolean res = false;

        HttpURLConnection conn = null;
        try {
            String target = "/repos" + "/" + System.getProperty("user.name") + "/" + localDirName + "/" + relPath;
            URL url = new URL("http", host, port, target);
            System.out.println("url = " + url);
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod(method);

			DataInputStream dis = new DataInputStream(conn.getInputStream());
			res = dis.readBoolean();
			dis.close();
        }catch(MalformedURLException ex) {
            System.err.println(ex);
        }catch (IOException ex) {
           System.err.println(ex);
        }

		conn.disconnect();

		return res;
	}
}