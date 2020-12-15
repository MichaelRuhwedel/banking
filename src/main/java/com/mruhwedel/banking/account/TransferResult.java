package com.mruhwedel.banking.account;

public enum TransferResult {
    TRANSFERRED,
    INSUFFICIENT_FUNDS,
    CANNOT_WITHDRAW_FROM_LOAN,
    INVALID_ACCOUNT_TARGET,
    ACCOUNT_NONEXISTENT
}
