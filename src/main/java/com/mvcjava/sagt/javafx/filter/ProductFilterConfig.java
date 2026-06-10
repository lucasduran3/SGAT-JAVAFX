/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.filter;

import com.mvcjava.sagt.javafx.dao.model.Category;
import com.mvcjava.sagt.javafx.viewmodel.ProductViewModel;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author lucas
 */
public final class ProductFilterConfig {
    
    private ProductFilterConfig() {}
    
    public static List<FilterGroup<ProductViewModel>> buildGroups(Set<Category> availableCategories) {
        List<FilterGroup<ProductViewModel>> groups = new ArrayList<>();
        
        if (!availableCategories.isEmpty()) {
            List<FilterCriteria<ProductViewModel>> catCriteria = new ArrayList<>();
 
            for (Category cat : availableCategories) {
                final String catName = cat.getName();
                catCriteria.add(FilterCriteria.of(
                        cat.getId().toString(),          
                        capitalize(catName),              
                        vm -> vm.getCategories().stream()
                                .anyMatch(c -> c.getName().equalsIgnoreCase(catName))
                ));
            }
 
            groups.add(FilterGroup.multi("Categoría",
                    catCriteria.toArray(new FilterCriteria[0])));
        }
 
        groups.add(FilterGroup.single("Estado de stock",
                FilterCriteria.of("stock_normal",  "Stock normal",
                        vm -> vm.stockProperty().get() > vm.minStockProperty().get()),
                FilterCriteria.of("stock_bajo",    "Stock bajo",
                        vm -> {
                            int s = vm.stockProperty().get();
                            int m = vm.minStockProperty().get();
                            return s > 0 && s <= m;
                        }),
                FilterCriteria.of("stock_agotado", "Sin stock",
                        vm -> vm.stockProperty().get() == 0)
        ));
 
        groups.add(FilterGroup.single("Fecha de carga",
                FilterCriteria.of("fecha_hoy",     "Hoy",
                        vm -> isWithin(vm.getEntryDate(), 0)),
                FilterCriteria.of("fecha_semana",  "Esta semana",
                        vm -> isWithin(vm.getEntryDate(), 7)),
                FilterCriteria.of("fecha_mes",     "Este mes",
                        vm -> isWithin(vm.getEntryDate(), 30))
        ));
 
        return groups;
    }
    
    public static boolean textMatch(ProductViewModel vm, String query) {
        return contains(vm.nameProperty().get(),  query)
            || contains(vm.brandProperty().get(), query)
            || contains(vm.modelProperty().get(), query);
    }
 
    //Helpers
    private static boolean isWithin(Timestamp ts, long days) {
        if (ts == null) return false;
        Instant cutoff = (days == 0)
                ? Instant.now().truncatedTo(ChronoUnit.DAYS)
                : Instant.now().minus(days, ChronoUnit.DAYS);
        return ts.toInstant().isAfter(cutoff);
    }
 
    private static boolean contains(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }
 
    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
