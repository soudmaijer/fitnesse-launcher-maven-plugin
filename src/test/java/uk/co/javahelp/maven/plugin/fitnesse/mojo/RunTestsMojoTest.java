package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Build;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.co.javahelp.maven.plugin.fitnesse.util.FitNesseHelper;
import fitnesse.junit.TestHelper;
import fitnesse.responders.run.formatters.PageInProgressFormatter;

public class RunTestsMojoTest {

	private static final String TEST_RESULT_XML =
			"<testsuite errors=\"0\" skipped=\"0\" tests=\"1\" time=\"[0-9]+.[0-9]+\" failures=\"1\" name=\"ExampleFitNesseTestSuite\">" +
				"<properties></properties>" +
				"<testcase classname=\"ExampleFitNesseTestSuite\" time=\"[0-9]+.[0-9]+\" name=\"ExampleFitNesseTestSuite\">" +
					"<failure type=\"java.lang.AssertionError\" message=\" exceptions: 0 wrong: 1\">" +
					"</failure>" +
				"</testcase>" +
			"</testsuite>";
	
	private static final String FAILSAFE_SUMMARY_XML =
		    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<failsafe-summary result=\"255\" />\n";
			
	private RunTestsMojo mojo;
	
    private ByteArrayOutputStream logStream;
    
	@Before
	public void setUp() throws IOException {
		File workingDir = new File(System.getProperty("java.io.tmpdir"), "unit_test_working");
		
		mojo = new RunTestsMojo();
		mojo.pluginDescriptor = mock(PluginDescriptor.class);;
		mojo.fitNesseHelper = mock(FitNesseHelper.class);
		mojo.port = WikiMojoTest.PORT;
		mojo.workingDir = workingDir.getCanonicalPath();
		mojo.root = "FitNesseRoot";
		mojo.resultsDir = new File(System.getProperty("java.io.tmpdir"), "unit_test_results");
		mojo.reportsDir = new File(System.getProperty("java.io.tmpdir"), "unit_test_reports");
		mojo.summaryFile = new File(mojo.resultsDir, "failsafe-summary.xml");
		mojo.project = new MavenProject();
		mojo.project.setFile(new File(getClass().getResource("pom.xml").getPath()));
		mojo.project.setBuild(new Build());
		mojo.project.getBuild().setTestOutputDirectory("test_out");
		
		logStream = new ByteArrayOutputStream();
		mojo.setLog(new DefaultLog(new PrintStreamLogger(
			Logger.LEVEL_INFO, "test", new PrintStream(logStream))));
		
		FileUtils.deleteQuietly(workingDir);
		FileUtils.deleteQuietly(mojo.resultsDir);
		FileUtils.deleteQuietly(mojo.reportsDir);
		
		File root = new File(workingDir, mojo.root);
		root.mkdirs();
		FileUtils.copyDirectoryToDirectory(new File(getClass().getResource("/files").getPath()), root);
		FileUtils.copyDirectoryToDirectory(new File(getClass().getResource("/ExampleFitNesseTestSuite").getPath()), root);
	}
	
	/**
	 * We have to clean up the mess made by {@link TestsInProgress} and {@link PageInProgressFormatter}.
	 */
	@After
	public void tearDown() {
		FileUtils.deleteQuietly(new File("FitNesseRoot"));
	}
	
	@Test
	public void testRunTestsMojoBasic() throws Exception {
		
		mojo.createSymLink = false;
		when(mojo.fitNesseHelper.calcPageNameAndType(anyString(), anyString()))
		    .thenReturn(new String[]{"ExampleFitNesseTestSuite", TestHelper.PAGE_TYPE_SUITE});
		
		mojo.executeInternal();
		
		verify(mojo.fitNesseHelper, never()).launchFitNesseServer(anyString(), anyString(), anyString(), anyString());
		verify(mojo.fitNesseHelper, never()).createSymLink(anyString(), anyString(), any(File.class), anyString(), anyInt());
		verify(mojo.fitNesseHelper, never()).shutdownFitNesseServer(anyString());
		
		assertEquals(FAILSAFE_SUMMARY_XML, FileUtils.readFileToString(mojo.summaryFile));
		
		assertTrue(
			FileUtils.readFileToString(
				new File(mojo.resultsDir, "TEST-ExampleFitNesseTestSuite.xml")).matches(TEST_RESULT_XML));
		
		assertEquals(
		    IOUtils.toString(getClass().getResourceAsStream("ExampleFitNesseTestSuite.html")),
			FileUtils.readFileToString(new File(mojo.reportsDir, "ExampleFitNesseTestSuite.html")).replaceAll("\r\n", "\n"));
	}
	
