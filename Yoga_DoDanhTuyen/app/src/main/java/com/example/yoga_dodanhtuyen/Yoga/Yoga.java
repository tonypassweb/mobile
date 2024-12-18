package com.example.yoga_dodanhtuyen.Yoga;

import com.example.yoga_dodanhtuyen.Instance.Instance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class Yoga implements Serializable {
    private String id;
    private String dayOfWeek; // Day of the week, e.g., Monday, Tuesday
    private String timeOfCourse; // Time of the course, e.g., 10:00, 11:00
    private int capacity; // Number of persons that can attend
    private String duration; // Duration in minutes, e.g., 60
    private double pricePerClass; // Price per class, e.g., £10
    private String typeOfClass; // Type of class, e.g., Flow Yoga, Aerial Yoga
    private String description; // Optional field for additional information
    private HashMap<String, Instance> instances;

    public Yoga() {
        this.instances = new HashMap<>();
    }

    public Yoga(String id, String typeOfClass, String dayOfWeek, String timeOfCourse,
                int capacity, String duration, double pricePerClass, String description, HashMap<String, Instance> instances) {
        this.id = id;
        this.typeOfClass = typeOfClass;
        this.dayOfWeek = dayOfWeek;
        this.timeOfCourse = timeOfCourse;
        this.capacity = capacity;
        this.duration = duration;
        this.pricePerClass = pricePerClass;
        this.description = description;
        this.instances = instances;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getTimeOfCourse() {
        return timeOfCourse;
    }

    public void setTimeOfCourse(String timeOfCourse) {
        this.timeOfCourse = timeOfCourse;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public double getPricePerClass() {
        return pricePerClass;
    }

    public void setPricePerClass(double pricePerClass) {
        this.pricePerClass = pricePerClass;
    }

    public String getTypeOfClass() {
        return typeOfClass;
    }

    public void setTypeOfClass(String typeOfClass) {
        this.typeOfClass = typeOfClass;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean addInstanceLocally(Instance instance) {
        if (instance != null) {
            // If the instance doesn't have an ID, allow adding it with a null or empty ID
            // Later, you can update the ID once it's assigned (from Firebase or SQLite)
            if (instance.getId() == null || instance.getId().isEmpty()) {
                instance.setId(generateTempId()); // Optionally generate a temporary ID if needed
            }

            // Add or update the instance in the local map (HashMap)
            this.instances.put(instance.getId(), instance);
            return true;
        }
        return false;
    }

    // Optional method to generate a temporary ID (e.g., for local purposes)
    private String generateTempId() {
        // Example: Generate a unique ID, e.g., using UUID or timestamp
        return UUID.randomUUID().toString();
    }

    public HashMap<String, Instance> getInstances() {
        return instances;
    }

    public void setInstances(HashMap<String, Instance> instances) {
        this.instances = instances;
    }
}

