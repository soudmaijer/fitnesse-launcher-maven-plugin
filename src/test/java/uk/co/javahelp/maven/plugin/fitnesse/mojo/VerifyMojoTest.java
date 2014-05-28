package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.logging.Logger;
import org.junit.Before;
import org.junit.Test;

public class VerifyMojoTest {

	private VerifyMojo mojo;
	
    private ByteArrayOutputStream logStream;
    
	@Before
	public void setUp() throws IOException {
		
		mojo = new VerifyMojo();
		
		logStream = new ByteArrayOutputStream();
		mojo.setLog(new DefaultLog(new PrintStreamLogger(
			Logger.LEVEL_INFO, "test", new PrintStream(logStream))));
	}
	
	@Test
	public void testSuccess() throws Exception {
		
		mojo.summaryFile = new File(getClass().getResource("verify-success.xml").getPath());
		
		mojo.execute();
		
		assertEquals("", logStream.toString());
	}
	
	@Test
	public void testFailure() throws Exception {
		
		mojo.summaryFile = new File(getClass().getResource("verify-failure.xml").getPath());
		
		try {
			mojo.execute();
			fail("Expected MojoFailureException");
		} catch (MojoFailureException e) {
    		assertEquals("There are test failures.\n\n" +
   				String.format("Please refer to %s for the individual test results.",
   						mojo.reportsDir), e.getMessage());
		}
		
		assertEquals("", logStream.toString());
	}
	
	@Test
	public void testNoTests() throws Exception {
		
		mojo.summaryFile = new File(getClass().getResource("verify-no-tests.xml").getPath());
		
		mojo.execute();
		
		assertEquals("", logStream.toString());
	}
	
	@Test
	public void testBadXml() throws Exception {
		
		mojo.summaryFile = new File(getClass().getResource("verify-bad-xml.xml").getPath());
		
		try {
			mojo.execute();
			fail("Expected MojoExecutionException");
		} catch (MojoExecutionException e) {
    		assertEquals("no more data available - expected end tag </failsafe-summary> to close start tag <failsafe-summary> from line 1, " +
    				"parser stopped on START_DOCUMENT seen <failsafe-summary result=\"THIS IS NOT XML!\\n... @2:1", e.getMessage());
		}
		
		assertEquals("", logStream.toString());
	}
	
	@Test
	public void testBadXml2() throws Exception {
		
		mojo.summaryFile = new File(getClass().getResource("verify-not-failsafe.xml").getPath());
		
		try {
			mojo.execute();
			fail("Expected MojoExecutionException");
		} catch (MojoExecutionException e) {
    		assertEquals("Expected root element 'failsafe-summary' but found 'not-failsafe-summary' (position: " +
    				"START_TAG seen ...ion=\"1.0\" encoding=\"UTF-8\"?>\\n<not-failsafe-summary result=\"255\" />... @2:38) ", e.getMessage());
		}
		
		assertEquals("", logStream.toString());
	}
}
