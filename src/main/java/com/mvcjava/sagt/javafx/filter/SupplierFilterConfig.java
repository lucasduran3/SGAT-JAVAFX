/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.filter;

/**
 *
 * @author lucas
 */

import com.mvcjava.sagt.javafx.viewmodel.SupplierViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class SupplierFilterConfig {

    private SupplierFilterConfig() {}

    public static List<FilterGroup<SupplierViewModel>> buildGroups(
            Set<String> availableProvinces) {

        List<FilterGroup<SupplierViewModel>> groups = new ArrayList<>();

        if (!availableProvinces.isEmpty()) {
            List<FilterCriteria<SupplierViewModel>> criteria = new ArrayList<>();
            for (String province : availableProvinces) {
                criteria.add(FilterCriteria.of(
                        province.toLowerCase(),
                        capitalize(province),
                        vm -> province.equalsIgnoreCase(vm.provinceProperty().get())
                ));
            }
            groups.add(FilterGroup.multi("Provincia",
                    criteria.toArray(new FilterCriteria[0])));
        }

        return groups;
    }

    public static boolean textMatch(SupplierViewModel vm, String query) {
        return contains(vm.nameProperty().get(),     query)
            || contains(vm.emailProperty().get(),    query)
            || contains(vm.cityProperty().get(),     query)
            || contains(vm.provinceProperty().get(), query);
    }

    private static boolean contains(String f, String q) {
        return f != null && f.toLowerCase().contains(q);
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }
}
