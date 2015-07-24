package net.sf.sitemonitoring.service;

import static org.junit.Assert.*;

import org.junit.Test;

public class ErrorUtilsTest {
	
	@Test
	public void testGetErrorShouldReturnNull() {
		assertNull(ErrorUtils.getError(null));
	}
	
	@Test
	public void testGetErrorShouldReturnMessage() {
		Exception exception = new Exception("custom error message");
		assertEquals("custom error message", ErrorUtils.getError(exception));
	}
	
	@Test
	public void testGetErrorShouldReturnMessageAndCauseMessage() {
		Exception exceptionCause = new Exception("cause");
		Exception exception = new Exception("custom error message", exceptionCause);
		assertEquals("custom error message, cause", ErrorUtils.getError(exception));
	}

	@Test
	public void testGetErrorShouldReturnMessageAndCauseMessage2ndLevel() {
		Exception exceptionCause = new Exception("cause", new Exception("cause 2"));
		Exception exception = new Exception("custom error message", exceptionCause);
		assertEquals("custom error message, cause, cause 2", ErrorUtils.getError(exception));
	}

}
