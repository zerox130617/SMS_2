package com.example.sms;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.example.sms.R;

import java.io.IOException;

public class play_song extends Activity {
    MediaPlayer mediaPlayer =new MediaPlayer();
    ;
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        Bundle extras = getIntent().getExtras();
        String num = extras.getString("num");
        String msg = extras.getString("msg");
        Log.i("res",msg);
        Resources res = context.getResources();
        int soundId = res.getIdentifier(msg, "raw", context.getPackageName());

    if(soundId>0.){
        mediaPlayer=MediaPlayer.create(getApplicationContext(), soundId);
        mediaPlayer.start();
    }


    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.stop();
        mediaPlayer.release();
    }
}
