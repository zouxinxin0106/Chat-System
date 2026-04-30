package com.chat.proto;

public final class MessageContent {
    private final TextContent textContent;
    private final ImageContent imageContent;
    private final VoiceContent voiceContent;
    private final AttendanceContent attendanceContent;
    private final ReadReceiptContent readReceiptContent;
    private final int contentCase;

    private MessageContent(TextContent t, ImageContent i, VoiceContent v, AttendanceContent a, ReadReceiptContent r, int case_) {
        this.textContent = t; this.imageContent = i; this.voiceContent = v;
        this.attendanceContent = a; this.readReceiptContent = r; this.contentCase = case_;
    }

    public static MessageContent ofText(TextContent t) { return new MessageContent(t, null, null, null, null, 1); }
    public static MessageContent ofImage(ImageContent i) { return new MessageContent(null, i, null, null, null, 2); }
    public static MessageContent ofVoice(VoiceContent v) { return new MessageContent(null, null, v, null, null, 3); }
    public static MessageContent ofAttendance(AttendanceContent a) { return new MessageContent(null, null, null, a, null, 4); }
    public static MessageContent ofReadReceipt(ReadReceiptContent r) { return new MessageContent(null, null, null, null, r, 5); }

    public int getContentCase() { return contentCase; }
    public TextContent getText() { return textContent; }
    public ImageContent getImage() { return imageContent; }
    public VoiceContent getVoice() { return voiceContent; }
    public AttendanceContent getAttendance() { return attendanceContent; }
    public ReadReceiptContent getReadReceipt() { return readReceiptContent; }
}
