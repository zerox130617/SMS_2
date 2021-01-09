package com.example.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import org.w3c.dom.Text;

public class SMS_receiver extends BroadcastReceiver {
    private static final String SMS_RECEIVED="android.provider.Telephony.SMS_RECEIVED";
    private static final String Tag="SmsBroadcastReceiver";
    //private MyBroadcastListener listener;

    String msg,phonenum="";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(Tag,"intent received: "+intent.getAction());
        if(intent.getAction().equals(SMS_RECEIVED)){
    //        retrieve a map of  extended data from intent
            Bundle databundle=intent.getExtras();
            if(databundle!=null)
            {
    //            create pdu(protocol data unit)
                Object []mypdu=(Object[])databundle.get("pdus");
                final SmsMessage[] message=new SmsMessage[mypdu.length];
                for(int i=0;i<mypdu.length;i++){
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                        String format =databundle.getString("format");
                        message[i]=SmsMessage.createFromPdu((byte[])mypdu[i],format);
                    }
                    else{
                        message[i]=SmsMessage.createFromPdu((byte[])mypdu[i]);
                    }
                    msg=message[i].getMessageBody();
                    phonenum=message[i].getOriginatingAddress();
                }
                int where=-1;
                if((where=msg.indexOf("$sr"))!=-1){
                    Toast.makeText(context, "Message" +msg.substring(where,msg.length())+where, Toast.LENGTH_SHORT).show();
                    this.abortBroadcast();
                    Intent in = new Intent(context, play_song.class);
                    in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    in.putExtra("num", phonenum);
                    in.putExtra("msg", msg.substring(where+4,msg.length()));//command hardcoded I am lazy to parse string
                    context.startActivity(in);
                }
                where=-1;
                //            when get a sms with $ytb, call interface play_url
    //            (implement in mainactivity)
                if((where = msg.indexOf("$ytb")) != -1){
                    String[] messages = msg.split("\\s+");
                    if(messages.length < 2)
                    {
                        return;
                    }
                    String url = messages[1];
                    if(MainActivity.getInstace()!=null) {
                        MainActivity.getInstace().play_url(url, phonenum);
                    }
                    else{
                        Toast.makeText(context, "Message" + url, Toast.LENGTH_SHORT).show();
                    }
                }

                //Pause music to read message
                //todo: schedule message to read before desired music
                where = -1;
                if((where = msg.indexOf("$msg")) != -1)
                {
                    String[] messages = msg.split("\\s+");
                    if(messages.length < 2)
                    {
                        return;
                    }
                    String TextToSpeak = messages[1];
                    if(MainActivity.getInstace()!=null) {
                        MainActivity.getInstace().ReadMessage(TextToSpeak);
                    }
                    else{
                        Toast.makeText(context, "Message" + TextToSpeak, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}