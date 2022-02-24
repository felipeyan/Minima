package com.felipeyan.minima;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class Adapter extends RecyclerView.Adapter<Adapter.Holder> implements Filterable {
    public static final String NOTES_RV = "notes";

    Database database;
    DialogMenus dialogMenus;
    Encryption encryption;
    Export export;
    Preferences preferences;

    Context context;
    String origin;
    ArrayList<ArrayList<String>> data;
    ArrayList<String> dataForFilter;

    public Adapter(Context context, String origin, ArrayList<ArrayList<String>> data) {
        this.database = new Database(context);
        this.dialogMenus = new DialogMenus(context);
        this.encryption = new Encryption();
        this.export = new Export(context);
        this.preferences = new Preferences(context);

        this.context = context;
        this.origin = origin;
        this.data = data;

        switch (origin) {
            case NOTES_RV:
                this.dataForFilter = new ArrayList<>(getArrayByName("noteTEXTS"));
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

                String decryptedNote = encryption.decryptNote(context, getArrayByName("noteTEXTS").get(holder.getAdapterPosition()));

                note.setText(reduceNoteDisplay(decryptedNote));
                note.setTextSize(TypedValue.COMPLEX_UNIT_SP, preferences.getFontSize());
                dateTime.setText(preferences.dateTimeDisplay(getArrayByName("noteMOD").get(position)));
                layout.setOnClickListener(noteClick(holder, decryptedNote));
                layout.setOnLongClickListener(noteLongClick(holder));
                break;
        }
    }

    private String reduceNoteDisplay(String decryptedNote) {
        return decryptedNote.length() < 150 ? decryptedNote : decryptedNote.substring(0, 150);
    }

    @Override
    public int getItemCount() {
        switch (origin) {
            case NOTES_RV:
                return getArrayByName("noteIDS").size();
            default:
                return 0;
        }
    }

    final Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            ArrayList<String> filteredResults = new ArrayList<>();

            switch (origin) {
                case NOTES_RV:
                    if (constraint.toString().isEmpty()) {
                        filteredResults.addAll(dataForFilter);
                    } else {
                        for (String note : dataForFilter) {
                            if (encryption.decryptNote(context, note).toLowerCase().contains(constraint.toString().toLowerCase())) {
                                filteredResults.add(note);
                            }
                        }
                    }
                    break;
            }

            filterResults.values = filteredResults;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            switch (origin) {
                case NOTES_RV:
                    getArrayByName("noteTEXTS").clear();
                    getArrayByName("noteTEXTS").addAll((Collection<? extends String>) results.values);
                    notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    public Filter getFilter() {
        return filter;
    }

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

                new Preferences(context).storeExtra(intent,
                        new String[] { "selectedID", "selectedNote", "selectedLastModification" },
                        new String[] { getArrayByName("noteIDS").get(holder.getAdapterPosition()), note, getArrayByName("noteMOD").get(holder.getAdapterPosition()) }
                );
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
        dialogMenus.popupMenu(view, R.menu.note_menu, new noteMenu(position));
    }

    public class noteMenu implements PopupMenu.OnMenuItemClickListener {
        int position;

        public noteMenu(int position) {
            this.position = position;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            String menuTitle = item.getTitle().toString();

            String[] menuOptions = {
                    context.getString(R.string.menu_delete),
                    context.getString(R.string.menu_duplicate),
                    context.getString(R.string.menu_share)
            };

            if (menuTitle.equals(menuOptions[0])) {
                if (database.deleteData(getArrayByName("noteIDS").get(position)) != 0) {
                    Toast.makeText(context, R.string.deleted_note, Toast.LENGTH_SHORT).show();
                    ((Activity) context).recreate();
                } else {
                    Toast.makeText(context, R.string.error_deleting, Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (menuTitle.equals(menuOptions[1])) {
                if (database.insertData(getArrayByName("noteTEXTS").get(position))) {
                    Toast.makeText(context, R.string.duplicated_note, Toast.LENGTH_SHORT).show();
                    ((Activity) context).recreate();
                } else {
                    Toast.makeText(context, R.string.error_duplicating, Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (menuTitle.equals(menuOptions[2])) {
                export.shareText(getArrayByName("noteTEXTS").get(position));
                return true;
            } else {
                return false;
            }
        }
    }

    public ArrayList<String> getArrayByName(String name) {
        ArrayList<String> arrayList = new ArrayList<>();

        switch (origin) {
            case NOTES_RV:
                switch (name) {
                    case "noteIDS":
                        arrayList = data.get(0);
                        break;
                    case "noteTEXTS":
                        arrayList = data.get(1);
                        break;
                    case "noteMOD":
                        arrayList = data.get(2);
                        break;
                }
                break;

        }

        return arrayList;
    }
}
