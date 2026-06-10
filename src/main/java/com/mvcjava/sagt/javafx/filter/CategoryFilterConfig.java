/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.filter;

import com.mvcjava.sagt.javafx.viewmodel.CategoryViewModel;
import java.util.List;

/**
 *
 * @author lucas
 */
public final class CategoryFilterConfig {

    private CategoryFilterConfig() {}

    public static List<FilterGroup<CategoryViewModel>> buildGroups() {
        return List.of(
            FilterGroup.single("Inicial",
                FilterCriteria.of("a_e", "A – E",
                        vm -> startsInRange(vm, 'a', 'e')),
                FilterCriteria.of("f_m", "F – M",
                        vm -> startsInRange(vm, 'f', 'm')),
                FilterCriteria.of("n_z", "N – Z",
                        vm -> startsInRange(vm, 'n', 'z'))
            )
        );
    }

    public static boolean textMatch(CategoryViewModel vm, String query) {
        return vm.nameProperty().get() != null
            && vm.nameProperty().get().toLowerCase().contains(query);
    }

    private static boolean startsInRange(CategoryViewModel vm, char from, char to) {
        String name = vm.nameProperty().get();
        if (name == null || name.isEmpty()) return false;
        char first = Character.toLowerCase(name.charAt(0));
        return first >= from && first <= to;
    }
}
