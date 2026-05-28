/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.util;

import java.util.function.BiConsumer;
import java.util.function.Predicate;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

/**
 *
 * @author lucas
 */
public class EditableCellFactory<S,T> implements Callback<TableColumn<S,T>, TableCell<S,T>> {
    private final StringConverter<T> converter;
    private final BiConsumer<T, String> validator;
    private final BiConsumer<S, T> propertyUpdater;
    
    public EditableCellFactory(StringConverter<T> converter, BiConsumer<T, String> validator, BiConsumer<S, T> propertyUpdater) {
        this.converter = converter;
        this.validator = validator;
        this.propertyUpdater = propertyUpdater;
    }

    @Override
    public TableCell<S, T> call(TableColumn<S, T> column) {
        return new TextFieldTableCell<S, T>(converter) {
            @Override
            public void commitEdit(T newValue) {
                try {
                    if (newValue == null) {
                        cancelEdit();
                        return;
                    }
                    
                    validator.accept(newValue, column.getText());
                    S item = getTableRow().getItem();
                    
                    super.commitEdit(newValue);
                    
                    if (item != null) {
                        propertyUpdater.accept(item, newValue);
                    }
                    
                } catch (IllegalArgumentException ex) {
                    AlertUtils.showError(ex.getMessage());
                }
            }
        };
    }
    
    //Factory metodos estaticos para casos comunes
    
    public static <S> EditableCellFactory <S, String> forString(
            int minLength, 
            int maxLength, 
            BiConsumer<S, String> propertyUpdater
    ) {
        BasicStringValidator validator = new BasicStringValidator();
        return new EditableCellFactory<>(new DefaultStringConverter(), 
                (value, fieldName) -> validator.validate(value, minLength, maxLength, fieldName), propertyUpdater);
    }
    
    public static <S> EditableCellFactory<S, Number> forNumber(
            Predicate<Float> validator,
            String errorMessage,
            BiConsumer<S, Number> propertyUpdater,
            boolean isFloat
    ) {
        return new EditableCellFactory<>(
                isFloat ? createFloatConverter() : createIntegerConverter(),
                (value, fieldName) -> {
                    if (!validator.test(value.floatValue())) {
                        throw new IllegalArgumentException(errorMessage);
                    }
                },
                propertyUpdater
        );
    }
    
    private static StringConverter<Number> createFloatConverter() {
        return new StringConverter<Number>() {
            @Override
            public String toString(Number number) {
                return number != null ? String.format("$%.2f", number.floatValue()) : "";
            }

            @Override
            public Number fromString(String string) {
                if (string == null || string.trim().isEmpty()) {
                    return 0;
                }
                string = string.replace("$", "").trim();
                try {
                    return Float.valueOf(string);
                } catch (NumberFormatException ex) {
                    AlertUtils.showError("Formato de numero inválido.\n\n*Sólo use el punto (.) para decimales.*\n*No use el punto (.) o la coma (,) para separar miles.*");
                    return null;
                }
            }
        };
    }
    
    private static StringConverter<Number> createIntegerConverter() {
        return new StringConverter<Number>() {
            @Override
            public String toString(Number number) {
                if (number == null) {
                    return "";
                }
                return String.valueOf(number.intValue());
            }

            @Override
            public Number fromString(String string) {
                if (string == null || string.trim().isEmpty()) {
                    return 0;
                }
                try {
                    return Integer.valueOf(string.trim());
                } catch (NumberFormatException ex) {
                    AlertUtils.showError("Formato de numero inválido.\n\n*Sólo se admiten números enteros*");
                    return null;
                }
            }
        };
    }
}
