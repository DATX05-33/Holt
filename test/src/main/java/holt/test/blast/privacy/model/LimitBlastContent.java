package holt.test.blast.privacy.model;

import holt.test.blast.model.EmailContent;

public record LimitBlastContent(EmailContent emailContent, String policy, boolean verdict) {
}
