package com.brettonw.servlet;

import java.io.IOException;

@FunctionalInterface
public interface Handler<Type> {
    void handle (Type t) throws IOException;
}
