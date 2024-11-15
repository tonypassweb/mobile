package com.example.yoga_dodanhtuyen.Yoga;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import java.util.Set;
import java.util.UUID;

public class YogaFormActivity extends AppCompatActivity {
    private Spinner dayOfWeekSpinner;
    private Spinner classTypeSpinner;
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
    private long checkInstance = -1; //Validation for instance

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
        }

        //Set onClickListener on instance add button
        instanceAdd_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInstanceInputDialog(-1);
            }
        });

        yogaAddOrUpdate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addOrUpdateYoga();
                refreshData();
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
                                timeResult.setText("Observation Time: " + selectedTime);
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
                // Update Yoga class
                yogaToUpdate.setTypeOfClass(yogaClassType);
                yogaToUpdate.setDayOfWeek(yogaDayofTheWeek);
                yogaToUpdate.setDuration(YogaUtils.formatDuration(yogaHours, yogaMinutes, yogaSeconds));
                yogaToUpdate.setCapacity(Integer.parseInt(yogaCapacityStr));
                yogaToUpdate.setDescription(yogaDescription);
                yogaToUpdate.setPricePerClass(yogaPrice);
                yogaToUpdate.setTimeOfCourse(yogaTimeofCourse);

                dbHelper = new DatabaseHelper(this);
                dbHelper.updateYoga(yogaToUpdate);

            } else {
                String yogaId = UUID.randomUUID().toString();
                String yogaDuration = YogaUtils.formatDuration(yogaHours, yogaMinutes, yogaSeconds);
                int yogaCapacity = Integer.parseInt(yogaCapacityStr);
                Yoga newYoga = new Yoga(yogaId, yogaClassType, yogaDayofTheWeek, yogaTimeofCourse,
                        yogaCapacity, yogaDuration, yogaPrice, yogaDescription, new HashMap<String, Instance>()); // Create a new Yoga object

                dbHelper = new DatabaseHelper(this);
                dbHelper.addYoga(newYoga);
            }
        }
    }

    // Method to update instances of a yoga class in Firebase
    private void updateInstancesInDatabase(Yoga yoga) {
        DatabaseReference instancesRef = FirebaseDatabase.getInstance().getReference("YogaClasses")
                .child(String.valueOf(yoga.getId())).child("instances");

        // Delete instances that need to be removed
        if (!instanceIdsToDelete.isEmpty()) {
            for (Long instanceId : instanceIdsToDelete) {
                instancesRef.child(String.valueOf(instanceId)).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            // Handle success
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(YogaFormActivity.this, "Failed to delete instance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        }

        // Add new instances if necessary
//        for (Instance instance : yoga.getInstances()) {
//            if (instance.getId() == null || instance.getId().isEmpty()) { // If instance ID is not set
//                String instanceId = instancesRef.push().getKey();
//                instance.setId(instanceId); // Convert push ID to long
//                instancesRef.child(instanceId).setValue(instance);
//            }
//        }
    }

    // Method to add instances to Firebase for a new yoga class
//    private void addInstancesToDatabase(Yoga yoga) {
//        DatabaseReference instancesRef = FirebaseDatabase.getInstance().getReference("YogaClasses")
//                .child(String.valueOf(yoga.getId())).child("instances");
//
//        for (Instance instance : yoga.getInstances()) {
//            String instanceId = instancesRef.push().getKey();
//            instance.setId(instanceId); // Convert push ID to long
//            instancesRef.child(instanceId).setValue(instance)
//                    .addOnSuccessListener(aVoid -> {
//                        // Handle success
//                    })
//                    .addOnFailureListener(e -> {
//                        Toast.makeText(YogaFormActivity.this, "Failed to add instance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    });
//        }
//    }

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

                    // Convert the HashMap to a List if needed (e.g., for an adapter)
                    List<Instance> instanceList = new ArrayList<>(instanceMap.values());
                    instanceAdapter.setData(instanceList);
                    // Log the size of the list
                    Log.d("Firebase", "Instance list size: " + instanceList.size());

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

    private void showInstanceInputDialog(int position) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.instances_form, null);
        dialogBuilder.setView(dialogView);

        EditText teacherNameInput = dialogView.findViewById(R.id.teacherName_input);
        EditText additionCommentInput = dialogView.findViewById(R.id.additionComment_input);
        TextView headerTextView = dialogView.findViewById(R.id.headerTextView);
        DatePicker yogaDatePicker = dialogView.findViewById(R.id.instanceDate_input);

        Calendar calendar = Calendar.getInstance();

        String instanceId;
        Instance instance;

        if (position != -1) { // Update existing instance
            instance = yogaToUpdate != null ? yogaToUpdate.getInstances().get(position) : newYoga.getInstances().get(position);
            instanceId = instance.getId();

            // Pre-fill fields with existing instance data
            teacherNameInput.setText(instance.getTeacherName());
            additionCommentInput.setText(instance.getComment());

            calendar.setTime(instance.getDate());
            yogaDatePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            headerTextView.setText("Updating Instance");
            dialogBuilder.setPositiveButton("Update", (dialogInterface, i) -> {
                handleInstanceSave(instance, instanceId, teacherNameInput, additionCommentInput, yogaDatePicker, calendar, true);
                dialogInterface.dismiss();
            });
        } else { // Add new instance
            instance = new Instance();
            instanceId = UUID.randomUUID().toString();

            headerTextView.setText("Add New Instance");
            dialogBuilder.setPositiveButton("Add", (dialogInterface, i) -> {
                handleInstanceSave(instance, instanceId, teacherNameInput, additionCommentInput, yogaDatePicker, calendar, false);
                dialogInterface.dismiss();
            });
        }

        dialogBuilder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
        dialogBuilder.create().show();
    }

    public void showUpdateInstanceForm(Instance instance, String instanceId) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.instances_form, null);
        dialogBuilder.setView(dialogView);

        EditText teacherNameInput = dialogView.findViewById(R.id.teacherName_input);
        EditText additionCommentInput = dialogView.findViewById(R.id.additionComment_input);
        TextView headerTextView = dialogView.findViewById(R.id.headerTextView);
        DatePicker yogaDatePicker = dialogView.findViewById(R.id.instanceDate_input);

        Calendar calendar = Calendar.getInstance();

        // Check if we are updating an existing instance
        if (instanceId != null && instance != null) {
            headerTextView.setText("Update Instance");

            // Pre-fill form with existing instance data
            teacherNameInput.setText(instance.getTeacherName());
            additionCommentInput.setText(instance.getComment());

            calendar.setTime(instance.getDate());
            yogaDatePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            dialogBuilder.setPositiveButton("Update", (dialogInterface, i) -> {
                handleInstanceSave(instance, instanceId, teacherNameInput, additionCommentInput, yogaDatePicker, calendar, true);
            });
        } else {
            headerTextView.setText("Add New Instance");

            dialogBuilder.setPositiveButton("Add", (dialogInterface, i) -> {
                handleInstanceSave(new Instance(), UUID.randomUUID().toString(), teacherNameInput, additionCommentInput, yogaDatePicker, calendar, false);
            });
        }

        dialogBuilder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
        dialogBuilder.create().show();
    }

    private void handleInstanceSave(Instance instance, String instanceId, EditText teacherNameInput, EditText additionCommentInput, DatePicker yogaDatePicker, Calendar calendar, boolean isUpdate) {
        String teacherName = teacherNameInput.getText().toString().trim();
        String additionComment = additionCommentInput.getText().toString().trim();

        if (TextUtils.isEmpty(teacherName)) {
            Toast.makeText(YogaFormActivity.this, "Please fill in the teacher name!", Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(additionComment)) {
            Toast.makeText(YogaFormActivity.this, "Please fill in the additional comment!", Toast.LENGTH_SHORT).show();
            return;
        }

        int day = yogaDatePicker.getDayOfMonth();
        int month = yogaDatePicker.getMonth();
        int year = yogaDatePicker.getYear();
        calendar.set(year, month, day);

        instance.setTeacherName(teacherName);
        instance.setComment(additionComment);
        instance.setDate(calendar.getTime());

        DatabaseReference instanceRef = FirebaseDatabase.getInstance("https://yoga-dodanhtuyen-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Yoga Peak")
                .child(yogaToUpdate.getId())
                .child("instances")
                .child(instanceId);

        instanceRef.setValue(instance).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String message = isUpdate ? "Instance updated successfully!" : "Instance added successfully!";
                Toast.makeText(YogaFormActivity.this, message, Toast.LENGTH_SHORT).show();
                instanceAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(YogaFormActivity.this, "Failed to save instance", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(YogaFormActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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


//    public void onInstanceToDeleteReceived(Instance instanceToDelete) {
//        //Delete only the display side but not in the database
//        if (newYoga != null & !newYoga.getInstances().isEmpty()) {
//            newYoga.getInstances().remove(instanceToDelete);
//        } else if (yogaToUpdate != null & !yogaToUpdate.getInstances().isEmpty()) {
//            yogaToUpdate.getInstances().remove(instanceToDelete);
//            instanceIdsToDelete.add(instanceToDelete.getId());
//        }
//        instanceAdapter.notifyDataSetChanged();
//    }
}