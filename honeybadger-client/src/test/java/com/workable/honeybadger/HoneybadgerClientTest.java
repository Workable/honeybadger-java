package com.workable.honeybadger;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class HoneybadgerClientTest {

    @Test
    public void testShouldExclude() {
        HoneybadgerClient client;

        client = new HoneybadgerClient(null, null, null, "com.workable.honeybadger");
        assertThat(client.shouldExclude(new IOException("This is a test")), is(true));

        client = new HoneybadgerClient(null, null, null, "com.workable.honeybadger.test");
        assertThat(client.shouldExclude(new IOException("This is a test")), is(false));
    }
}
