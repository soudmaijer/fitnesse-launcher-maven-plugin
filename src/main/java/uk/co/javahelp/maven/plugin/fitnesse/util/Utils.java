package uk.co.javahelp.maven.plugin.fitnesse.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Utils {

	private Utils() { }

    public static boolean isBlank(final String string) {
        return string == null || string.trim().equals("");
    }
    
    public static boolean isWindows() {
        return System.getProperty("os.name", "unknown").toLowerCase().startsWith("windows");
    }
    
    public static boolean whitespaceSituation(final String path) {
		return (path.contains(" ") && !isWindows());
    }
    
    public static String whitespaceWarning(final String path, final String comment) {
        return String.format("THERE IS WHITESPACE IN CLASSPATH ELEMENT [%s]%n%s", path, comment);
    }
    
    public static String getRelativePath(final File from, final File to) throws IOException {
    	final String canonicalFrom = from.getCanonicalPath();
    	final String canonicalTo = to.getCanonicalPath();
    	if(canonicalFrom.equals(canonicalTo)) {
    		return ".";
    	}
    	final String[] partsFrom = canonicalFrom.split("/");
    	final String[] partsTo = canonicalTo.split("/");
    	if(partsFrom.length == 0 || partsTo.length == 0) {
    		return canonicalTo;
    	}
    	final List<String> listFrom = new ArrayList<String>(Arrays.asList(partsFrom));
    	final List<String> listTo = new ArrayList<String>(Arrays.asList(partsTo));
    	
    	boolean same = true;
    	int i = 0;
    	while(same && i < partsFrom.length && i < partsTo.length) {
    		same = partsFrom[i].equals(partsTo[i]);
    		if(same) {
    			listFrom.remove(0);
    			listTo.remove(0);
    			i++;
    		}
    	}
    	
    	final StringBuilder relativePath = new StringBuilder();
    	final int fromSize = listFrom.size();
    	if(fromSize == partsFrom.length - 1) {
       	    relativePath.append("/");
    	} else {
    	    for(int j = 0 ; j < fromSize ; j++ ) {
        		relativePath.append("../");
    		}
    	}
    	for(final String part : listTo) {
        	relativePath.append(part);
        	relativePath.append("/");
    	}
    	relativePath.deleteCharAt(relativePath.length() - 1);
    	return relativePath.toString();
    }
}
