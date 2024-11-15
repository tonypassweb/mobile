package com.example.yoga_dodanhtuyen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;

import com.example.yoga_dodanhtuyen.Database.DatabaseHelper;
import com.example.yoga_dodanhtuyen.Yoga.YogaFragment;
import com.example.yoga_dodanhtuyen.databinding.ActivityMainBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new YogaFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {

            switch (item.getItemId()){
                case R.id.yoga:
                    replaceFragment(new YogaFragment());
                    break;
                case R.id.delete:
                    checkAndDeleteAllYogas();
                    break;
            }
            return true;
        });
    }


    private void checkAndDeleteAllYogas() {
        dbHelper.getTotalCountOfYogas(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) { // Check if any yoga classes exist
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Delete All Yogas")
                            .setMessage("Are you sure you want to delete all yogas and instances?")
                            .setPositiveButton("Yes", (dialogInterface, i) -> {
                                // Call the deleteAllYogas function from DatabaseHelper
                                dbHelper.deleteAllYogas();
                                refreshYogaFragmentData(); // Refresh the UI or Fragment data
                            })
                            .setNegativeButton("No", (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            })
                            .create().show();
                } else {
                    Toast.makeText(MainActivity.this, "No Yoga class found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void refreshYogaFragmentData() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        for (Fragment fragment : fragmentManager.getFragments()) {
            if (fragment instanceof YogaFragment) {
                ((YogaFragment) fragment).refreshData();
                break;
            }
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

}