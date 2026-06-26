package com.github.jglm_184.travel_expense_manager.util;

import org.springframework.stereotype.Component;

@Component
public class FormatterUtil {
    public String cleanNumbers(String value) {
        return value == null ? null : value.replaceAll("\\D", "");
    }
}
