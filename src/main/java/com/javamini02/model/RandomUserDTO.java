package com.javamini02.model;


import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RandomUserDTO {

    private List<UserResult> results;

    @Data
    public static class UserResult {
        private String gender;
        private UserName name;
        private UserLocation location;
        private String nat;
        private DOB dob;

    }
    @Data
    public static class UserName {
        private String title;
        private String first;
        private String last;

    }

    @Data
    public static class DOB{
        private LocalDateTime date;
        private Long age;
    }

    @Data
    public static class UserLocation {
        private UserStreet street;
        private String city;
        private String state;
        private String country;
        private String postcode;
        private UserCoordinates coordinates;
        private UserTimezone timezone;

    }



    @Data
    public static class UserStreet {
        private int number;
        private String name;
    }

    @Data
    public static class UserCoordinates {
        private String latitude;
        private String longitude;
        // Getters and setters...
    }

    @Data
    public static class UserTimezone {
        private String offset;
        private String description;
    }

}

