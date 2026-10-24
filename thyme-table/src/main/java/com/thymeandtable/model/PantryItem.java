package com.thymeandtable.model;
public class PantryItem {
    private final int id; private final String name,unit,expiryDate; private final double quantity;
    public PantryItem(int id,String name,double quantity,String unit,String expiryDate){this.id=id;this.name=name;this.quantity=quantity;this.unit=unit;this.expiryDate=expiryDate;}
    public int getId(){return id;} public String getName(){return name;} public double getQuantity(){return quantity;} public String getUnit(){return unit;} public String getExpiryDate(){return expiryDate;}
}
