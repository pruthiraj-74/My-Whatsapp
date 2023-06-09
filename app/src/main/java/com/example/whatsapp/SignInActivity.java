package com.example.whatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.whatsapp.databinding.ActivitySignInBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

public class SignInActivity extends AppCompatActivity {

    ActivitySignInBinding binding;
    ProgressDialog dialog;
    FirebaseAuth auth;
    GoogleSignInClient googleSignInClient;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding=ActivitySignInBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        AnimationDrawable animationDrawable = (AnimationDrawable)binding.signinlayout.getBackground();
        animationDrawable.setEnterFadeDuration(1500);
        animationDrawable.setExitFadeDuration(3000);
        animationDrawable.start();

        auth=FirebaseAuth.getInstance();
        dialog=new ProgressDialog(SignInActivity.this);
        dialog.setTitle("Login");
        dialog.setMessage("Login to your account");

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(SignInActivity.this, googleSignInOptions);
        binding.gin.setOnClickListener((View.OnClickListener) view -> {
            Intent intent = googleSignInClient.getSignInIntent();
            startActivityForResult(intent, 100);
        });
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            startActivity(new Intent(SignInActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        binding.signinbtn.setOnClickListener(v -> {
            if(binding.gmailfield.getText().toString().isEmpty()){
                binding.gmailfield.setError("Enter your email");
                return;
            }
            if(binding.signinpass.getText().toString().isEmpty()){
                binding.signinpass.setError("Enter your password");
                return;
            }
            dialog.show();
            auth.signInWithEmailAndPassword(binding.gmailfield.getText().toString(),binding.signinpass.getText().toString())
                    .addOnCompleteListener(task -> {
                        dialog.dismiss();
                        if(task.isSuccessful()){
                            Intent intent=new Intent(SignInActivity.this,MainActivity.class);
                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(SignInActivity.this, Objects.requireNonNull(task.getException()).getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        binding.newacc.setOnClickListener(v -> {
            Intent intent=new Intent(SignInActivity.this,SignUpActivity.class);
            startActivity(intent);
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (signInAccountTask.isSuccessful()) {
                Toast.makeText(SignInActivity.this,"Google sign in successful",Toast.LENGTH_SHORT).show();
                try {
                    GoogleSignInAccount googleSignInAccount = signInAccountTask.getResult(ApiException.class);
                    if (googleSignInAccount != null) {
                        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
                        firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                startActivity(new Intent(SignInActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                Toast.makeText(SignInActivity.this,"Firebase authentication successful", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SignInActivity.this,"Authentication Failed :" + Objects.requireNonNull(task.getException()).getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.signinmenu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.phonesignin:
                Intent in=new Intent(getApplicationContext(),PhoneNumberAuthActivity.class);
                startActivity(in);
                break;
        }
        return true;
    }
}