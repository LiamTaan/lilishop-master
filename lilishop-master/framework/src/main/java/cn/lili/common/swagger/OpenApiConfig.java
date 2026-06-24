package cn.lili.common.swagger;

import cn.lili.common.vo.PageVO;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springdoc.core.customizers.PropertyCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String ENUM_STRING_HINT = "请传字符串枚举值，不要传 Swagger 列表前面的数字序号。";

    @Bean
    public OpenAPI openAPI(@Value("${spring.application.name:lilishop}") String appName) {
        return new OpenAPI().info(new Info().title(appName).version("v3"));
    }

    @Bean
    public PropertyCustomizer globalSwaggerPropertyCustomizer() {
        return (schema, type) -> {
            if (schema == null || type == null) {
                return schema;
            }

            Type javaType = resolveType(type);
            if (javaType instanceof Class<?> clazz) {
                if (clazz.isEnum()) {
                    appendDescription(schema, ENUM_STRING_HINT);
                }
                if (Boolean.class.equals(clazz) || boolean.class.equals(clazz)) {
                    appendDescription(schema, "布尔值字段，请传 true/false。");
                }
            }

            return schema;
        };
    }

    @Bean
    public ParameterCustomizer globalSwaggerParameterCustomizer() {
        return (parameter, methodParameter) -> {
            if (parameter == null || methodParameter == null) {
                return parameter;
            }

            Class<?> parameterType = methodParameter.getParameterType();
            if (parameterType != null && parameterType.isEnum()) {
                appendDescription(parameter, ENUM_STRING_HINT);
            }

            if (PageVO.class.isAssignableFrom(methodParameter.getContainingClass())) {
                appendPageParameterDescription(parameter);
            }

            return parameter;
        };
    }

    @Bean
    public OpenApiCustomizer globalSwaggerOpenApiCustomizer() {
        return openApi -> {
            if (openApi == null || openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
                return;
            }

            Schema<?> resultMessageSchema = openApi.getComponents().getSchemas().get("ResultMessage");
            if (resultMessageSchema != null && resultMessageSchema.getProperties() != null) {
                describeResultMessage(resultMessageSchema);
            }

            Schema<?> pageVoSchema = openApi.getComponents().getSchemas().get("PageVO");
            if (pageVoSchema != null && pageVoSchema.getProperties() != null) {
                describePageVo(pageVoSchema);
            }

            Schema<?> tokenSchema = openApi.getComponents().getSchemas().get("Token");
            if (tokenSchema != null && tokenSchema.getProperties() != null) {
                describeToken(tokenSchema);
            }

            for (Schema<?> schema : openApi.getComponents().getSchemas().values()) {
                if (schema != null) {
                    walkSchema(schema);
                }
            }
        };
    }

    private void describeResultMessage(Schema<?> schema) {
        appendDescription(schema, "统一响应包装。绝大多数接口都返回该结构。");
        describeProperty(schema, "success", "业务是否成功。true 表示本次业务成功，false 表示业务失败。");
        describeProperty(schema, "message", "业务提示文案。失败时优先结合 code 一起判断，不要只依赖文案。");
        describeProperty(schema, "code", "统一业务状态码。200 表示成功，其他值表示具体业务失败原因。");
        describeProperty(schema, "timestamp", "服务端响应时间戳，毫秒。");
        describeProperty(schema, "result", "真实业务数据载荷。不同接口的 result 结构不同。");
    }

    private void describePageVo(Schema<?> schema) {
        appendDescription(schema, "通用分页查询参数。");
        describeProperty(schema, "pageNumber", "页码，从 1 开始。", 1);
        describeProperty(schema, "pageSize", "每页条数。", 10);
        describeProperty(schema, "sort", "排序字段。默认按项目约定进行驼峰转下划线。", "createTime");
        describeProperty(schema, "order", "排序方向，传 asc 或 desc。", "desc");
        describeProperty(schema, "notConvert", "是否关闭排序字段的驼峰转下划线。true 表示按原字段名直接排序。", false);
    }

    private void describeToken(Schema<?> schema) {
        appendDescription(schema, "登录成功后返回的 token 结构。");
        describeProperty(schema, "accessToken", "访问令牌。后续请求通常放在请求头 Authorization 中。");
        describeProperty(schema, "refreshToken", "刷新令牌。accessToken 过期后用于换取新 token。");
    }

    private void walkSchema(Schema<?> schema) {
        if (schema.getEnum() != null && !schema.getEnum().isEmpty()) {
            appendDescription(schema, ENUM_STRING_HINT);
        }

        if (schema.getProperties() != null) {
            for (Object propertySchema : schema.getProperties().values()) {
                if (propertySchema instanceof Schema<?> nestedSchema) {
                    walkSchema(nestedSchema);
                }
            }
        }

        Schema<?> items = schema.getItems();
        if (items != null) {
            walkSchema(items);
        }

        List<Schema> oneOf = schema.getOneOf();
        if (oneOf != null) {
            for (Schema<?> nestedSchema : oneOf) {
                walkSchema(nestedSchema);
            }
        }

        List<Schema> anyOf = schema.getAnyOf();
        if (anyOf != null) {
            for (Schema<?> nestedSchema : anyOf) {
                walkSchema(nestedSchema);
            }
        }

        List<Schema> allOf = schema.getAllOf();
        if (allOf != null) {
            for (Schema<?> nestedSchema : allOf) {
                walkSchema(nestedSchema);
            }
        }
    }

    private void appendPageParameterDescription(Parameter parameter) {
        if ("pageNumber".equals(parameter.getName())) {
            appendDescription(parameter, "页码，从 1 开始。");
            parameter.setExample("1");
        } else if ("pageSize".equals(parameter.getName())) {
            appendDescription(parameter, "每页条数。");
            parameter.setExample("10");
        } else if ("sort".equals(parameter.getName())) {
            appendDescription(parameter, "排序字段。默认按驼峰字段自动转下划线。");
            parameter.setExample("createTime");
        } else if ("order".equals(parameter.getName())) {
            appendDescription(parameter, "排序方向，传 asc 或 desc。");
            parameter.setExample("desc");
        } else if ("notConvert".equals(parameter.getName())) {
            appendDescription(parameter, "是否关闭排序字段的驼峰转下划线。传 true/false。");
            parameter.setExample("false");
        }
    }

    private void describeProperty(Schema<?> parentSchema, String propertyName, String description) {
        describeProperty(parentSchema, propertyName, description, null);
    }

    private void describeProperty(Schema<?> parentSchema, String propertyName, String description, Object example) {
        Object propertySchema = parentSchema.getProperties().get(propertyName);
        if (propertySchema instanceof Schema<?> schema) {
            appendDescription(schema, description);
            if (example != null && schema.getExample() == null) {
                schema.setExample(example);
            }
        }
    }

    private void appendDescription(Schema<?> schema, String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        if (schema.getDescription() == null || schema.getDescription().isBlank()) {
            schema.setDescription(text);
            return;
        }
        if (!schema.getDescription().contains(text)) {
            schema.setDescription(schema.getDescription() + " " + text);
        }
    }

    private void appendDescription(Parameter parameter, String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        if (parameter.getDescription() == null || parameter.getDescription().isBlank()) {
            parameter.setDescription(text);
            return;
        }
        if (!parameter.getDescription().contains(text)) {
            parameter.setDescription(parameter.getDescription() + " " + text);
        }
    }

    private Type resolveType(AnnotatedType annotatedType) {
        if (annotatedType.getType() != null) {
            return annotatedType.getType();
        }
        if (annotatedType.getCtxAnnotations() != null && annotatedType.getCtxAnnotations().length > 0) {
            return annotatedType.getCtxAnnotations()[0].annotationType();
        }
        return null;
    }
}
