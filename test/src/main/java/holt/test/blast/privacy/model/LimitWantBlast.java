package holt.test.blast.privacy.model;

import holt.test.blast.model.EmailContent;

public record LimitWantBlast(EmailContent emailContent, String policy, boolean verdict) {
}
