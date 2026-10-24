package com.thymeandtable.model;
public class Ingredient {
    private final int id; private final String name, unit;
    public Ingredient(int id,String name,String unit){this.id=id;this.name=name;this.unit=unit;}
    public int getId(){return id;} public String getName(){return name;} public String getUnit(){return unit;}
}
