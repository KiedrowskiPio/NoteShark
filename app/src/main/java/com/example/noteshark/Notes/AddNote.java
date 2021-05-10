package com.example.noteshark.Notes;

import android.os.Bundle;

import com.example.noteshark.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class AddNote extends AppCompatActivity {

    FirebaseFirestore fStore;
    EditText noteTitle, noteContent;
    ProgressBar progressBarSaving;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        fStore = FirebaseFirestore.getInstance();
        noteContent = findViewById(R.id.addNoteContent);
        noteTitle = findViewById(R.id.addNoteTitle);
        progressBarSaving = findViewById(R.id.progressBar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nTitle = noteTitle.getText().toString();
                String nContent = noteContent.getText().toString();

                if(nTitle.isEmpty() || nContent.isEmpty()){
                    Toast.makeText(AddNote.this,"Aby zapisać notatkę wypełnij oba pola", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Pasek ładowania
                progressBarSaving.setVisibility(View.VISIBLE);


                DocumentReference docref = fStore.collection("notes").document(currentUser.getUid()).collection("sortedNotes").document();
                Map<String, Object> note = new HashMap<>();
                note.put("title",nTitle);
                note.put("content",nContent);

                docref.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddNote.this, "Dodano notatkę", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddNote.this, "Wystąpił błąd", Toast.LENGTH_SHORT).show();
                        progressBarSaving.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }
}