package com.example.sms;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//import org.apache.commons.io.IOUtils;

public class MainActivity extends YouTubeBaseActivity {
    YouTubePlayerView youtuber;
    YouTubePlayer.OnInitializedListener mOnInitializedListener;
    private YouTubePlayer VT_player;
    RequestQueue requestQueue;

    private static final int MY_PERMISSIONS_REQUEST_RECEIVE_SMS=0;
    private static final int PERMISSION_SEND_SMS = 1;
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

    private TextToSpeech tts;
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

        createLanguageTTS();
        Log.d("TTS", "created");
//      -------------------

//        check permission for sms part
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_SEND_SMS);
            }

        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
//            check if denied permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, MY_PERMISSIONS_REQUEST_RECEIVE_SMS);
            }
        }

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
                    if(last == -1 || !url.equals(videoList.get(last)))
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

        next_song_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                int dur = VT_player.getDurationMillis();
                VT_player.seekToMillis(dur - 1);
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
        Log.d("onPaused", title.getText().toString());
        //tts.speak(title.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
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
    protected void sendSMSMessage(String title, String phone_num, int index) {
        String message1="Thank you for request, "+phone_num;
        message1 += "\n You are #" + String.valueOf(index) + " in queue.";
        String message2 = "Your song: " + title;
        Log.d("SendSMS", title);
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phone_num, null, message1, null, null);
            smsManager.sendTextMessage(phone_num, null, message2, null, null);
            Toast.makeText(getApplicationContext(), "SMS sent.",
                    Toast.LENGTH_LONG).show();
            Log.d("SendSMS", message1);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "SMS failed, please try again.",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    //    send sms part end
// interface for sms receiver talk to main activity
//    see below
//    https://stackoverflow.com/questions/14643385/how-to-update-ui-in-a-broadcastreceiver/54364171#54364171
    public void play_url(String result, String phonenum){
        Log.d("Main_activity", "size: " + videoList.size());
        url=result;
        if(!VT_player.isPlaying())
        {
            VT_player.loadVideo(url);
            getTitleQuietly(url, false);
        }
        else
        {
            int last = videoList.size() - 1;
            if(last == -1 || !url.equals(videoList.get(last)))
            {
                videoList.add(url);
                if(videoList.size() == 1)
                {
                    getTitleQuietly(url, true);
                }
            }
            else return;
        }
        getTitleForSending(url, phonenum, videoList.size());
    }
//
    public void ReadMessage(String msg)
    {
        VT_player.pause();
        //pause for 5 seconds to read message
        //todo: detect message reading ends before resuming music
        (new Handler()).postDelayed(() -> tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null), 2000);

        (new Handler()).postDelayed(() -> VT_player.play(), 5000);
    }

//  get video title from url(json), no need to modify?
//    url only respond with single json object
    public void getTitleQuietly(String youtubeUrl, boolean nextSong) {
        try {
            if (youtubeUrl != null) {
                String tempurl="HTTPS://www.youtube.com/watch?v="+youtubeUrl;
                URL embededURL = new URL("HTTPS://www.youtube.com/oembed?url=" +
                        tempurl + "&format=json"

                );
                JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, embededURL .toString(), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(Tag,"title:in");
                        try {
                                String name = response.getString("title");
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

    public void getTitleForSending(String youtubeUrl, String phonenum, int index) {
        try {
            if (youtubeUrl != null) {
                String tempurl="HTTPS://www.youtube.com/watch?v="+youtubeUrl;
                URL embededURL = new URL("HTTPS://www.youtube.com/oembed?url=" +
                        tempurl + "&format=json");
                JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, embededURL .toString(), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONObject json_o = response;
                        try {
                            String name = json_o.getString("title");
                            sendSMSMessage(name,phonenum,index);
                        }
                        catch (Exception w)
                        {
                            Log.d(Tag,"title:fail");
                            Toast.makeText(MainActivity.this,w.getMessage(),Toast.LENGTH_LONG).show();
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
        return;
    }

    private void createLanguageTTS()
    {
        if(tts == null)
        {
            tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    Log.d("TTS", String.valueOf(status));
                    if(status != TextToSpeech.ERROR) {
                        Log.d("initTTS", "success");
                        tts.setLanguage(Locale.TAIWAN);
                    }
                }
            });
        }
    }
}