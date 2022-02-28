package com.felipeyan.minima;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.widget.Toast;

public class DialogMenus {
    UserPreferences preferences;

    Context context;

    public DialogMenus(Context context) {
        preferences = new UserPreferences(context);
        this.context = context;
    }

    public AlertDialog.Builder alertBuilder(int message, DialogInterface.OnClickListener... clickListeners) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message).setCancelable(true).setPositiveButton(R.string.yes, clickListeners[0]);
        if (clickListeners.length > 1) builder.setNegativeButton(R.string.no, clickListeners[1]);
        builder.show();

        return builder;
    }

    public AlertDialog.Builder alertBuilder(boolean cancelable, int message, DialogInterface.OnClickListener... clickListeners) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message).setCancelable(cancelable).setPositiveButton(R.string.yes, clickListeners[0]);
        if (clickListeners.length > 1) builder.setNegativeButton(R.string.no, clickListeners[1]);
        builder.show();

        return builder;
    }

    public PopupMenu popupMenu(View view, int menuRes, PopupMenu.OnMenuItemClickListener clickListener) {
        PopupMenu menu = new PopupMenu(context, view);
        menu.getMenuInflater().inflate(menuRes, menu.getMenu());
        menu.setOnMenuItemClickListener(clickListener);
        menu.show();

        return menu;
    }

    public AlertDialog preferenceSingleChoiceMenu(String preference, int arrayId, int title, int message, boolean recreateActivity) {
        String[] items = context.getResources().getStringArray(arrayId);
        int checkedItem = preferences.getStringArrayIndex(preference);

        return new AlertDialog.Builder(context)
            .setTitle(title)
            .setSingleChoiceItems(items, checkedItem, preferenceClick(preference, items, message, recreateActivity))
            .show();
    }

    public DialogInterface.OnClickListener preferenceClick(String preference, String[] items, int message, boolean recreateActivity) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                preferences.storePreference(preference, items[i]);
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                dialog.dismiss();

                if (recreateActivity) ((Activity) context).recreate();
            }
        };
    }

    public Dialog customLayoutDialog(int layout, boolean cancelable) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(layout);
        dialog.getWindow().setBackgroundDrawable(null);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.setCancelable(cancelable);

        return dialog;
    }

    public View.OnClickListener dismissDialog(Dialog dialog) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        };
    }
}
