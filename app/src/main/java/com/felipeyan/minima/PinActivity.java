package com.felipeyan.minima;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import java.util.Objects;

public class PinActivity extends AppCompatActivity {
    UserPreferences preferences;
    ViewStyler viewStyler;

    AppCompatTextView pinTitle;
    AppCompatEditText pinInput;
    AppCompatImageView pinBackspace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        preferences = new UserPreferences(this);
        viewStyler = new ViewStyler(this);

        pinTitle = findViewById(R.id.pinTitle);
        pinInput = findViewById(R.id.pinInput);
        pinBackspace = findViewById(R.id.pinBackspace);
        pinBackspace.setOnClickListener(deleteNumber()); // Backspace click
        pinBackspace.setOnLongClickListener(deleteAllNumbers()); // Backspace long click
        pinInput.addTextChangedListener(pinText()); // When PIN input text is modified
    }

    @Override
    protected void onStart() {
        super.onStart();

        // If a PIN password exists, go to PinActivity
        if (preferences.getAppPIN().isEmpty()) startActivity(new Intent(this, MainActivity.class));
        viewStyler.changeAppFont(); // Changes the Activity text font to the stored value
        viewStyler.changeViewFont(pinTitle); // Changes toolbar title font
    }

    public void insertNumber(View view) { // Applied via the onClick property in pinNumber (values/themes.xml)
        AppCompatButton button = (AppCompatButton) view; // Stores click View
        pinInput.append(button.getText().toString()); // Adds View text to PIN input
    }

    public View.OnClickListener deleteNumber() { // Backspace
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!pinInput.getText().toString().isEmpty()) { // If PIN input is not empty, delete the last character
                    pinInput.setText(Objects.requireNonNull(pinInput.getText()).toString().substring(0, pinInput.length() - 1));
                }
            }
        };
    }

    public View.OnLongClickListener deleteAllNumbers() {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                pinInput.setText("");
                return true;
            }
        };
    }

    private TextWatcher pinText() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                try { // Check if the input text is the same as the stored PIN
                    if (s.toString().equals(preferences.getEncryptedPreference(preferences.APP_PIN, true))) {
                        startActivity(new Intent(PinActivity.this, MainActivity.class)); // If it is the same, close this activity
                    }
                } catch (Exception e) { // Display the error message
                    Toast.makeText(PinActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
    }

    @Override
    public void onBackPressed() { // Display the error message
        finish();
    }
}
