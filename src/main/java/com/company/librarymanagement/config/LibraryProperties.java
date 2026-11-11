package com.company.librarymanagement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "library")
public record LibraryProperties(int loanDays, int rareLoanDays, BigDecimal finePerDay) {
}
