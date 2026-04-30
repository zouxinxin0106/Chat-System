package com.chat.service.model;

public final class ProcessedMessage {
    private ChatMessage message;
    private long sequenceId;
    private String correlationId;
    private boolean valid = true;
    private String validationError;
    private boolean authorized = true;
    private String authorizationError;

    public ProcessedMessage() {}

    public ChatMessage getMessage() { return message; }
    public void setMessage(ChatMessage message) { this.message = message; }

    public long getSequenceId() { return sequenceId; }
    public void setSequenceId(long sequenceId) { this.sequenceId = sequenceId; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public String getValidationError() { return validationError; }
    public void setValidationError(String validationError) { this.validationError = validationError; }

    public boolean isAuthorized() { return authorized; }
    public void setAuthorized(boolean authorized) { this.authorized = authorized; }

    public String getAuthorizationError() { return authorizationError; }
    public void setAuthorizationError(String authorizationError) { this.authorizationError = authorizationError; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private ChatMessage message;
        private long sequenceId;
        private String correlationId;
        private boolean valid = true;
        private String validationError;
        private boolean authorized = true;
        private String authorizationError;

        public Builder message(ChatMessage message) { this.message = message; return this; }
        public Builder sequenceId(long sequenceId) { this.sequenceId = sequenceId; return this; }
        public Builder correlationId(String correlationId) { this.correlationId = correlationId; return this; }
        public Builder valid(boolean valid) { this.valid = valid; return this; }
        public Builder validationError(String error) { this.validationError = error; return this; }
        public Builder authorized(boolean authorized) { this.authorized = authorized; return this; }
        public Builder authorizationError(String error) { this.authorizationError = error; return this; }

        public ProcessedMessage build() {
            ProcessedMessage pm = new ProcessedMessage();
            pm.message = this.message;
            pm.sequenceId = this.sequenceId;
            pm.correlationId = this.correlationId;
            pm.valid = this.valid;
            pm.validationError = this.validationError;
            pm.authorized = this.authorized;
            pm.authorizationError = this.authorizationError;
            return pm;
        }
    }
}
