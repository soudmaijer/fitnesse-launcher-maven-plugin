package uk.co.javahelp.maven.plugin.fitnesse.reporting;

import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;


/**
 * @goal html-report
 * @phase site
 */
public class FitNesseHtmlReport extends AbstractMavenReport {
	
	/**
	 * See fitnesse-html-report.properties
	 */
	private static final String OUTPUT_NAME = "fitnesse-html-report";
	
    /**
     * Directory where reports will go.
     *
     * @parameter default-value="${project.reporting.outputDirectory}"
     * @required
     * @readonly
     */
    private String outputDirectory;
 
    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
 
    /**
     * @component
     * @required
     * @readonly
     */
    private Renderer siteRenderer;

	@Override
	public String getOutputName() {
		return OUTPUT_NAME;
	}

	@Override
	public String getName(Locale locale) {
		return getBundle( locale ).getString( "report.name" );
	}

	@Override
	public String getDescription(Locale locale) {
		return getBundle( locale ).getString( "report.description" );
	}

	@Override
	protected Renderer getSiteRenderer() {
		return this.siteRenderer;
	}

	@Override
	protected String getOutputDirectory() {
		return this.outputDirectory;
	}

	@Override
	protected MavenProject getProject() {
		return this.project;
	}
	
	private ResourceBundle getBundle( Locale locale ) {
	    return ResourceBundle.getBundle( OUTPUT_NAME, locale, this.getClass().getClassLoader() );
	}

	@Override
	protected void executeReport(Locale locale) throws MavenReportException {

		// Be aware of http://maven.apache.org/plugin-developers/common-bugs.html#Determining_the_Output_Directory_for_a_Site_Report
	}

}
