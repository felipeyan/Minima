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

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> implements Filterable {
    Database database;
    Export export;
    Preferences preferences;
    DialogMenus dialogMenus;
    Encryption encryption = new Encryption();

    Context context;
    ArrayList<String> noteIDS, noteTEXTS, noteMOD, noteTEXTSAll;

    public NoteAdapter(Context context, ArrayList<String> noteIDS, ArrayList<String> noteTEXTS, ArrayList<String> noteMOD) {
        this.database = new Database(context);
        this.export = new Export(context);
        this.preferences = new Preferences(context);
        this.dialogMenus = new DialogMenus(context);

        this.context = context;
        this.noteIDS = noteIDS; // IDs received from main activity
        this.noteTEXTS = noteTEXTS; // Encrypted notes received from main activity
        this.noteMOD = noteMOD; // Modification dates received from main activity
        this.noteTEXTSAll = new ArrayList<>(noteTEXTS); // List of all notes (used in the search system)
    }

    @NonNull
    @Override
    public NoteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_note, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteAdapter.ViewHolder holder, int position) {
        holder.note.setTextSize(TypedValue.COMPLEX_UNIT_SP, new Preferences(context).getFontSize());

        // Decrypt and store current received note
        String decryptedNote = encryption.decryptNote(context, noteTEXTS.get(holder.getAdapterPosition()));

        // Checks the length of the current note and displays only part of it to save processing
        reduceNoteDisplay(decryptedNote, holder);

        // Displays formatted date and time
        holder.dateTime.setText(new Preferences(context).dateTimeDisplay(noteMOD.get(position)));

        holder.layout.setOnClickListener(noteClick(holder, decryptedNote));
        holder.layout.setOnLongClickListener(noteLongClick(holder));
    }

    // Shows the note menu when the note is long-clicked
    public View.OnLongClickListener noteLongClick(@NonNull NoteAdapter.ViewHolder holder) {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                openMenu(view, holder.getAdapterPosition());
                return true;
            }
        };
    }

    // Starts the note activity with the values of the clicked note
    public View.OnClickListener noteClick(@NonNull NoteAdapter.ViewHolder holder, String note) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Transfers information from the clicked note to the note view activity
                Intent intent = new Intent(context, NoteActivity.class);

                new Preferences(context).storeExtra(intent,
                        new String[] { "selectedID", "selectedNote", "selectedLastModification" },
                        new String[] { noteIDS.get(holder.getAdapterPosition()), note, noteMOD.get(holder.getAdapterPosition()) }
                );
            }
        };
    }

    @Override
    public int getItemCount() {
        return noteTEXTS.size(); // Creates the RecyclerView based on the length of the ID's list
    }

    public void openMenu(View view, int position) { // Menu when the note is long clicked
        dialogMenus.popupMenu(view, R.menu.note_menu, new noteMenu(position));
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<String> filteredNotes = new ArrayList<>();

            if (constraint.toString().isEmpty()) {
                filteredNotes.addAll(noteTEXTSAll);
            } else {
                for (String note: noteTEXTSAll) {
                    if (encryption.decryptNote(context, note).toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filteredNotes.add(note);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredNotes;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            noteTEXTS.clear();
            noteTEXTS.addAll((Collection<? extends String>) results.values);
            notifyDataSetChanged();
        }
    };

    public void reduceNoteDisplay(String decryptedNote, NoteAdapter.ViewHolder holder) {
        if (decryptedNote.length() < 150) {
            holder.note.setText(decryptedNote);
        } else {
            holder.note.setText(decryptedNote.substring(0, 150));
        }
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
                if (database.deleteData(noteIDS.get(position)) != 0) { // Check the number of deleted lines
                    Toast.makeText(context, R.string.deleted_note, Toast.LENGTH_SHORT).show(); // Display a success message
                    ((Activity) context).recreate(); // Refresh the main screen
                } else { // If no lines are affected
                    Toast.makeText(context, R.string.error_deleting, Toast.LENGTH_SHORT).show(); // Display a error message
                }
                return true;
            } else if (menuTitle.equals(menuOptions[1])) {
                if (database.insertData(noteTEXTS.get(position))) { // Stores the encrypted value in the database
                    Toast.makeText(context, R.string.duplicated_note, Toast.LENGTH_SHORT).show(); // Display a success message
                    ((Activity) context).recreate(); // Refresh the main screen
                } else { // If unable to store in database
                    Toast.makeText(context, R.string.error_duplicating, Toast.LENGTH_SHORT).show(); // Display a error message
                }
                return true;
            } else if (menuTitle.equals(menuOptions[2])) {
                export.shareText(noteTEXTS.get(position));
                return true;
            } else {
                return false;
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView note;
        AppCompatTextView dateTime;
        CardView layout;

        public ViewHolder(@NonNull View view) {
            super(view);

            note = view.findViewById(R.id.notePreview);
            dateTime = view.findViewById(R.id.dateTimePreview);
            layout = view.findViewById(R.id.noteLayout);
        }
    }
}
