package holt.test.casestudy.policy;

import holt.padfd.Policy;

public enum AccessUserReason implements Policy, Agreement {
    MARKETING, RESET_PASSWORD, DELETE;
}
