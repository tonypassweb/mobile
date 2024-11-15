package com.example.yoga_dodanhtuyen.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.yoga_dodanhtuyen.Yoga.Yoga;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.function.Function;

public class DatabaseHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME = "Yoga_Peak.db";
    private static final int DATABASE_VERSION = 1;

    // Yoga Table
    private static final String TABLE_YOGA = "yoga_courses";
    private static final String COLUMN_ID = "_id"; // Primary key
    private static final String COLUMN_FIREBASE_ID = "firebaseId"; // Firebase ID
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
    private static final String INSTANCE_FIREBASE_ID = "firebaseId"; // Firebase ID
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
                COLUMN_FIREBASE_ID + " TEXT NOT NULL, " +
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
                INSTANCE_COURSE_ID + " INTEGER NOT NULL, " +
                INSTANCE_FIREBASE_ID + "TEXT NOT NULL, " +
                INSTANCE_DATE + " TEXT NOT NULL, " +
                INSTANCE_TEACHER + " TEXT NOT NULL, " +
                INSTANCE_COMMENTS + " TEXT, " +
                "FOREIGN KEY(" + INSTANCE_COURSE_ID + ") REFERENCES " + TABLE_YOGA + "(" + COLUMN_ID + "));";
        db.execSQL(createClassInstancesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_YOGA);
        onCreate(sqLiteDatabase);
    }

    public void addYoga(Yoga yoga) {
        // Firebase
        FirebaseDatabase.getInstance("https://yoga-dodanhtuyen-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Yoga Peak")
                .child(yoga.getId()) // Use the ID of the existing class to update
                .setValue(yoga)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Firebase success
                        addYogaToSQLite(yoga, true); // Pass true to indicate Firebase success
                    } else {
                        // Firebase failure
                        addYogaToSQLite(yoga, false); // Pass false to indicate Firebase failure
                    }
                })
                .addOnFailureListener(e -> {
                    // Firebase failure
                    addYogaToSQLite(yoga, false); // Pass false to indicate Firebase failure
                    Toast.makeText(context, "Firebase Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addYogaToSQLite(Yoga yoga, boolean firebaseSuccess) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_FIREBASE_ID, yoga.getId());
        cv.put(COLUMN_DAY_OF_WEEK, yoga.getDayOfWeek());
        cv.put(COLUMN_TIME_OF_COURSE, yoga.getTimeOfCourse());
        cv.put(COLUMN_CAPACITY, yoga.getCapacity());
        cv.put(COLUMN_DURATION, yoga.getDuration());
        cv.put(COLUMN_PRICE_PER_CLASS, yoga.getPricePerClass());
        cv.put(COLUMN_TYPE_OF_CLASS, yoga.getTypeOfClass());
        cv.put(COLUMN_DESCRIPTION, yoga.getDescription());

        long result = db.insert(TABLE_YOGA, null, cv);
        db.close();

        if (result != -1 && firebaseSuccess) {
            // Success in both Firebase and SQLite
            Toast.makeText(context, "Yoga class added successfully to both Firebase and SQLite!", Toast.LENGTH_SHORT).show();
        } else if (result != -1) {
            // Success only in SQLite
            Toast.makeText(context, "Yoga class added successfully to SQLite, but Firebase failed.", Toast.LENGTH_SHORT).show();
        } else if (firebaseSuccess) {
            // Success only in Firebase
            Toast.makeText(context, "Yoga class added successfully to Firebase, but SQLite failed.", Toast.LENGTH_SHORT).show();
        } else {
            // Failure in both
            Toast.makeText(context, "Error: Failed to add yoga class to both Firebase and SQLite.", Toast.LENGTH_SHORT).show();
        }
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
        int result = db.delete(TABLE_YOGA, "firebaseId=?", new String[]{yogaId});
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

    public void updateYoga(Yoga yoga) {
        // Firebase Update
        FirebaseDatabase.getInstance("https://yoga-dodanhtuyen-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Yoga Peak")
                .child(yoga.getId()) // Use the ID of the yoga class to update
                .setValue(yoga)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Firebase success
                        updateYogaInSQLite(yoga, true); // Pass true to indicate Firebase success
                    } else {
                        // Firebase failure
                        updateYogaInSQLite(yoga, false); // Pass false to indicate Firebase failure
                        Toast.makeText(context, "Failed to update yoga class in Firebase.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Firebase failure
                    updateYogaInSQLite(yoga, false); // Pass false to indicate Firebase failure
                    Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
        cv.put(COLUMN_FIREBASE_ID, yoga.getId());

        int result = db.update(TABLE_YOGA, cv, "firebaseId=?", new String[]{yoga.getId()});
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
                        deleteAllYogasFromSQLite();
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

    // Delete all yogas from SQLite
    private void deleteAllYogasFromSQLite() {
        SQLiteDatabase db = this.getWritableDatabase();
        int deletedRows = db.delete(TABLE_YOGA, null, null);  // Deletes all rows from the table
        db.close();

        if (deletedRows > 0) {
            Toast.makeText(context, "All yoga classes deleted from SQLite.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to delete all yoga classes from SQLite.", Toast.LENGTH_SHORT).show();
        }
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
