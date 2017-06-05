package com.chen.plugin.reflect;

/**
 * Created by chenzhaohua on 17/5/16.
 */

public class Validate {

    static void isTrue(final boolean expression, final String message, final Object... values) {
        if (expression == false) {
            throw new IllegalArgumentException(String.format(message, values));
        }
    }

}
