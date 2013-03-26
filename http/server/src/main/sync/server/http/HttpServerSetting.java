package main.sync.server.http;

import java.io.File;
import java.util.HashMap;

public final class HttpServerSetting {
	public static final String REPOSITORY = System.getProperty("user.dir") 
		+ File.separatorChar + "webapps" + File.separatorChar + "repos";

	private static HashMap<String, String> settings;

	static {
		settings = new HashMap<String, String>();
		settings.put("server.host", "namenode");
		settings.put("server.port", "8099");
		settings.put("server.idletime.max", "1500");
		settings.put("server.threadpool.size", "20");
		settings.put("server.user.database.url", "");
	}

	public static String getProperty(String key) {
		return settings.get(key);
	}
}
