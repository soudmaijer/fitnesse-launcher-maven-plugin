package uk.co.javahelp.maven.plugin.fitnesse.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.ConnectException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.co.javahelp.maven.plugin.fitnesse.mojo.PrintStreamLogger;
import fitnesse.Arguments;

public class CreateSymLinkTest {

    private static final String BASE_DIR = "CreateSymLinkTest_BASEDIR";
    
    private static final String SYMLINK_URL_EXPECTED = "responder=symlink&linkName=SuiteName&linkPath=file%3A%2F%2F%2Ftmp%2F" + BASE_DIR + "%2Fsrc%2Ftest%2Ffitnesse%2FSuiteName&submit=Create%2FReplace";
        
    private static final String SYMLINK_LOG_EXPECTED = String.format("[INFO] Calling http://localhost:9123/root?%s%n[INFO] Response code: 200%n", SYMLINK_URL_EXPECTED);
    
	private FitNesseHelper fitNesseHelper;
	
    private ByteArrayOutputStream logStream;
    
    private File baseDir;
    
    private File baseDirWhitespace;
    
	@Before
	public void setUp() {
		logStream = new ByteArrayOutputStream();
		Log log = new DefaultLog(new PrintStreamLogger(
			Logger.LEVEL_INFO, "test", new PrintStream(logStream)));
		
		fitNesseHelper = new FitNesseHelper(log);
		
        baseDir = new File("/tmp", BASE_DIR);
        baseDir.mkdir();
		
        baseDirWhitespace = new File("/tmp", BASE_DIR.replace("BASEDIR", "BASE DIR"));
        baseDirWhitespace.mkdir();
	}
	
	@After
	public void tearDown() {
		FileUtils.deleteQuietly(baseDir);
		FileUtils.deleteQuietly(baseDirWhitespace);
	}
		
    private static String whitespace(String symlink) {
    	return symlink.replace("BASEDIR", "BASE+DIR");
    }
        
	@Test
	public void testCreateSymLinkOkSuite() throws Exception {
		int port = Arguments.DEFAULT_COMMAND_PORT;
		Server server = new Server(port);
	    server.setHandler(new OkHandler("/root", SYMLINK_URL_EXPECTED));
	    server.start();
	    
	    try {
			int response = fitNesseHelper.createSymLink(
				"SuiteName.NestedSuite", null, baseDir,
				"src/test/fitnesse", port);
			
			assertEquals(200, response);
			assertEquals(SYMLINK_LOG_EXPECTED, logStream.toString());
		} finally {
    		server.stop();
		}
	}
		
	@Test
	public void testCreateSymLinkWhitespace() throws Exception {
		int port = Arguments.DEFAULT_COMMAND_PORT;
		Server server = new Server(port);
	    server.setHandler(new OkHandler("/root", whitespace(SYMLINK_URL_EXPECTED)));
	    server.start();
	    
	    try {
			int response = fitNesseHelper.createSymLink(
				"SuiteName.NestedSuite", null, baseDirWhitespace,
				"src/test/fitnesse", port);
			
			assertEquals(200, response);
			assertEquals(whitespace(SYMLINK_LOG_EXPECTED), logStream.toString());
		} finally {
    		server.stop();
		}
	}
		
	@Test
	public void testCreateSymLinkOkTest() throws Exception {
		int port = Arguments.DEFAULT_COMMAND_PORT;
		Server server = new Server(port);
	    server.setHandler(new OkHandler("/root", SYMLINK_URL_EXPECTED));
	    server.start();
	    
	    try {
			int response = fitNesseHelper.createSymLink(
				null, "SuiteName.NestedSuite.TestName", baseDir,
				"src/test/fitnesse", port);
			
			assertEquals(200, response);
			assertEquals(SYMLINK_LOG_EXPECTED, logStream.toString());
		} finally {
    		server.stop();
		}
	}
		
	@Test
	public void testCreateSymLinkDisconnect() throws Exception {
		int port = Arguments.DEFAULT_COMMAND_PORT;
		Server server = new Server(port);
	    server.setHandler(new DisconnectingHandler(server));
	    server.start();
	    
	    try {
			fitNesseHelper.createSymLink(
				"SuiteName.NestedSuite", null, baseDir,
				"src/test/fitnesse", port);
			
			fail("Expected ConnectException");

		} catch(ConnectException e) {
			// OK
		} finally {
    		server.stop();
		}
	}
}
