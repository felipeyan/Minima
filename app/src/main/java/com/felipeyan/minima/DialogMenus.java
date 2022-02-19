package com.felipeyan.minima;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class DialogMenus {
    Context context;

    public DialogMenus(Context context) {
        this.context = context;
    }

    public class dialogBuilder extends AlertDialog.Builder {
        public dialogBuilder(int message, DialogInterface.OnClickListener... buttons) {
            super(context);

            setMessage(message);
            setCancelable(true);
            setPositiveButton(R.string.yes, buttons[0]);
            setNegativeButton(R.string.no, buttons.length > 1 ? buttons[1] : new dialogClick(""));
            show();
        }

        public dialogBuilder(int title, String[] items, int checkedItem, DialogInterface.OnClickListener dialogClick) {
            super(context, R.style.dialogStyle);

            setTitle(title);
            setSingleChoiceItems(items, checkedItem, dialogClick);
            show();
        }
    }

    public void singleChoiceMenu(int options, int dialogTitle, String preference, int message, boolean recreate) {
        String[] optionsList = context.getResources().getStringArray(options);

        new dialogBuilder(dialogTitle,
                context.getResources().getStringArray(options),
                new Preferences(context).getStringArrayIndex(optionsList, preference),
                new singleChoiceMenuClick(optionsList, preference, message, recreate));
    }

    public class singleChoiceMenuClick implements DialogInterface.OnClickListener {
        String[] options; // List of options in dialog box
        String preference; // Preference name to be stored
        int message; // Message to be displayed
        boolean recreate; // If it's necessary to recreate the current screen

        public singleChoiceMenuClick(String[] options, String preference, int message, boolean recreate) {
            this.options = options;
            this.preference = preference;
            this.message = message;
            this.recreate = recreate;
        }

        @Override
        public void onClick(DialogInterface dialog, int i) {
            new Preferences(context).storeData(preference, options[i]); // Stores pressed value as preference
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show(); // Displays the success message
            dialog.dismiss(); // Closes the current dialog

            if (recreate) ((Activity) context).recreate(); // If "recreate" is true, recreates the screen with the selected font
        }
    }

    public class dialogClick implements DialogInterface.OnClickListener {
        String indicator;

        public dialogClick(String indicator) {
            this.indicator = indicator;
        }

        @Override
        public void onClick(DialogInterface dialog, int i) {
            switch (indicator) {
                case "removePassPositive":
                    new Preferences(context).removeData("userPIN");
                    Toast.makeText(context, R.string.removed_pass, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    break;
                case "deleteDBPositive":
                    new Database(context).deleteDatabase();
                    Toast.makeText(context, R.string.database_deleted, Toast.LENGTH_SHORT).show();
                    break;
                case "finish":
                    dialog.dismiss();
                    ((Activity) context).finish();
                    break;
                default:
                    dialog.dismiss();
                    break;
            }
        }
    }

    public static class dismissDialog implements View.OnClickListener {
        Dialog dialog;

        public dismissDialog(Dialog dialog) {
            this.dialog = dialog; // Receive the dialog
        }

        @Override
        public void onClick(View v) {
            dialog.dismiss(); // Closes the dialog
        }
    }
}
