package com.example.myapplication;

public class User {

    public String id, surname, middlename, birthDatePlace, dateOfIssue, dateOfExpiration, division, code, residence, categories;

    User(String id, String surname, String middlename, String birthDatePlace, String dateOfIssue, String dateOfExpiration, String division, String code, String residence, String categories){
        this.id = id;
        this.surname = surname;
        this.middlename = middlename;
        this.birthDatePlace = birthDatePlace;
        this.dateOfIssue = dateOfIssue;
        this.dateOfExpiration = dateOfExpiration;
        this.division = division;
        this.code = code;
        this.residence = residence;
        this.categories = categories;
    }
}
