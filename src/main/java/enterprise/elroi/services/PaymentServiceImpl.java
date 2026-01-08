package enterprise.elroi.services;

import enterprise.elroi.model.Payment;
import enterprise.elroi.paystack.PaystackClient;
import enterprise.elroi.paystack.PaystackResponse;
import enterprise.elroi.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Service
public class PaymentServiceImpl implements PaymentServiceInterface {

    private final PaymentRepository paymentRepository;
    private final PaystackClient paystackClient;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            PaystackClient paystackClient
    ) {
        this.paymentRepository = paymentRepository;
        this.paystackClient = paystackClient;
    }

    @Override
    public Payment verifyAndSave(String reference) {
        try {
            PaystackResponse response = paystackClient.verifyTransaction(reference);

            if (!response.isStatus()) {
                throw new RuntimeException("Payment verification failed: " + response.getMessage());
            }

            if (response.getData() == null) {
                throw new RuntimeException("No data returned from Paystack");
            }

            PaystackResponse.PaystackData data = response.getData();

            Payment existingPayment = paymentRepository.findByReference(reference);
            if (existingPayment != null) {
                return existingPayment;
            }

            Payment payment = new Payment();

            if (data.getCustomer() != null) {
                String fName = data.getCustomer().getFirstName() != null ? data.getCustomer().getFirstName() : "";
                String lName = data.getCustomer().getLastName() != null ? data.getCustomer().getLastName() : "";
                payment.setCustomerName(fName + " " + lName);
                payment.setEmail(data.getCustomer().getEmail());
                payment.setPhoneNumber(data.getCustomer().getPhone());
            }

            if (data.getMetadata() != null) {
                payment.setStudentName(data.getMetadata().getStudentName());
                payment.setPaymentDuration(data.getMetadata().getPaymentDuration());
            } else {
                payment.setStudentName("Unknown");
                payment.setPaymentDuration("Unknown");
            }

            if (data.getAmount() != null) {
                BigDecimal amountInNaira = BigDecimal.valueOf(data.getAmount()).divide(BigDecimal.valueOf(100));
                payment.setAmount(amountInNaira);
            } else {
                payment.setAmount(BigDecimal.ZERO);
            }

            payment.setChannel(data.getChannel());
            payment.setReference(data.getReference());
            payment.setStatus("SUCCESS");

            if (data.getPaidAt() != null && !data.getPaidAt().isEmpty()) {
                try {
                    payment.setPaidAt(OffsetDateTime.parse(data.getPaidAt()).toLocalDateTime());
                } catch (Exception e) {
                    payment.setPaidAt(LocalDateTime.now());
                }
            } else {
                payment.setPaidAt(LocalDateTime.now());
            }

            return paymentRepository.save(payment);

        } catch (Exception e) {
            throw new RuntimeException("Failed to verify and save payment: " + e.getMessage(), e);
        }
    }
}

