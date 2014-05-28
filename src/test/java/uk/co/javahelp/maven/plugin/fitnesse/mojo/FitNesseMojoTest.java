package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;
import org.junit.Test;

public class FitNesseMojoTest {
	
	private FitNesseMojoTestHelper helper;
	
	@Before
	public void setUp() {
		helper = new FitNesseMojoTestHelper();
	}
	
	@Test
	public void testExecute() throws MojoExecutionException, MojoFailureException, IOException {
		String expected = IOUtils.toString(FitNesseMojoTest.class.getResourceAsStream("exec-output.log"));
		
		helper.mojo.execute();
		
		assertTrue(helper.executeCalled);
		assertEquals(format(expected.replaceAll("[\n\r]", ""),
				this.getClass().getResource("/dummy.jar").getFile(),
				helper.mojo.project.getBasedir()),
				helper.logStream.toString());
	}
}
