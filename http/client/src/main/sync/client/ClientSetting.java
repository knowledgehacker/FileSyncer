package main.sync.client;

import java.util.HashMap;

public class ClientSetting {
	public static final String NEWLINE = System.getProperty("line.separator");
	public static final String LOCALUSER = System.getProperty("user.name");

	private static final HashMap<String, String> settings;

	static {
		settings = new HashMap<String, String>();
		settings.put("checks.per.run", "1000");
		settings.put("check.delay.per.run", "1000");
	}

	public static String getProperty(String key) {
		return settings.get(key);
	}
}
		
