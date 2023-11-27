package com.ateamincorporated.rentals.db;

public final class DatabaseContract {
    public static final String GLOBAL_ASSET_PATH = "assets";
    public static final String GLOBAL_FAVORITE_PATH = "favorites";
    public static final String GLOBAL_USER_PATH = "users";
    public static final String GLOBAL_CATEGORY_PATH = "categories";
    public static final String GLOBAL_LOCATION_PATH = "locations";
    public static final String GLOBAL_MESSAGE_PATH = "messages";
    public static final String GLOBAL_IMAGES_PATH = "images";
    public static final String GLOBAL_PROFILE_PICTURES_PATH = "profilePics";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private DatabaseContract() {
    }
}
