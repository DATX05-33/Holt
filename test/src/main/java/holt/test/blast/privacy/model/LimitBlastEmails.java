package holt.test.blast.privacy.model;

import holt.test.blast.model.Emails;

public record LimitBlastEmails(Emails emails, String policy, boolean verdict) {
}
