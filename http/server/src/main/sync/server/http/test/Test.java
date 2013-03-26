
public class Test{

	public static String URI2Path(String uri) {
		//char separator = File.separatorChar;
		char separator = '\\';
		StringBuilder path = new StringBuilder(uri);
		int index = uri.indexOf('/');
		while(index != -1) {
			System.out.println("index = " + index);
			path.setCharAt(index, separator);
			index = uri.indexOf('/', index+1);
		}

		return path.toString();
	}
	
	public static void main(String[] args) {
		String uri = "repos/minglin/client/HttpSyncClient.jar";
		String path = URI2Path(uri);
		System.out.println("path = " + path);
	}
}
