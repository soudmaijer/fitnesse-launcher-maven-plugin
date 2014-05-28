package uk.co.javahelp.maven.plugin.fitnesse.responders.run;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import util.TimeMeasurement;
import fitnesse.responders.run.ResultsListener;
import fitnesse.testsystems.CompositeExecutionLog;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.wiki.WikiPageDummy;

public class DelegatingResultsListenerTest {
	
	private static final int DELEGATE_COUNT = 3;

	private DelegatingResultsListener delegatingListener;
	
    private ResultsListener[] delegates;
    
    @Before
    public void setUp() {
    	delegates = new ResultsListener[DELEGATE_COUNT];
    	
    	for(int i = 0 ; i < DELEGATE_COUNT ; i++) {
    		delegates[i] = mock(ResultsListener.class);
    	}
    	
    	delegatingListener = new DelegatingResultsListener(delegates);
    }
    
    @Test
    public void testAllTestingComplete() throws Exception {
        TimeMeasurement totalTimeMeasurement = new TimeMeasurement();
        
    	delegatingListener.allTestingComplete(totalTimeMeasurement);
    	
    	for(int i = 0 ; i < DELEGATE_COUNT ; i++) {
    		verify(delegates[i], times(1)).allTestingComplete(totalTimeMeasurement);
    	}
    }
    
    @Test
    public void testSetExecutionLogAndTrackingId() throws Exception {
    	String stopResponderId = "stopResponderId";
    	CompositeExecutionLog log = new CompositeExecutionLog(new WikiPageDummy());
    	
    	delegatingListener.setExecutionLogAndTrackingId(stopResponderId, log);
    	
    	for(int i = 0 ; i < DELEGATE_COUNT ; i++) {
    		verify(delegates[i], times(1)).setExecutionLogAndTrackingId(stopResponderId, log);
    	}
    }
    
    @Test
    public void testAnnounceNumberTestsToRun() {
    	int testsToRun = 12345;
    	
    	delegatingListener.announceNumberTestsToRun(testsToRun);
    	
    	for(int i = 0 ; i < DELEGATE_COUNT ; i++) {
    		verify(delegates[i], times(1)).announceNumberTestsToRun(testsToRun);
    	}
    }
    
    @Test
    public void testTestSystemStarted() throws Exception {
    	TestSystem testSystem = mock(TestSystem.class);
    	String testSystemName = "testSystemName";
    	String testRunner = "testRunner";
    	
    	delegatingListener.testSystemStarted(testSystem, testSystemName, testRunner);
    	
    	for(int i = 0 ; i < DELEGATE_COUNT ; i++) {
    		verify(delegates[i], times(1)).testSystemStarted(testSystem, testSystemName, testRunner);
    	}
    }
    
    @Test
    public void testNewTestStarted() throws Exception {
    	TestPage test = new TestPage(new WikiPageDummy());
        TimeMeasurement timeMeasurement = new TimeMeasurement();
    	
    	delegatingListener.newTestStarted(test, timeMeasurement);
    	
    	for(int i = 0 ; i < DELEGATE_COUNT ; i++) {
    		verify(delegates[i], times(1)).newTestStarted(test, timeMeasurement);
    	}
    }
    
    @Test
    public void testTestOutputChunk() throws Exception {
    	String output = "output";
    	
    	delegatingListener.testOutputChunk(output);
    	
    	for(int i = 0 ; i < DELEGATE_COUNT ; i++) {
    		verify(delegates[i], times(1)).testOutputChunk(output);
    	}
    }
    
    @Test
    public void testTestComplete() throws Exception {
    	TestPage test = new TestPage(new WikiPageDummy());
        TestSummary testSummary = new TestSummary();
        TimeMeasurement timeMeasurement = new TimeMeasurement();
    	
    	delegatingListener.testComplete(test, testSummary, timeMeasurement);
    	
    	for(int i = 0 ; i < DELEGATE_COUNT ; i++) {
    		verify(delegates[i], times(1)).testComplete(test, testSummary, timeMeasurement);
    	}
    }
    
    @Test
    public void testErrorOccured() {
    	delegatingListener.errorOccured();
    	
    	for(int i = 0 ; i < DELEGATE_COUNT ; i++) {
    		verify(delegates[i], times(1)).errorOccured();
    	}
    }
}
