package com.hibernate.learning.inheritance;

import com.hibernate.learning.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InheritanceDemo {
    public static void main(String[] args) {
        // Demo Single Table Strategy
        demoSingleTableStrategy();

        // Demo Joined Table Strategy
        demoJoinedTableStrategy();

        // Demo Table Per Class Strategy
        demoTablePerClassStrategy();
    }

    private static void demoSingleTableStrategy() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            // Create Credit Card Payment using Single Table Strategy
            com.hibernate.learning.inheritance.singletable.CreditCardPayment creditCard =
                    new com.hibernate.learning.inheritance.singletable.CreditCardPayment();
            creditCard.setAmount(new BigDecimal("100.00"));
            creditCard.setPaymentDate(LocalDateTime.now());
            creditCard.setCardNumber("4111111111111111");
            creditCard.setCardHolderName("John Doe");
            creditCard.setExpirationMonth(12);
            creditCard.setExpirationYear(25);
            creditCard.setCvv("123");

            session.save(creditCard);

            // Create Bank Transfer Payment using Single Table Strategy
            com.hibernate.learning.inheritance.singletable.BankTransferPayment bankTransfer =
                    new com.hibernate.learning.inheritance.singletable.BankTransferPayment();
            bankTransfer.setAmount(new BigDecimal("500.00"));
            bankTransfer.setPaymentDate(LocalDateTime.now());
            bankTransfer.setBankName("Chase Bank");
            bankTransfer.setAccountNumber("123456789");
            bankTransfer.setRoutingNumber("987654321");
            bankTransfer.setIban("US123456789");
            bankTransfer.setSwiftCode("CHASUS33");

            session.save(bankTransfer);

            tx.commit();
        }
    }

    private static void demoJoinedTableStrategy() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            // Create Credit Card Payment using Joined Table Strategy
            com.hibernate.learning.inheritance.joined.CreditCardPayment creditCard =
                    new com.hibernate.learning.inheritance.joined.CreditCardPayment();
            creditCard.setAmount(new BigDecimal("200.00"));
            creditCard.setPaymentDate(LocalDateTime.now());
            creditCard.setCardNumber("4111111111111111");
            creditCard.setCardHolderName("Jane Doe");
            creditCard.setExpirationMonth(12);
            creditCard.setExpirationYear(25);
            creditCard.setCvv("123");

            session.save(creditCard);

            // Create Bank Transfer Payment using Joined Table Strategy
            com.hibernate.learning.inheritance.joined.BankTransferPayment bankTransfer =
                    new com.hibernate.learning.inheritance.joined.BankTransferPayment();
            bankTransfer.setAmount(new BigDecimal("300.00"));
            bankTransfer.setPaymentDate(LocalDateTime.now());
            bankTransfer.setBankName("Deutsche Bank");
            bankTransfer.setAccountNumber("1234567890");
            bankTransfer.setRoutingNumber("987654321");
            bankTransfer.setIban("DE123456789");
            bankTransfer.setSwiftCode("DEUTDE33");

            session.save(bankTransfer);

            tx.commit();
        }
    }

    private static void demoTablePerClassStrategy() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            // Create Credit Card Payment using Table Per Class Strategy
            com.hibernate.learning.inheritance.tableperclass.CreditCardPayment creditCard =
                    new com.hibernate.learning.inheritance.tableperclass.CreditCardPayment();
            creditCard.setAmount(new BigDecimal("150.00"));
            creditCard.setPaymentDate(LocalDateTime.now());
            creditCard.setCardNumber("4111111111111111");
            creditCard.setCardHolderName("Alice Smith");
            creditCard.setExpirationMonth(12);
            creditCard.setExpirationYear(25);
            creditCard.setCvv("123");

            session.save(creditCard);

            // Create Bank Transfer Payment using Table Per Class Strategy
            com.hibernate.learning.inheritance.tableperclass.BankTransferPayment bankTransfer =
                    new com.hibernate.learning.inheritance.tableperclass.BankTransferPayment();
            bankTransfer.setAmount(new BigDecimal("400.00"));
            bankTransfer.setPaymentDate(LocalDateTime.now());
            bankTransfer.setBankName("Barclays");
            bankTransfer.setAccountNumber("9876543210");
            bankTransfer.setRoutingNumber("123456789");
            bankTransfer.setIban("GB123456789");
            bankTransfer.setSwiftCode("BARCGB22");

            session.save(bankTransfer);

            tx.commit();
        }
    }
}
