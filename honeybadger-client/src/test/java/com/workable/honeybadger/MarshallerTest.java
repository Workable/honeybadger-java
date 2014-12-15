package com.workable.honeybadger;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class MarshallerTest {

    private JsonMarshaller marshaller;
    Set<String> excludedSysProps = Collections.emptySet();

    @Before
    public void setup() throws Exception {
        marshaller = new JsonMarshaller(excludedSysProps);
    }

    @Test
    public void shouldMarshal() throws Exception {

        Throwable throwable = new IllegalArgumentException("something went wrong");
        Error error = new Error(throwable);
        String result = marshaller.marshall(error);

        Gson gson = new Gson();

        HoneybadgerError map = gson.fromJson(result, HoneybadgerError.class);

        Map<String, Object> errorMap = map.getError();

        //TODO add assertions

    }

    private static class HoneybadgerError {
        private Map<String, Object> error;

        public Map<String, Object> getError() {
            return error;
        }

        public void setError(Map<String, Object> error) {
            this.error = error;
        }
    }
}
