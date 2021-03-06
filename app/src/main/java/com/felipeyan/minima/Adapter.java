package com.felipeyan.minima;

import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class Adapter extends RecyclerView.Adapter<Adapter.Holder> implements Filterable {
    public static final String NOTES_RV = "notes";
    public static final String SETTINGS_RV = "settings";
    public static final String PAGES_RV = "pages";

    Database database;
    DialogMenus dialogMenus;
    Encryption encryption;
    Export export;
    Pages pages;
    UserPreferences preferences;
    ViewStyler viewStyler;

    Context context;
    String origin;

    public Adapter(Context context, String origin) {
        this.database = new Database(context);
        this.encryption = new Encryption();
        this.export = new Export(context);
        this.dialogMenus = new DialogMenus(context);
        this.preferences = new UserPreferences(context);
        this.viewStyler = new ViewStyler(context);

        this.context = context;
        this.origin = origin;

        switch (origin) {
            case PAGES_RV: case NOTES_RV:
                this.pages = ((MainActivity) context).pages;
                break;
        }
    }

    @NonNull
    @Override
    public Adapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = null;

        switch (origin) {
            case NOTES_RV:
                view = inflater.inflate(R.layout.item_note, parent, false);
                break;
            case SETTINGS_RV:
                view = inflater.inflate(R.layout.item_option, parent, false);
                break;
            case PAGES_RV:
                view = inflater.inflate(R.layout.item_page, parent, false);
                break;
        }

        return new Holder(Objects.requireNonNull(view));
    }

    @Override
    public void onBindViewHolder(@NonNull Adapter.Holder holder, int position) {
        switch (origin) {
            case NOTES_RV:
                AppCompatTextView note = holder.itemView.findViewById(R.id.notePreview);
                AppCompatTextView dateTime = holder.itemView.findViewById(R.id.dateTimePreview);
                CardView layout = holder.itemView.findViewById(R.id.noteLayout);

                String decryptedNote = encryption.decryptNote(context, pages.noteTexts.get(position));

                note.setText(viewStyler.reduceNoteDisplay(decryptedNote));
                note.setTextSize(TypedValue.COMPLEX_UNIT_SP, preferences.fontSizeToSP());
                dateTime.setText(viewStyler.dateTimeFormat(pages.noteMods.get(position)));
                layout.setOnClickListener(noteClick(holder, decryptedNote));
                layout.setOnLongClickListener(noteLongClick(holder));
                break;
            case SETTINGS_RV:
                AppCompatImageView optionIcon = holder.itemView.findViewById(R.id.settingsIV);
                AppCompatTextView optionText = holder.itemView.findViewById(R.id.settingsTV);
                CardView optionCard = holder.itemView.findViewById(R.id.settingsCV);
                LinearLayout optionsLayout = holder.itemView.findViewById(R.id.settingsLL);

                int[] icons = {
                        R.drawable.ic_lock, R.drawable.ic_font, R.drawable.ic_font_size,
                        R.drawable.ic_date, R.drawable.ic_restore_export, R.drawable.ic_delete
                };

                String[] options = ((SettingsActivity) context).getSettings();

                if (position == (options.length - 1)) optionsLayout.setBackground(context.getResources().getDrawable(R.drawable.red_app_bar));

                optionIcon.setImageResource(icons[position]);
                optionText.setText(options[position]);
                optionCard.setOnClickListener(((SettingsActivity) context).settingsItemClick(position));
                break;
            case PAGES_RV:
                ViewStyler.CircleButton pageButton = holder.itemView.findViewById(R.id.pageButton);
                pageButton.setText(String.valueOf(position + 1));
                pageButton.setOnClickListener(pages.pageClick(position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        int itemCount = 0;

        switch (origin) {
            case NOTES_RV:
                itemCount = pages.noteTexts.size();
                break;
            case SETTINGS_RV:
                itemCount = ((SettingsActivity) context).getSettings().length;
                break;
            case PAGES_RV:
                itemCount = pages.getPagesCount();
                break;
        }

        return itemCount;
    }

    @Override
    public Filter getFilter() {
        return dataFilter;
    }

    Filter dataFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            pages.getArraysData(0, pages.data.getDataCount());
            ArrayList<String> filteredData = new ArrayList<>();

            if (charSequence.toString().isEmpty()) {
                filteredData.addAll(pages.noteTexts);
            } else {
                for (String note : pages.noteTexts) {
                    if (encryption.decryptNote(context, note).toLowerCase().contains(charSequence.toString().toLowerCase())) {
                        filteredData.add(note);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredData;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            if (filterResults.values != null) {
                pages.noteTexts.clear();
                pages.noteTexts.addAll((Collection<? extends String>) filterResults.values);
                notifyDataSetChanged();
            }
        }
    };

    public static class Holder extends RecyclerView.ViewHolder {
        public Holder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public View.OnClickListener noteClick(@NonNull Adapter.Holder holder, String note) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, NoteActivity.class);
                intent.putExtra("selectedID", pages.noteIds.get(holder.getAdapterPosition()));
                intent.putExtra("selectedNote", note);
                intent.putExtra("selectedLastModification", pages.noteMods.get(holder.getAdapterPosition()));
                context.startActivity(intent);
            }
        };
    }

    public View.OnLongClickListener noteLongClick(@NonNull Adapter.Holder holder) {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                openMenu(view, holder.getAdapterPosition());
                return true;
            }
        };
    }

    public void openMenu(View view, int position) {
        dialogMenus.popupMenu(view, R.menu.note_menu, noteMenu(position));
    }

    public PopupMenu.OnMenuItemClickListener noteMenu(int position) {
        return new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String itemTitle = item.getTitle().toString();
                String[] menuOptions = context.getResources().getStringArray(R.array.note_options);

                if (itemTitle.equals(menuOptions[0])) {
                    if (database.deleteData(pages.noteIds.get(position)) != 0) {
                        Toast.makeText(context, R.string.deleted_note, Toast.LENGTH_SHORT).show();
                        ((MainActivity) context).listNotes();
                    } else {
                        Toast.makeText(context, R.string.error_deleting, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                } else if (itemTitle.equals(menuOptions[1])) {
                    if (database.insertData(pages.noteTexts.get(position), null)) {
                        Toast.makeText(context, R.string.duplicated_note, Toast.LENGTH_SHORT).show();
                        ((MainActivity) context).listNotes();
                    } else {
                        Toast.makeText(context, R.string.error_duplicating, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                } else if (itemTitle.equals(menuOptions[2])) {
                    export.shareText(pages.noteTexts.get(position));
                    return true;
                }

                return false;
            }
        };
    }
}
