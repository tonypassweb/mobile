package com.example.logbook3.Task;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.logbook3.Database.DatabaseHelper;
import com.example.logbook3.MainActivity;
import com.example.logbook3.R;

import java.util.ArrayList;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private ArrayList<Task> tasks;
    private DatabaseHelper dbHelper;
    private Context context;

    public TaskAdapter(ArrayList<Task> tasks, DatabaseHelper dbHelper, Context context) {
        this.tasks = tasks;
        this.dbHelper = dbHelper;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.taskNameTextView.setText(task.getName());

        holder.editButton.setOnClickListener(v -> {
            if (context instanceof MainActivity) {
                ((MainActivity) context).startEditingTask(position, task);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            dbHelper.deleteTask(task.getId());
            tasks.remove(position);
            notifyItemRemoved(position);
        });

    }

    @Override
    public int getItemCount() { return tasks.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView taskNameTextView;
        ImageButton deleteButton, editButton;

        public ViewHolder(View itemView) {
            super(itemView);
            taskNameTextView = itemView.findViewById(R.id.taskName);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editButton = itemView.findViewById(R.id.editButton);
        }
    }

}
