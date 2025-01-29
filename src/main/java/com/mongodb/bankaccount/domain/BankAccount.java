package com.mongodb.bankaccount.domain;

import com.mongodb.bankaccount.application.web.BankResponse;
import com.mongodb.bankaccount.resources.BankAccountEntity;

public record BankAccount(
    String accountHolderName,
    String accountNumber,
    int cardVerificationCode,
    Double accountBalance,
    String phoneNumber) {
    public BankResponse toResponse() {
        return new BankResponse(accountHolderName, accountNumber, cardVerificationCode, accountBalance, phoneNumber);
    }

    public BankAccountEntity toEntity() {
        return new BankAccountEntity(accountHolderName, accountNumber, cardVerificationCode, accountBalance, phoneNumber);
    }
}