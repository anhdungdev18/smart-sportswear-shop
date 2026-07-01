package com.dunghaiquyen.ecommerce.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * For PATCH DTOs: null means "field not sent, leave unchanged" (must stay
 * valid, unlike {@code @NotBlank} which rejects null). But if the field IS
 * sent, it must not be blank after trim - otherwise the service's existing
 * "if (request.x() != null) entity.setX(request.x().trim())" pattern happily
 * writes an empty string to a column that should never be empty.
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR,
        ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NullOrNotBlankValidator.class)
public @interface NullOrNotBlank {

    String message() default "must not be blank";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
