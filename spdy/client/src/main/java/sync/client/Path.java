/**
 * Copyright (c) 2013 minglin. All rights reserved.

 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package sync.client;

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
