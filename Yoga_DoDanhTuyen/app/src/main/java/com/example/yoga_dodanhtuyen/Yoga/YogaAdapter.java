package com.example.yoga_dodanhtuyen.Yoga;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.yoga_dodanhtuyen.R;

import java.util.ArrayList;
import java.util.List;

public class YogaAdapter extends RecyclerView.Adapter<YogaAdapter.MyViewHolder>{
    private Context context;
    private List<Yoga> yogaList;
    public YogaAdapter(Context context, List<Yoga> yogaList) {
        this.context = context;
        this.yogaList = yogaList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.yoga_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // Get the yoga object at the given position
        Yoga yoga = yogaList.get(position);

        // Set the data to the appropriate TextViews in the ViewHolder
        holder.yoga_id_txt.setText(String.valueOf(position + 1));
        holder.yoga_name_txt.setText("Yoga" + (position + 1));
        holder.yoga_type_of_class_txt.setText(yoga.getTypeOfClass());
        holder.yoga_capacity_txt.setText(String.valueOf(yoga.getCapacity()));

        // Format and set the yoga duration
        holder.yoga_duration_txt.setText(yoga.getDuration());

        // Set the day of the week
        holder.yoga_day_txt.setText(yoga.getDayOfWeek());
        holder.yoga_price_txt.setText(String.valueOf(yoga.getPricePerClass()));
        holder.yoga_time_of_course.setText(String.valueOf(yoga.getTimeOfCourse()));

        // Set the onClickListener for the main layout
        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, YogaFormActivity.class); // Start YogaAddOrUpdateActivity
                intent.putExtra("yogaToUpdate", yogaList.get(holder.getAdapterPosition())); // Pass the Yoga object to the next activity
                context.startActivity(intent); // Start activity
            }
        });
    }

    @Override
    public int getItemCount() {
        return (yogaList != null) ? yogaList.size() : 0;
    }

    public void searchDataList(ArrayList<Yoga> searchList){
        yogaList = searchList;
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView yoga_id_txt, yoga_name_txt, yoga_capacity_txt, yoga_duration_txt, yoga_time_of_course,
                yoga_type_of_class_txt, yoga_day_txt, yoga_price_txt;
        LinearLayout mainLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views with matching IDs from the XML layout
            yoga_id_txt = itemView.findViewById(R.id.yoga_id_txt);
            yoga_name_txt = itemView.findViewById(R.id.yoga_time_txt);
            yoga_capacity_txt = itemView.findViewById(R.id.yoga_capacity_txt);
            yoga_duration_txt = itemView.findViewById(R.id.yoga_duration_txt);
            yoga_type_of_class_txt = itemView.findViewById(R.id.yoga_type_of_class_txt);
            yoga_time_of_course = itemView.findViewById(R.id.yoga_time_of_course_txt);
            yoga_day_txt = itemView.findViewById(R.id.yoga_day_txt);
            yoga_price_txt = itemView.findViewById(R.id.yoga_price_txt);
            mainLayout = itemView.findViewById(R.id.mainLayout);
        }
    }

}
