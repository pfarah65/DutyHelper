package com.uottawa.dutyhelper.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.uottawa.dutyhelper.R;
import com.uottawa.dutyhelper.model.Task;
import com.uottawa.dutyhelper.model.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NewTaskActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseTasks;
    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;

    private List<Task> mTasks;
    private List<User> mUsers;
    private List<String> mAssignedUserIds;
    private List<String> mAssignedResources;
    private SparseBooleanArray mCheckedUsers;
    private SparseBooleanArray mCheckedResources;

    private EditText mTaskTitle;
    private EditText mTaskDescription;
    private EditText mTaskDueDate;
    private DatePicker mDatePicker;
    private RadioGroup mTaskStatus;
    private Button mBtnAssignUser;
    private Button mBtnAssignResources;

    private TextInputLayout mTitleLayout;
    private TextInputLayout mDescLayout;
    private TextInputLayout mDueDateLayout;

    private ListView mResourcesListView;
    private ListView mUserListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        mDatabaseTasks = FirebaseDatabase.getInstance().getReference("tasks");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference("users");
        mAuth = FirebaseAuth.getInstance();

        mUsers = new ArrayList<>();
        mTasks = new ArrayList<>();
        mAssignedUserIds = new ArrayList<>();
        mAssignedResources = new ArrayList<>();

        mTaskTitle = (EditText) findViewById(R.id.task_name);
        mTaskDescription = (EditText) findViewById(R.id.task_description);
        mTaskDueDate = (EditText) findViewById(R.id.task_due_date);
        mTaskStatus = (RadioGroup) findViewById(R.id.status_radio_group);
        mBtnAssignUser = (Button) findViewById(R.id.btn_assign_user);
        mBtnAssignResources = (Button) findViewById(R.id.btn_add_resources);

        mTitleLayout = (TextInputLayout) findViewById(R.id.task_name_layout);
        mDescLayout = (TextInputLayout) findViewById(R.id.task_description_layout);
        mDueDateLayout = (TextInputLayout) findViewById(R.id.task_due_date_layout);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTitleLayout.setError(null);
                mDescLayout.setError(null);
                mDueDateLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        mTaskTitle.addTextChangedListener(textWatcher);
        mTaskDueDate.addTextChangedListener(textWatcher);
        mTaskDescription.addTextChangedListener(textWatcher);

        mTaskDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogDatePicker = LayoutInflater.from(NewTaskActivity.this).inflate(R.layout.dialog_date_picker, null);
                mDatePicker = (DatePicker) dialogDatePicker.findViewById(R.id.date_picker);
                mDatePicker.init(year, month, day, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(NewTaskActivity.this);
                builder.setTitle(R.string.dialog_title_due_date)
                        .setView(dialogDatePicker)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                int year = mDatePicker.getYear();
                                int month = mDatePicker.getMonth() + 1;
                                int day = mDatePicker.getDayOfMonth();
                                String date = String.format("%s/%s/%s", day, month, year);
                                mTaskDueDate.setText(date);

                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .show();
            }
        });

        mBtnAssignUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> userNames = new ArrayList<>();
                for (User user : mUsers) {
                    String name = user.getFirstName() + " " + user.getLastName();
                    userNames.add(name);
                }

                View dialogUsers = LayoutInflater.from(NewTaskActivity.this).inflate(R.layout.dialog_assign_user, null);
                mUserListView = (ListView) dialogUsers.findViewById(R.id.users_list_view);
                mUserListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(NewTaskActivity.this, android.R.layout.simple_list_item_multiple_choice, userNames);
                mUserListView.setAdapter(adapter);

                if (mCheckedUsers != null) {
                    for (int i = 0; i < mUserListView.getCount(); i++) {
                        mUserListView.setItemChecked(i, mCheckedUsers.get(i));
                    }
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(NewTaskActivity.this);
                builder.setTitle(R.string.dialog_title_assign_users)
                        .setView(dialogUsers)
                        .setCancelable(false)
                        .setPositiveButton(R.string.dialog_btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mAssignedUserIds.clear();
                                mCheckedUsers = mUserListView.getCheckedItemPositions();
                                for (int i = 0; i < mUserListView.getCount(); i++) {
                                    if (mCheckedUsers.get(i)) {
                                        mAssignedUserIds.add(mUsers.get(i).getId());
                                    }
                                }
                            }
                        })
                        .setNegativeButton(R.string.dialog_btn_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        });


        mBtnAssignResources.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] resources = getResources().getStringArray(R.array.task_resources);

                View dialogResources = LayoutInflater.from(NewTaskActivity.this).inflate(R.layout.dialog_assign_resources, null);
                mResourcesListView = (ListView) dialogResources.findViewById(R.id.resourcesListView);
                mResourcesListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(NewTaskActivity.this, android.R.layout.simple_list_item_multiple_choice, resources);
                mResourcesListView.setAdapter(adapter);

                if (mCheckedResources != null) {
                    for (int i = 0; i < mResourcesListView.getCount(); i++) {
                        mResourcesListView.setItemChecked(i, mCheckedResources.get(i));

                    }

                }
                AlertDialog.Builder builder = new AlertDialog.Builder(NewTaskActivity.this);
                builder.setTitle(R.string.dialog_title_add_resources)
                        .setView(dialogResources)
                        .setCancelable(false)
                        .setPositiveButton(R.string.dialog_btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                mAssignedResources.clear();
                                mCheckedResources = mResourcesListView.getCheckedItemPositions();
                                for (int i = 0; i < mResourcesListView.getCount(); i++) {
                                    if (mCheckedResources.get(i)) {
                                        mAssignedResources.add(resources[i]);
                                    }


                                }
                            }
                        })
                        .setNegativeButton(R.string.dialog_btn_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();


            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDatabaseTasks.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mTasks.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Task task = snapshot.getValue(Task.class);
                    mTasks.add(task);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    mUsers.add(user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_task, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        Intent goBack = new Intent(NewTaskActivity.this, TaskListActivity.class);

        if (id == R.id.action_cancel) {
            startActivity(goBack);
        } else if (id == R.id.action_save_new) {
            if (isValidTask()) {
                addTask();
                startActivity(goBack);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void addTask() {

        String name = mTaskTitle.getText().toString().trim();
        String description = mTaskDescription.getText().toString().trim();
        String dueDate = mTaskDueDate.getText().toString();
        String status = "incomplete";
        int statusId = mTaskStatus.getCheckedRadioButtonId();

        if (statusId == R.id.radio_in_progress) {
            status = "in progress";
        }

        String taskId = mDatabaseTasks.push().getKey();

        Task task = new Task();
        task.setId(taskId);
        task.setTitle(name);
        task.setDescription(description);
        task.setDueDate(dueDate);
        task.setStatus(status);
        task.setAssignedUsers(mAssignedUserIds);
        task.setResources(mAssignedResources);
        task.setCreatorId(mAuth.getCurrentUser().getUid());

        mDatabaseTasks.child(taskId).setValue(task);

        for (String userId : mAssignedUserIds) {
            for (User user : mUsers) {
                if (user.getId().equals(userId)) {
                    List<String> assignedTasks = new ArrayList<>();
                    if (user.getAssignedTasks() != null) {
                        assignedTasks = user.getAssignedTasks();
                    }
                    assignedTasks.add(taskId);
                    user.setAssignedTasks(assignedTasks);
                    mDatabaseUsers.child(user.getId()).setValue(user);
                }
            }
        }

        Toast.makeText(this, R.string.toast_task_added, Toast.LENGTH_LONG).show();
    }

    private boolean isValidTask() {

        boolean isValid = true;

        String name = mTaskTitle.getText().toString().trim();
        String description = mTaskDescription.getText().toString().trim();
        String dueDate = mTaskDueDate.getText().toString();

        if (TextUtils.isEmpty(name)) {
            mTitleLayout.setError(getString(R.string.error_required_field));
            isValid = false;
        }
        if (TextUtils.isEmpty(description)) {
            mDescLayout.setError(getString(R.string.error_required_field));
            isValid = false;
        }
        if (TextUtils.isEmpty(dueDate)) {
            mDueDateLayout.setError(getString(R.string.error_required_field));
            isValid = false;
        }
        if (mAssignedUserIds.isEmpty()) {
            Toast.makeText(NewTaskActivity.this, R.string.error_assign_user, Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        return isValid;
    }
}
