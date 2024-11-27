package com.hibernate.learning.inheritance.joined;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * Credit Card Payment implementation using JOINED inheritance strategy.
 */
@Entity
@Table(name = "CREDIT_CARD_PAYMENT")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CreditCardPayment extends Payment {

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
    @Column(name = "card_number", nullable = false)
    private String cardNumber;

    @NotBlank(message = "Card holder name is required")
    @Column(name = "card_holder_name", nullable = false)
    private String cardHolderName;

    @NotBlank(message = "Expiry date is required")
    @Pattern(regexp = "(?:0[1-9]|1[0-2])/[0-9]{2}", message = "Expiry date must be in MM/YY format")
    @Column(name = "expiry_date", nullable = false)
    private String expiryDate;

    @Column(name = "expiration_month", nullable = false)
    private int expirationMonth;

    @Column(name = "expiration_year", nullable = false)
    private int expirationYear;

    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "\\d{3}", message = "CVV must be 3 digits")
    @Column(name = "cvv", nullable = false)
    private String cvv;

    @Override
    public void validate() {
        super.validate();

        if (cardNumber == null || !cardNumber.matches("\\d{16}")) {
            throw new IllegalStateException("Invalid card number");
        }

        if (cardHolderName == null || cardHolderName.trim().length() < 2) {
            throw new IllegalStateException("Invalid card holder name");
        }

        if (expiryDate == null || !expiryDate.matches("(?:0[1-9]|1[0-2])/[0-9]{2}")) {
            throw new IllegalStateException("Invalid expiry date");
        }

        if (expirationMonth < 1 || expirationMonth > 12) {
            throw new IllegalStateException("Invalid expiration month");
        }

        if (expirationYear < 0) {
            throw new IllegalStateException("Invalid expiration year");
        }

        if (cvv == null || !cvv.matches("\\d{3}")) {
            throw new IllegalStateException("Invalid CVV");
        }
    }

    @Override
    protected void processInternal() {
        // Simulate credit card processing
        try {
            Thread.sleep(1000); // Simulate processing time
            if (cardNumber.endsWith("0000")) {
                throw new RuntimeException("Test failure scenario");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Payment processing interrupted", e);
        }
    }
}
