package com.gpeters.geoffbedmonkey;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnInitListener {

	// Intent request codes
	private static final int RECOGNIZE_REQUEST_CODE = 1234;
	private static final int SPEECH_CHECK_CODE = 1235;
	
	private TextToSpeech tts;
	private Button speakButton;
	private boolean notSupported;
	
	private void showAboutDialog() {
		final Dialog aboutDialog = new Dialog(MainActivity.this);
		aboutDialog.setContentView(R.layout.about_frag);
		aboutDialog.setTitle("About this App");

		TextView textAbout = (TextView)aboutDialog.findViewById(R.id.textAbout);
        Button closeAbout = (Button)aboutDialog.findViewById(R.id.btnCloseAbout);
        
        textAbout.setText(Html.fromHtml("<p>Created by <b>Geoff Peters</b>, a software developer and videographer in Vancouver BC Canada."
        		+ " To watch some of his videos, please visit: <b><a href='http://geoffmobile.com'>www.geoffmobile.com</a></b></p>"
        		+ "<p>Photo: There's a whippet in the bed! by <a href='http://www.flickr.com/photos/lachlanhardy/'>Lachlan Hardy</a></p>"
        		+ "<p>Note: If you are having trouble, you may need to install Google Voice and/or Ivona TTS.</p>"));

        textAbout.setMovementMethod(LinkMovementMethod.getInstance());
        
        closeAbout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
            	aboutDialog.dismiss();
            }
        });
        aboutDialog.show();
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        speakButton = (Button) findViewById(R.id.speakButton);
        speakButton.setEnabled(false);
         
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0)
        {
            Toast.makeText(MainActivity.this,
                    "Error occurred while initializing Speech Recognizer. You may need to install Google Voice.", Toast.LENGTH_LONG).show();
            speakButton.setText("Recognizer not present");
            notSupported = true;
        }
        else
        {
	        notSupported = false;
	        
	        Intent checkIntent = new Intent();
	        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
	        startActivityForResult(checkIntent, SPEECH_CHECK_CODE);
        }
    }

    public void speakButtonClicked(View v)
    {
        startVoiceRecognitionActivity();
    }
    
    private void startVoiceRecognitionActivity()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something please...");
        startActivityForResult(intent, RECOGNIZE_REQUEST_CODE);
    }
 
    private void say(String textToSay)
    {
    	tts.speak(textToSay, TextToSpeech.QUEUE_ADD, null);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == SPEECH_CHECK_CODE) 
        {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) 
            {
                tts = new TextToSpeech(this, this);
            }
            else 
            {
            	Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        } 
        else if (requestCode == RECOGNIZE_REQUEST_CODE && resultCode == RESULT_OK)
        {
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);

            if (matches.size() > 0) {
            	say(matches.get(0) + " in bed.");
            }
            else {
            	say("Say again? I didn't catch it.");
            	
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onInit(int status) {       
        if (status == TextToSpeech.SUCCESS) {

            if (!notSupported) {
                speakButton.setEnabled(true);
                speakButton.setText("Click Me!");
            }
        }
        else if (status == TextToSpeech.ERROR) {
            Toast.makeText(MainActivity.this,
                    "Error occurred while initializing Text-To-Speech engine", Toast.LENGTH_LONG).show();
                                    
            speakButton.setEnabled(false);
            speakButton.setText("Text-To-Speech error");
            notSupported = true;
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_about:
            showAboutDialog();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}