/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.filter;

/**
 *
 * @author lucas
 */

import com.mvcjava.sagt.javafx.enums.ClientType;
import com.mvcjava.sagt.javafx.viewmodel.ClientViewModel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class ClientFilterConfig {

    private ClientFilterConfig() {}

    public static List<FilterGroup<ClientViewModel>> buildGroups() {
        List<FilterGroup<ClientViewModel>> groups = new ArrayList<>();

        groups.add(FilterGroup.single("Tipo de cliente",
                FilterCriteria.of("tipo_empresa",    "Empresa",
                        vm -> ClientType.EMPRESA.equals(vm.clientTypeProperty().get())),
                FilterCriteria.of("tipo_particular", "Particular",
                        vm -> ClientType.PARTICULAR.equals(vm.clientTypeProperty().get()))
        ));

        groups.add(FilterGroup.single("Fecha de alta",
                FilterCriteria.of("alta_hoy",    "Hoy",
                        vm -> vm.getEntryDate() != null &&
                              vm.getEntryDate().toLocalDate().isEqual(LocalDate.now())),
                FilterCriteria.of("alta_semana", "Esta semana",
                        vm -> vm.getEntryDate() != null &&
                              !vm.getEntryDate().toLocalDate()
                               .isBefore(LocalDate.now().minusDays(7))),
                FilterCriteria.of("alta_mes",    "Este mes",
                        vm -> isCurrentMonth(vm.getEntryDate()))
        ));

        return groups;
    }

    public static boolean textMatch(ClientViewModel vm, String query) {
        return contains(vm.companyNameProperty().get(), query)
            || contains(vm.cuitCuilProperty().get(),    query)
            || contains(vm.emailProperty().get(),       query)
            || contains(vm.locationProperty().get(),    query);
    }

    private static boolean contains(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }

    private static boolean isCurrentMonth(java.sql.Date date) {
        if (date == null) return false;
        LocalDate d = date.toLocalDate();
        LocalDate n = LocalDate.now();
        return d.getYear() == n.getYear() && d.getMonth() == n.getMonth();
    }
}
