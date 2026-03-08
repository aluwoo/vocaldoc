package com.vocaldoc.common.enums;

public enum VoiceRecordType {
    VOWEL("元音"),
    DURATION("声时长"),
    SONG("完整曲目");

    private final String description;

    VoiceRecordType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
