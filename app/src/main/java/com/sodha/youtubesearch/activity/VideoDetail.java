package com.sodha.youtubesearch.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.sodha.youtubesearch.api.CustomVolleyRequest;
import com.sodha.youtubesearch.config.Config;
import com.sodha.youtubesearch.data.VideoData;
import com.sodha.youtubesearch.R;
/**
 * Created by sodha on 10/3/16.
 */
public class VideoDetail extends AppCompatActivity {
    private VideoData videoDetail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_detail);

        try {
            videoDetail = (VideoData)getIntent().getSerializableExtra("ob");
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: 10/3/16 Handle Error
            Toast.makeText(getApplicationContext(), "Some Error", Toast.LENGTH_LONG).show();
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(videoDetail.getVideoTitle());
        generateView(getApplicationContext());
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

    public void generateView(Context context) {
        NetworkImageView imageView = (NetworkImageView)findViewById(R.id.videoDetailThumbnail);
        ImageLoader imageLoader = CustomVolleyRequest.getInstance(context).getImageLoader();
        imageLoader.get(videoDetail.getLargeThumbnail(), ImageLoader.getImageListener(imageView, R.drawable.ic_action_android, R.drawable.ic_action_android));
        imageView.setImageUrl(videoDetail.getLargeThumbnail(), imageLoader);
        TextView likes = (TextView)findViewById(R.id.detailLikes);
        TextView dislikes = (TextView)findViewById(R.id.detailDislike);
        TextView duration = (TextView)findViewById(R.id.detailDuration);
        TextView views = (TextView)findViewById(R.id.detailViews);
        TextView title = (TextView)findViewById(R.id.detailTitle);
        FloatingActionButton playButton =   (FloatingActionButton)findViewById(R.id.playButton);

        likes.setText(videoDetail.getLikeCount());
        dislikes.setText(videoDetail.getDislikeCount());
        duration.setText(videoDetail.getDuration());
        views.setText(videoDetail.getViewCount());
        title.setText(videoDetail.getVideoTitle());
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Config.YOUTUBE_URL_VIDEO + videoDetail.getVideoId())));
            }
        });
    }
}
