package com.chat.service.model;

public final class ImageContent {
    private final String url;
    private final int width;
    private final int height;
    private final String thumbnailUrl;

    public ImageContent(String url, int width, int height, String thumbnailUrl) {
        if (url == null) {
            throw new IllegalArgumentException("url cannot be null");
        }
        this.url = url;
        this.width = width;
        this.height = height;
        this.thumbnailUrl = thumbnailUrl;
    }

    public String url() { return url; }
    public int width() { return width; }
    public int height() { return height; }
    public String thumbnailUrl() { return thumbnailUrl; }
}
