package com.example.audiojacktest;


import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

   private TextView textField;
   private int minSize;
   private AudioRecord audioInput;
   private Thread recordingThread;
   private boolean isRecording;
   private short[] audio;
private EditText editText;
private String s;
   private static int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
   	
      textField = (TextView)findViewById(R.id.textView);
      editText = (EditText)findViewById(R.id.editText1);
   	
      minSize = AudioRecord.getMinBufferSize(44100,  AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
   }

	
   public void onToggleClicked(View view) {
      boolean on = ((ToggleButton) view).isChecked();
       
      if (on) {
         startRecording();
         audioInput.startRecording();
         isRecording = true;
      } 
      else {
         isRecording = false;
         audioInput.stop();
         audioInput.release();
         recordingThread.interrupt();
         recordingThread = null;
         
         s = "";
         Thread stringThread = new Thread(
                 new Runnable() {
                     
                    @Override
                     public void run() {
                    	for(int i=0;i<audio.length;i++) {
                       	 s+= String.valueOf(audio[i]) + "\n";
                        }
                    }
                 });
         stringThread.start();
         try {
			stringThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         
         editText.setTextSize(5);
         editText.setText(s); 
      }
   }
	
   private void startRecording(){
      audioInput = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minSize);
        //audioInput = findAudioRecord();
        
      audioInput.startRecording();
      recordingThread = new Thread(
            new Runnable() {
                
               @Override
                public void run() {
					recordAudio();
               }
            },"AudioRecorder Thread");
        
      recordingThread.start();
   }
	
   private void recordAudio() {

	  ArrayList<Short> array = new ArrayList<Short>();
      short[] buffer = new short[minSize];
      int read = 0;
   	
      while(isRecording) {
         read = audioInput.read(buffer, 0, minSize);
         if(AudioRecord.ERROR_INVALID_OPERATION != read){
            for (int i = 0; i < read; i++){
            	array.add(buffer[i]);
            }
         }
      }
      audio = convert(array.toArray());
   }

   public static short[] convert(Object[] objectArray){
	   short[] shortArray = new short[objectArray.length];
	   for(int i=0; i<objectArray.length; i++){
	    shortArray[i] = (Short) objectArray[i];
	   }
	   return shortArray;
	 }
   
   public AudioRecord findAudioRecord() {
      for (int rate : mSampleRates) {
         for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
            for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
               try {
                  int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);
               
                  if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                           // check if we can instantiate and have a success
                     AudioRecord recorder = new AudioRecord(AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);
                  
                     if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                        return recorder;
                  }
               } 
               catch (Exception e) {
               
               }
            }
         }
      }
      return null;
   }
	
	
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
   	// Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.main, menu);
      return true;
   }

}
