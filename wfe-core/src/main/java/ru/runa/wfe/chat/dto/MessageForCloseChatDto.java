package ru.runa.wfe.chat.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

public class MessageForCloseChatDto extends ChatDto {
    private Long processId;
    private boolean mentionedFlag = false;
    private boolean coreUserFlag = false;

    @JsonGetter("processId")
    public Long getCurrentMessageId() {
        return processId;
    }

    @JsonSetter("processId")
    public void setCurrentMessageId(Long currentMessageId) {
        this.processId = processId;
    }

    @JsonGetter("mentioned")
    public boolean isMentioned() {
        return mentionedFlag;
    }

    public void setMentionedFlag(boolean mentionedFlag) {
        this.mentionedFlag = mentionedFlag;
    }

    @JsonGetter("coreUser")
    public boolean isCoreUser() {
        return coreUserFlag;
    }

    public void setCoreUserFlag(boolean coreUserFlag) {
        this.coreUserFlag = coreUserFlag;
    }

}