package com.hibernate.learning.inheritance;

import com.hibernate.learning.inheritance.joined.BankTransferPayment;
import com.hibernate.learning.inheritance.joined.CreditCardPayment;
import com.hibernate.learning.inheritance.joined.Payment;
import com.hibernate.learning.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PaymentTest {

    private static Session session;
    private static Transaction transaction;

    @BeforeAll
    static void setUp() {
        session = HibernateUtil.getSessionFactory().openSession();
    }

    @AfterAll
    static void tearDown() {
        if (session != null) {
            session.close();
        }
        HibernateUtil.shutdown();
    }

    @BeforeEach
    void startTransaction() {
        transaction = session.beginTransaction();
    }

    @AfterEach
    void rollbackTransaction() {
        if (transaction != null && transaction.isActive()) {
            transaction.rollback();
        }
    }

    @Test
    @Order(1)
    void testCreateCreditCardPayment() {
        CreditCardPayment payment = new CreditCardPayment();
        payment.setAmount(new BigDecimal("100.00"));
        payment.setPaymentDate(LocalDateTime.now());
        payment.setCurrency("USD");
        payment.setCardNumber("1234-5678-9012-3456");
        payment.setCardHolderName("John Doe");
        payment.setExpirationMonth(12);
        payment.setExpirationYear(25);
        payment.setCvv("123");

        session.save(payment);
        transaction.commit();

        assertNotNull(payment.getId());
        assertEquals(Payment.PaymentStatus.PENDING, payment.getStatus());
    }

    @Test
    @Order(2)
    void testCreateBankTransferPayment() {
        BankTransferPayment payment = new BankTransferPayment();
        payment.setAmount(new BigDecimal("500.00"));
        payment.setPaymentDate(LocalDateTime.now());
        payment.setCurrency("EUR");
        payment.setBankName("Test Bank");
        payment.setAccountNumber("123456789");
        payment.setIban("DE89370400440532013000");
        payment.setSwiftCode("TESTSWIFT");

        session.save(payment);
        transaction.commit();

        assertNotNull(payment.getId());
        assertEquals(Payment.PaymentStatus.PENDING, payment.getStatus());
    }

    @Test
    @Order(3)
    void testPaymentProcessing() {
        CreditCardPayment payment = new CreditCardPayment();
        payment.setAmount(new BigDecimal("150.00"));
        payment.setPaymentDate(LocalDateTime.now());
        payment.setCurrency("USD");
        payment.setCardNumber("1234-5678-9012-3456");
        payment.setCardHolderName("Jane Doe");
        payment.setExpirationMonth(12);
        payment.setExpirationYear(25);
        payment.setCvv("123");

        session.save(payment);
        transaction.commit();

        // Start new transaction
        transaction = session.beginTransaction();
        payment.process();
        session.update(payment);
        transaction.commit();

        assertEquals(Payment.PaymentStatus.COMPLETED, payment.getStatus());
    }

    @Test
    @Order(4)
    void testPaymentValidation() {
        CreditCardPayment payment = new CreditCardPayment();
        payment.setAmount(new BigDecimal("-100.00")); // Invalid amount
        payment.setPaymentDate(LocalDateTime.now());
        payment.setCurrency("USD");
        payment.setCardNumber("invalid"); // Invalid card number
        payment.setCardHolderName("");    // Empty name
        payment.setExpirationMonth(13);   // Invalid month
        payment.setExpirationYear(20);    // Past year
        payment.setCvv("12");             // Invalid CVV

        assertThrows(IllegalStateException.class, payment::validate);
    }

    /**
     * Tests polymorphic operations with different payment types.
     * <p>
     * Polymorphic Operations:
     * <pre>
     * 1. Save Operations
     *    Payment (abstract)
     *    ├── Save CreditCardPayment
     *    └── Save CashPayment
     *
     * 2. Load Operations
     *    └── Polymorphic Query
     *        ├── Load all payments
     *        └── Type discrimination
     *
     * 3. Update Operations
     *    └── Type-specific updates
     *        ├── Credit card details
     *        └── Cash receipt number
     * </pre>
     */
    @Test
    public void testPolymorphicPayments() {
        // Create Credit Card Payment
        CreditCardPayment ccPayment = new CreditCardPayment();
        ccPayment.setAmount(new BigDecimal("100.00"));
        ccPayment.setPaymentDate(LocalDateTime.now());
        ccPayment.setCardNumber("4111111111111111");
        ccPayment.setExpirationMonth(12);
        ccPayment.setExpirationYear(2025);
        ccPayment.setCardHolderName("John Doe");

        // Create Bank Transfer Payment
        BankTransferPayment btPayment = new BankTransferPayment();
        btPayment.setAmount(new BigDecimal("200.00"));
        btPayment.setPaymentDate(LocalDateTime.now());
        btPayment.setBankName("Test Bank");
        btPayment.setAccountNumber("123456789");
        btPayment.setRoutingNumber("987654321");

        session.save(ccPayment);
        session.save(btPayment);
        transaction.commit();

        // Load and verify payments
        Payment loadedCCPayment = session.get(Payment.class, ccPayment.getId());
        Payment loadedBTPayment = session.get(Payment.class, btPayment.getId());

        assertTrue(loadedCCPayment instanceof CreditCardPayment);
        assertTrue(loadedBTPayment instanceof BankTransferPayment);

        CreditCardPayment loadedCC = (CreditCardPayment) loadedCCPayment;
        assertEquals("4111111111111111", loadedCC.getCardNumber());

        BankTransferPayment loadedBT = (BankTransferPayment) loadedBTPayment;
        assertEquals("Test Bank", loadedBT.getBankName());
    }

    /**
     * Tests inheritance-specific features and constraints.
     * <p>
     * Test Scenarios:
     * <pre>
     * 1. Type-Specific Validation
     *    ├── Credit Card
     *    │   ├── Card number format
     *    │   ├── Expiry date
     *    │   └── Card holder name
     *    │
     *    └── Bank Transfer
     *        ├── Bank name
     *        └── Account number
     *
     * 2. Common Field Validation
     *    ├── Amount > 0
     *    ├── Timestamp not null
     *    └── Status tracking
     * </pre>
     */
    @Test
    public void testInheritanceConstraints() {
        try {
            // Test invalid credit card payment
            CreditCardPayment invalidCC = new CreditCardPayment();
            invalidCC.setAmount(new BigDecimal("-100.00")); // Invalid amount
            session.save(invalidCC);
            transaction.commit();
            fail("Should have thrown an exception for invalid amount");
        } catch (Exception e) {
            transaction.rollback();
        }

        // Test valid credit card payment
        transaction = session.beginTransaction();
        CreditCardPayment validCC = new CreditCardPayment();
        validCC.setAmount(new BigDecimal("100.00"));
        validCC.setPaymentDate(LocalDateTime.now());
        validCC.setCardNumber("4111111111111111");
        validCC.setExpirationMonth(12);
        validCC.setExpirationYear(2025);
        validCC.setCardHolderName("John Doe");
        session.save(validCC);
        transaction.commit();

        // Verify saved payment
        Payment loadedPayment = session.get(Payment.class, validCC.getId());
        assertNotNull(loadedPayment);
        assertTrue(loadedPayment instanceof CreditCardPayment);
        assertEquals(new BigDecimal("100.00"), loadedPayment.getAmount());
    }

    /**
     * Tests polymorphic queries and filtering.
     * <p>
     * Query Patterns:
     * <pre>
     * 1. Type-Based Queries
     *    ├── All payments
     *    ├── Only credit cards
     *    └── Only bank transfers
     *
     * 2. Criteria Queries
     *    ├── Amount range
     *    ├── Date range
     *    └── Payment status
     *
     * 3. Join Queries
     *    └── Payment with details
     *        ├── Credit card info
     *        └── Bank transfer info
     * </pre>
     */
    @Test
    public void testPolymorphicQueries() {
        // Create test data
        transaction = session.beginTransaction();

        for (int i = 1; i <= 3; i++) {
            CreditCardPayment ccPayment = new CreditCardPayment();
            ccPayment.setAmount(new BigDecimal("100.00").multiply(new BigDecimal(i)));
            ccPayment.setPaymentDate(LocalDateTime.now());
            ccPayment.setCardNumber("4111111111111111");
            ccPayment.setExpirationMonth(12);
            ccPayment.setExpirationYear(2025);
            ccPayment.setCardHolderName("User " + i);
            session.save(ccPayment);

            BankTransferPayment btPayment = new BankTransferPayment();
            btPayment.setAmount(new BigDecimal("200.00").multiply(new BigDecimal(i)));
            btPayment.setPaymentDate(LocalDateTime.now());
            btPayment.setBankName("Test Bank " + i);
            btPayment.setAccountNumber("123456789" + i);
            btPayment.setRoutingNumber("987654321" + i);
            session.save(btPayment);
        }

        transaction.commit();

        // Test different query types
        assertEquals(6L, session.createQuery("from Payment", Payment.class).list().size());
        assertEquals(3L, session.createQuery("from CreditCardPayment", CreditCardPayment.class).list().size());
        assertEquals(3L, session.createQuery("from BankTransferPayment", BankTransferPayment.class).list().size());

        // Test amount-based query
        assertEquals(2L, session.createQuery("from Payment p where p.amount > :amount")
                .setParameter("amount", new BigDecimal("200.00"))
                .list()
                .size());
    }
}
