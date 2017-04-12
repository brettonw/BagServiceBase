package com.brettonw.servlet;

@FunctionalInterface
public interface Handler<Type> {
    void handle (Type t);
}
