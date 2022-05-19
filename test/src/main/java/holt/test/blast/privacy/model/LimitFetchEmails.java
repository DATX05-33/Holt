package holt.test.blast.privacy.model;

import holt.test.blast.model.Emails;

public record LimitFetchEmails(Emails emails, String policy, boolean verdict) {
}
