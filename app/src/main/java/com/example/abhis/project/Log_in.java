package com.example.abhis.project;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class Log_in extends AppCompatActivity {

    //For Google Authentication

    private static final String TAG = "Log_in";
    private static final int RC_SIGN_IN = 0;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private com.google.android.gms.common.SignInButton signInButton;
    private GoogleApiClient googleApiClient;

    //For Facebook Authentication

    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;

    ProfileTracker profileTracker;
    LoginButton loginButton;
    FacebookCallback<LoginResult> callback;

    //Normal Fields

    private EditText editTextName,editTextPass;
    private TextView textViewSignUp;
    private Button btnLogIn;
    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_log_in);

        //Components ID's

        editTextName = findViewById(R.id.EtUN);
        editTextPass = findViewById(R.id.EtPass);
        textViewSignUp = findViewById(R.id.TextView);
        btnLogIn = findViewById(R.id.LogInBtn);
        signInButton = findViewById(R.id.googlebtn);
        loginButton = findViewById(R.id.facebookbtn);

        //Google Integration code

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,
                        new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                } /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user!=null){

                    Log.d(TAG,"onAuthStateChanged:signed_in:"+user.getUid());
                }
                else{
                    Log.d(TAG,"onAuthStateChanged:signed_out");
                }
            }
        };

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        //Facebook Integration code

        FacebookSdk.sdkInitialize(getApplicationContext());

        callbackManager = CallbackManager.Factory.create();

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {

            }
        };

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                displayMessage(currentProfile);
            }
        };

        accessTokenTracker.startTracking();
        profileTracker.startTracking();

        callback = new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                AccessToken accessToken = loginResult.getAccessToken();

                Profile profile = Profile.getCurrentProfile();

                displayMessage(profile);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        };

        loginButton.setReadPermissions("user_friends");
        loginButton.registerCallback(callbackManager,callback);
    }

    //Google Integration code

    private void signIn() {

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //For Google

        if (requestCode==RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()){
                GoogleSignInAccount account = result.getSignInAccount();

                firebaseAuthWithGoogle(account);
            }
            else {

            }
        }

        //For Facebook

        callbackManager.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    protected void onStart() {
        super.onStart();

        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {

        //For Facebook

        accessTokenTracker.stopTracking();
        profileTracker.stopTracking();
        super.onStop();

        //For Google

        if (authStateListener!=null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    //For Facebook


    @Override
    protected void onPostResume() {
        super.onPostResume();

        Profile profile = Profile.getCurrentProfile();
        displayMessage(profile);
    }

    private void displayMessage(Profile profile) {

        if (profile!=null) {

            intent = new Intent(this,First_Page.class);
            startActivity(intent);

            Toast.makeText(this, "Log_In Successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        Log.d(TAG,"firebaseAuthWithGoogle:-"+account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);

        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                Log.d(TAG,"SignInWithCredential:OnCompleteListener:"+task.isSuccessful());

                if (!task.isSuccessful()){
                    Log.w(TAG,"SignInWithCredential",task.getException());
                    Toast.makeText(Log_in.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
