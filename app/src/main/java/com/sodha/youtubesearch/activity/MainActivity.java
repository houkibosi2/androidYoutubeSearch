package com.sodha.youtubesearch.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;
import com.sodha.youtubesearch.R;
import com.sodha.youtubesearch.adapter.MainActivityListAdapter;
import com.sodha.youtubesearch.api.MakeJsonObjectRequest;
import com.sodha.youtubesearch.api.VolleyResponseListner;
import com.sodha.youtubesearch.config.Config;
import com.sodha.youtubesearch.config.JsonKeys;
import com.sodha.youtubesearch.data.VideoData;
import com.sodha.youtubesearch.utils.EndlessRecyclerOnScrollListner;
import com.sodha.youtubesearch.utils.RecyclerItemClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getName();
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private MainActivityListAdapter adapter;

    private List<VideoData> videoList = new ArrayList<>();
    private String nextPageToken = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView)findViewById(R.id.mainRecycleList);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                VideoData intentData = videoList.get(position);
                Intent videoDetailsIntent = new Intent(getApplicationContext(), VideoDetail.class);
//                videoDetailsIntent.putExtra("ob", (Serializable)intentData);
                startActivity(videoDetailsIntent);
            }
        }));

        recyclerView.setOnScrollListener(new EndlessRecyclerOnScrollListner(layoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                Log.i(TAG, "onLoadMore: " + current_page);
                getData();
            }
        });
        adapter = new MainActivityListAdapter(getApplicationContext(), videoList);
        recyclerView.setAdapter(adapter);
        getData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView)menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    public void parseData(JSONArray items) {
        for (int i = 0; i < items.length(); i++) {
            VideoData video = new VideoData();
            try {
                JSONObject itemObject = items.getJSONObject(i);
                video.setVideoId(itemObject.getString(JsonKeys.ID));
                JSONObject snippet = itemObject.getJSONObject(JsonKeys.SNIPPET);
                video.setChannelTitle(snippet.getString(JsonKeys.CHANNEL_TITLE));
                video.setPublishedAt(snippet.getString(JsonKeys.PUBLISHED_AT));
                video.setChannelId(snippet.getString(JsonKeys.CHANNL_ID));
                video.setVideoTitle(snippet.getString(JsonKeys.VIDEO_TITLE));
                video.setDescription(snippet.getString(JsonKeys.DESCRIPTION));
                JSONObject thumbnails = snippet.getJSONObject(JsonKeys.THUMBNAILS);
                video.setSmallThumbnail(thumbnails.getJSONObject(JsonKeys.DEFAULT_THUMBNAIL).getString(JsonKeys.URL));
                video.setMediumThumbnail(thumbnails.getJSONObject(JsonKeys.MEDIUM_THUMBNAIL).getString(JsonKeys.URL));
                video.setLargeThumbnail(thumbnails.getJSONObject(JsonKeys.HIGH_THUMBNAIL).getString(JsonKeys.URL));
                JSONObject contentDetails = itemObject.getJSONObject(JsonKeys.CONTENT_DETAILS);
                video.setDuration(contentDetails.getString(JsonKeys.DURATION));
                JSONObject statistics = itemObject.getJSONObject(JsonKeys.STATISTICS);
                video.setViewCount(statistics.getString(JsonKeys.VIEW_COUNT));
                video.setLikeCount(statistics.getString(JsonKeys.LIKE_COUNT));
                video.setDislikeCount(statistics.getString(JsonKeys.DISLIKE_COUNT));
                video.setFavouriteCount(statistics.getString(JsonKeys.FAVORITE_COUNT));
                video.setCommentCount(statistics.getString(JsonKeys.COMMENT_COUNT));
                videoList.add(video);
            } catch (JSONException e) {
                // TODO: 9/3/16 Handle Error
                e.printStackTrace();
            }
        }
        adapter.addAll(videoList);
        adapter.notifyDataSetChanged();
    }
    public void getData() {
        String URL = Config.POPULARVIDEO_URL;
        if(nextPageToken != null) {
            URL = URL + "&pageToken=" + nextPageToken;
        }
        // TODO: 9/3/16 Add Spinners for loading data
        MakeJsonObjectRequest.call(getApplicationContext(), Request.Method.GET, URL, null, new VolleyResponseListner() {
            @Override
            public void onError(String message) {
                Log.e(TAG, "onError: " + message );
            }

            @Override
            public void onResponse(Object response) {
                try {
                    JSONObject jsonObject = (JSONObject)response;
                    nextPageToken = jsonObject.getString(JsonKeys.NEXT_PAGE_TOKEN);
                    JSONArray items = jsonObject.getJSONArray(JsonKeys.ITEMS);
                    parseData(items);
                } catch (JSONException e) {
                    // TODO: 9/3/16 Handle Error
                    e.printStackTrace();
                }
            }
        });
    }
}