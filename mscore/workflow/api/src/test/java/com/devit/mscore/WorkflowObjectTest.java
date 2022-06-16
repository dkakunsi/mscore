package com.devit.mscore;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WorkflowObjectTest {

    @Test
    public void testToJson_Default() {
        assertTrue(new DummyWorkflowObject().toJson().isEmpty());
    }

    private static class DummyWorkflowObject implements WorkflowObject {
        @Override
        public String getId() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public void complete() {
        }
    }
}
