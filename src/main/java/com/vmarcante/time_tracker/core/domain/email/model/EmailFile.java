package com.vmarcante.time_tracker.core.domain.email.model;

public record EmailFile(
        String name,
        String mimeType,
        byte[] binary,
        boolean isInline,
        String contentId) {

    public EmailFile {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        if (mimeType == null || mimeType.isBlank()) {
            throw new IllegalArgumentException("MIME type cannot be null or empty");
        }
        if (binary == null || binary.length == 0) {
            throw new IllegalArgumentException("File binary content cannot be null or empty");
        }
        if (isInline && (contentId == null || contentId.isBlank())) {
            throw new IllegalArgumentException("Content ID is required for inline attachments");
        }
    }

    public EmailFile(String name, String mimeType, byte[] binary) {
        this(name, mimeType, binary, false, null);
    }

    public static EmailFile inline(String name, String mimeType, byte[] binary, String contentId) {
        return new EmailFile(name, mimeType, binary, true, contentId);
    }

    public long size() {
        return binary.length;
    }
}
