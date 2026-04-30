package com.chat.service.model;

public final class TextContent {
    private final String text;

    public TextContent(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text cannot be null");
        }
        this.text = text;
    }

    public String text() { return text; }
}
