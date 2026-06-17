package fi.vm.sade.organisaatio.config;

import tools.jackson.databind.AnnotationIntrospector;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.AccessorNamingStrategy;
import tools.jackson.databind.introspect.AnnotatedClass;
import tools.jackson.databind.introspect.DefaultAccessorNamingStrategy;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

public class Jackson2AccessorNamingStrategy extends DefaultAccessorNamingStrategy {

    protected Jackson2AccessorNamingStrategy(MapperConfig<?> config, AnnotatedClass forClass,
            String mutatorPrefix, String getterPrefix, String isGetterPrefix,
            BaseNameValidator baseNameValidator) {
        super(config, forClass, mutatorPrefix, getterPrefix, isGetterPrefix, baseNameValidator);
    }

    @Override
    protected String stdManglePropertyName(String basename, int offset) {
        int end = basename.length();
        if (end == offset) {
            return null;
        }
        char c = basename.charAt(offset);
        if (_baseNameValidator != null && !_baseNameValidator.accept(c, basename, offset)) {
            return null;
        }

        char d = Character.toLowerCase(c);
        if (c == d) {
            return basename.substring(offset);
        }

        StringBuilder sb = new StringBuilder(end - offset);
        sb.append(d);
        int i = offset + 1;
        for (; i < end; ++i) {
            c = basename.charAt(i);
            d = Character.toLowerCase(c);
            if (c == d) {
                sb.append(basename, i, end);
                break;
            }
            sb.append(d);
        }
        return sb.toString();
    }

    public static class Provider extends DefaultAccessorNamingStrategy.Provider {
        @Override
        public AccessorNamingStrategy forPOJO(MapperConfig<?> config, AnnotatedClass targetClass) {
            return new Jackson2AccessorNamingStrategy(config, targetClass,
                    _setterPrefix, _getterPrefix, _isGetterPrefix, _baseNameValidator);
        }

        @Override
        public AccessorNamingStrategy forBuilder(MapperConfig<?> config,
                AnnotatedClass builderClass, BeanDescription valueTypeDesc) {
            AnnotationIntrospector annotationIntrospector = config.getAnnotationIntrospector();
            if (valueTypeDesc != null) {
                String prefix = annotationIntrospector.findBuilderPrefix(config, valueTypeDesc.getClassInfo());
                if (prefix != null) {
                    return new Jackson2AccessorNamingStrategy(config, builderClass,
                            prefix, _getterPrefix, _isGetterPrefix, _baseNameValidator);
                }
            }

            String mutatorPrefix = _withPrefix;
            JsonPOJOBuilder.Value builderConfig = annotationIntrospector.findPOJOBuilderConfig(config, builderClass);
            if (builderConfig != null) {
                mutatorPrefix = builderConfig.withPrefix;
            }

            return new Jackson2AccessorNamingStrategy(config, builderClass,
                    mutatorPrefix, _getterPrefix, _isGetterPrefix, _baseNameValidator);
        }
    }
}
