package com.klaimz.model.api;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Singleton;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Introspected
public class Filter {

    @NotBlank
    private String field;

    @Nullable
    private String value;

    private Range range;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Introspected
    public static class Range {

        @Positive
        private long from;
        @Positive
        private long to;
    }

    @Constraint(validatedBy = FilterValidator.class)
    @Target({ElementType.TYPE,ElementType.TYPE_USE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface ValidatedFilter {

        String message() default "Either value or range must be present";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }

    @Singleton
    @Introspected
    static class FilterValidator implements ConstraintValidator<ValidatedFilter, Filter> {

        @Override
        public boolean isValid(Filter filter, ConstraintValidatorContext context) {
            var isValuePresent = filter.getValue() != null && !filter.getValue().isBlank();
            var isRangePresent = filter.getRange() != null && filter.getRange().getFrom() > 0 && filter.getRange().getTo() > 0;

            //make sure that either value or range is present and not both
            var isValueOrRangePresent = isValuePresent ^ isRangePresent;

            if (!isValueOrRangePresent) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Either value or range must be present")
                        .addPropertyNode("value")
                        .addConstraintViolation();
            }

            return isValueOrRangePresent;
        }
    }
}

