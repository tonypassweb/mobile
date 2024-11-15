package com.example.logbook2;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.logbook2.Task.TaskAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText taskEditText;
    private Button addButton;
    private RecyclerView recyclerView;
    private ArrayList<String> tasks;
    private TaskAdapter taskAdapter;
    private int editingPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        taskEditText = findViewById(R.id.taskEditText);
        addButton = findViewById(R.id.addButton);
        recyclerView = findViewById(R.id.todoRecyclerView);

        tasks = new ArrayList<>();
        taskAdapter = new TaskAdapter(tasks, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);

        addButton.setOnClickListener(v -> {
            String task = taskEditText.getText().toString().trim();
            if (!task.isEmpty()) {
                if (editingPosition >= 0) {
                    // Edit the existing task
                    tasks.set(editingPosition, task);
                    taskAdapter.notifyItemChanged(editingPosition);
                    editingPosition = -1; // Reset editing position
                    addButton.setText("Add Task"); // Reset button text
                } else {
                    // Add new task
                    tasks.add(task);
                    taskAdapter.notifyDataSetChanged();
                }
                taskEditText.setText(""); // Clear input field
            } else {
                Toast.makeText(MainActivity.this, "Please enter a task", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void startEditingTask(int position, String task) {
        taskEditText.setText(task); // Set the selected task text in the EditText
        editingPosition = position; // Track the task being edited
        addButton.setText("Update Task"); // Change button text to indicate editing
    }
}
