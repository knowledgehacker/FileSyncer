package main.sync.client;

import java.io.File;

public class Path {

	public static String normalize(String path) {
        String res = path;

		// path begins with '~'.
        if(path.charAt(0) == '~')
            res = System.getProperty("user.home") + path.substring(1);
        else if(path.charAt(0) == '.') {
			if(path.length() == 1)
				res = System.getProperty("user.dir");
			else if(path.charAt(1) != '.')
	            res = System.getProperty("user.dir") + path.substring(1);
			else {
				// path begins with ".."
				if((path.charAt(0) == '.') && (path.charAt(1) == '.')) {
	        		String userDir = System.getProperty("user.dir");
					int i = 0;
        			while((path.charAt(i) == '.') && (path.charAt(i+1) == '.')) {
        				int lastSlash = userDir.lastIndexOf(File.separator);
			            if(lastSlash == -1)
    			        	return null;

        	    		userDir = userDir.substring(0, lastSlash);
		            	i += 2;
						if(i >= path.length()-1)
							break;
						if(path.charAt(i) != File.separatorChar)
							return null;
						++i;	
    	    		}
		            String postfix = path.substring(i, path.length());
        		    if(postfix.equals(File.separator))
                		res = userDir;
		            else
        		        res = userDir + File.separator + postfix;
				}
			}
		}

		// Take the last '/' if exists away.
		if(res.charAt(res.length()-1) == File.separatorChar)
			res = res.substring(0, res.length()-1);

        //System.out.println("res = " + res);
		return res;
    }
}
