package com.ateamincorporated.rentals.db;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Asset implements Serializable {

    public String name;
    public String description;
    public String timeOfCreation;
    public String genericLocation;
    public String startDate;
    public String endDate;
    public String price;
    public String priceUnit;
    public String category;
    public String image;

    public Asset() {
        // Default constructor required for calls to DataSnapshot.getValue(Asset.class)
    }

    public Asset(String name, String description, String timeOfCreation, String genericLocation, String startDate, String endDate, String price, String priceUnit, String category, String image) {
        this.name = name;
        this.description = description;
        this.timeOfCreation = timeOfCreation;
        this.genericLocation = genericLocation;
        this.startDate = startDate;
        this.endDate = endDate;
        this.price = price;
        this.priceUnit = priceUnit;
        this.category = category;
        this.image = image;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("description", description);
        result.put("timeOfCreation", timeOfCreation);
        result.put("genericLocation", genericLocation);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("price", price);
        result.put("priceUnit", priceUnit);
        result.put("category", category);
        result.put("image", image);

        return result;
    }
}
