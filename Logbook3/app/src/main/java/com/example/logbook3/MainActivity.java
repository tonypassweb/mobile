package com.example.logbook3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.logbook3.Database.DatabaseHelper;
import com.example.logbook3.Task.Task;
import com.example.logbook3.Task.TaskAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText taskEditText;
    private Button addButton;
    private RecyclerView recyclerView;
    private DatabaseHelper dbHelper;
    private ArrayList<Task> tasks;
    private TaskAdapter taskAdapter;
    private int editingPosition = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        taskEditText = findViewById(R.id.taskEditText);
        addButton = findViewById(R.id.addButton);
        recyclerView = findViewById(R.id.todoRecyclerView);

        dbHelper = new DatabaseHelper(this);
        tasks = new ArrayList<>();
        loadTasksFromDatabase();

        taskAdapter = new TaskAdapter(tasks, dbHelper, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);

        addButton.setOnClickListener(v -> {
            String taskName = taskEditText.getText().toString().trim();
            if (!taskName.isEmpty()) {
                if (editingPosition >= 0) {
                    // Edit existing task
                    Task taskToEdit = tasks.get(editingPosition);
                    taskToEdit.setName(taskName);
                    dbHelper.updateTaskName(taskToEdit.getId(), taskName);
                    taskAdapter.notifyItemChanged(editingPosition);

                    // Reset edit state
                    editingPosition = -1;
                    addButton.setText("Add Task");
                } else {
                    // Add new task
                    long id = dbHelper.addTask(taskName);
                    tasks.add(new Task((int) id, taskName, false));
                    taskAdapter.notifyDataSetChanged();
                }
                taskEditText.setText(""); // Clear input field
            } else {
                Toast.makeText(MainActivity.this, "Please enter a task", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTasksFromDatabase() {
        Cursor cursor = dbHelper.getAllTasks();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
            String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME));
            boolean completed = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_COMPLETED)) == 1;
            tasks.add(new Task(id, name, completed));
        }
        cursor.close();
    }

    public void startEditingTask(int position, Task task) {
        Log.d("MainActivity", "startEditingTask called with task: " + task.getName());
        taskEditText.setText(task.getName()); // Set the selected task text in the EditText
        editingPosition = position; // Track the task being edited
        addButton.setText("Update Task"); // Change button text to indicate editing
    }
}
