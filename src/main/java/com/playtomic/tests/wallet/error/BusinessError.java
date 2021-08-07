package com.playtomic.tests.wallet.error;

import com.playtomic.tests.wallet.exception.BusinessException;

public class BusinessError extends RestError {
    private String messageKey;
    private String[] arguments;

    private BusinessError() {
    }

    public BusinessError(String messageKey) {
        this.messageKey = messageKey;
    }

    public BusinessError(String messageKey, String... arguments) {
        this.messageKey = messageKey;
        this.arguments = arguments;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String[] getArguments() {
        return arguments;
    }

    @Override
    public BusinessException getException() {
        return new BusinessException(messageKey, arguments);
    }
}
