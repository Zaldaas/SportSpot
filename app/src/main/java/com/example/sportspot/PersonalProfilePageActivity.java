package com.example.sportspot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonalProfilePageActivity extends AppCompatActivity {
    private ArrayList<String> postListArray;
    private RecyclerView postList;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference, PostsRef;
    private FirebaseDatabase database;
    private ImageView personalProfileImage;
    private TextView username;
    private Button editButton, backButton;
    private String profileImageLocation = "";
    private FirebaseStorage firebaseStorage;
    private StorageReference storageRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_profile_page);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        firebaseStorage = FirebaseStorage.getInstance();
        storageRef = firebaseStorage.getReference();

        backButton = findViewById(R.id.backButton);

        personalProfileImage = findViewById(R.id.personalProfileImage);
        username = findViewById(R.id.profileUsername);
        editButton = findViewById(R.id.profileEditButton);

        FirebaseUser currentUser = mAuth.getCurrentUser();

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
        pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                personalProfileImage.setImageBitmap(bmp);
                Toast.makeText(getApplicationContext(), "Image Found!", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("location", profileImageLocation);
                Toast.makeText(getApplicationContext(), "No path found", Toast.LENGTH_LONG).show();
            }
        });

        databaseReference.child("Users/" + currentUser.getUid() + "/userName").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {



                if(task.isSuccessful()){
                    Log.i("value", String.valueOf(task.getResult().getValue()));
                    username.setText(String.valueOf(task.getResult().getValue()));

                }
                else{
                    Log.i("Error","error retrieving username", task.getException());
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PersonalProfilePageActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });


/*        databaseReference.child("Posts/").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DataSnapshot> retrievePosts) {

                if(retrievePosts.isSuccessful()){
                    Log.i("value", String.valueOf(retrievePosts.getResult().getValue()));
                    username.setText(String.valueOf(retrievePosts.getResult().getValue()));
                    for (DataSnapshot child : retrievePosts.getResult().getChildren()) {
                        Log.i("Children", String.valueOf(child.getValue()));
                        for (DataSnapshot newChild : child.getChildren()) {
                            Log.i("Children of Children", String.valueOf(newChild.getValue()));

                        }

                    }
                }
                else{
                    Log.i("Error","error retrieving posts", retrievePosts.getException());
                }
            }
        });


*/

        postListArray = new ArrayList<>();
        retrieveUserPosts(currentUser.getUid());
        Log.i("After assignment", postListArray.toString());
//currentUser.getUid(
//"VER9lYwYzUYwAbJim8UoEGjok6p1"




    }

    private ArrayList retrieveUserPosts(String uid) {

        DatabaseReference db = FirebaseDatabase.getInstance().getReference("Posts");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postID : snapshot.getChildren()) {
                    if(postID.child("uid").getValue().toString().equals(uid)){
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

        return postListArray;
    }

    private void setUserPostList(ArrayList<String> userPosts, String uid){
        if(postListArray.size() > 0) {
            postListArray = userPosts;
            //Log.i("In method", postListArray.get(0).toString());
            //PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts/" + postListArray.get(0));
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
                new FirebaseRecyclerAdapter<Posts, PersonalProfilePageActivity.PostsViewHolder>(options) {


                    @Override
                    protected void onBindViewHolder(@NonNull PersonalProfilePageActivity.PostsViewHolder holder, int position, @NonNull Posts model) {
                        final String PostKey = getRef(position).getKey();

                        if(model.getUid().equals(uid)) {
                            holder.setUsername(model.getUsername()); // Fix the method name here
                            holder.setTime(model.getTime());
                            holder.setDate(model.getDate());
                            holder.setSport(model.getSport());
                            holder.setDaterange(model.getDaterange());
                            holder.setDescription(model.getDescription());
                            holder.setProfileimage(getApplicationContext(), model.getProfileimage());
                            holder.setPostimage(getApplicationContext(), model.getPostimage());
                            Log.i("Holder", "made");

                        }
                        else{

                            Log.i("UID", model.getUid());
                        }

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent clickPostIntent = new Intent(PersonalProfilePageActivity.this,ClickPostActivity.class);
                                clickPostIntent.putExtra("PostKey", PostKey);
                                startActivity(clickPostIntent);
                            }
                        });
                    }

                    //@NonNull
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
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            username.setText(userName);
        }

        public void setProfileimage(Context ctx, String profileimage) {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            Picasso.get().load(profileimage).into(image);
        }

        public void setTime(String time) {
            TextView PostTime = (TextView) mView.findViewById(R.id.post_time);
            PostTime.setText("    " + time);
        }

        public void setDate(String date) {
            TextView PostDate = (TextView) mView.findViewById(R.id.post_date);
            PostDate.setText("    " + date);
        }

        public void setDescription(String description) {
            TextView PostDescription = (TextView) mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setSport(String sport) {
            TextView PostSport = (TextView) mView.findViewById(R.id.post_sport);
            PostSport.setText(sport);
        }

        public void setDaterange(String daterange) {
            TextView PostDateRange = (TextView) mView.findViewById(R.id.post_date_range);
            PostDateRange.setText(daterange);

        }
        public void setPostimage(Context ctx1,  String postimage)
        {
            ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.get().load(postimage).into(PostImage);
        }

    }
}