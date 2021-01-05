package com.example.sms;

    import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
    import android.view.View;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.TextView;
    import android.widget.Toast;

import com.android.volley.Request;
    import com.android.volley.RequestQueue;
    import com.android.volley.Response;
import com.android.volley.VolleyError;
    import com.android.volley.toolbox.JsonArrayRequest;
    import com.android.volley.toolbox.JsonObjectRequest;
    import com.android.volley.toolbox.StringRequest;
    import com.android.volley.toolbox.Volley;
    import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

//import org.apache.commons.io.IOUtils;
    import org.apache.commons.io.IOUtils;
    import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

    import java.io.BufferedReader;
    import java.io.IOException;
    import java.io.InputStream;
    import java.io.InputStreamReader;
    import java.net.HttpURLConnection;
    import java.net.MalformedURLException;
    import java.net.URL;
    import java.util.ArrayList;
    import java.util.List;

    import okhttp3.OkHttpClient;

public class MainActivity extends YouTubeBaseActivity {
    YouTubePlayerView youtuber;
    YouTubePlayer.OnInitializedListener mOnInitializedListener;
    private YouTubePlayer VT_player;
    RequestQueue requestQueue;


    private static final int MY_PERMISSIONS_REQUEST_RECEIVE_SMS=0;
    private static final String Tag="Main_activity";
    private String url="";

    //for receiver get instance
    private static MainActivity mainActivityRunningInstance;
    public static MainActivity  getInstace(){
        return mainActivityRunningInstance;
    }
    private TextView title;
    private TextView next_title;
    private EditText userinput;
    private Button play_btn;
    private Button next_song_btn;
    private List<String> videoList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivityRunningInstance =this;

//      find view by id part
        youtuber=(YouTubePlayerView)findViewById(R.id.youtube_player);
        title=(TextView)findViewById(R.id.songtitle);
        next_title=(TextView)findViewById(R.id.next_songtitle);
        play_btn=(Button)findViewById(R.id.button_add_local);
        userinput=(EditText)findViewById(R.id.songinput);

        next_song_btn=(Button)findViewById(R.id.button_next_song);
//      find view by id part end
//      -------------------

//        check permission for sms part
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
//            checkif denied permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, MY_PERMISSIONS_REQUEST_RECEIVE_SMS);

            }
        }

//      check permission for sms part end

//      youtube init
        mOnInitializedListener=new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                Log.d(Tag,"done init");
//                youTubePlayer.loadVideo(url);
                VT_player = youTubePlayer;
                VT_player.setPlaybackEventListener(playbackEventListener);
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                if (youTubeInitializationResult.isUserRecoverableError()) {
                    Log.e(Tag," :"+youTubeInitializationResult.toString());
//                    youTubeInitializationResult.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
                } else {
                    Log .d(Tag," :"+youTubeInitializationResult.toString());
                    Log.d(Tag,"dd");
                }
            }
        };
        youtuber.initialize(youtubeconfig.getApiKey(),mOnInitializedListener);

//       youtube init part end

//       --------------
        //for volley(get json)
        requestQueue = Volley.newRequestQueue(this);

        play_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                url=userinput.getText().toString();
                if(!VT_player.isPlaying())
                {
                    VT_player.loadVideo(url);
                    getTitleQuietly(url, false);
                }
                else
                {
                    int last = videoList.size() - 1;
                    Log.d("Main_activity", "size: " + videoList.size());
                    if(last == -1 || (last >= 0 && !url.equals(videoList.get(last))))
                    {
                        videoList.add(url);
                        if(videoList.size() == 1)
                        {
                            getTitleQuietly(url, true);
                        }
                    }
                }
            }
        });
    }
// oncreate_fin

//event listener
    private YouTubePlayer.PlaybackEventListener playbackEventListener = new YouTubePlayer.PlaybackEventListener() {
    private boolean started = false;
    @Override
    public void onPlaying() {
        started = true;
    }

    @Override
    public void onPaused() {

    }

    @Override
    public void onStopped() {
        if(!videoList.isEmpty())
            Log.d("onStopped", videoList.get(0));
        if(!videoList.isEmpty() && started)
        {
            String nextSong = videoList.get(0);
            videoList.remove(0);
            VT_player.loadVideo(nextSong);
            getTitleQuietly(nextSong, false);
            if(videoList.isEmpty())
            {
                next_title.setText("");
            }
            else
            {
                String nextnextSong = videoList.get(0);
                getTitleQuietly(nextnextSong, true);
            }
            started = false;
        }
    }

    @Override
    public void onBuffering(boolean b) {

    }

    @Override
    public void onSeekTo(int i) {

    }
};

//    SMS_check permission fun (from web)
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[] ,int []grantResults)
    {
//        check request code
        switch(requestCode)
    {
        case MY_PERMISSIONS_REQUEST_RECEIVE_SMS:
        {
            if(grantResults.length>0&& grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Thank", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "access denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    }
// interface for sms receiver talk to main activity
//    see below
//    https://stackoverflow.com/questions/14643385/how-to-update-ui-in-a-broadcastreceiver/54364171#54364171
    public void play_url(String result){
                 // Calling method from Interface
        url=result;
        if(!VT_player.isPlaying())
        {
            VT_player.loadVideo(url);
            getTitleQuietly(url, false);
        }
        else
        {
            int last = videoList.size() - 1;
            Log.d("Main_activity", "size: " + videoList.size());
            if(last == -1 || (last >= 0 && !url.equals(videoList.get(last))))
            {
                videoList.add(url);
                if(videoList.size() == 1)
                {
                    getTitleQuietly(url, true);
                }
            }
        }
    }
//


//  get video title from url(json), no need to modify?
//    url only respond with single json object
    public void getTitleQuietly(String youtubeUrl, boolean nextSong) {
        try {
            if (youtubeUrl != null) {
                String tempurl="HTTPS://www.youtube.com/watch?v="+youtubeUrl;
                URL embededURL = new URL("HTTPS://www.youtube.com/oembed?url=" +
                        tempurl + "&format=json"

                );
//                String url_2="https://www.googleapis.com/youtube/v3/videos?id=" + youtubeUrl + "&key=" +
//                         youtubeconfig.getApiKey()+
//                        "&part=snippet,contentDetails,statistics,status";
//                Log.d(Tag," :"+url_2);
                JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, embededURL .toString(), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(Tag,"title:in");
                        JSONObject json_o = response;
                        try {
                                String name = json_o.getString("title");
                                Log.d(Tag,"title:"+name);
                                if(!nextSong)
                                {
                                    title.setText(name);
                                }
                                else
                                {
                                    next_title.setText(name);
                                }
                        }
                        catch (Exception w)
                        {
                            Log.d(Tag,"title:fail");
                            Toast.makeText(MainActivity.this,w.getMessage(),Toast.LENGTH_LONG).show();
                            if(nextSong)
                            {
                                videoList.remove(0);
                                if(videoList.isEmpty())
                                {
                                    next_title.setText("");
                                }
                                else
                                {
                                    String nextnextSong = videoList.get(0);
                                    getTitleQuietly(nextnextSong, true);
                                }
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
                requestQueue.add(jsonArrayRequest);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}