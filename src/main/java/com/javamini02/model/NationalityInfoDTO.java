package com.javamini02.model;

import lombok.Data;

import java.util.List;

@Data
public class NationalityInfoDTO {

    private int count;
    private String name;
    private List<CountryInfo> country;

    @Data
    public static class CountryInfo {
        private String country_id;
        private double probability;
    }
}

