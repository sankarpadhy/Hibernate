package com.hibernate.learning.inheritance.joined;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * Bank Transfer Payment implementation using JOINED inheritance strategy.
 * <p>
 * In JOINED strategy, this class will have its own table that contains only
 * the fields specific to bank transfer payments. The primary key of this table
 * will also be a foreign key to the base Payment table.
 * <p>
 * Note that in JOINED strategy:
 * - A separate table is created for this entity
 * - The table only contains fields specific to bank transfer payments
 * - The primary key is shared with the parent Payment table
 * <p>
 * Security Consideration:
 * - Sensitive bank data is stored in a separate table
 * - Consider additional security measures or encryption for sensitive fields
 * <p>
 * Annotations explained:
 *
 * @Data - Lombok annotation to generate getters, setters, equals, hashCode and toString
 * @EqualsAndHashCode - Lombok annotation to generate equals and hashCode methods
 * @NoArgsConstructor - Lombok annotation to generate a no-args constructor
 * @AllArgsConstructor - Lombok annotation to generate an all-args constructor
 * @Entity - Marks this class as a JPA entity
 * @PrimaryKeyJoinColumn - Specifies the foreign key column linking to parent table
 */
@Entity(name = "JoinedBankTransferPayment")
@Table(name = "bank_transfer_payment")
@PrimaryKeyJoinColumn(name = "payment_id")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class BankTransferPayment extends Payment {

    /**
     * Account number at the bank
     */
    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "\\d{10,12}", message = "Account number must be between 10 and 12 digits")
    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    /**
     * Routing number for the bank
     */
    @NotBlank(message = "Routing number is required")
    @Pattern(regexp = "\\d{9}", message = "Routing number must be 9 digits")
    @Column(name = "routing_number", nullable = false)
    private String routingNumber;

    /**
     * IBAN for international transfers
     */
    @NotBlank(message = "IBAN is required")
    @Pattern(regexp = "[A-Z]{2}\\d{2}[A-Z0-9]{1,30}", message = "Invalid IBAN format")
    @Column(name = "iban", nullable = false)
    private String iban;

    /**
     * Name of the bank
     */
    @NotBlank(message = "Bank name is required")
    @Column(name = "bank_name", nullable = false)
    private String bankName;

    /**
     * SWIFT code for international transfers
     */
    @NotBlank(message = "SWIFT code is required")
    @Pattern(regexp = "[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?", message = "Invalid SWIFT code format")
    @Column(name = "swift_code", nullable = false)
    private String swiftCode;

    /**
     * Reference number for the transfer
     */
    @Column(name = "reference_number")
    private String referenceNumber;

    @Override
    public void validate() {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalStateException("Account number is required");
        }

        if (routingNumber == null || routingNumber.trim().isEmpty()) {
            throw new IllegalStateException("Routing number is required");
        }

        if (iban == null || iban.trim().isEmpty()) {
            throw new IllegalStateException("IBAN is required");
        }

        if (bankName == null || bankName.trim().isEmpty()) {
            throw new IllegalStateException("Bank name is required");
        }

        if (swiftCode == null || !swiftCode.matches("[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?")) {
            throw new IllegalStateException("Invalid SWIFT code format");
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
