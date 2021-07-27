package com.felipeyan.minima;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {

    Encryption encryption = new Encryption();
    Context context;
    ArrayList<String> noteIDS, noteTEXTS;

    public NoteAdapter(Context originContext, ArrayList<String> originIDS, ArrayList<String> originTEXTS) {
        context = originContext;
        noteIDS = originIDS;
        noteTEXTS = originTEXTS;
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
        String decryptedNote = decryptNote(noteTEXTS.get(position));

        // Checks the length of the note and displays only part of it to save processing
        if (decryptedNote.length() < 150) {
            holder.note.setText(decryptedNote);
        } else {
            holder.note.setText(decryptedNote.substring(0, 150));
        }

        // Starts the note activity with the values of the clicked note
        holder.note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, NoteActivity.class);
                intent.putExtra("selectedID", noteIDS.get(position));
                intent.putExtra("selectedNote", decryptedNote);
                context.startActivity(intent);
            }
        });

        // Shows the note menu when the note is long-clicked
        holder.note.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openMenu(v);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteIDS.size(); // Creates the RecyclerView based on the length of the ID's list
    }

    // Menu when the note is long clicked
    public void openMenu(View view) {
        PopupMenu menu = new PopupMenu(context, view);
        menu.getMenu().add(R.string.menu_delete); // Delete note option
        menu.getMenu().add(R.string.menu_duplicate); // Duplicate note option
        menu.getMenu().add(R.string.menu_share); // Share note option

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle().toString().equals(context.getString(R.string.menu_delete))) {
                    Toast.makeText(context, R.string.menu_delete, Toast.LENGTH_SHORT).show();
                    return true;
                } else if (item.getTitle().toString().equals(context.getString(R.string.menu_duplicate))) {
                    Toast.makeText(context, R.string.menu_duplicate, Toast.LENGTH_SHORT).show();
                    return true;
                } else if (item.getTitle().toString().equals(context.getString(R.string.menu_share))) {
                    Toast.makeText(context, R.string.menu_share, Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    return false;
                }
            }
        });

        menu.show(); // Display the menu
    }

    public String decryptNote(String note) {
        try { // Decrypts the note using the stored password
            note = encryption.decrypt(note, new Preferences(context).getPassword());
        } catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }

        return note; // Returns the decrypted note as a String
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView note;

        public ViewHolder(@NonNull View view) {
            super(view);

            note = view.findViewById(R.id.notePreview);
        }
    }
}
