package fi.vm.sade.varda.rekisterointi.model;

public class EmailMessageDto {

    public final String subject;
    public final String body;
    public final boolean html;
    public final String charset;

    public EmailMessageDto(String subject, String body, boolean html, String charset) {
        this.subject = subject;
        this.body = body;
        this.html = html;
        this.charset = charset;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String subject;
        private String body;
        private boolean html;
        private String charset = "UTF-8";

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder html(boolean html) {
            this.html = html;
            return this;
        }

        public Builder charset(String charset) {
            this.charset = charset;
            return this;
        }

        public EmailMessageDto build() {
            return new EmailMessageDto(subject, body, html, charset);
        }

    }

}
