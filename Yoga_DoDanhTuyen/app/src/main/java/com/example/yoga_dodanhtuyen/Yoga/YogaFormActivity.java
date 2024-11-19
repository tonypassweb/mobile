package com.example.yoga_dodanhtuyen.Yoga;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.yoga_dodanhtuyen.Database.DatabaseHelper;
import com.example.yoga_dodanhtuyen.Instance.Instance;
import com.example.yoga_dodanhtuyen.Instance.InstanceAdapter;
import com.example.yoga_dodanhtuyen.R;
import com.example.yoga_dodanhtuyen.YogaUtils;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class YogaFormActivity extends AppCompatActivity {
    private List<Instance> instanceList;
    private Spinner dayOfWeekSpinner;
    private Spinner classTypeSpinner, spinner_day_of_week;
    private ArrayAdapter<CharSequence> adapter;
    private Button timePicker_btn;
    private Button yogaAddOrUpdate_btn, yogaDelete_btn;
    private Calendar calendar = Calendar.getInstance();
    private TextView timeResult;
    private ImageView instanceDelete_btn;
    private EditText edit_text_capacity, edit_text_description, edit_text_price;
    private NumberPicker durationHours_input, durationMinutes_input, durationSeconds_input;
    private Yoga newYoga; //a new Yoga entity to contains before adding into the database
    private Yoga yogaToUpdate; //Yoga that will be updated
    private Set<Long> instanceIdsToDelete = new HashSet<>(); //Declare HashSet to store observations ids to actual deletion in the database
    private String selectedTimeofCourse;
    private InstanceAdapter instanceAdapter;
    private DatabaseReference databaseReference;
    private DatabaseHelper dbHelper;
    private ValueEventListener eventListener;
    private RecyclerView instanceRecyclerView;
    private MaterialButton instanceAdd_btn;

    private void refreshData() { //Function to refresh Data
        Intent intent = new Intent(); //Creates Intent object to carry data data between different activities in application
        intent.putExtra("dataChanged", true); //Key-value pair indicates data has changed and needs to be refreshed
        setResult(RESULT_OK, intent); //Set the result of activity of the ACTIVITY to RESULT_OK and passes it
        finish(); //Finish the activity
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoga_form);

        //Initialize a newYoga object
        newYoga = new Yoga();

        dbHelper = new DatabaseHelper(YogaFormActivity.this);

        dayOfWeekSpinner = findViewById(R.id.spinner_day_of_week);
        classTypeSpinner = findViewById(R.id.spinner_class_type);
        edit_text_capacity = findViewById(R.id.edit_text_capacity);
        edit_text_price = findViewById(R.id.edit_text_price);
        edit_text_description = findViewById(R.id.edit_text_description);
        timeResult = findViewById(R.id.timeResult);
        durationHours_input = findViewById(R.id.durationHours_input);
        durationMinutes_input = findViewById(R.id.durationMinutes_input);
        durationSeconds_input = findViewById(R.id.durationSeconds_input);
        timePicker_btn = findViewById(R.id.timePicker_btn);
        yogaAddOrUpdate_btn = findViewById(R.id.addOrUpdate_button);
        yogaDelete_btn = findViewById(R.id.delete_button);
        instanceRecyclerView = findViewById(R.id.instanceRecyclerView);
        instanceAdd_btn = findViewById(R.id.instanceAdd_button);
        spinner_day_of_week = findViewById(R.id.spinner_day_of_week);
        durationHours_input.setMaxValue(23); // Hours range from 0 to 23
        durationMinutes_input.setMaxValue(59); // Minutes range from 0 to 59
        durationSeconds_input.setMaxValue(59); // Seconds range from 0 to 59

        //Set min value of hike duration
        durationHours_input.setMinValue(0);
        durationMinutes_input.setMinValue(0);
        durationSeconds_input.setMinValue(0);

        adapter = ArrayAdapter.createFromResource(this, R.array.class_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classTypeSpinner.setAdapter(adapter);

        adapter = ArrayAdapter.createFromResource(this, R.array.days_of_week, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dayOfWeekSpinner.setAdapter(adapter);

        GridLayoutManager layoutManager = new GridLayoutManager(YogaFormActivity.this, 1);
        instanceRecyclerView.setLayoutManager(layoutManager);

        instanceAdapter = new InstanceAdapter(YogaFormActivity.this, new HashMap<>());
        instanceRecyclerView.setAdapter(instanceAdapter);

        if (getIntent().hasExtra("yogaToUpdate")) { //Get Intent Data hikeToUpdate and set from AddActivity to UpdateActivity
            yogaToUpdate = (Yoga) getIntent().getSerializableExtra("yogaToUpdate");
            yogaDelete_btn.setVisibility(View.VISIBLE);
            yogaAddOrUpdate_btn.setText("Update");
            getAndSetIntentData();
        } else {
            instanceAdapter.setData(newYoga.getInstances());
        }

        //Set onClickListener on instance add button
        instanceAdd_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateForm()) {
                    showInstanceForm(null);
                }
            }
        });

        yogaAddOrUpdate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateForm()) {
                    addOrUpdateYoga();
                    refreshData();
                }
            }
        });

        yogaDelete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String yogaId = yogaToUpdate.getId(); // Update this with the actual ID or pass it dynamically
                dbHelper = new DatabaseHelper(YogaFormActivity.this);
                dbHelper.deleteYoga(yogaId);
                finish();
            }
        });

        timePicker_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get the current time for the initial selection
                Calendar currentTime = Calendar.getInstance();
                int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
                int currentMinute = currentTime.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        YogaFormActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                // Handle the time selection here.
                                String selectedTime = String.format("%02d:%02d", hourOfDay, minute);
                                selectedTimeofCourse = selectedTime;
                                timeResult.setText("Time of course: " + selectedTime);
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                            }
                        },
                        currentHour, // Initial hour
                        currentMinute, // Initial minute
                        true // 24-hour format (true) or AM/PM format (false)
                );
                timePickerDialog.show();
            }
        });

    }

    private boolean validateForm() {
        if (spinner_day_of_week.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a day of the week.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (timeResult.getText().toString().equals("Time of course: Not Set")) {
            Toast.makeText(this, "Please select a time for the course.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (edit_text_capacity.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter the capacity.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (durationHours_input.getValue() == 0 && durationMinutes_input.getValue() == 0 && durationSeconds_input.getValue() == 0) {
            Toast.makeText(this, "Please set a valid duration.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (edit_text_price.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter the price per class.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (classTypeSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a class type.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // All validations passed
        return true;
    }

    private void addOrUpdateYoga() {
        // Extract user input data
        String yogaDayofTheWeek = dayOfWeekSpinner.getSelectedItem().toString();
        String yogaClassType = classTypeSpinner.getSelectedItem().toString();
        String yogaCapacityStr = edit_text_capacity.getText().toString().trim();
        String yogaPriceStr = edit_text_price.getText().toString().trim();

        // Handle float parsing with proper checks
        double yogaPrice = 0.0;
        if (!TextUtils.isEmpty(yogaPriceStr)) {
            yogaPrice = Double.parseDouble(yogaPriceStr); // Convert the String to a double
        }

        String yogaTimeofCourse = TextUtils.isEmpty(selectedTimeofCourse) ? yogaToUpdate.getTimeOfCourse() : selectedTimeofCourse.trim();
        String yogaDescription = edit_text_description.getText().toString().trim();

        // Extract values for duration
        int yogaHours = durationHours_input.getValue();
        int yogaMinutes = durationMinutes_input.getValue();
        int yogaSeconds = durationSeconds_input.getValue();

        // Validation
        if (TextUtils.isEmpty(yogaCapacityStr)) {
            Toast.makeText(YogaFormActivity.this, "Please enter a yoga class capacity!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(yogaPriceStr)) {
            Toast.makeText(YogaFormActivity.this, "Please enter the price of the class!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(yogaTimeofCourse)) {
            Toast.makeText(YogaFormActivity.this, "Please enter the time of course of the class!", Toast.LENGTH_SHORT).show();
        } else {
            if (yogaToUpdate != null) { // Update existing class
                HashMap<String, Instance> existingInstances = yogaToUpdate.getInstances();
                yogaToUpdate.setInstances(existingInstances); // Preserve instances
//                dbHelper.updateYoga(yogaToUpdate);

                // Sync existing instances to Firebase
                for (Instance instance : existingInstances.values()) {
                    DatabaseReference instanceRef = FirebaseDatabase.getInstance("https://yoga-dodanhtuyen-default-rtdb.asia-southeast1.firebasedatabase.app/")
                            .getReference("Yoga Peak")
                            .child(yogaToUpdate.getId())
                            .child("instances")
                            .child(instance.getId());
                    instanceRef.setValue(instance);
                }
                // Update Yoga class
                yogaToUpdate.setTypeOfClass(yogaClassType);
                yogaToUpdate.setDayOfWeek(yogaDayofTheWeek);
                yogaToUpdate.setDuration(YogaUtils.formatDuration(yogaHours, yogaMinutes, yogaSeconds));
                yogaToUpdate.setCapacity(Integer.parseInt(yogaCapacityStr));
                yogaToUpdate.setDescription(yogaDescription);
                yogaToUpdate.setPricePerClass(yogaPrice);
                yogaToUpdate.setTimeOfCourse(yogaTimeofCourse);

//                dbHelper.updateYoga(yogaToUpdate);

            } else {
                newYoga.setDuration(YogaUtils.formatDuration(yogaHours, yogaMinutes, yogaSeconds));
                newYoga.setCapacity(Integer.parseInt(yogaCapacityStr));
                newYoga.setTypeOfClass(yogaClassType); // Assuming you have a field for class type
                newYoga.setDayOfWeek(yogaDayofTheWeek); // Set day of the week
                newYoga.setTimeOfCourse(yogaTimeofCourse); // Set time of the course
                newYoga.setPricePerClass(yogaPrice); // Set the price for the class
                newYoga.setDescription(yogaDescription); // Set the description for the yoga class

                // Save the Yoga
                dbHelper.addYoga(newYoga, newYoga.getInstances());
            }
        }
    }

    private void getAndSetIntentData() {
        if (yogaToUpdate != null) {
            // Extract and set Yoga data
            String id = yogaToUpdate.getId();

            // Set Spinner values by finding the correct position in the adapter
            String dayOfWeek = yogaToUpdate.getDayOfWeek();
            if (dayOfWeek != null) {
                ArrayAdapter<String> dayOfWeekAdapter = (ArrayAdapter<String>) dayOfWeekSpinner.getAdapter();
                int dayPosition = dayOfWeekAdapter.getPosition(dayOfWeek);
                dayOfWeekSpinner.setSelection(dayPosition);
            }

            String classType = yogaToUpdate.getTypeOfClass();
            if (classType != null) {
                ArrayAdapter<String> classTypeAdapter = (ArrayAdapter<String>) classTypeSpinner.getAdapter();
                int classTypePosition = classTypeAdapter.getPosition(classType);
                classTypeSpinner.setSelection(classTypePosition);
            }

            // Set capacity, description, and price
            edit_text_capacity.setText(String.valueOf(yogaToUpdate.getCapacity()));
            edit_text_description.setText(yogaToUpdate.getDescription());
            edit_text_price.setText(String.valueOf(yogaToUpdate.getPricePerClass()));

            // Set duration fields
            String[] durationParts = yogaToUpdate.getDuration().split(":");
            durationHours_input.setValue(Integer.parseInt(durationParts[0]));   // Hours
            durationMinutes_input.setValue(Integer.parseInt(durationParts[1])); // Minutes
            durationSeconds_input.setValue(Integer.parseInt(durationParts[2])); // Seconds

            // Set time of course
            timeResult.setText("Time of course: " + yogaToUpdate.getTimeOfCourse());

            // Fetch and set instances from Firebase
            databaseReference = FirebaseDatabase.getInstance("https://yoga-dodanhtuyen-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Yoga Peak")
                    .child(String.valueOf(yogaToUpdate.getId())).child("instances");

            eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Create a HashMap to store the instances
                    HashMap<String, Instance> instanceMap = new HashMap<>();

                    // Loop through the children (each snapshot will represent an instance)
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Instance instance = snapshot.getValue(Instance.class);
                        if (instance != null) {
                            // Put the instance into the map with its unique key (instanceId)
                            instanceMap.put(snapshot.getKey(), instance);
                        }
                    }

                    instanceAdapter.setData(instanceMap);

                    // Notify the adapter to update the UI
                    instanceAdapter.notifyDataSetChanged(); // Ensure you're using the correct adapter
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(YogaFormActivity.this, "Failed to fetch instances: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void showInstanceForm(@Nullable Instance instance) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.instances_form, null);
        dialogBuilder.setView(dialogView);

        EditText teacherNameInput = dialogView.findViewById(R.id.teacherName_input);
        EditText additionCommentInput = dialogView.findViewById(R.id.additionComment_input);
        TextView headerTextView = dialogView.findViewById(R.id.headerTextView);
        DatePicker yogaDatePicker = dialogView.findViewById(R.id.instanceDate_input);

        Calendar calendar = Calendar.getInstance();

        boolean isUpdate = (instance != null);

        if (isUpdate) {
            // Update existing instance
            headerTextView.setText("Update Instance");

            // Pre-fill fields with existing instance data
            teacherNameInput.setText(instance.getTeacherName());
            additionCommentInput.setText(instance.getComment());

            calendar.setTime(instance.getDate());
            yogaDatePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            dialogBuilder.setPositiveButton("Update", (dialogInterface, i) -> {
                handleInstanceSave(instance, teacherNameInput, additionCommentInput, yogaDatePicker, calendar, true);
                dialogInterface.dismiss();
            });
        } else {
            // Add new instance
            headerTextView.setText("Add New Instance");

            dialogBuilder.setPositiveButton("Add", (dialogInterface, i) -> {
                Instance newInstance = new Instance();
                handleInstanceSave(newInstance, teacherNameInput, additionCommentInput, yogaDatePicker, calendar, false);
                dialogInterface.dismiss();
            });
        }

        dialogBuilder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
        dialogBuilder.create().show();
    }
    private void handleInstanceSave(
            Instance newInstance,
            EditText teacherNameInput,
            EditText additionCommentInput,
            DatePicker yogaDatePicker,
            Calendar calendar,
            boolean isUpdate
    ) {
        String teacherName = teacherNameInput.getText().toString().trim();
        String additionComment = additionCommentInput.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(teacherName)) {
            Toast.makeText(YogaFormActivity.this, "Please fill in the teacher name!", Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(additionComment)) {
            Toast.makeText(YogaFormActivity.this, "Please fill in the additional comment!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set date
        int day = yogaDatePicker.getDayOfMonth();
        int month = yogaDatePicker.getMonth();
        int year = yogaDatePicker.getYear();
        calendar.set(year, month, day);

        // Update instance fields
        newInstance.setTeacherName(teacherName);
        newInstance.setComment(additionComment);
        newInstance.setDate(calendar.getTime());

        // Add instance to local data structure
        boolean check = (yogaToUpdate != null ? yogaToUpdate : newYoga).addInstanceLocally(newInstance);

        // Save to Firebase and SQLite
        if (check) {
            Toast.makeText(YogaFormActivity.this, "Successfully added instance locally!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(YogaFormActivity.this, "Failed to add instance locally!", Toast.LENGTH_SHORT).show();
        }

        // Update RecyclerView with the new instance
        instanceAdapter.setData(newYoga.getInstances());
    }

    public void deleteInstance(String instanceId) {
        // Reference to the specific instance in Firebase
        DatabaseReference instanceRef = FirebaseDatabase.getInstance("https://yoga-dodanhtuyen-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Yoga Peak")
                .child(yogaToUpdate.getId())
                .child("instances")
                .child(instanceId);

        // Confirm before deleting
        new AlertDialog.Builder(this)
                .setTitle("Delete Instance")
                .setMessage("Are you sure you want to delete this instance?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    instanceRef.removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Instance deleted successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to delete instance", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

}