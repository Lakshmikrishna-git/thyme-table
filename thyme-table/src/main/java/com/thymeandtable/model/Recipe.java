package com.thymeandtable.model;
public class Recipe {
    private final int id; private final String title,description,instructions,category,tags,creator; private final double influencerScore; private final Double averageRating;
    public Recipe(int id,String title,String description,String instructions,String category,String tags,String creator,double influencerScore,Double averageRating){this.id=id;this.title=title;this.description=description;this.instructions=instructions;this.category=category;this.tags=tags;this.creator=creator;this.influencerScore=influencerScore;this.averageRating=averageRating;}
    public int getId(){return id;} public String getTitle(){return title;} public String getDescription(){return description;} public String getInstructions(){return instructions;} public String getCategory(){return category;} public String getTags(){return tags;} public String getCreator(){return creator;} public double getInfluencerScore(){return influencerScore;} public Double getAverageRating(){return averageRating;}
}
