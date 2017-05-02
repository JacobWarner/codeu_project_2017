package projectgroup32.googlecodeu.androidguiclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import codeu.chat.client.ClientContext;

public class AccountActivity extends AppCompatActivity{
    private ClientContext clientContext;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int result = intent.getIntExtra(LoginIntentService.RESULT, LoginIntentService.ERROR);
            Log.d("receiver", "Got message: " + result);
            if(result == LoginIntentService.SUCCESS)
            {
                Intent launchConvs = new Intent(AccountActivity.this, ConversationActivity.class);
                launchConvs.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(launchConvs);
            }
            else if(result == LoginIntentService.FAILURE)
            {
                ((TextView) findViewById(R.id.signinMsg)).setText(
                        "Invalid username and/or password");
            }
            else
            {
                ((TextView) findViewById(R.id.signinMsg)).setText(
                        "Communication with server failed.");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(clientContext == null)
            clientContext = ClientContextInit.init();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        Button clickButton = (Button) findViewById(R.id.buttonSignIn);
        clickButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(clientContext != null) {
                    String username =
                            ((EditText) findViewById(R.id.editUsername)).getText().toString();
                    String password =
                            ((EditText) findViewById(R.id.editPassword)).getText().toString();
                    Log.d("Username", username);
                    Log.d("Password",password);
                    if(!clientContext.user.isValidName(username))
                        ((TextView) findViewById(R.id.signinMsg)).setText("Username is invalid");
                    else if(!clientContext.user.isValidPassword(password))
                        ((TextView) findViewById(R.id.signinMsg)).setText("Password is invalid");
                    else
                    {
                        login(username,password);
                    }
                }
            }
        });
    }

    private void login(String username, String password)
    {
        Intent intent = new Intent(this, LoginIntentService.class);
        intent.putExtra(LoginIntentService.USERNAME, username);
        intent.putExtra(LoginIntentService.PASSWORD, password);
        Log.d("AccountActivity", "starting service");
        this.startService(intent);
    }


    @Override
    protected void onPause()
    {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(LoginIntentService.LOGIN_RESPONSE));
    }


    }
