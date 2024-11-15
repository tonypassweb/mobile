package com.example.logbook2.Task;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.logbook2.MainActivity;
import com.example.logbook2.R;

import java.util.ArrayList;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private ArrayList<String> tasks;
    private Context context;

    public TaskAdapter(ArrayList<String> tasks, Context context) {
        this.tasks = tasks;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String task = tasks.get(position);
        holder.taskNameTextView.setText(task);

        holder.deleteButton.setOnClickListener(v -> {
            tasks.remove(position);
            notifyItemRemoved(position);
        });

        holder.editButton.setOnClickListener(v -> {
            if (context instanceof MainActivity) {
                ((MainActivity) context).startEditingTask(position, task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

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
