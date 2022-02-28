package com.felipeyan.minima;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;

public class ViewStyler {
    UserPreferences preferences;

    Context context;

    public ViewStyler(Context context) {
        preferences = new UserPreferences(context);

        this.context = context;
    }

    public void changeViewVisibility(View view, boolean reverse) {
        view.setVisibility(!reverse ? view.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE : view.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
    }

    public String getViewType(View view) {
        String[] split = view.getClass().toString().split("\\.");
        return split[split.length - 1];
    }

    public void changeAppFont() {
        String fontIdentifier = "font" + preferences.getFontFamily().replaceAll(" ", "");
        context.getTheme().applyStyle(context.getResources().getIdentifier(fontIdentifier, "style", context.getPackageName()), true);
    }

    public void changeViewFont(View... view) {
        for (View item : view) {
            switch (getViewType(item)) {
                case "AppCompatTextView":
                    AppCompatTextView textView = (AppCompatTextView) item;
                    textView.setTypeface(ResourcesCompat.getFont(context, preferences.getFontResource()));
                    break;
                case "AppCompatEditText":
                    AppCompatEditText editText = (AppCompatEditText) item;
                    editText.setTypeface(ResourcesCompat.getFont(context, preferences.getFontResource()));
                    break;
            }
        }
    }

    public void changeOrderIcon(View view) {
        AppCompatImageView orderIcon = (AppCompatImageView) view;

        switch (preferences.getListOrder()) {
            case "DESC":
                orderIcon.setImageResource(R.drawable.ic_down);
                break;
            case "ASC":
                orderIcon.setImageResource(R.drawable.ic_up);
                break;
        }
    }

    public String reduceNoteDisplay(String decryptedNote) {
        return decryptedNote.length() < 150 ?decryptedNote : decryptedNote.substring(0, 150);
    }

    public String dateTimeFormat(String dateTime) {
        String formattedValue = "";

        switch (preferences.getDateTimeFormat()) {
            case "12/31/2021 12h59":
                formattedValue =
                    dateTime.substring(4, 6) + "/" + // Month
                    dateTime.substring(6, 8) + "/" + // Day
                    dateTime.substring(0, 4) + " "; // Year
                break;
            case "31/12/2021 12h59":
                formattedValue =
                    dateTime.substring(6, 8) + "/" + // Day
                    dateTime.substring(4, 6) + "/" + // Month
                    dateTime.substring(0, 4) + " "; // Year
                break;
            case "2021/12/31 12h59":
                formattedValue =
                    dateTime.substring(0, 4) + "/" + // Year
                    dateTime.substring(4, 6) + "/" + // Month
                    dateTime.substring(6, 8) + " "; // Day
                break;
        }

        formattedValue += dateTime.substring(8, 10) + "h" + dateTime.substring(10, 12); // Time
        return formattedValue;
    }

    public static class CircleButton extends AppCompatButton {
        public CircleButton(@NonNull Context context) {
            super(context);
        }

        public CircleButton(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public CircleButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        }
    }
}
