package com.devit.mscore.util;

import java.lang.reflect.Type;

@Deprecated
@FunctionalInterface
public interface FromMapper {
    <T> T map(String value, Type type);
}