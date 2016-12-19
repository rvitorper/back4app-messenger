package back4app.messenger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import tgio.parselivequery.*;
import tgio.parselivequery.interfaces.OnListener;

public class MessageBoardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_board);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Elements we are going to use
        FloatingActionButton sendButton = (FloatingActionButton) findViewById(R.id.send);
        final TextView messageBoard = (TextView) findViewById(R.id.messageBoard);
        messageBoard.setMovementMethod(new ScrollingMovementMethod());
        final EditText message = (EditText) findViewById(R.id.message);

        if(!initialized) {
            // Back4App's Parse setup
            Parse.initialize(new Parse.Configuration.Builder(this)
                    .applicationId("X5SnUHTneE1aB5xJQCpp9J7N6y6yvt8xl5FIPtKL")
                    .clientKey("LLlVZSZ3ZI85vzEbWkV4PdtlW6lhKkFvhpAeh7Rz")
                    .server("https://parseapi.back4app.com/").build()
            );

            LiveQueryClient.init("wss://messageexample.back4app.io", "X5SnUHTneE1aB5xJQCpp9J7N6y6yvt8xl5FIPtKL", true);
            LiveQueryClient.connect();

            // Subscription being made that receives every message
            final Subscription sub = new BaseQuery.Builder("Message")
                    .where("type", "message")
                    .addField("sender")
                    .addField("content")
                    .addField("type")
                    .build()
                    .subscribe();

            // If a message is created, we add to the text field
            sub.on(LiveQueryEvent.CREATE, new OnListener() {
                @Override
                public void on(JSONObject object) {
                    try {
                        String message = (String) ((JSONObject) object.get("object")).get("content");
                        String sender = (String) ((JSONObject) object.get("object")).get("sender");
                        String toBeAdded = sender + ": " + message;

                        final String updatedText = '\n' + toBeAdded;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                messageBoard.append(updatedText);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            initialized = true;
        }

        // Implementing the actions that our app will have
        // Starting with the sendButton functionality
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                String messageToSend = message.getText().toString();

                Log.i("Message", "created correctly, "+messageToSend);

                ParseObject click = new ParseObject("Message");
                click.put("content", messageToSend);
                click.put("sender", sender);
                click.put("type", "message");
                click.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.i("Message", "Sent correctly");
                    }
                });

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        message.setText("");
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_message_board, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            final EditText txtUrl = new EditText(this);

            new AlertDialog.Builder(this)
                    .setTitle("Settings")
                    .setMessage("Please write the username you want")
                    .setView(txtUrl)
                    .setPositiveButton("Change username", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            sender = txtUrl.getText().toString();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    })
                    .show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    String sender = "Back4User";
    boolean initialized = false;
}
