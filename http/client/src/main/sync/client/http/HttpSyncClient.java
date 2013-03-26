package main.sync.client.http;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import java.io.File;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;

import main.sync.client.ClientSetting;
import main.sync.client.Path;
import main.sync.client.FileSyncOps;
import main.sync.client.SyncClient;

public class HttpSyncClient extends SyncClient implements Runnable {
	private final String localDir;
	private final String localDirName;
	private final String host;
	private final int port;

	public HttpSyncClient(String localDir, FileSyncOps syncOps, String host, int port) {
		super(localDir, syncOps);

		this.localDir = localDir;
		this.localDirName = localDir.substring(localDir.lastIndexOf(File.separator)+1);
		this.host = host;
		this.port = port;
	}

	public boolean connect() {
		boolean result = false;

        HttpURLConnection conn = null;
        try {
            URL url = new URL("http", host, port, "/index.html");
			System.out.println("url = " + url);
            conn = (HttpURLConnection)url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
	
			OutputStream os = conn.getOutputStream();
            PrintWriter out = new PrintWriter(os);
			out.println(System.getProperty("user.name"));
			out.println(localDirName);
			out.close();
			
			DataInputStream dis = new DataInputStream(conn.getInputStream());
			result = dis.readBoolean();
			dis.close();
        }catch(MalformedURLException ex) {
            System.err.println(ex);
        }catch (IOException ex) {
            System.err.println(ex);
        }
        conn.disconnect();
		
		return result;
	}

	public void disconnect() {
		// nothing to do ...
	}

	public void run() {
        initialize();

        try{
            Thread.sleep(Integer.parseInt(ClientSetting.getProperty("check.delay.per.run"))*20);
        }catch(InterruptedException ie) {
        }

        finalize();
    }

    public static void main(String args[]) {
        if(args.length < 2) {
            System.out.println("Usage: java -jar HttpSyncClient.jar localdir host:port");
            return;
        }

		String server = args[1];
		int colon = server.indexOf(':');
		String host = server.substring(0, colon);
		int port = Integer.parseInt(server.substring(colon+1));
		String localDir = Path.normalize(args[0]);
		FileSyncOpsOverURL syncOps = new FileSyncOpsOverURL(localDir, host, port);
		//FileSyncOpsOverURLCompressed syncOps = new FileSyncOpsOverURLCompressed(localDir, host, port);
        Thread syncThread = new Thread(new HttpSyncClient(localDir, syncOps, host, port));
        syncThread.start();
    }
}
