package com.felipeyan.minima;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> implements Filterable {

    Database database;
    Encryption encryption = new Encryption();
    Context context;
    ArrayList<String> noteIDS, noteTEXTS, noteTEXTSAll;

    public NoteAdapter(Context originContext, ArrayList<String> originIDS, ArrayList<String> originTEXTS) {
        database = new Database(originContext);
        context = originContext;
        noteIDS = originIDS;
        noteTEXTS = originTEXTS;
        noteTEXTSAll = new ArrayList<>(originTEXTS);
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
        // Decrypts and stores the received note
        String decryptedNote = encryption.decryptNote(context, noteTEXTS.get(position));

        // Checks the length of the note and displays only part of it to save processing
        if (decryptedNote.length() < 150) {
            holder.note.setText(decryptedNote);
        } else {
            holder.note.setText(decryptedNote.substring(0, 150));
        }

        // Starts the note activity with the values of the clicked note
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, NoteActivity.class);
                intent.putExtra("selectedID", noteIDS.get(position));
                intent.putExtra("selectedNote", decryptedNote);
                context.startActivity(intent);
            }
        });

        // Shows the note menu when the note is long-clicked
        holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openMenu(v, position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteTEXTS.size(); // Creates the RecyclerView based on the length of the ID's list
    }

    // Menu when the note is long clicked
    public void openMenu(View view, int position) {
        PopupMenu menu = new PopupMenu(context, view);
        menu.getMenu().add(R.string.menu_delete); // Delete note option
        menu.getMenu().add(R.string.menu_duplicate); // Duplicate note option
        menu.getMenu().add(R.string.menu_share); // Share note option

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle().toString().equals(context.getString(R.string.menu_delete))) {
                    if (database.deleteData(noteIDS.get(position)) != 0) { // Check the number of deleted lines
                        Toast.makeText(context, R.string.deleted_note, Toast.LENGTH_SHORT).show(); // Display a success message
                        ((Activity) context).recreate(); // Refresh the main screen
                    } else { // If no lines are affected
                        Toast.makeText(context, R.string.error_deleting, Toast.LENGTH_SHORT).show(); // Display a error message
                    }
                    return true;
                } else if (item.getTitle().toString().equals(context.getString(R.string.menu_duplicate))) {
                    if (database.insertData(noteTEXTS.get(position))) { // Stores the encrypted value in the database
                        Toast.makeText(context, R.string.duplicated_note, Toast.LENGTH_SHORT).show(); // Display a success message
                        ((Activity) context).recreate(); // Refresh the main screen
                    } else { // If unable to store in database
                        Toast.makeText(context, R.string.error_duplicating, Toast.LENGTH_SHORT).show(); // Display a error message
                    }
                    return true;
                } else if (item.getTitle().toString().equals(context.getString(R.string.menu_share))) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, encryption.decryptNote(context, noteTEXTS.get(position))); // Decrypts and stores the selected note
                    intent.setType("text/plain");
                    context.startActivity(Intent.createChooser(intent, null));
                    return true;
                } else {
                    return false;
                }
            }
        });

        menu.show(); // Display the menu
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView note;
        LinearLayout layout;

        public ViewHolder(@NonNull View view) {
            super(view);

            note = view.findViewById(R.id.notePreview);
            layout = view.findViewById(R.id.noteLayout);
        }
    }
}
