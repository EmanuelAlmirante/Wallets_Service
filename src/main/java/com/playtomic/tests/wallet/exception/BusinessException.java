package com.playtomic.tests.wallet.exception;

import java.util.Arrays;

public class BusinessException extends RuntimeException {
    private String messageKey;
    private String[] arguments;

    private BusinessException() {
    }

    public BusinessException(String messageKey, String... arguments) {
        super(messageKey);
        this.messageKey = messageKey;
        this.arguments = arguments == null ? new String[0] : arguments;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String[] getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return super.toString() + ", arguments=" + Arrays.toString(arguments);
    }
}
