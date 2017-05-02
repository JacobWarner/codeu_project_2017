package projectgroup32.googlecodeu.androidguiclient;

/**
 * Created by travis on 4/30/17.
 */

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class LoginIntentService extends IntentService {

    private static final String TAG = LoginIntentService.class.getSimpleName();

    public static final String RESULT = "result";
    public static final String PASSWORD = "password";
    public static final String USERNAME = "username";
    public static final String LOGIN_RESPONSE = "loginIntentServiceResponse";

    public static final int SUCCESS = 0;
    public static final int FAILURE = 1;
    public static final int ERROR = 2;

    public LoginIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "handler started");
        try
        {
            String username = intent.getStringExtra(USERNAME);
            String password = intent.getStringExtra(PASSWORD);
            boolean success = ClientContextInit.init().user.signInUser(username,password);
            Intent result = new Intent();
            result.setAction(LOGIN_RESPONSE);
            if(success)
                result.putExtra(RESULT, SUCCESS);
            else
                result.putExtra(RESULT, FAILURE);
            LocalBroadcastManager.getInstance(this).sendBroadcast(result);
            Log.d(TAG, "Sent message: " + success);
        } catch (Exception exc)
        {
            Intent result = new Intent();
            result.setAction(LOGIN_RESPONSE);
            result.putExtra(RESULT, ERROR);
            LocalBroadcastManager.getInstance(this).sendBroadcast(result);
            Log.d(TAG, "Sent message: error");
        }
    }
}