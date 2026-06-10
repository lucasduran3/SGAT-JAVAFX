/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.filter;

import java.util.function.Predicate;

/**
 *
 * @author lucas
 */
public final class FilterCriteria<T> {
    private final String key; //id unico
    private final String displayName; //label en la ui
    private final Predicate<T> predicate;
    
    private FilterCriteria(String key, String displayName, Predicate<T> predicate) {
        this.key = key;
        this.displayName = displayName;
        this.predicate = predicate;
    }
    
    //Factory
    
    public static <T> FilterCriteria<T> of(String key, String displayName, Predicate<T> predicate) {
        return new FilterCriteria<>(key, displayName, predicate);
    }
    
    public static <T> FilterCriteria<T> of(String displayName, Predicate<T> predicate) {
        return new FilterCriteria<>(displayName, displayName, predicate);
    }
    
    public String getKey() {return key;}
    public String getDisplayName() {return displayName;}
    public Predicate<T> getPredicate() {return predicate;}
    
    @Override
    public String toString() { return displayName; }
}
