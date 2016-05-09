package com.github.sdw8001.sample;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;

/**
 * Created by sdw80 on 2016-04-20.
 */
public interface MyJsonService {

    @GET("/1kpjf")
    void listEvents(Callback<List<Event>> eventsCallback);

}