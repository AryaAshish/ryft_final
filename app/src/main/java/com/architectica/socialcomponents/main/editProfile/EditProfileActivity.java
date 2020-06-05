/*
 * Copyright 2018 Rozdoum
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.architectica.socialcomponents.main.editProfile;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.architectica.socialcomponents.views.CircularImageView;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.main.pickImageBase.PickImageActivity;
import com.architectica.socialcomponents.utils.GlideApp;
import com.architectica.socialcomponents.utils.ImageUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class EditProfileActivity<V extends EditProfileView, P extends EditProfilePresenter<V>> extends PickImageActivity<V, P> implements EditProfileView {
    private static final String TAG = EditProfileActivity.class.getSimpleName();

    // UI references.
    private EditText nameEditText,uriEditText,bioEditText,phoneNumberEditText,mEditTextSixDigitCode;
    Button verify,verifyPhone,browseUrl;
    private AutoCompleteTextView usertype1,skillEditText,startUpTypeEditText;
    protected CircularImageView imageView;
    private ProgressBar avatarProgressBar;
    public String[] userTypes = {"Startup","Intern","Others"};
    public String[] skillTypes = {"Android Development(Java)","Android Development(Kotlin)","Web Development(MEAN Stack)","Web Development(MARN Stack)","Web Development","Business Development","Content Writing","Campus Ambassadors","IOS Development","Hybrid App Development"};
    private String phoneVerifyId;
    PhoneAuthProvider.ForceResendingToken resendingToken;
    LinearLayout verifyLayout;
    ProgressDialog pd;

    //public Uri imageUri;
    int i = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black);

        }

        startUpTypeEditText = findViewById(R.id.startUpType);
        avatarProgressBar = findViewById(R.id.avatarProgressBar);
        imageView = findViewById(R.id.imageView);
        nameEditText = findViewById(R.id.nameEditText);
        usertype1 = findViewById(R.id.usertype1);
        uriEditText=findViewById(R.id.userurl);
        bioEditText=findViewById(R.id.userbio);
        phoneNumberEditText = findViewById(R.id.editText_phone);
        mEditTextSixDigitCode = findViewById(R.id.edit_text_6_digit_code);
        verifyLayout = findViewById(R.id.verifyLayout);

        imageView.setOnClickListener(this::onSelectImageClick);

        mEditTextSixDigitCode.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        mEditTextSixDigitCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mEditTextSixDigitCode.setPaddingRelative(0, 0, 0, 0);
                if (Build.VERSION.SDK_INT>=21){

                    mEditTextSixDigitCode.setLetterSpacing(1);

                }
                mEditTextSixDigitCode.setTextSize(20);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mEditTextSixDigitCode.length() == 0) {
                    mEditTextSixDigitCode.setPadding(10, 0, 0, 0);
                    if (Build.VERSION.SDK_INT>=21){

                        mEditTextSixDigitCode.setLetterSpacing(0);

                    }
                    mEditTextSixDigitCode.setTextSize(12);
                }
            }
        });

        mEditTextSixDigitCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mEditTextSixDigitCode.setBackground(getResources().getDrawable(R.drawable.linear_activity_in_activity_first_run_second_background_on_focus));
                } else {
                    mEditTextSixDigitCode.setBackground(getResources().getDrawable(R.drawable.enter_code_background));
                }
            }
        });

        browseUrl = findViewById(R.id.button_browse);

        browseUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String url = uriEditText.getText().toString();

                if (url.length() != 0){

                    if (!url.startsWith("http://") && !url.startsWith("https://")){

                        url = "http://" + url;

                    }

                    Intent browserIntent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url));
                    startActivity(browserIntent);

                }
                else {

                    Toast.makeText(EditProfileActivity.this, "please enter a valid url", Toast.LENGTH_SHORT).show();

                }

            }
        });

        verifyPhone = findViewById(R.id.verifyPhone);

        verifyPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pd = new ProgressDialog(EditProfileActivity.this);
                pd.setMessage("Verifying...");
                pd.show();

                String otp = mEditTextSixDigitCode.getText().toString();

                verifyCode(otp);

            }
        });

        verify = findViewById(R.id.button_verify);

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (phoneNumberEditText.getText().toString().length() == 10 && phoneNumberEditText.isEnabled()){

                    String number = "+91" + phoneNumberEditText.getText().toString();

                    verifyLayout.setVisibility(View.VISIBLE);

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            number,
                            60,
                            TimeUnit.SECONDS,
                            EditProfileActivity.this,
                            CallBacks);

                }
                else {

                    Toast.makeText(EditProfileActivity.this, "Enter the phone number correctly", Toast.LENGTH_SHORT).show();

                }
            }
        });

        skillEditText = findViewById(R.id.skill);

        //Create Array Adapter
        ArrayAdapter<String> skillAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, skillTypes);

        skillEditText.setAdapter(skillAdapter);

        skillEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                skillEditText.showDropDown();

                return false;
            }
        });


        usertype1.setThreshold(1);

        //Create Array Adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, userTypes);

        //Set the adapter
        usertype1.setAdapter(adapter);

        usertype1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String userType = usertype1.getText().toString();

                if ("Startup".equals(userType)){

                    skillEditText.setVisibility(View.GONE);
                    startUpTypeEditText.setVisibility(View.VISIBLE);

                }
                else {

                    skillEditText.setVisibility(View.VISIBLE);
                    startUpTypeEditText.setVisibility(View.GONE);

                }

            }
        });

        usertype1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                usertype1.showDropDown();

                return false;
            }
        });

        initContent();

    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks CallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            Toast.makeText(EditProfileActivity.this, "Google play verified your phone number automatically", Toast.LENGTH_SHORT).show();
            mEditTextSixDigitCode.setText(phoneAuthCredential.getSmsCode());

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

            Toast.makeText(getApplicationContext(), "Request Failed!.Please Try after some time.", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {

            Toast.makeText(getApplicationContext(), "Code has been sent to your mobile number", Toast.LENGTH_SHORT).show();

            phoneVerifyId = s;

            resendingToken = forceResendingToken;

            super.onCodeSent(s, forceResendingToken);
        }

    };

    private void verifyCode(String code){

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(phoneVerifyId, code);

        signInWithPhoneAuthCredential(credential);

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    pd.dismiss();

                    verifyLayout.setVisibility(View.GONE);

                    Toast.makeText(EditProfileActivity.this, "Phone Number verified", Toast.LENGTH_SHORT).show();

                    phoneNumberEditText.setEnabled(false);

                }
                else {

                    pd.dismiss();
                    Toast.makeText(EditProfileActivity.this, "Incorrect verification code.", Toast.LENGTH_SHORT).show();

                }

            }
        });
    }

    @NonNull
    @Override
    public P createPresenter() {
        if (presenter == null) {
            return (P) new EditProfilePresenter(this);
        }
        return presenter;
    }

    protected void initContent() {
        presenter.loadProfile();
    }

    @Override
    public ProgressBar getProgressView() {
        return avatarProgressBar;
    }

    @Override
    public ImageView getImageView() {
        return imageView;
    }

    @Override
    public void onImagePikedAction() {
        startCropImageActivity();
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of pick image chooser
        super.onActivityResult(requestCode, resultCode, data);
        handleCropImageResult(requestCode, resultCode, data);
    }

    @Override
    public void setUsertype(String usertype) {
         usertype1.setText(usertype);
    }

    @Override
    public void setName(String username) {
        nameEditText.setText(username);
    }

    @Override
    public void setProfilePhoto(String photoUrl) {

        //Log.i("url",photoUrl);

        //imageUri = Uri.parse(photoUrl);
        ImageUtil.loadImage(GlideApp.with(this), photoUrl, imageView, new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                avatarProgressBar.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                avatarProgressBar.setVisibility(View.GONE);
                return false;
            }
        });
    }

    @Override
    public void setUseruri(String Useruri) {
        uriEditText.setText(Useruri);
    }

    @Override
    public void setBio(String bio) {
       bioEditText.setText(bio);
    }

    @Override
    public void setPhoneNumber(String number) {

        phoneNumberEditText.setText(number);

    }

    @Override
    public void setSkill(String userType,String skill) {

        if ("Startup".equals(userType)){

            skillEditText.setVisibility(View.GONE);
            startUpTypeEditText.setVisibility(View.VISIBLE);

            startUpTypeEditText.setText(skill);

        }
        else {

            skillEditText.setVisibility(View.VISIBLE);
            startUpTypeEditText.setVisibility(View.GONE);

            skillEditText.setText(skill);

        }

    }

    @Override
    public String getUsertypeText() {
        return usertype1.getText().toString();
    }

    @Override
    public String getSkill() {
        return skillEditText.getText().toString();
    }

    @Override
    public String getNameText() {
        return nameEditText.getText().toString();
    }

    @Override
    public String getUseruriText() {
        return uriEditText.getText().toString();
    }

    @Override
    public String getBioText() {
        return bioEditText.getText().toString();
    }

    @Override
    public String getNumber() {
        return phoneNumberEditText.getText().toString();
    }

    @Override
    public void setNameError(@Nullable String string) {
        nameEditText.setError(string);
//        nameEditText.requestFocus();
    }

    @Override
    public void setSkillError(@Nullable String string) {
        skillEditText.setError(string);
        skillEditText.requestFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.save:
                presenter.attemptCreateProfile(imageUri);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

