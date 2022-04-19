package com.deskconn.texttospeechandviceversa;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import net.gotev.speech.GoogleVoiceTypingDisabledException;
import net.gotev.speech.Speech;
import net.gotev.speech.SpeechDelegate;
import net.gotev.speech.SpeechRecognitionNotAvailable;
import net.gotev.speech.SpeechUtil;
import net.gotev.speech.TextToSpeechCallback;
import net.gotev.speech.ui.SpeechProgressView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final int PERMISSIONS_REQUEST = 1;

    private ImageButton button;
    private Button speak;
    private TextView text;
    private EditText textToSpeech;
    private SpeechProgressView progress;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Speech.init(this, getPackageName());

        linearLayout = findViewById(R.id.linearLayout);

        button = findViewById(R.id.button);
        button.setOnClickListener(view -> {
            if (Speech.getInstance().isListening()) {
                Speech.getInstance().stopListening();
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    button.setVisibility(View.GONE);
                    linearLayout.setVisibility(View.VISIBLE);

                    try {
                        Speech.getInstance().stopTextToSpeech();
                        Speech.getInstance().startListening(progress, new SpeechDelegate() {
                            @Override
                            public void onStartOfSpeech() {

                            }

                            @Override
                            public void onSpeechRmsChanged(float value) {

                            }

                            @Override
                            public void onSpeechPartialResults(List<String> results) {
                                text.setText("");
                                for (String partial : results) {
                                    text.append(partial + " ");
                                }
                            }

                            @Override
                            public void onSpeechResult(String result) {
                                button.setVisibility(View.VISIBLE);
                                linearLayout.setVisibility(View.GONE);

                                text.setText(result);

                                if (result.isEmpty()) {
                                    Speech.getInstance().say(getString(R.string.repeat));

                                } else {
                                    Speech.getInstance().say(result);
                                }
                            }
                        });

                    } catch (SpeechRecognitionNotAvailable exc) {
                        showSpeechNotSupportedDialog();

                    } catch (GoogleVoiceTypingDisabledException exc) {
                        showEnableGoogleVoiceTyping();
                    }
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST);
                }
            }
        });

        speak = findViewById(R.id.speak);
        speak.setOnClickListener(view -> {
            if (textToSpeech.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, R.string.input_something, Toast.LENGTH_LONG).show();
                return;
            }

            Speech.getInstance().say(textToSpeech.getText().toString().trim(), new TextToSpeechCallback() {
                @Override
                public void onStart() {
                    Log.i("speak", "TTS onStart");
                }

                @Override
                public void onCompleted() {
                    Log.i("speak", "TTS onCompleted");
                }

                @Override
                public void onError() {
                    Log.i("speak", "TTS onError");
                }
            });
        });

        text = findViewById(R.id.text);
        textToSpeech = findViewById(R.id.textToSpeech);
        progress = findViewById(R.id.progress);

        int[] colors = {
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.darker_gray),
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.holo_orange_dark),
                ContextCompat.getColor(this, android.R.color.holo_red_dark)
        };
        progress.setColors(colors);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Speech.getInstance().shutdown();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != PERMISSIONS_REQUEST) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                button.setVisibility(View.GONE);
                linearLayout.setVisibility(View.VISIBLE);

                try {
                    Speech.getInstance().stopTextToSpeech();
                    Speech.getInstance().startListening(progress, new SpeechDelegate() {
                        @Override
                        public void onStartOfSpeech() {
                            Log.i("speech", "onSpeechStarted");
                        }

                        @Override
                        public void onSpeechRmsChanged(float value) {

                        }

                        @Override
                        public void onSpeechPartialResults(List<String> results) {
                            text.setText("");
                            for (String partial : results) {
                                text.append(partial + " ");
                            }
                        }

                        @Override
                        public void onSpeechResult(String result) {
                            button.setVisibility(View.VISIBLE);
                            linearLayout.setVisibility(View.GONE);

                            text.setText(result);

                            if (result.isEmpty()) {
                                Speech.getInstance().say(getString(R.string.repeat));
                            } else {
                                Speech.getInstance().say(result);
                            }
                        }
                    });

                } catch (SpeechRecognitionNotAvailable exc) {
                    showSpeechNotSupportedDialog();

                } catch (GoogleVoiceTypingDisabledException exc) {
                    showEnableGoogleVoiceTyping();
                }
            } else {
                Toast.makeText(MainActivity.this, R.string.permission_required, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showSpeechNotSupportedDialog() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    SpeechUtil.redirectUserToGoogleAppOnPlayStore(MainActivity.this);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    dialog.dismiss();
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.speech_not_available)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener)
                .show();
    }

    private void showEnableGoogleVoiceTyping() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.enable_google_voice_typing)
                .setCancelable(false)
                .setPositiveButton(R.string.OK, (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }

}