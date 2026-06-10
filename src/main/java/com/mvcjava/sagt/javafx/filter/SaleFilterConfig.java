/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.filter;

/**
 *
 * @author lucas
 */

import com.mvcjava.sagt.javafx.enums.PaymentMethod;
import com.mvcjava.sagt.javafx.viewmodel.SaleViewModel;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class SaleFilterConfig {

    private SaleFilterConfig() {}

    public static List<FilterGroup<SaleViewModel>> buildGroups() {
        List<FilterGroup<SaleViewModel>> groups = new ArrayList<>();

        List<FilterCriteria<SaleViewModel>> paymentCriteria = new ArrayList<>();
        for (PaymentMethod method : PaymentMethod.values()) {
            paymentCriteria.add(FilterCriteria.of(
                    method.name(),
                    formatMethod(method),
                    vm -> method.equals(vm.getPaymentMethod())
            ));
        }
        groups.add(FilterGroup.multi("Método de pago",
                paymentCriteria.toArray(new FilterCriteria[0])));

        groups.add(FilterGroup.single("Período",
                FilterCriteria.of("periodo_hoy",   "Hoy",
                        vm -> isSameDay(vm.getDate(), LocalDate.now())),
                FilterCriteria.of("periodo_semana", "Esta semana",
                        vm -> isWithinDays(vm.getDate(), 7)),
                FilterCriteria.of("periodo_mes",   "Este mes",
                        vm -> isCurrentMonth(vm.getDate())),
                FilterCriteria.of("periodo_anio",  "Este año",
                        vm -> isCurrentYear(vm.getDate()))
        ));

        return groups;
    }

    public static boolean textMatch(SaleViewModel vm, String query) {
        return contains(vm.getBillNumber(),   query)
            || contains(vm.getClientName(),   query);
    }
    
    private static boolean isSameDay(Date date, LocalDate today) {
        if (date == null) return false;
        return date.toLocalDate().isEqual(today);
    }

    private static boolean isWithinDays(Date date, int days) {
        if (date == null) return false;
        LocalDate cutoff = LocalDate.now().minusDays(days);
        return !date.toLocalDate().isBefore(cutoff);
    }

    private static boolean isCurrentMonth(Date date) {
        if (date == null) return false;
        LocalDate d = date.toLocalDate();
        LocalDate now = LocalDate.now();
        return d.getYear() == now.getYear() && d.getMonth() == now.getMonth();
    }

    private static boolean isCurrentYear(Date date) {
        if (date == null) return false;
        return date.toLocalDate().getYear() == LocalDate.now().getYear();
    }

    private static boolean contains(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }

    private static String formatMethod(PaymentMethod method) {
        switch (method) {
            case EFECTIVO:
                return "Efectivo";
            case TRANSFERENCIA:
                return "Transferencia";
            case DEBITO:
                return "Débito";
            case CREDITO:
                return "Crédito";
            default:
                throw new IllegalArgumentException("Método de pago no soportado: " + method);
        }
    }
}
