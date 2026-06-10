/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.filter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lucas
 */
public final class FilterGroup<T> {
    public enum SelectMode {
        SINGLE, MULTI
    }
    
    private final String title;
    private final SelectMode selectMode;
    private final List<FilterCriteria<T>> criteria;
    
    private FilterGroup(String title, SelectMode selectMode, List<FilterCriteria<T>> criteria) {
        this.title = title;
        this.selectMode = selectMode;
        this.criteria = criteria;
    }
    
    @SafeVarargs
    public static <T> FilterGroup<T> single(String title, FilterCriteria<T>... criteria) {
        List<FilterCriteria<T>> list = new ArrayList<>();
        for (FilterCriteria<T> c : criteria) list.add(c);
        return new FilterGroup<>(title, SelectMode.SINGLE, list);
    }
    
    @SafeVarargs
    public static <T> FilterGroup<T> multi(String title, FilterCriteria<T>... criteria) {
        List<FilterCriteria<T>> list = new ArrayList<>();
        for (FilterCriteria<T> c : criteria) list.add(c);
        return new FilterGroup<>(title, SelectMode.MULTI, list);
    }
    
    public String getTitle() {return title;}
    public SelectMode getSelectMode() {return selectMode;}
    public List<FilterCriteria<T>> getCriteria() {return criteria;}
    
    public boolean isSingle() {return selectMode == SelectMode.SINGLE;}
}
