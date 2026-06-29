package com.dunghaiquyen.ecommerce.common.validation;

public final class Patterns {

    /** lowercase letters/digits, hyphen-separated, no leading/trailing/double hyphens. */
    public static final String SLUG = "^[a-z0-9]+(?:-[a-z0-9]+)*$";

    private Patterns() {
    }
}
