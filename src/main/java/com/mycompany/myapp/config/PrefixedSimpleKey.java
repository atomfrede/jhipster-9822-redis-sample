package com.mycompany.myapp.config;

import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Arrays;

public class PrefixedSimpleKey implements Serializable {

    private final String prefix;
    private final Object[] params;
    private final int hashCode;

    public PrefixedSimpleKey(String prefix, Object... elements) {
        Assert.notNull(prefix, "Prefix must not be null");
        Assert.notNull(elements, "Elements must not be null");
        this.prefix = prefix;
        this.params = new Object[elements.length];
        System.arraycopy(elements, 0, this.params, 0, elements.length);
        this.hashCode = prefix.hashCode() + Arrays.deepHashCode(this.params);
    }

    @Override
    public boolean equals(Object other) {
        return (this == other ||
            (other instanceof PrefixedSimpleKey && this.prefix.equals(((PrefixedSimpleKey) other).prefix) && Arrays.deepEquals(this.params, ((PrefixedSimpleKey) other).params)));
    }

    @Override
    public final int hashCode() {
        return this.hashCode;
    }

    @Override
    public String toString() {
        return prefix + " " + getClass().getSimpleName() + " [" + StringUtils.arrayToCommaDelimitedString(this.params) + "]";
    }

}
