package com.example.yoga_dodanhtuyen.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.yoga_dodanhtuyen.Instance.Instance;
import com.example.yoga_dodanhtuyen.Yoga.Yoga;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class DatabaseHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME = "Yoga_Peak.db";
    private static final int DATABASE_VERSION = 1;

    // Yoga Table
    private static final String TABLE_YOGA = "yoga_courses";
    private static final String COLUMN_ID = "_id"; // Primary key
    private static final String COLUMN_DAY_OF_WEEK = "day_of_week"; // Day of the week, e.g., Monday
    private static final String COLUMN_TIME_OF_COURSE = "time_of_course"; // Time of the course, e.g., 10:00
    private static final String COLUMN_CAPACITY = "capacity"; // Capacity of the class
    private static final String COLUMN_DURATION = "duration"; // Duration in minutes
    private static final String COLUMN_PRICE_PER_CLASS = "price_per_class"; // Price per class
    private static final String COLUMN_TYPE_OF_CLASS = "type_of_class"; // Type of class, e.g., Flow Yoga
    private static final String COLUMN_DESCRIPTION = "description"; // Optional description

    // Class Instances Table
    private static final String TABLE_INSTANCES = "class_instances";
    private static final String INSTANCE_ID = "_id"; // Primary key for instances
    private static final String INSTANCE_COURSE_ID = "course_id"; // Foreign key referencing yoga_courses
    private static final String INSTANCE_DATE = "date"; // Date of the class instance, must match day of the week
    private static final String INSTANCE_TEACHER = "teacher"; // Teacher for this instance
    private static final String INSTANCE_COMMENTS = "comments"; // Optional comments for this instance

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Yoga Courses table
        String createYogaCoursesTable = "CREATE TABLE " + TABLE_YOGA + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DAY_OF_WEEK + " TEXT NOT NULL, " +
                COLUMN_TIME_OF_COURSE + " TEXT NOT NULL, " +
                COLUMN_CAPACITY + " INTEGER NOT NULL, " +
                COLUMN_DURATION + " INTEGER NOT NULL, " +
                COLUMN_PRICE_PER_CLASS + " REAL NOT NULL, " +
                COLUMN_TYPE_OF_CLASS + " TEXT NOT NULL, " +
                COLUMN_DESCRIPTION + " TEXT);";
        db.execSQL(createYogaCoursesTable);

        // Create Class Instances table
        String createClassInstancesTable = "CREATE TABLE " + TABLE_INSTANCES + " (" +
                INSTANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                INSTANCE_COURSE_ID + " INTEGER NOT NULL, " + // Foreign key to yoga_courses
                INSTANCE_DATE + " TEXT NOT NULL, " + // Date of the class instance
                INSTANCE_TEACHER + " TEXT NOT NULL, " + // Teacher for this instance
                INSTANCE_COMMENTS + " TEXT, " + // Optional comments
                "FOREIGN KEY(" + INSTANCE_COURSE_ID + ") REFERENCES " + TABLE_YOGA + "(" + COLUMN_ID + ") ON DELETE CASCADE);"; // Foreign key constraint with ON DELETE CASCADE
        db.execSQL(createClassInstancesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_YOGA);
        onCreate(sqLiteDatabase);
    }

    public String addYoga(Yoga yoga, HashMap<String, Instance> instances) {
        // Step 1: Add Yoga to SQLite and get the generated ID
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_DAY_OF_WEEK, yoga.getDayOfWeek());
        cv.put(COLUMN_TIME_OF_COURSE, yoga.getTimeOfCourse());
        cv.put(COLUMN_CAPACITY, yoga.getCapacity());
        cv.put(COLUMN_DURATION, yoga.getDuration());
        cv.put(COLUMN_PRICE_PER_CLASS, yoga.getPricePerClass());
        cv.put(COLUMN_TYPE_OF_CLASS, yoga.getTypeOfClass());
        cv.put(COLUMN_DESCRIPTION, yoga.getDescription());

        // Insert and get the row ID
        long result = db.insert(TABLE_YOGA, null, cv);
        db.close();

        // Step 2: Check if the insertion was successful
        if (result == -1) {
            Toast.makeText(context, "Failed to add yoga class to SQLite.", Toast.LENGTH_SHORT).show();
            return null; // Return null if SQLite insert fails
        }

        // SQLite generated ID
        String generatedId = String.valueOf(result);

        // Step 3: Set the generated ID to the Yoga object
        yoga.setId(generatedId); // Set the SQLite ID to the Yoga object

        // Step 4: Add the Yoga class to Firebase
        FirebaseDatabase.getInstance("https://yoga-dodanhtuyen-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Yoga Peak")
                .child(generatedId) // Use the generated ID to reference the yoga class in Firebase
                .setValue(yoga)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Firebase success, now add instances if any exist
                        if (instances != null && !instances.isEmpty()) {
                            addInstances(instances, generatedId);
                        } else {
                            Toast.makeText(context, "No instances provided, yoga class added without instances.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Firebase failure
                        Toast.makeText(context, "Failed to add yoga class to Firebase.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Firebase failure
                    Toast.makeText(context, "Firebase Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Step 5: Return the generated ID from SQLite (this is the ID used in Firebase)
        return generatedId;
    }

    private void addInstances(HashMap<String, Instance> instances, String yogaId) {
        for (Instance instance : instances.values()) {
            // Set the yogaId for each instance
            instance.setYogaId(yogaId);

            // Add the instance to SQLite
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();

            cv.put(INSTANCE_COURSE_ID, instance.getYogaId()); // Foreign key referencing yoga_courses
            cv.put(INSTANCE_DATE, instance.getDate().getTime()); // Date of the instance
            cv.put(INSTANCE_TEACHER, instance.getTeacherName()); // Teacher of the instance
            cv.put(INSTANCE_COMMENTS, instance.getComment()); // Optional comments

            long result = db.insert(TABLE_INSTANCES, null, cv);
            db.close();

            if (result != -1) {
                // Successfully added to SQLite, now add to Firebase
                FirebaseDatabase.getInstance("https://yoga-dodanhtuyen-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .getReference("Yoga Peak")
                        .child(yogaId) // The yoga class ID to associate with the instance
                        .child("instances")
                        .child(instance.getId()) // Unique ID for the instance
                        .setValue(instance)
                        .addOnCompleteListener(firebaseTask -> {
                            if (firebaseTask.isSuccessful()) {
                                // Successfully added to Firebase
                                Toast.makeText(context, "Instance added successfully to Firebase!", Toast.LENGTH_SHORT).show();
                            } else {
                                // Firebase failure
                                Toast.makeText(context, "Failed to add instance to Firebase.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Firebase failure
                            Toast.makeText(context, "Error adding instance to Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Failed to add to SQLite
                Toast.makeText(context, "Failed to add instance to SQLite.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean updateYoga(Yoga yoga, HashMap<String, Instance> instances) {
        // Step 1: Update Yoga in SQLite
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_DAY_OF_WEEK, yoga.getDayOfWeek());
        cv.put(COLUMN_TIME_OF_COURSE, yoga.getTimeOfCourse());
        cv.put(COLUMN_CAPACITY, yoga.getCapacity());
        cv.put(COLUMN_DURATION, yoga.getDuration());
        cv.put(COLUMN_PRICE_PER_CLASS, yoga.getPricePerClass());
        cv.put(COLUMN_TYPE_OF_CLASS, yoga.getTypeOfClass());
        cv.put(COLUMN_DESCRIPTION, yoga.getDescription());

        // Update the record where the yogaId matches
        int rowsUpdated = db.update(TABLE_YOGA, cv, COLUMN_ID + " = ?", new String[]{yoga.getId()});
        db.close();

        // Step 2: Check if the update was successful in SQLite
        if (rowsUpdated == 0) {
            Toast.makeText(context, "Failed to update yoga class in SQLite.", Toast.LENGTH_SHORT).show();
            return false; // Return false if the update failed
        }

        // Step 3: Update the Yoga class in Firebase
        FirebaseDatabase.getInstance("https://yoga-dodanhtuyen-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Yoga Peak")
                .child(yoga.getId()) // Use the yoga class ID to reference the yoga class in Firebase
                .setValue(yoga)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (instances != null && !instances.isEmpty()) {
                            for (Instance instance : instances.values()) {
                                updateInstance(instance, instance.getId());
                            }
                        }
                        Toast.makeText(context, "Yoga class updated successfully in Firebase.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to update yoga class in Firebase.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error updating yoga class in Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Return true if the update was successful in both SQLite and Firebase
        return true;
    }

    public boolean updateInstance(Instance instance, String Id) {
        // Step 1: Update Instance in SQLite
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(INSTANCE_COURSE_ID, instance.getYogaId()); // Foreign key referencing yoga_courses
        cv.put(INSTANCE_DATE, instance.getDate().getTime()); // Date of the instance
        cv.put(INSTANCE_TEACHER, instance.getTeacherName()); // Teacher of the instance
        cv.put(INSTANCE_COMMENTS, instance.getComment()); // Optional comments

        // Update the record where the instanceId matches
        int rowsUpdated = db.update(TABLE_INSTANCES, cv, INSTANCE_ID + " = ?", new String[]{instance.getId()});
        db.close();

        // Step 2: Check if the update was successful in SQLite
        if (rowsUpdated == 0) {
            Toast.makeText(context, "Failed to update instance in SQLite.", Toast.LENGTH_SHORT).show();
            return false; // Return false if the update failed
        }

        // Step 3: Update the Instance in Firebase
        FirebaseDatabase.getInstance("https://yoga-dodanhtuyen-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Yoga Peak")
                .child(instance.getYogaId()) // Reference the yoga class ID
                .child("instances")
                .child(instance.getId()) // Reference the instance ID
                .setValue(instance)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Instance updated successfully in Firebase.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to update instance in Firebase.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error updating instance in Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Return true if the update was successful in both SQLite and Firebase
        return true;
    }

    public void deleteYoga(String yogaId) {
        // Delete from Firebase
        FirebaseDatabase.getInstance("https://yoga-dodanhtuyen-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Yoga Peak")
                .child(yogaId) // Use the ID of the yoga class to delete
                .removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Firebase success
                        deleteYogaFromSQLite(yogaId, true); // Pass true to indicate Firebase success
                    } else {
                        // Firebase failure
                        deleteYogaFromSQLite(yogaId, false); // Pass false to indicate Firebase failure
                        Toast.makeText(context, "Failed to delete yoga class from Firebase.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Firebase failure
                    deleteYogaFromSQLite(yogaId, false); // Pass false to indicate Firebase failure
                    Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteYogaFromSQLite(String yogaId, boolean firebaseSuccess) {
        // Delete from SQLite
        SQLiteDatabase db = this.getWritableDatabase();

        // Use a whereClause to specify the row to delete
        int result = db.delete(TABLE_YOGA, COLUMN_ID + " = ?", new String[]{yogaId});
        db.close();

        if (result > 0 && firebaseSuccess) {
            // Success in both Firebase and SQLite
            Toast.makeText(context, "Yoga class deleted successfully from both Firebase and SQLite!", Toast.LENGTH_SHORT).show();
        } else if (result > 0) {
            // Success only in SQLite
            Toast.makeText(context, "Yoga class deleted successfully from SQLite, but Firebase failed.", Toast.LENGTH_SHORT).show();
        } else if (firebaseSuccess) {
            // Success only in Firebase
            Toast.makeText(context, "Yoga class deleted successfully from Firebase, but SQLite failed.", Toast.LENGTH_SHORT).show();
        } else {
            // Failure in both
            Toast.makeText(context, "Error: Failed to delete yoga class from both Firebase and SQLite.", Toast.LENGTH_SHORT).show();
        }
    }

    public Cursor readAllYogaCourses() {
        String query = "SELECT * FROM " + TABLE_YOGA;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        if (db != null) {
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    private void updateYogaInSQLite(Yoga yoga, boolean firebaseSuccess) {
        // Update in SQLite
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_DAY_OF_WEEK, yoga.getDayOfWeek());
        cv.put(COLUMN_TIME_OF_COURSE, yoga.getTimeOfCourse());
        cv.put(COLUMN_CAPACITY, yoga.getCapacity());
        cv.put(COLUMN_DURATION, yoga.getDuration());
        cv.put(COLUMN_PRICE_PER_CLASS, yoga.getPricePerClass());
        cv.put(COLUMN_TYPE_OF_CLASS, yoga.getTypeOfClass());
        cv.put(COLUMN_DESCRIPTION, yoga.getDescription());

        int result = db.update(TABLE_YOGA, cv, COLUMN_ID + " = ?", new String[]{yoga.getId()});
        db.close();

        if (result > 0 && firebaseSuccess) {
            // Success in both Firebase and SQLite
            Toast.makeText(context, "Yoga class updated successfully in both Firebase and SQLite!", Toast.LENGTH_SHORT).show();
        } else if (result > 0) {
            // Success only in SQLite
            Toast.makeText(context, "Yoga class updated successfully in SQLite, but Firebase failed.", Toast.LENGTH_SHORT).show();
        } else if (firebaseSuccess) {
            // Success only in Firebase
            Toast.makeText(context, "Yoga class updated successfully in Firebase, but SQLite failed.", Toast.LENGTH_SHORT).show();
        } else {
            // Failure in both
            Toast.makeText(context, "Error: Failed to update yoga class in both Firebase and SQLite.", Toast.LENGTH_SHORT).show();
        }
    }

    // Delete all yogas from both SQLite and Firebase
    public void deleteAllYogas() {
        // Firebase: Delete all yogas
        FirebaseDatabase.getInstance("https://yoga-dodanhtuyen-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Yoga Peak")
                .removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Firebase deletion success, now delete from SQLite
                        SQLiteDatabase db = this.getWritableDatabase();
                        int deletedRows = db.delete(TABLE_YOGA, null, null); // Deletes all rows from the table
                        db.close();

                        if (deletedRows > 0) {
                            Toast.makeText(context, "All yoga classes deleted from both Firebase and SQLite.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to delete all yoga classes from SQLite.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Firebase deletion failed
                        Toast.makeText(context, "Failed to delete all yogas from Firebase.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Firebase deletion failed
                    Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void getTotalCountOfYogas(final ValueEventListener listener) {
        // Reference to the Yoga Peak node in Firebase
        DatabaseReference reference = FirebaseDatabase.getInstance("https://yoga-dodanhtuyen-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Yoga Peak");

        // Attach a listener to the Firebase reference to get the data
        reference.addListenerForSingleValueEvent(listener);
    }

    public <T> ArrayList<T> fetchDataFromDatabase(Cursor cursor, Function<Cursor, T> createObject) { //Pass a function that takes 'Cursor' and produce an object of type T
        ArrayList<T> dataList = new ArrayList<>();
        //Checks if the Cursor object is null or not and checks if the cursor can move to the first row of the result set
        if (cursor != null && cursor.moveToFirst()) {
            try {
                do {
                    //Applying creatingObject function to the "Cursor", which creates an object then adds that
                    //object to the list
                    T data = createObject.apply(cursor);
                    dataList.add(data); //Add data to the dataList
                } while (cursor.moveToNext()); //Cursor move to the next row == true => continue the while loop[
            } finally { //Even exception the cursor will still be closed
                cursor.close();
            }
        }
        return dataList;
    }
}
