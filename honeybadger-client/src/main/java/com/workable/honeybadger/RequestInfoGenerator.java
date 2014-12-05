package com.workable.honeybadger;

import com.google.gson.JsonObject;

/**
 * Interface that parses the properties of an object and turns it into the the response JSON sent to Honeybadger.

 */
public interface RequestInfoGenerator<T> {

    /**
     * Generates JSON for a request object of a specified type.
     *
     * @param requestSource request object to parse
     * @return JSON representation of request object
     */
    JsonObject generateRequest(T requestSource);

    /**
     * Routes a generic request object to a specific
     *
     * @param requestSource request object to parse
     * @return JSON representation of request object
     */
    JsonObject routeRequest(Object requestSource);
}
