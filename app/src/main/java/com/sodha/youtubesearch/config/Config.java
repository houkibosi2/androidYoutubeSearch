package com.sodha.youtubesearch.config;

/**
 * Created by sodha on 8/3/16.
 */
public class Config {
    public static final String API_KEY = "AIzaSyCmZtU7-s_1NO9IRlFsf3G3tl3MYVxtsas";
    public static final String POPULARVIDEO_URL = "https://www.googleapis.com/youtube/v3/videos?part=snippet%2CcontentDetails%2Cstatistics&chart=mostPopular&maxResults=15&key=" + API_KEY;
    public static final String SEARCH_VIDEO_URL = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=15&key=" + API_KEY;
    public static final String VIDEO_DETAILS_URL = "https://www.googleapis.com/youtube/v3/videos?part=snippet%2CcontentDetails%2Cstatistics&key=" + API_KEY;
    // Json
}










