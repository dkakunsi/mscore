package com.devit.mscore.history;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import com.devit.mscore.Configuration;
import com.devit.mscore.exception.ConfigException;

import org.junit.Before;
import org.junit.Test;

public class ApplicationStarterTest {

    private Configuration configuration;

    @Before
    public void setup() throws ConfigException {
        this.configuration = mock(Configuration.class);
        doReturn("history").when(this.configuration).getServiceName();
        doReturn(Optional.of("topic1,topic2")).when(this.configuration).getConfig("services.history.topics");
    }

    @Test
    public void testGetTopicsToListen() throws ConfigException {
        var topics = ApplicationStarter.getTopicsToListen(this.configuration);
        assertThat(topics.size(), is(2));
        assertThat(topics.get(0), is("topic1"));
        assertThat(topics.get(1), is("topic2"));
    }
}
