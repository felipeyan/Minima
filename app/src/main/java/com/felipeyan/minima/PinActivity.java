package com.felipeyan.minima;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import java.util.Objects;

public class PinActivity extends AppCompatActivity {
    AppCompatTextView pinTitle;
    AppCompatEditText pinInput;
    AppCompatImageView pinBackspace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        pinTitle = findViewById(R.id.pinTitle);
        pinInput = findViewById(R.id.pinInput);
        pinBackspace = findViewById(R.id.pinBackspace);
        pinBackspace.setOnClickListener(new deleteNumber()); // Backspace click
        pinBackspace.setOnLongClickListener(new deleteAllNumbers()); // Backspace long click
        pinInput.addTextChangedListener(new pinText()); // When PIN input text is modified
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Changes the Activity text font to the stored value
        new Preferences(this).changeAppFont();
        // Changes toolbar title font
        new Preferences(this).changeViewFont(pinTitle);
    }

    public void insertNumber(View view) { // Applied via the onClick property in pinNumber (values/themes.xml)
        AppCompatButton button = (AppCompatButton) view; // Stores click View
        pinInput.append(button.getText().toString()); // Adds View text to PIN input
    }

    public class deleteNumber implements View.OnClickListener { // Backspace
        @Override
        public void onClick(View view) {
            if (!pinInput.getText().toString().isEmpty()) { // If PIN input is not empty, delete the last character
                pinInput.setText(Objects.requireNonNull(pinInput.getText()).toString().substring(0, pinInput.length() - 1));
            }
        }
    }

    public class deleteAllNumbers implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View view) {
            pinInput.setText("");
            return true;
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

    public static class PinButton extends AppCompatButton {
        public PinButton(@NonNull Context context) {
            super(context);
        }

        public PinButton(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public PinButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        }
    }

    @Override
    public void onBackPressed() { // Display the error message
        Toast.makeText(this, R.string.cant_close_screen, Toast.LENGTH_SHORT).show();
    }
}