public class TestSyncOps {

   public void testCreateFile(String relPath) {
		System.out.println("testCreateFile starts: relPath = " + relPath);

        HttpURLConnection conn = null;
        try {
            String target = "/repos";
            URL url = new URL("http", host, port, target);
            System.out.println("url = " + url);
            conn = (HttpURLConnection)url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            OutputStream os = conn.getOutputStream();
            PrintWriter out = new PrintWriter(os);
            out.println(System.getProperty("user.name"));
            out.println(localDirName + File.separator + relPath);
			out.println("dir");
			out.flush();	// flush is very important here!!!

            File file = new File(localDir, relPath);
            FileInputStream fis = new FileInputStream(file);
			int bufferSize = 128 * 1024;
            byte[] buffer = new byte[bufferSize];
            int bytesRead = fis.read(buffer);
            while(bytesRead != -1) {
                os.write(buffer, 0, bytesRead);
                bytesRead = fis.read(buffer);
            }
            fis.close();
			out.flush();

            out.close();

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String respMsg = reader.readLine();
            System.out.println("respMsg = " + respMsg);
            reader.close();
        }catch(MalformedURLException ex) {
            System.err.println(ex);
        }catch (IOException ex) {
           System.err.println(ex);
        }

        conn.disconnect();
		
		System.out.println("testCreateFile ends ...");
    }
 
   public void testCreateDir(String relPath) {
		System.out.println("testCreateDir starts: relPath = " + relPath);

        HttpURLConnection conn = null;
        try {
            String target = "/repos";
            URL url = new URL("http", host, port, target);
            System.out.println("url = " + url);
            conn = (HttpURLConnection)url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            OutputStream os = conn.getOutputStream();
            PrintWriter out = new PrintWriter(os);
            out.println(System.getProperty("user.name"));
            out.println(localDirName + File.separator + relPath);
			out.println("dir");
			out.flush();	// flush is very important here!!!
            out.close();

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String respMsg = reader.readLine();
            System.out.println("respMsg = " + respMsg);
            reader.close();
        }catch(MalformedURLException ex) {
            System.err.println(ex);
        }catch (IOException ex) {
           System.err.println(ex);
        }

        conn.disconnect();
		
		System.out.println("testCreateDir ends ...");
    }

    public void testDelete(String relPath) {
		System.out.println("testDelete starts: relPath = " + relPath);

        HttpURLConnection conn = null;
        try {
            String target = "/repos" + "/" + System.getProperty("user.name") + "/" + localDirName + "/" + relPath;
            URL url = new URL("http", host, port, target);
            System.out.println("url = " + url);
            conn = (HttpURLConnection)url.openConnection();
			//conn.setDoOutput(true);
            conn.setRequestMethod("DELETE");

			/*
            OutputStream os = conn.getOutputStream();
            PrintWriter out = new PrintWriter(os);
            out.println(System.getProperty("user.name"));
            out.println(localDirName + File.separator + relPath);
			out.flush();	// flush is very important here!!!
            out.close();
			*/

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String respMsg = reader.readLine();
            System.out.println("respMsg = " + respMsg);
            reader.close();
        }catch(MalformedURLException ex) {
            System.err.println(ex);
        }catch (IOException ex) {
           System.err.println(ex);
        }

        conn.disconnect();
		
		System.out.println("testDelete ends ...");
    }
}
