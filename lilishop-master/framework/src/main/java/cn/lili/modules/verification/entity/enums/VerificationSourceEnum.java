package cn.lili.modules.verification.entity.enums;

import java.util.Locale;

/**
 * 验证码资源枚举
 *
 * @author Chopper
 * @since 2021/1/26 15:55
 */
public enum VerificationSourceEnum {
    /**
     * 滑块
     */
    SLIDER("滑块"),
    /**
     * 验证码源
     */
    RESOURCE("验证码源");

    private final String description;

    VerificationSourceEnum(String des) {
        this.description = des;
    }

    public static String normalizeType(String type) {
        if (type == null) {
            return null;
        }
        String normalized = type.trim().toUpperCase(Locale.ROOT);
        if ("SOURCE".equals(normalized) || RESOURCE.name().equals(normalized)) {
            return RESOURCE.name();
        }
        if (SLIDER.name().equals(normalized)) {
            return SLIDER.name();
        }
        return normalized;
    }

    public static boolean isResourceType(String type) {
        return RESOURCE.name().equals(normalizeType(type));
    }

    public static boolean isSliderType(String type) {
        return SLIDER.name().equals(normalizeType(type));
    }
}
