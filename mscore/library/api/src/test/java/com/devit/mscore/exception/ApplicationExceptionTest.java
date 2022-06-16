package com.devit.mscore.exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ApplicationExceptionTest {
    
    @Test
    public void testGetMessage_UseDirectMessage() {
        var cause = new Exception("Inner message");
        var ex = new ApplicationException("Error message", cause);
        assertThat(ex.getMessage(), is("Error message"));
    }
    
    @Test
    public void testGetMessage_UseInnerMessage() {
        var cause = new Exception("Inner message");
        var ex = new ApplicationException(cause);
        assertThat(ex.getMessage(), is("Inner message"));
    }
    
    @Test
    public void testGetMessage_UseDefaultMessage() {
        var cause = new Exception();
        var ex = new ApplicationException(cause);
        assertThat(ex.getMessage(), is("java.lang.Exception"));
    }
    
    @Test
    public void testGetMessage_UseExceptionWord() {
        var cause = new Exception("Inner message");
        var ex = new ApplicationException("java.lang.Exception", cause);
        assertThat(ex.getMessage(), is("Inner message"));
    }

    @Test
    public void testGetMessage_NoCauseAndNoMessage() {
        var ex = new ApplicationException((String) null);
        assertNull(ex.getMessage());
    }
}
