package com.example.noteshark;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.noteshark.Notes.AddNote;
import com.example.noteshark.Notes.NoteDetails;
import com.example.noteshark.auth.Login;
import com.example.noteshark.model.Adapter;
import com.example.noteshark.model.Note;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class MainActivity extends AppCompatActivity{

    NavigationView nav_view;
    ImageView newNoteBtn, syncBtn, logoutBtn, showNotesBtn;
    RecyclerView noteLists;
    Adapter adapter;
    private Thread addNoteThread, logoutThread;
    FirebaseFirestore fStore;
    FirestoreRecyclerAdapter<Note,ViewHolderNote> noteAdapter;
    RelativeLayout relativeLayout;
    FirebaseUser currentUser;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initButtons();
        relativeLayout = findViewById(R.id.toolbar);
        noteLists = findViewById(R.id.notelist);
        fStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        Query query = fStore.collection("notes").document(currentUser.getUid()).collection("sortedNotes").orderBy("title", Query.Direction.DESCENDING);
        // Notatki uporządkowane w kolekcje "sortedNotes" na bazie UID użytkownika

        FirestoreRecyclerOptions<Note> allNotes = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();

        noteAdapter = new FirestoreRecyclerAdapter<Note, ViewHolderNote>(allNotes) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolderNote viewHolderNote, int i, @NonNull Note note) {
                viewHolderNote.noteTitle.setText(note.getTitle());
                final String docId = noteAdapter.getSnapshots().getSnapshot(i).getId();
                viewHolderNote.noteContent.setText(note.getContent());
                viewHolderNote.view.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(v.getContext(), NoteDetails.class);
                        i.putExtra("title", note.getTitle());
                        i.putExtra("content", note.getContent());
                        i.putExtra("noteId", docId);
                        v.getContext().startActivity(i);
                    }
                });

                ImageView deleteNoteIcon = viewHolderNote.view.findViewById(R.id.deleteNoteIcon);
                deleteNoteIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        DocumentReference docRef = fStore.collection("notes").document(docId);
                        docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MainActivity.this, "Usunięto notatkę", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, "Wystąpił błąd w usuwaniu", Toast.LENGTH_SHORT).show();
                                //nie udało się usunąć
                            }
                        });
                    }
                });
            }

            @NonNull
            @Override
            public ViewHolderNote onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_view_layout,parent,false);
                return new ViewHolderNote(view);
            }
        };

        noteLists.setLayoutManager(new LinearLayoutManager(this));
        noteLists.setAdapter(noteAdapter);

        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if(signInAccount != null){
//            Toast.makeText(this, signInAccount.getEmail(), Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    protected void onResume() {
        newNoteBtn();
        logOutBtn();
        super.onResume();
    }


    private void newNoteBtn() {
        try{
            addNoteThread = new Thread(){
                @Override
                public void run() {
                    super.run();
                    newNoteBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) { newNoteBtnClicked(); }
                    });
                }
            };
            addNoteThread.start();
        }
        catch(Exception e){
            Log.e("BRUH", "-------------------=-----------------------"+e);
        }

    }


    private void newNoteBtnClicked(){
        startActivity(new Intent(this, AddNote.class));
    }


    public class ViewHolderNote extends RecyclerView.ViewHolder{
        TextView noteTitle, noteContent;
        View view;

        public ViewHolderNote(@NonNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.titles);
            noteContent = itemView.findViewById(R.id.content);
            view = itemView;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        noteAdapter.startListening();
        // Initialize Firebase Auth

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Toast.makeText(this, "Zalogowano pomyślnie jako "+currentUser.getEmail(), Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Brak zalogowanego użytkownika", Toast.LENGTH_SHORT).show();
        }
    }


    private void logOutBtn(){
        logoutThread = new Thread(){
            @Override
            public void run(){
                super.run();
                logoutBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAuth.signOut();
                        Intent intent = new Intent(getApplicationContext(), Login.class);
                        startActivity(intent);
                        Toast.makeText(MainActivity.this, "Wylogowano pomyślnie", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
        logoutThread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try{
            noteAdapter.stopListening();
        }
        catch (Exception e){
            System.out.println("Wystąpił oczekiwany wyjątek" + e.getMessage());
        }
    }


    private void initButtons() {
        newNoteBtn = findViewById(R.id.addNote);
        syncBtn = findViewById(R.id.sync);
        logoutBtn = findViewById(R.id.logout);
        showNotesBtn = findViewById(R.id.notes);
    }

}
