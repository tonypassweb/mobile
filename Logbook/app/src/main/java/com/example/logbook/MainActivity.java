package com.example.logbook;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText inputValue;
    private Spinner sourceUnitSpinner, targetUnitSpinner;
    private TextView resultTextView;
    private Button convertButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputValue = findViewById(R.id.inputValue);
        sourceUnitSpinner = findViewById(R.id.sourceUnitSpinner);
        targetUnitSpinner = findViewById(R.id.targetUnitSpinner);
        resultTextView = findViewById(R.id.resultTextView);
        convertButton = findViewById(R.id.convertButton);

        // Setting up Spinner with units
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.unit_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sourceUnitSpinner.setAdapter(adapter);
        targetUnitSpinner.setAdapter(adapter);

        // Button click listener for conversion
        convertButton.setOnClickListener(v -> {
            String valueStr = inputValue.getText().toString();
            if (valueStr.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter a value", Toast.LENGTH_SHORT).show();
                return;
            }

            double value = Double.parseDouble(valueStr);
            String sourceUnit = sourceUnitSpinner.getSelectedItem().toString();
            String targetUnit = targetUnitSpinner.getSelectedItem().toString();
            double result = convertUnits(value, sourceUnit, targetUnit);

            resultTextView.setText(String.format("Result: %.2f %s", result, targetUnit));
        });
    }

    private double convertUnits(double value, String sourceUnit, String targetUnit) {
        // Conversion logic for different units
        if (sourceUnit.equals("Metre")) {
            if (targetUnit.equals("Millimetre")) {
                return value * 1000;
            } else if (targetUnit.equals("Mile")) {
                return value * 0.000621371;
            } else if (targetUnit.equals("Foot")) {
                return value * 3.28084;
            }
        } else if (sourceUnit.equals("Millimetre")) {
            if (targetUnit.equals("Metre")) {
                return value / 1000;
            } else if (targetUnit.equals("Mile")) {
                return value * 6.2137e-7;
            } else if (targetUnit.equals("Foot")) {
                return value * 0.00328084;
            }
        } else if (sourceUnit.equals("Mile")) {
            if (targetUnit.equals("Metre")) {
                return value * 1609.34;
            } else if (targetUnit.equals("Millimetre")) {
                return value * 1609340;
            } else if (targetUnit.equals("Foot")) {
                return value * 5280;
            }
        } else if (sourceUnit.equals("Foot")) {
            if (targetUnit.equals("Metre")) {
                return value * 0.3048;
            } else if (targetUnit.equals("Millimetre")) {
                return value * 304.8;
            } else if (targetUnit.equals("Mile")) {
                return value * 0.000189394;
            }
        }

        return value; // Default, in case units are the same
    }
}