package com.hibernate.learning.inheritance.singletable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * Bank Transfer Payment implementation using SINGLE_TABLE inheritance strategy.
 * <p>
 * In SINGLE_TABLE strategy, this class's fields will be added as columns to the base
 * Payment table. The discriminator value "BANK_TRANSFER" will be used to identify
 * rows belonging to this type of payment.
 * <p>
 * Note that in SINGLE_TABLE strategy:
 * - All columns for this class must allow NULL values since they're in the shared table
 * - The discriminator column will contain "BANK_TRANSFER" for these payments
 * - No separate table is created for this entity
 * <p>
 * Security Consideration:
 * - Bank account data shares the same table as other payment types
 * - Consider additional security measures or encryption for sensitive fields
 * <p>
 * Annotations explained:
 *
 * @Data - Lombok annotation to generate getters, setters, equals, hashCode and toString
 * @EqualsAndHashCode - Lombok annotation to generate equals and hashCode methods
 * @NoArgsConstructor - Lombok annotation to generate a no-args constructor
 * @Entity - Marks this class as a JPA entity
 * @DiscriminatorValue - Specifies the value that identifies this class in the discriminator column
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
@DiscriminatorValue("BANK_TRANSFER")
public class BankTransferPayment extends Payment {

    /**
     * Name of the bank
     */
    @NotBlank(message = "Bank name is required")
    @Column(name = "bank_name")
    private String bankName;

    /**
     * Account number at the bank
     */
    @NotBlank(message = "Account number is required")
    @Column(name = "account_number")
    private String accountNumber;

    /**
     * International Bank Account Number
     */
    @NotBlank(message = "IBAN is required")
    @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$", message = "Invalid IBAN format")
    @Column(name = "iban")
    private String iban;

    /**
     * SWIFT code for international transfers
     */
    @NotBlank(message = "SWIFT code is required")
    @Pattern(regexp = "^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$", message = "Invalid SWIFT code format")
    @Column(name = "swift_code")
    private String swiftCode;

    /**
     * Routing number for the bank
     */
    @NotBlank(message = "Routing number is required")
    @Column(name = "routing_number")
    private String routingNumber;

    /**
     * Reference number for the transfer
     */
    @Column(name = "reference_number")
    private String referenceNumber;

    @Override
    public void validate() {
        if (bankName == null || bankName.trim().isEmpty()) {
            throw new IllegalStateException("Bank name is required");
        }

        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalStateException("Account number is required");
        }

        if (iban == null || !iban.matches("^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$")) {
            throw new IllegalStateException("Invalid IBAN format");
        }

        if (swiftCode == null || !swiftCode.matches("^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$")) {
            throw new IllegalStateException("Invalid SWIFT code format");
        }

        if (routingNumber == null || routingNumber.trim().isEmpty()) {
            throw new IllegalStateException("Routing number is required");
        }
    }

    @Override
    protected void processInternal() {
        // Simulate bank transfer processing
        try {
            Thread.sleep(2000); // Bank transfers typically take longer
            // In a real implementation, this would integrate with a banking system
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Payment processing interrupted");
        }
    }
}
