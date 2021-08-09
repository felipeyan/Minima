package com.felipeyan.minima;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import java.util.Objects;

public class PinActivity extends AppCompatActivity {
    AppCompatTextView pinTitle;
    AppCompatEditText pinInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        pinTitle = findViewById(R.id.pinTitle);
        pinInput = findViewById(R.id.pinInput);
        pinInput.addTextChangedListener(new pinText()); // When PIN input text is modified
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Changes the Activity text font to the stored value
        new Preferences(this).changeAppFont(this);
        // Changes toolbar title font
        new Preferences(this).changeViewFont("TextView", pinTitle);
    }

    public void insertNumber(View view) { // Applied via the onClick property in pinNumber (values/themes.xml)
        AppCompatTextView textView = (AppCompatTextView) view; // Stores click View
        pinInput.append(textView.getText().toString()); // Adds View text to PIN input
    }

    public void deleteNumber(View view) { // Backspace
        if (!pinInput.getText().toString().isEmpty()) { // If PIN input is not empty, delete the last character
            pinInput.setText(Objects.requireNonNull(pinInput.getText()).toString().substring(0, pinInput.length() - 1));
        }
    }

    private class pinText implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void afterTextChanged(Editable s) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            try { // Check if the input text is the same as the stored PIN
                if (s.toString().equals(new Preferences(PinActivity.this).getPIN())) {
                    ((Activity) PinActivity.this).finish(); // If it is the same, close this activity
                }
            } catch (Exception e) { // Display the error message
                Toast.makeText(PinActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() { // Display the error message
        Toast.makeText(this, R.string.cant_close_screen, Toast.LENGTH_SHORT).show();
    }
}