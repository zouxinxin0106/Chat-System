package com.chat.proto;

public final class ImageContent {
    private final String url;
    private final int width;
    private final int height;
    private final String thumbnailUrl;
    public ImageContent(String url, int width, int height, String thumbnailUrl) {
        this.url = url; this.width = width; this.height = height; this.thumbnailUrl = thumbnailUrl;
    }
    public String getUrl() { return url; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getThumbnailUrl() { return thumbnailUrl; }
}
