package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import uk.co.javahelp.maven.plugin.fitnesse.util.FitNesseHelper;
import fitnesse.Arguments;

/**
 * Goal that shuts down FitNesse.
 * This is done by sending GET to http://localhost:&lt;port&gt;/?responder=shutdown.
 *
 * @goal shutdown
 */
public class ShutdownMojo extends org.apache.maven.plugin.AbstractMojo {
	
    /**
     * @parameter property="fitnesse.port" default-value="9123"
     */
    private String port = Integer.toString(Arguments.DEFAULT_COMMAND_PORT);

	@Override
    public final void execute() {
       	new FitNesseHelper(getLog()).shutdownFitNesseServer(port);
       	getLog().info("FitNesse wiki server is shutdown.");
    }
}
