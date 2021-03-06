package com.uottawa.dutyhelper.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.uottawa.dutyhelper.R;
import com.uottawa.dutyhelper.model.Group;
import com.uottawa.dutyhelper.model.User;

import java.util.ArrayList;
import java.util.List;

public class NewGroupActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseGroups;

    private EditText mGroupName;
    private Button mCreateGroupBtn;
    private ListView mUserList;
    private ArrayAdapter<String> mAdapter;


    private List<String> mUserNames;
    private List<String> mUserIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference("users");
        mDatabaseGroups = FirebaseDatabase.getInstance().getReference("groups");

        mGroupName = (EditText) findViewById(R.id.group_name);
        mUserList = (ListView) findViewById(R.id.user_list);

        mUserNames = new ArrayList<>();
        mUserIds = new ArrayList<>();
        mCreateGroupBtn = (Button) findViewById(R.id.btn_create_group);
        mCreateGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroup();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUserNames.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    User user = postSnapshot.getValue(User.class);
                    String name = user.getFirstName() + " " + user.getLastName();
                    mUserIds.add(user.getId());
                    mUserNames.add(name);
                }
                mAdapter = new ArrayAdapter<>(NewGroupActivity.this, android.R.layout.simple_list_item_multiple_choice, mUserNames);
                mUserList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                mUserList.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void createGroup() {

        String name = mGroupName.getText().toString();

        SparseBooleanArray checked = mUserList.getCheckedItemPositions();
        ArrayList<String> selectedUsers = new ArrayList<>();
        for (int i = 0; i < checked.size(); i++) {
            if (checked.get(i)) {
                String userId = mUserIds.get(i);
                selectedUsers.add(userId);
            }
        }
        String groupId = mDatabaseGroups.push().getKey();

        Group newGroup = new Group(name);
        newGroup.setId(groupId);
        newGroup.setUsers(selectedUsers);

        mDatabaseGroups.child(groupId).setValue(newGroup);

        for (String id: selectedUsers) {
            mDatabaseUsers.child(id).child("group").setValue(groupId);
        }
    }
}
