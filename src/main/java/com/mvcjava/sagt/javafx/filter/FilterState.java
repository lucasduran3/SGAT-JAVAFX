/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.filter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author lucas
 */
public final class FilterState<T> {
    
    private static final class GroupBucket<T> {
        final FilterGroup.SelectMode mode;
        final Map<String, Predicate<T>> active = new HashMap<>();
        
        GroupBucket(FilterGroup.SelectMode mode) {this.mode = mode;}
        
        Predicate<T> combined() {
            if (active.isEmpty()) return null;
            return active.values().stream()
                    .reduce(vm -> false, Predicate::or);
        }
    }
    
    private final Map<String, GroupBucket<T>> buckets = new HashMap<>();
    
    private final ObjectProperty<Predicate<T>> combinedPredicate = 
            new SimpleObjectProperty<>(null);
    
    public void setCriterion(FilterGroup<T> group, FilterCriteria<T> criterion) {
        GroupBucket<T> bucket = buckets.computeIfAbsent(
                group.getTitle(), k -> new GroupBucket<>(group.getSelectMode()));
        
        if (bucket.mode == FilterGroup.SelectMode.SINGLE) {
            bucket.active.clear();
        }
        bucket.active.put(criterion.getKey(), criterion.getPredicate());
        recompute();
    }
    
    public void clearCriterion(FilterGroup<T> group, FilterCriteria<T> criterion) {
        GroupBucket<T> bucket = buckets.get(group.getTitle());
        if (bucket != null) {
            bucket.active.remove(criterion.getKey());
            recompute();
        }
    }
    
    public void clearAll() {
        buckets.values().forEach(b -> b.active.clear());
        combinedPredicate.set(null);
    }
    
    public boolean isActive(FilterGroup<T> group, FilterCriteria<T> criterion) {
        GroupBucket<T> bucket = buckets.get(group.getTitle());
        return bucket != null && bucket.active.containsKey(criterion.getKey());
    }
    
    public boolean hasActiveFilters() {
        return buckets.values().stream().anyMatch(b -> !b.active.isEmpty());
    }
    
    //para testing 
    public Map<String, Map<String, Predicate<T>>> getActiveCriteria() {
        Map<String, Map<String, Predicate<T>>> snapshot = new HashMap<>();
        buckets.forEach((title, bucket) -> snapshot.put(title, Collections.unmodifiableMap(bucket.active)));
        return Collections.unmodifiableMap(snapshot);
    }
    
    public ObjectProperty<Predicate <T>> combinedPredicateProperty() {
        return combinedPredicate;
    }
    
    private void recompute() {
        Predicate<T> result = null;
        
        for (GroupBucket<T> bucket : buckets.values()) {
            Predicate<T> groupPredicate = bucket.combined();
            if (groupPredicate == null) continue;
            result = (result == null) ? groupPredicate : result.and(groupPredicate);
        }
        
        combinedPredicate.set(result);
    }
}
