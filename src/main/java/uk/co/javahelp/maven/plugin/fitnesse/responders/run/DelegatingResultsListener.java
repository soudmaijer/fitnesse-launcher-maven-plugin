package uk.co.javahelp.maven.plugin.fitnesse.responders.run;

import java.io.IOException;

import util.TimeMeasurement;
import fitnesse.responders.run.ResultsListener;
import fitnesse.testsystems.CompositeExecutionLog;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;
import fitnesse.testsystems.slim.tables.Assertion;

public class DelegatingResultsListener implements ResultsListener {

    private final ResultsListener[] delegates;

    public DelegatingResultsListener(final ResultsListener... delegates) {
        this.delegates = delegates;
    }

	@Override
    public final void allTestingComplete(final TimeMeasurement totalTimeMeasurement)
            throws IOException {
        for(ResultsListener delegate : this.delegates) {
            delegate.allTestingComplete(totalTimeMeasurement);
        }
    }

	@Override
    public final void setExecutionLogAndTrackingId(final String stopResponderId, final CompositeExecutionLog log) {
        for(ResultsListener delegate : this.delegates) {
            delegate.setExecutionLogAndTrackingId(stopResponderId, log);
        }
    }

	@Override
    public final void announceNumberTestsToRun(final int testsToRun) {
        for(ResultsListener delegate : this.delegates) {
            delegate.announceNumberTestsToRun(testsToRun);
        }
    }

	@Override
    public final void testSystemStarted(final TestSystem testSystem, final String testSystemName, final String testRunner) {
        for(ResultsListener delegate : this.delegates) {
            delegate.testSystemStarted(testSystem, testSystemName, testRunner);
        }
    }

	@Override
    public final void newTestStarted(final TestPage test, final TimeMeasurement timeMeasurement)
            throws IOException {
        for(ResultsListener delegate : this.delegates) {
            delegate.newTestStarted(test, timeMeasurement);
        }
    }

	@Override
    public final void testOutputChunk(final String output) throws IOException {
        for(ResultsListener delegate : this.delegates) {
            delegate.testOutputChunk(output);
        }
    }

	@Override
	public void testAssertionVerified(final Assertion assertion, final TestResult testResult) {
        for(ResultsListener delegate : this.delegates) {
            delegate.testAssertionVerified(assertion, testResult);
        }
	}

	@Override
	public void testExceptionOccurred(final Assertion assertion, final ExceptionResult exceptionResult) {
        for(ResultsListener delegate : this.delegates) {
            delegate.testExceptionOccurred(assertion, exceptionResult);
        }
	}

	@Override
    public final void testComplete(final TestPage test, final TestSummary testSummary, final TimeMeasurement timeMeasurement)
            throws IOException {
        for(ResultsListener delegate : this.delegates) {
            delegate.testComplete(test, testSummary, timeMeasurement);
        }
    }

	@Override
    public final void errorOccured() {
        for(ResultsListener delegate : this.delegates) {
            delegate.errorOccured();
        }
    }
}
