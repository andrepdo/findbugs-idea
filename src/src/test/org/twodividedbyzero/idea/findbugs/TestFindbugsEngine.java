package org.twodividedbyzero.idea.findbugs;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * $Date$
 *
 * @version $Revision$
 */
public class TestFindbugsEngine {


	/**
	 * Sets up the test fixture.
	 * (Called ONCE before every test case.)
	 */
	@BeforeClass
	public static void onceOnlySetup() {

		//_reporter = new SwingGUIBugReporter(this);
		//_reporter.setPriorityThreshold(Detector.EXP_PRIORITY);
	}


	


	/**
	 * Sets up the test fixture.
	 * (Called before every test case method.)
	 */
	@Before
	public void setUp() {
	}


	/**
	 * Tears down the test fixture.
	 * (Called after every test case method.)
	 */
	@After
	public void tearDown() {
	}


	@Test
	public void testAnalysisRun() {

	}


	/*@Test(expected = IndexOutOfBoundsException.class)
	public void testForException() {

	}*/
}
