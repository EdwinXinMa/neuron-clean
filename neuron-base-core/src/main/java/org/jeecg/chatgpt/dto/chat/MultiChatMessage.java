package org.jeecg.chatgpt.dto.chat;

import java.io.Serializable;

public class MultiChatMessage implements Serializable {
    private Role role;
    private String content;

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public static Builder builder() { return new Builder(); }

    public enum Role {
        SYSTEM, USER, ASSISTANT
    }

    public static class Builder {
        private Role role;
        private String content;

        public Builder role(Role role) { this.role = role; return this; }
        public Builder content(String content) { this.content = content; return this; }

        public MultiChatMessage build() {
            MultiChatMessage msg = new MultiChatMessage();
            msg.role = this.role;
            msg.content = this.content;
            return msg;
        }
    }
}
