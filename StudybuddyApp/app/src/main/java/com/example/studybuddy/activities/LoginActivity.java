package com.example.studybuddy.activities;

import static com.example.studybuddy.utils.MyConstants.*;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studybuddy.R;
import com.example.studybuddy.validator.LoginFormValidator;
import com.example.studybuddy.utils.MyGlobals;
import com.example.studybuddy.validator.ValidationResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class LoginActivity extends AppCompatActivity
{
    private MaterialToolbar loginToolbar;
    private TextInputLayout mailTextInputLayout, passwordTextInputLayout;
    private TextInputEditText mailTextInput, passwordTextInput;
    private TextView tvForgotPassword;
    private Button btnAccess;
    private SignInButton btnGoogle;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();

        MyGlobals.setupToolbar(this, loginToolbar);
        mAuth = FirebaseAuth.getInstance();

        setupGoogleSignIn();

        signIn();
        googleSignIn();
        forgotPassword();
    }

    private void setupGoogleSignIn()
    {
        String webClientId = "";
        try
        {
            int resId = getResources().getIdentifier(STRING_RES_WEB_CLIENT_ID, STRING_RES_TYPE, getPackageName());
            if (resId != ZERO)
            {
                webClientId = getString(resId);
            }
        }
        catch (Exception ignored)
        {
        }

        if (webClientId.isEmpty())
        {
            webClientId = DEFAULT_WEB_CLIENT_ID;
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (mailTextInput.getText() != null)
        {
            outState.putString(KEY_FIELD_EMAIL, mailTextInput.getText().toString());
        }

        if (passwordTextInput.getText() != null)
        {
            outState.putString(KEY_FIELD_PASSWORD, passwordTextInput.getText().toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        String savedEmail = savedInstanceState.getString(KEY_FIELD_EMAIL);
        String savedPassword = savedInstanceState.getString(KEY_FIELD_PASSWORD);

        if (savedEmail != null)
        {
            mailTextInput.setText(savedEmail);
        }

        if (savedPassword != null)
        {
            passwordTextInput.setText(savedPassword);
        }
    }

    private void initView()
    {
        loginToolbar = findViewById(R.id.login_toolbar);
        mailTextInputLayout = findViewById(R.id.emailInputLayout);
        passwordTextInputLayout = findViewById(R.id.passwordInputLayout);
        mailTextInput = findViewById(R.id.emailEditText);
        passwordTextInput = findViewById(R.id.passwordEditText);
        tvForgotPassword = findViewById(R.id.forgotPassword);
        btnAccess = findViewById(R.id.accessButton);
        btnGoogle = findViewById(R.id.google_button);
        progressBar = findViewById(R.id.loginProgressBar);
    }

    private void googleSignIn()
    {
        btnGoogle.setOnClickListener(v ->
        {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN)
        {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try
            {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null)
                {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            }
            catch (ApiException e)
            {
                MyGlobals.showToast(this, getString(R.string.google_sign_in_failed) + e.getMessage());
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken)
    {
        MyGlobals.setLoadingState(progressBar, btnAccess, true);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task ->
                {
                    MyGlobals.setLoadingState(progressBar, btnAccess, false);

                    if (task.isSuccessful())
                    {
                        MyGlobals.showToast(LoginActivity.this, getString(R.string.welcome_toast));
                        MyGlobals.goToHomePage(LoginActivity.this);
                    }
                    else
                    {
                        handleFirebaseError(task.getException());
                    }
                });
    }

    private void signIn()
    {
        btnAccess.setOnClickListener(view ->
        {
            MyGlobals.setLoadingState(progressBar, btnAccess, true);

            String email = (mailTextInput.getText() != null) ? mailTextInput.getText().toString().trim() : "";
            String password = (passwordTextInput.getText() != null) ? passwordTextInput.getText().toString().trim() : "";

            mailTextInputLayout.setError(null);
            passwordTextInputLayout.setError(null);

            LoginFormValidator validator = new LoginFormValidator();
            ValidationResult result = validator.validate(email, password);

            if (!result.isValid())
            {
                if (result.getError().toLowerCase().contains("email"))
                {
                    mailTextInputLayout.setError(result.getError());
                }
                else
                {
                    passwordTextInputLayout.setError(result.getError());
                }

                MyGlobals.setLoadingState(progressBar, btnAccess, false);
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task ->
                    {
                        MyGlobals.setLoadingState(progressBar, btnAccess, false);

                        if (task.isSuccessful())
                        {
                            MyGlobals.showToast(LoginActivity.this, getString(R.string.welcome_back_toast));
                            MyGlobals.goToHomePage(LoginActivity.this);
                        }
                        else
                        {
                            handleFirebaseError(task.getException());
                        }
                    });
        });
    }

    private void handleFirebaseError(Exception e)
    {
        if (e == null)
        {
            MyGlobals.showToast(this, getString(R.string.unknown_error));
            return;
        }

        if (e instanceof FirebaseAuthInvalidCredentialsException)
        {
            String code = ((FirebaseAuthInvalidCredentialsException) e).getErrorCode();

            if (FIREBASE_ERROR_WRONG_PASSWORD.equals(code))
            {
                passwordTextInputLayout.setError(getString(R.string.incorrect_password));
            }
            else if (FIREBASE_ERROR_INVALID_EMAIL.equals(code))
            {
                mailTextInputLayout.setError(getString(R.string.invalid_email_format));
            }
            else
            {
                mailTextInputLayout.setError(getString(R.string.incorrect_credentials));
            }
            return;
        }

        if (e instanceof FirebaseAuthInvalidUserException)
        {
            String code = ((FirebaseAuthInvalidUserException) e).getErrorCode();

            if (FIREBASE_ERROR_USER_NOT_FOUND.equals(code))
            {
                mailTextInputLayout.setError(getString(R.string.account_not_found));
            }
            else if (FIREBASE_ERROR_USER_DISABLED.equals(code))
            {
                mailTextInputLayout.setError(getString(R.string.account_disabled));
            }
            else
            {
                mailTextInputLayout.setError(getString(R.string.user_not_found));
            }
            return;
        }

        if (e instanceof FirebaseAuthUserCollisionException)
        {
            mailTextInputLayout.setError(getString(R.string.email_already_in_use));
            return;
        }

        String msg = (e.getMessage() != null) ? e.getMessage() : getString(R.string.unknown_error);

        if (msg.toLowerCase().contains(KEY_NETWORK))
        {
            MyGlobals.showToast(this, getString(R.string.no_internet));
        }
        else if (msg.toLowerCase().contains(KEY_TOO_MANY_REQUESTS) || msg.contains(KEY_TOO_MANY_ATTEMPTS))
        {
            MyGlobals.showToast(this, getString(R.string.too_many_attempts));
        }
        else if (msg.contains(KEY_APP_CHECK) || msg.contains(KEY_APP_CHECK_ALT))
        {
            MyGlobals.showToast(this, getString(R.string.app_check_error));
        }
        else
        {
            MyGlobals.showToast(this, getString(R.string.login_failed) + msg);
        }
    }

    private void forgotPassword()
    {
        tvForgotPassword.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