	@Test
	public void testRunTestsMojoCreateSymLink() throws Exception {
		
		mojo.createSymLink = true;
		when(mojo.fitNesseHelper.calcPageNameAndType(anyString(), anyString()))
		    .thenReturn(new String[]{"ExampleFitNesseTestSuite", TestHelper.PAGE_TYPE_SUITE});
		
		mojo.executeInternal();
		
		verify(mojo.fitNesseHelper, times(1)).launchFitNesseServer(WikiMojoTest.PORT_STRING, mojo.workingDir, mojo.root, mojo.logDir);
		verify(mojo.fitNesseHelper, times(1)).createSymLink(mojo.suite, mojo.test, mojo.project.getBasedir(), mojo.testResourceDirectory, WikiMojoTest.PORT);
		verify(mojo.fitNesseHelper, times(1)).shutdownFitNesseServer(WikiMojoTest.PORT_STRING);
		
		assertEquals(FAILSAFE_SUMMARY_XML, FileUtils.readFileToString(mojo.summaryFile));
		
		assertTrue(
			FileUtils.readFileToString(
				new File(mojo.resultsDir, "TEST-ExampleFitNesseTestSuite.xml")).matches(TEST_RESULT_XML));
		
		assertEquals(
		    IOUtils.toString(getClass().getResourceAsStream("ExampleFitNesseTestSuite.html")),
			FileUtils.readFileToString(new File(mojo.reportsDir, "ExampleFitNesseTestSuite.html")).replaceAll("\r\n", "\n"));
	}
	
	@Test
	public void testCreateSymLinkException() throws Exception {
		
		mojo.createSymLink = true;
		doThrow(new IOException("TEST")).when(mojo.fitNesseHelper).launchFitNesseServer(anyString(), anyString(), anyString(), anyString());
		
		try {
			mojo.executeInternal();
			fail("Expected MojoExecutionException");
		} catch (MojoExecutionException e) {
			assertEquals("Exception creating FitNesse SymLink", e.getMessage());
			assertEquals(IOException.class, e.getCause().getClass());
		}
		
		verify(mojo.fitNesseHelper, times(1)).launchFitNesseServer(anyString(), anyString(), anyString(), anyString());
		verify(mojo.fitNesseHelper, never()).createSymLink(anyString(), anyString(), any(File.class), anyString(), anyInt());
		verify(mojo.fitNesseHelper, times(1)).shutdownFitNesseServer(anyString());
		
		assertFalse(mojo.summaryFile.exists());
		assertFalse(new File(mojo.resultsDir, "TEST-ExampleFitNesseTestSuite.xml").exists());
		assertFalse(new File(mojo.reportsDir, "ExampleFitNesseTestSuite.html").exists());
	}
	
	@Test
	public void testSuiteAndTestException() throws Exception {
		when(mojo.fitNesseHelper.calcPageNameAndType(anyString(), anyString())).thenCallRealMethod();
		
		try {
			mojo.executeInternal();
			fail("Expected MojoExecutionException");
		} catch (MojoExecutionException e) {
			assertEquals("Exception running FitNesse tests", e.getMessage());
			assertEquals(IllegalArgumentException.class, e.getCause().getClass());
		}
	}
	
	@Test
	public void testSuiteOk() throws Exception {
		when(mojo.fitNesseHelper.calcPageNameAndType(anyString(), anyString())).thenCallRealMethod();
		
		mojo.suite = "ExampleFitNesseTestSuite";
		
		mojo.executeInternal();
		
		verify(mojo.fitNesseHelper, times(1)).calcPageNameAndType(anyString(), anyString());
	}
	
	@Test
	public void testWriteSummaryException() throws Exception {
		when(mojo.fitNesseHelper.calcPageNameAndType(anyString(), anyString())).thenCallRealMethod();
		
		mojo.suite = "ExampleFitNesseTestSuite";
		mojo.resultsDir.createNewFile();
		
		try {
			mojo.executeInternal();
			fail("Expected MojoExecutionException");
		} catch (MojoExecutionException e) {
			assertEquals("Exception writing Failsafe summary", e.getMessage());
			assertEquals(FileNotFoundException.class, e.getCause().getClass());
		}
	}
	
	@Test
	public void testSurefireReportParameters() {
		mojo.setSkipTests(true);
		assertFalse(mojo.isSkipTests());
		
		mojo.setSkipExec(true);
		assertFalse(mojo.isSkipExec());
		
		mojo.setSkip(true);
		assertFalse(mojo.isSkip());
		
		mojo.setFailIfNoTests(false);
		assertFalse(mojo.getFailIfNoTests());
		
		mojo.setFailIfNoTests(true);
		assertTrue(mojo.getFailIfNoTests());
		
		mojo.setTestFailureIgnore(false);
		assertTrue(mojo.isTestFailureIgnore());
		
		mojo.setBasedir(new File(""));
		assertEquals(mojo.project.getBasedir(), mojo.getBasedir());
		
		mojo.setTestClassesDirectory(new File(""));
		assertEquals(new File("test_out"), mojo.getTestClassesDirectory());
		
		File file = mojo.reportsDir;
		mojo.setReportsDirectory(null);
		assertNull(mojo.getReportsDirectory());
		assertNull(mojo.reportsDir);
		mojo.setReportsDirectory(file);
		assertSame(file, mojo.getReportsDirectory());
		assertSame(file, mojo.reportsDir);
	}
}
