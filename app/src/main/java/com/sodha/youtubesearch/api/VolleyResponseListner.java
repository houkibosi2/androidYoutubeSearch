package com.sodha.youtubesearch.api;

/**
 * Created by sodha on 8/3/16.
 */
public interface VolleyResponseListner {
    void onError(String message);

    void onResponse(Object response);
}
