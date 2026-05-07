package com.f1dashboard.model;

public class Constructor {
    public final int constructorId;
    public final String name, nationality;

    public Constructor(int constructorId, String name, String nationality) {
        this.constructorId = constructorId;
        this.name = name;
        this.nationality = nationality;
    }
}
