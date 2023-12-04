package com.example.sportspot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {

    private ImageView EditImage;
    private TextView EditSport, EditDateRange, EditDescription;
    private Button DeletePostButton, EditPostButton, EditDateRangeButton, EditSportButton;
    private DatabaseReference ClickPostRef;
    private FirebaseAuth mAuth;
    private String PostKey, currentUserID, databaseUserID, sport, daterange, description, image ;

    public ClickPostActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        //gets the postkey from the main activity
        PostKey = getIntent().getExtras().get("PostKey").toString();
        //will get the postkey from firebase under "Posts"
        ClickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);

        EditImage = (ImageView) findViewById(R.id.edit_image);
        EditSport = (TextView) findViewById(R.id.edit_sport);
        EditDateRange = (TextView) findViewById(R.id.edit_date_range);
        EditDescription = (TextView) findViewById(R.id.edit_description);
        DeletePostButton = (Button) findViewById(R.id.delete_post_button);
        EditPostButton = (Button) findViewById(R.id.edit_post_button);
        EditSportButton = (Button) findViewById(R.id.edit_sport_button);
        EditDateRangeButton= (Button) findViewById(R.id.edit_daterange_button);


        //hides edit and delete buttons if the user clicks on a post that isn't theirs
        DeletePostButton.setVisibility(View.INVISIBLE);
        EditPostButton.setVisibility(View.INVISIBLE);
        EditSportButton.setVisibility(View.INVISIBLE);
        EditDateRangeButton.setVisibility(View.INVISIBLE);


        ClickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //validation added to prevent app from crashing from nullpointer exceptions
                if (snapshot.exists()) {
                    // Check if the values are null before using them
                    if (snapshot.child("sport").getValue() != null) {
                        sport = snapshot.child("sport").getValue().toString();
                        EditSport.setText(sport);
                    }

                    if (snapshot.child("daterange").getValue() != null) {
                        daterange = snapshot.child("daterange").getValue().toString();
                        EditDateRange.setText(daterange);
                    }

                    if (snapshot.child("description").getValue() != null) {
                        description = snapshot.child("description").getValue().toString();
                        EditDescription.setText(description);
                    }

                    if (snapshot.child("postimage").getValue() != null) {
                        image = snapshot.child("postimage").getValue().toString();
                        Picasso.get().load(image).into(EditImage);
                    }

                    if (snapshot.child("uid").getValue() != null) {
                        databaseUserID = snapshot.child("uid").getValue().toString();

                        //if the uid is the same as the user's, then the option to change the post will appear
                        if (currentUserID.equals(databaseUserID)) {
                            DeletePostButton.setVisibility(View.VISIBLE);
                            EditPostButton.setVisibility(View.VISIBLE);
                            EditSportButton.setVisibility(View.VISIBLE);
                            EditDateRangeButton.setVisibility(View.VISIBLE);
                        }

                        EditPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                EditCurrentPost(description);
                            }
                        });

                        EditDateRangeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                EditCurrentDateRange(daterange);
                            }
                        });

                        EditSportButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                EditCurrentSport(sport);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });


        DeletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteCurrentPost();
            }
        });
    }

    private void EditCurrentPost(String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post:");

        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(description);
        builder.setView(inputField);

        builder.setPositiveButton("update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //gets info from firebase path: description
                ClickPostRef.child("description").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this, "Post Updated Successfully", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.color.white);
    }

    private void EditCurrentDateRange(String daterange) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit DateRange:");

        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(daterange);
        builder.setView(inputField);

        builder.setPositiveButton("update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //gets info from firebase path: daterange
                ClickPostRef.child("daterange").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this, "DateRange Updated Successfully", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.color.white);
    }

    private void EditCurrentSport(String sport) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Sport:");

        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(sport);
        builder.setView(inputField);

        builder.setPositiveButton("update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //gets info from firebase path: sport
                ClickPostRef.child("sport").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this, "Sport Updated Successfully", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.color.white);
    }


    private void DeleteCurrentPost() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Confirm Deletion");
        builder.setMessage("Are you sure you want to delete this post?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked Yes, delete the post
                ClickPostRef.removeValue();
                SendUserToMainActivity();
                Toast.makeText(ClickPostActivity.this, "Post has been deleted", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked No, do nothing
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.color.white);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(ClickPostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}