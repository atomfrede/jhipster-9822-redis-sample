package com.mycompany.myapp.config;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;


public class JHipsterKeyGenerator implements KeyGenerator {

    private String prefix;

    public JHipsterKeyGenerator(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Object generate(Object target, Method method, Object... params) {
        return new PrefixedSimpleKey(prefix, params);

//        return prefix + target.getClass().getSimpleName() + "_"
//            + method.getName() + "_"
//            + StringUtils.arrayToDelimitedString(params, "_");
    }
}
