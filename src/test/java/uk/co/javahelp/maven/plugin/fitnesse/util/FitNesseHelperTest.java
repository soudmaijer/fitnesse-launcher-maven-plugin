package uk.co.javahelp.maven.plugin.fitnesse.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.jetty.server.Server;
import org.junit.Before;
import org.junit.Test;

import uk.co.javahelp.maven.plugin.fitnesse.mojo.PrintStreamLogger;
import fitnesse.Arguments;
import fitnesse.junit.TestHelper;

public class FitNesseHelperTest {

	private FitNesseHelper fitNesseHelper;
	
	private ArtifactHandler artifactHandler;
	
    private ByteArrayOutputStream logStream;
    
	@Before
	public void setUp() {
		artifactHandler = mock(ArtifactHandler.class);
		
		logStream = new ByteArrayOutputStream();
		Log log = new DefaultLog(new PrintStreamLogger(
			Logger.LEVEL_INFO, "test", new PrintStream(logStream)));
		
		fitNesseHelper = new FitNesseHelper(log);
	}
	
	@Test
	public void testCalcPageNameAndTypeSuite() {
		
		String[] result = fitNesseHelper.calcPageNameAndType("SuiteName", null);
		assertEquals(2, result.length);
		assertEquals("SuiteName", result[0]);
		assertEquals(TestHelper.PAGE_TYPE_SUITE, result[1]);
	}
		
	@Test
	public void testCalcPageNameAndTypeTest() {
		
		String[] result = fitNesseHelper.calcPageNameAndType(null, "SuiteName.NestedSuite.TestName");
		assertEquals(2, result.length);
		assertEquals("SuiteName.NestedSuite.TestName", result[0]);
		assertEquals(TestHelper.PAGE_TYPE_TEST, result[1]);
	}
		
	@Test
	public void testCalcPageNameAndTypeIllegalBoth() {
		try {
			fitNesseHelper.calcPageNameAndType("SuiteName", "SuiteName.NestedSuite.TestName");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertEquals("Suite and test page parameters are mutually exclusive", e.getMessage());
		}
	}
		
	@Test
	public void testCalcPageNameAndTypeIllegalNeither() {
	    assertCalcPageNameAndTypeIllegalNeither(null, null);
	    assertCalcPageNameAndTypeIllegalNeither(" ", " ");
	}
	
	private void assertCalcPageNameAndTypeIllegalNeither(String suite, String test) {
		try {
			fitNesseHelper.calcPageNameAndType(suite, test);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertEquals("No suite or test page specified", e.getMessage());
		}
	}
		
	@Test
	public void testFormatAndAppendClasspath() {
		// Save the real os.name
		String os = System.getProperty("os.name");
	    
		System.setProperty("os.name", "windows");
	    assertFormatAndAppendClasspath("");
		
		System.setProperty("os.name", "linux");
	    assertFormatAndAppendClasspath(String.format(
	    		"[ERROR] THERE IS WHITESPACE IN CLASSPATH ELEMENT [/x/y z]%nFitNesse classpath may not function correctly in wiki mode%n"));
		
		// Restore the real os.name (to prevent side-effects on other tests)
		System.setProperty("os.name", os);
	}
		
	private void assertFormatAndAppendClasspath(String expectedLogMsg) {
        
		StringBuilder sb = new StringBuilder();
		
		assertSame(sb, fitNesseHelper.formatAndAppendClasspath(sb, "/x/y/z"));
		assertEquals("!path /x/y/z\n", sb.toString());
		assertEquals("", logStream.toString());
	    
		assertSame(sb, fitNesseHelper.formatAndAppendClasspath(sb, "/x/y z"));
		assertEquals("!path /x/y/z\n!path /x/y z\n", sb.toString());
		assertEquals(expectedLogMsg, logStream.toString());
	}
		
	@Test
	public void testFormatAndAppendClasspathArtifact() {
        String jarPath = new File(getClass().getResource("/dummy.jar").getPath()).getPath();
        Artifact artifact = new DefaultArtifact(
            "org.fitnesse", "fitnesse", "20130530", "compile", "jar", null, artifactHandler);
        artifact.setFile(new File(jarPath));
        
		StringBuilder sb = new StringBuilder();
		assertSame(sb, fitNesseHelper.formatAndAppendClasspathArtifact(sb, artifact));
		
		assertEquals("!path " + jarPath + "\n", sb.toString());
	}
		
	@Test
	public void testLaunchFitNesseServer() throws Exception {
		File logDir = new File(System.getProperty("java.io.tmpdir"), "fitnesse-launcher-logs");
		// Clean out logDir, as it might still exist from a previous run, 
		// because Windows doesn't always delete this file on exit
  		FileUtils.deleteQuietly(logDir);
		assertLaunchFitNesseServer(null);
		assertLaunchFitNesseServer(" ");
		assertLaunchFitNesseServer(logDir.getCanonicalPath());
		String[] logFiles = logDir.list();
		assertEquals(1, logFiles.length);
		assertTrue(logFiles[0].matches("fitnesse[0-9]+\\.log"));
		FileUtils.forceDeleteOnExit(logDir);
	}
		
	public void assertLaunchFitNesseServer(String logDir) throws Exception {
		String port = String.valueOf(Arguments.DEFAULT_COMMAND_PORT);
		File working = new File(System.getProperty("java.io.tmpdir"), "fitnesse-launcher-test");
		fitNesseHelper.launchFitNesseServer(port, working.getCanonicalPath(), "FitNesseRoot", logDir);
		URL local = new URL("http://localhost:" + port);
		InputStream in = local.openConnection().getInputStream();
		try {
			String content = IOUtils.toString(in);
			assertTrue(content.startsWith("<!DOCTYPE html>"));
			assertTrue(content.contains("<title>Page doesn't exist. Edit: FrontPage</title>"));
		} finally {
			IOUtils.closeQuietly(in);
    		fitNesseHelper.shutdownFitNesseServer(port);
    		Thread.sleep(100L);
			FileUtils.deleteQuietly(working);
		}
	}
		
	@Test
	public void testShutdownFitNesseServerOk() throws Exception {
		int port = Arguments.DEFAULT_COMMAND_PORT;
		Server server = new Server(port);
	    server.setHandler(new OkHandler("/", "responder=shutdown"));
	    server.start();
	    
	    try {
			fitNesseHelper.shutdownFitNesseServer(String.valueOf(port));
		} finally {
    		server.stop();
		}
	}
	
	@Test
	public void testShutdownFitNesseServerNotRunning() throws Exception {
		int port = Arguments.DEFAULT_COMMAND_PORT;
		fitNesseHelper.shutdownFitNesseServer(String.valueOf(port));
		assertEquals(String.format("[INFO] FitNesse already not running.%n"), logStream.toString());
	}
	
	@Test
	public void testShutdownFitNesseServerDisconnect() throws Exception {
		int port = Arguments.DEFAULT_COMMAND_PORT;
		Server server = new Server(port);
	    server.setHandler(new DisconnectingHandler(server));
	    server.start();
	    
	    try {
			fitNesseHelper.shutdownFitNesseServer(String.valueOf(port));
			
			assertTrue(logStream.toString().startsWith(String.format("[ERROR] %njava.io.IOException: Could not parse Response")));
		} finally {
    		server.stop();
		}
	}
}
