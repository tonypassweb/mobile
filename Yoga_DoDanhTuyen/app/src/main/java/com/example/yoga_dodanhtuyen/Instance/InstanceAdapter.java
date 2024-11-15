package com.example.yoga_dodanhtuyen.Instance;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yoga_dodanhtuyen.R;
import com.example.yoga_dodanhtuyen.Yoga.YogaFormActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InstanceAdapter extends RecyclerView.Adapter<InstanceAdapter.MyViewHolder> {
    private Context context;
    private HashMap<String, Instance> instanceMap;
    private List<Instance> instanceList;

    public InstanceAdapter(Context context, HashMap<String, Instance> instanceMap) {
        this.context = context;
        this.instanceMap = instanceMap;
        this.instanceList = new ArrayList<>(instanceMap.values()); // Convert to list for display purposes
    }

    // Method to update the adapter's data
    public void setData(List<Instance> newInstanceList) {
        this.instanceList = newInstanceList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InstanceAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.instance_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InstanceAdapter.MyViewHolder holder, int position) {
        // Bind data from instanceList to views in the ViewHolder
        Instance instance = instanceList.get(position);
        String instanceId = instance.getId();

        holder.teacher_name_txt.setText("Teacher: " + instance.getTeacherName());
        holder.instance_comment_txt.setText(String.valueOf(instance.getComment()));
        holder.instacne_date_txt.setText(String.valueOf(instance.getDate()));

        // Set up listeners as needed
        holder.delete_btn.setOnClickListener(view -> {
            if (context instanceof YogaFormActivity) {
                ((YogaFormActivity) context).deleteInstance(instanceId);
            }
        });
        holder.mainLayout.setOnClickListener(view -> {
            if (context instanceof YogaFormActivity) {
                ((YogaFormActivity) context).showUpdateInstanceForm(instance, instanceId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (instanceList != null) ? instanceList.size() : 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView teacher_name_txt, instance_comment_txt, instacne_date_txt;
        ImageView delete_btn;
        LinearLayout mainLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            teacher_name_txt = itemView.findViewById(R.id.teacher_name_txt);
            instance_comment_txt = itemView.findViewById(R.id.instance_comment_txt);
            instacne_date_txt = itemView.findViewById(R.id.instacne_date_txt);
            delete_btn = itemView.findViewById(R.id.instanceDelete_btn);
            mainLayout = itemView.findViewById(R.id.instanceMainLayout);
        }
    }
}

