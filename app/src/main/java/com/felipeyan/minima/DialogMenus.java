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

    public class singleChoiceMenuClick implements DialogInterface.OnClickListener {
        Preferences preferences = new Preferences(context);

        String[] options;
        String menuName;

        public singleChoiceMenuClick(String[] options, String menuName) {
            this.options = options;
            this.menuName = menuName; // Used to indicate which menu is being called
        }

        @Override
        public void onClick(DialogInterface dialog, int i) {
            switch (menuName) {
                case "fontMenu": // Click action for font choice menu
                    preferences.storeData("userFont", options[i]); // Stores the new font selected in preferences
                    Toast.makeText(context, R.string.changed_font, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    ((Activity) context).recreate();
                    break;
                case "dateTimeMenu": // Click action for date and time format choice menu
                    preferences.storeData("dateTimeFormat", options[i]); // Stores the new date and time format in preferences
                    Toast.makeText(context, R.string.changed_date_time_format, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    break;
                default:
                    break;
            }
        }
    }

    public class dismissDialog implements View.OnClickListener {
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
