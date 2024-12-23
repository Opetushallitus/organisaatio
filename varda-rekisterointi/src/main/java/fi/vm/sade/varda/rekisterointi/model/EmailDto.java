package fi.vm.sade.varda.rekisterointi.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EmailDto {

    public final List<EmailRecipientDto> recipient;
    public final EmailMessageDto email;

    private EmailDto(List<EmailRecipientDto> recipient, EmailMessageDto email) {
        this.recipient = recipient;
        this.email = email;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private final List<EmailRecipientDto> recipientList = new ArrayList<>();
        private EmailMessageDto message;

        public Builder email(String email) {
            return recipient(new EmailRecipientDto(email));
        }

        public Builder recipient(EmailRecipientDto recipient) {
            recipientList.add(recipient);
            return this;
        }

        public Builder emails(Collection<String> emails) {
            emails.forEach(this::email);
            return this;
        }

        public Builder message(EmailMessageDto message) {
            this.message = message;
            return this;
        }

        public EmailDto build() {
            return new EmailDto(recipientList, message);
        }

    }

}
