package com.example.sportspot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;

    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef;

    String currentUserID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            currentUserID = currentUser.getUid();

            // Initialize PostsRef
            UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
            PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

            // functions for the action bar submenu
            Toolbar mToolbar = findViewById(R.id.main_page);
            setSupportActionBar(mToolbar);
            Objects.requireNonNull(getSupportActionBar()).setTitle("Home");

            // new post button function
            ImageButton addNewPostButton = findViewById(R.id.add_new_post_button);

            // functions for the drawer layout
            DrawerLayout drawerLayout = findViewById(R.id.drawable_layout);
            actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
            drawerLayout.addDrawerListener(actionBarDrawerToggle);
            actionBarDrawerToggle.syncState();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            NavigationView navigationView = findViewById(R.id.navigation_view);

            postList = findViewById(R.id.all_users_post_list);
            postList.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setReverseLayout(true);
            linearLayoutManager.setStackFromEnd(true);
            postList.setLayoutManager(linearLayoutManager);

            View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
            NavProfileImage = navView.findViewById(R.id.nav_profile_image);
            NavProfileUserName = navView.findViewById(R.id.nav_user_full_name);

            UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChild("userName")) {
                            String username = Objects.requireNonNull(dataSnapshot.child("userName").getValue()).toString();
                            NavProfileUserName.setText(username);
                        }
                        if (dataSnapshot.hasChild("profileImage")) {
                            String image = Objects.requireNonNull(dataSnapshot.child("profileImage").getValue()).toString();
                            Picasso.get().load(image).placeholder(R.drawable.profile_image).into(NavProfileImage);
                        } else {
                            Toast.makeText(MainActivity.this, "Profile name does not exist...", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle onCancelled
                }
            });

            navigationView.setNavigationItemSelectedListener(item -> {
                UserMenuSelector(item);
                return false;
            });

            addNewPostButton.setOnClickListener(v -> SendUserToPostActivity());

            DisplayAllUsersPosts();

        } else {
            // If currentUser is null, the user is not authenticated
            SendUserToWelcomePageActivity();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            SendUserToWelcomePageActivity();
        } else {
            CheckUserExistence();
        }
    }

    private void CheckUserExistence() {
        final String current_user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(current_user_id)) {
                    SendUserToWelcomePageActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });
    }


    private void SendUserToWelcomePageActivity() {
        Intent loginIntent = new Intent(MainActivity.this, WelcomePageActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void SendUserToPersonalProfilePageActivity() {
        Intent loginIntent = new Intent(MainActivity.this, PersonalProfilePageActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }


    private void DisplayAllUsersPosts() {
        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(PostsRef, Posts.class)
                        .build();

        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull Posts model) {
                        final String PostKey = getRef(position).getKey();

                        holder.setUsername(model.getUsername()); // Fix the method name here
                        holder.setTime(model.getTime());
                        holder.setDate(model.getDate());
                        holder.setSport(model.getSport());
                        holder.setDaterange(model.getDaterange());
                        holder.setDescription(model.getDescription());
                        holder.setProfileimage(getApplicationContext(), model.getProfileimage());
                        holder.setPostimage(getApplicationContext(), model.getPostimage());

                        holder.mView.setOnClickListener(v -> {
                            Intent clickPostIntent = new Intent(MainActivity.this, ClickPostActivity.class);
                            clickPostIntent.putExtra("PostKey", PostKey);
                            startActivity(clickPostIntent);
                        });

                    }

                    @NonNull
                    @Override
                    public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout, parent, false);
                        return new PostsViewHolder(view);
                    }
                };

        postList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }


    public static class PostsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setUsername(String userName)
        {
            TextView username = mView.findViewById(R.id.post_user_name);
            username.setText(userName);
        }

        public void setProfileimage(Context ignoredCtx, String profileimage)
        {
            CircleImageView image = mView.findViewById(R.id.post_profile_image);
            Picasso.get().load(profileimage).into(image);
        }

        @SuppressLint("SetTextI18n")
        public void setTime(String time)
        {
            TextView PostTime = mView.findViewById(R.id.post_time);
            PostTime.setText("    " + time);
        }

        @SuppressLint("SetTextI18n")
        public void setDate(String date)
        {
            TextView PostDate = mView.findViewById(R.id.post_date);
            PostDate.setText("    " + date);
        }

        public void setDescription(String description)
        {
            TextView PostDescription = mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setSport(String sport)
        {
            TextView PostSport = mView.findViewById(R.id.post_sport);
            PostSport.setText(sport);
        }

        public void setDaterange(String daterange)
        {
            TextView PostDateRange = mView.findViewById(R.id.post_date_range);
            PostDateRange.setText(daterange);

        }

        public void setPostimage(Context ignoredCtx1, String postimage)
        {
            ImageView PostImage = mView.findViewById(R.id.post_image);
            Picasso.get().load(postimage).into(PostImage);
        }
    }

    private void SendUserToPostActivity() {
        Intent addNewPostIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(addNewPostIntent);
    }

    //syncs the actionbar with the item menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //creates a toast when a box is clicked from the item menu
    private void UserMenuSelector(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_profile) {
            SendUserToPersonalProfilePageActivity();
        } else if (itemId == R.id.nav_add_new_post) {
            SendUserToPostActivity();
            //Toast.makeText(this, "add new post", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_home) {
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_friends) {
            Toast.makeText(this, "Friend List", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_find_friends) {
            Toast.makeText(this, "Find Friends", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_messages) {
            Toast.makeText(this, "Messages", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_settings) {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_logout) {
            mAuth.signOut();
            SendUserToWelcomePageActivity();
        }
    }
}
