package com.example.yoga_dodanhtuyen.Yoga;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yoga_dodanhtuyen.Database.DatabaseHelper;
import com.example.yoga_dodanhtuyen.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class YogaFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton add_button;
    private TextView no_data;
    private ImageView empty_imageview;
    private SearchView searchView;
    private Spinner searchOptions;
    private YogaAdapter yogaAdapter;
    private List<Yoga> yogaList;
    private ArrayList<Yoga> yogas;
    private DatabaseReference databaseReference;
    private ValueEventListener eventListener;
    private DatabaseHelper myDB;

    public YogaFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_yoga, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        add_button = view.findViewById(R.id.floatingAdd_button);
        no_data = view.findViewById(R.id.no_data);
        empty_imageview = view.findViewById(R.id.empty_imageview);
        searchView = view.findViewById(R.id.yogaClassSearch_View);
        searchView.clearFocus();
        searchOptions = view.findViewById(R.id.searchOptions_spinner);

        //Creating an instance of the DatabaseHelper with the requireContext() is the context of this fragment
        myDB = new DatabaseHelper(requireContext());

        myDB.getTotalCountOfYogas(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    empty_imageview.setVisibility(View.GONE);
                    no_data.setVisibility(View.GONE);
                } else {
                    empty_imageview.setVisibility(View.GONE);
                    no_data.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.search_options, android.R.layout.simple_spinner_item);
        // Set the drop-down view resource for the adapter using the setDropDownViewResource() method.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Set the adapter for the searchOptions spinner using the setAdapter() method.
        searchOptions.setAdapter(adapter);

        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 1); // You can change the number of columns as needed
        recyclerView.setLayoutManager(layoutManager);

        yogaList = new ArrayList<>();
        yogaAdapter = new YogaAdapter(requireContext(), yogaList);

        yogas = myDB.fetchDataFromDatabase(myDB.readAllYogaCourses(), cursor -> {
            Yoga yogaCourse = new Yoga();
            yogaCourse.setId(cursor.getString(0)); // ID
            yogaCourse.setDayOfWeek(cursor.getString(1)); // Day of the week
            yogaCourse.setTimeOfCourse(cursor.getString(2)); // Time of course
            yogaCourse.setCapacity(cursor.getInt(3)); // Capacity
            yogaCourse.setDuration(cursor.getString(4)); // Duration
            yogaCourse.setPricePerClass(cursor.getDouble(5)); // Price per class
            yogaCourse.setTypeOfClass(cursor.getString(6)); // Type of class
            yogaCourse.setDescription(cursor.getString(7)); // Description (optional)
            return yogaCourse;
        });


        recyclerView.setAdapter(yogaAdapter);

        databaseReference = FirebaseDatabase.getInstance("https://yoga-dodanhtuyen-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Yoga Peak");

        eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                yogaList.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    Yoga yoga = itemSnapshot.getValue(Yoga.class);
                    yogaList.add(yoga);
                }
                Log.d("Firebase", "Yoga list size: " + yogaList.size());
                yogaAdapter.notifyDataSetChanged(); // Ensure you're using the correct adapter
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(requireContext(), "Failed to fetch instances: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), YogaFormActivity.class); //Intent object created specifies the activity wanted to start
                startForResult.launch(intent); //Starts activity class and returns ActivityResultLauncher object which will allow u to handle the result of the activity
            }
        });

        // Set up a SearchView.OnQueryTextListener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return true;
            }
        });
        return view;
    }

    private final ActivityResultLauncher<Intent> startForResult = registerForActivityResult( //Register for the result of the "HikeAddOrUpdateActivity" class
            new ActivityResultContracts.StartActivityForResult(), //A contract is provided to start activity for result
            result -> { //Result received
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData(); //Get data from result
                    if (data != null) {
                        boolean dataChanged = data.getBooleanExtra("dataChanged", false); //Get the boolean data from the "dataChanged"
                        if (dataChanged) { //If dataChanged == true
                            refreshData();
                        }
                    }
                }
            }
    );

    public void refreshData() {
        //Replaces the HikeFragment class with a new instance of the HikeFragment Class (Refresh the data)
        getActivity().getSupportFragmentManager().beginTransaction().replace(YogaFragment.this.getId(), new YogaFragment()).commit();
    }

    public void searchList(String text) {
        String searchOption = searchOptions.getSelectedItem().toString();

        ArrayList<Yoga> searchList = new ArrayList<>();

        // Check the selected search option
        switch (searchOption) {

            case "Day of week":
                for (Yoga dataClass : yogaList) {
                    if (dataClass.getDayOfWeek().toLowerCase().contains(text.toLowerCase())) {
                        searchList.add(dataClass);
                    }
                }
                break;

            case "Type of class":
                for (Yoga dataClass : yogaList) {
                    if (dataClass.getTypeOfClass().toLowerCase().contains(text.toLowerCase())) {
                        searchList.add(dataClass);
                    }
                }
                break;

            default:
                // Optionally, handle other cases or an invalid search option
                break;
        }

        // Update the adapter with the filtered list
        yogaAdapter.searchDataList(searchList);
    }
}