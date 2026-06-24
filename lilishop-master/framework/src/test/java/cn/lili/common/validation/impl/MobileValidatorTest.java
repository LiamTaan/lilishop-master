package cn.lili.common.validation.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MobileValidatorTest {

    private final MobileValidator validator = new MobileValidator();

    @Test
    void shouldAllowNullOrBlankValue() {
        Assertions.assertTrue(validator.isValid(null, null));
        Assertions.assertTrue(validator.isValid("", null));
        Assertions.assertTrue(validator.isValid("   ", null));
    }

    @Test
    void shouldValidateFilledValueByPattern() {
        Assertions.assertTrue(validator.isValid("13800138000", null));
        Assertions.assertFalse(validator.isValid("abc", null));
    }
}
