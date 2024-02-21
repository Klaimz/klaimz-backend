package com.klaimz.util;

import com.klaimz.model.Claim;
import com.klaimz.model.FormField;

import java.util.List;

public final class Constants {

    public static List<FormField> FORM_FIELDS = List.of(
            FormField.builder()
                    .key("text")
                    .defaultName("Text")
                    .regex(".*")
                    .build(),
            FormField.builder()
                    .key("number")
                    .defaultName("Number")
                    .regex("\\d+")
                    .build(),
            FormField.builder()
                    .key("date")
                    .defaultName("Date")
                    .regex("\\d{4}-\\d{2}-\\d{2}")
                    .build(),
            FormField.builder()
                    .key("image")
                    .defaultName("Images")
                    .regex("")
                    .build()
    );
}
