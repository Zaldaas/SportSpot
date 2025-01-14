package com.example.sportspot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonalProfilePageActivity extends AppCompatActivity {
    private ArrayList<String> postListArray;
    private RecyclerView postList;
    private DatabaseReference PostsRef;
    private ImageView personalProfileImage;
    private TextView username;
    private String profileImageLocation = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_profile_page);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageRef = firebaseStorage.getReference();

        Button backButton = findViewById(R.id.backButton);

        personalProfileImage = findViewById(R.id.personalProfileImage);
        username = findViewById(R.id.profileUsername);
        findViewById(R.id.profileEditButton);

        FirebaseUser currentUser = mAuth.getCurrentUser();

        assert currentUser != null;
        profileImageLocation = currentUser.getUid() + ".jpg";

        StorageReference pathReference = storageRef.child("ProfileImages/" + profileImageLocation);

        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");


        postList = findViewById(R.id.current_user_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        final long ONE_MEGABYTE = 1024 * 1024;
        pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            personalProfileImage.setImageBitmap(bmp);
            Toast.makeText(getApplicationContext(), "Image Found!", Toast.LENGTH_LONG).show();
        }).addOnFailureListener(e -> {
            Log.i("location", profileImageLocation);
            Toast.makeText(getApplicationContext(), "No path found", Toast.LENGTH_LONG).show();
        });

        databaseReference.child("Users/" + currentUser.getUid() + "/userName").get().addOnCompleteListener(task -> {



            if(task.isSuccessful()){
                Log.i("value", String.valueOf(task.getResult().getValue()));
                username.setText(String.valueOf(task.getResult().getValue()));

            }
            else{
                Log.i("Error","error retrieving username", task.getException());
            }
        });

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(PersonalProfilePageActivity.this, MainActivity.class);
            startActivity(intent);
        });


        postListArray = new ArrayList<>();
        retrieveUserPosts(currentUser.getUid());
        Log.i("After assignment", postListArray.toString());
//currentUser.getUid(
//"VER9lYwYzUYwAbJim8UoEGjok6p1"




    }

    private void retrieveUserPosts(String uid) {

        DatabaseReference db = FirebaseDatabase.getInstance().getReference("Posts");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postID : snapshot.getChildren()) {
                    if(Objects.requireNonNull(postID.child("uid").getValue()).toString().equals(uid)){
                        postListArray.add(postID.getKey());
                    }
                }
                Log.i("post list", postListArray.toString());
                setUserPostList(postListArray, uid);

            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        Log.i("After method in retrieve", postListArray.toString());

    }

    private void setUserPostList(ArrayList<String> userPosts, String uid){
        if(!postListArray.isEmpty()) {
            postListArray = userPosts;
            PostsRef = FirebaseDatabase.getInstance().getReference().child(("Posts"));
            //Log.i("POST REF", PostsRef.toString());
            DisplayCurrentUsersPosts(uid);
        }
    }


    private void DisplayCurrentUsersPosts(String uid) {
        // Create a query to retrieve only the posts of the current user
        Query userPostsQuery = PostsRef.orderByChild("uid").equalTo(uid);
        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(userPostsQuery, Posts.class) //alsp updated here -zootzoot
                        .build();

        FirebaseRecyclerAdapter<Posts, PersonalProfilePageActivity.PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<>(options) {


                    @Override
                    protected void onBindViewHolder(@NonNull PersonalProfilePageActivity.PostsViewHolder holder, int position, @NonNull Posts model) {
                        final String PostKey = getRef(position).getKey();

                        if (model.getUid().equals(uid)) {
                            holder.setUsername(model.getUsername()); // Fix the method name here
                            holder.setTime(model.getTime());
                            holder.setDate(model.getDate());
                            holder.setSport(model.getSport());
                            holder.setDaterange(model.getDaterange());
                            holder.setDescription(model.getDescription());
                            holder.setProfileimage(getApplicationContext(), model.getProfileimage());
                            holder.setPostimage(getApplicationContext(), model.getPostimage());
                            Log.i("Holder", "made");

                        } else {

                            Log.i("UID", model.getUid());
                        }

                        holder.mView.setOnClickListener(v -> {
                            Intent clickPostIntent = new Intent(PersonalProfilePageActivity.this, ClickPostActivity.class);
                            clickPostIntent.putExtra("PostKey", PostKey);
                            startActivity(clickPostIntent);
                        });
                    }

                    //@NonNull
                    @NonNull
                    @Override
                    public PersonalProfilePageActivity.PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout, parent, false);
                        return new PersonalProfilePageActivity.PostsViewHolder(view);
                    }

                };

        postList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

        }

        public void setUsername(String userName) {
            TextView username = mView.findViewById(R.id.post_user_name);
            username.setText(userName);
        }

        public void setProfileimage(Context ignoredCtx, String profileimage) {
            CircleImageView image = mView.findViewById(R.id.post_profile_image);
            Picasso.get().load(profileimage).into(image);
        }

        @SuppressLint("SetTextI18n")
        public void setTime(String time) {
            TextView PostTime = mView.findViewById(R.id.post_time);
            PostTime.setText("    " + time);
        }

        @SuppressLint("SetTextI18n")
        public void setDate(String date) {
            TextView PostDate = mView.findViewById(R.id.post_date);
            PostDate.setText("    " + date);
        }

        public void setDescription(String description) {
            TextView PostDescription = mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setSport(String sport) {
            TextView PostSport = mView.findViewById(R.id.post_sport);
            PostSport.setText(sport);
        }

        public void setDaterange(String daterange) {
            TextView PostDateRange = mView.findViewById(R.id.post_date_range);
            PostDateRange.setText(daterange);

        }
        public void setPostimage(Context ignoredCtx1, String postimage)
        {
            ImageView PostImage = mView.findViewById(R.id.post_image);
            Picasso.get().load(postimage).into(PostImage);
        }

    }
}