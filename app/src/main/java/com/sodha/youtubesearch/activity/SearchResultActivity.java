package com.sodha.youtubesearch.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
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
import com.sodha.youtubesearch.provider.SuggestionProvider;
import com.sodha.youtubesearch.utils.EndlessRecyclerOnScrollListner;
import com.sodha.youtubesearch.utils.RecyclerItemClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sodha on 9/3/16.
 */
public class SearchResultActivity extends AppCompatActivity {
    public final String TAG =SearchResultActivity.class.getName();
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private MainActivityListAdapter adapter;

    private List<VideoData> videoList = new ArrayList<>();
    private String nextPageToken = null;
    private String query = null;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_result_actvity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = (RecyclerView)findViewById(R.id.searchRecycleList);
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

        adapter = new MainActivityListAdapter(getApplicationContext(), videoList);
        recyclerView.setAdapter(adapter);
        handleIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        adapter.removeAll();
        adapter.notifyDataSetChanged();
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return  true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            recyclerView.setOnScrollListener(null);
            setScrollListener();
            getData(query, null);
            Log.i(TAG, "handleIntent: " + query);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
        }
    }
    public void getData(String q, String nextToken) {
        String URL = Config.SEARCH_VIDEO_URL;
        if(nextToken != null) {
            URL = URL + "&pageToken=" + nextToken;
        }
        URL = URL + "&q=" + q;

        MakeJsonObjectRequest.call(getApplicationContext(), Request.Method.GET, URL, null, new VolleyResponseListner() {
            @Override
            public void onError(String message) {
                Log.e(TAG, "onError: " + message);
                // TODO: 9/3/16 Handle Error
            }

            @Override
            public void onResponse(Object response) {
                try {
                    JSONObject jsonObject = (JSONObject) response;
                    nextPageToken = jsonObject.getString(JsonKeys.NEXT_PAGE_TOKEN);
                    JSONArray items = jsonObject.getJSONArray(JsonKeys.ITEMS);
                    getVideoIds(items);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public void getVideoIds(JSONArray items) {
        List<String> videoIdList= new ArrayList<String>();
        for (int i = 0; i < items.length(); i++) {
            try {
                JSONObject idObject = items.getJSONObject(i).getJSONObject(JsonKeys.ID);
                if(idObject.getString(JsonKeys.KIND).equals(JsonKeys.KIND_VIDEO)) {
                    String videoId = idObject.getString(JsonKeys.VIDEO_ID);
                    videoIdList.add(videoId);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                // TODO: 9/3/16 Handle Error
            }
        }
        String videoIdsForDetail = TextUtils.join(",", videoIdList);
        getVideoDetails(videoIdsForDetail);
    }
    public void getVideoDetails(String Ids) {
        String  URL =  Config.VIDEO_DETAILS_URL + "&id=" + Ids;

        MakeJsonObjectRequest.call(getApplicationContext(), Request.Method.GET, URL, null, new VolleyResponseListner() {
            @Override
            public void onError(String message) {
                Log.e(TAG, "onError: " + message);
            }

            @Override
            public void onResponse(Object response) {
                try {
                    JSONObject jsonObject = (JSONObject) response;
                    JSONArray itemsArray = jsonObject.getJSONArray(JsonKeys.ITEMS);
                    parseData(itemsArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

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
    public void setScrollListener() {
        recyclerView.setOnScrollListener(new EndlessRecyclerOnScrollListner(layoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                Log.i(TAG, "onLoadMore: " + current_page);
                getData(query, nextPageToken);
            }
        });
    }
}
