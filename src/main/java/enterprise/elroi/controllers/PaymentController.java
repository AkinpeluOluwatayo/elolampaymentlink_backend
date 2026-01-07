package enterprise.elroi.controllers;

import enterprise.elroi.model.Payment;
import enterprise.elroi.services.PaymentServiceInterface;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/elolam")
public class PaymentController {

    private final PaymentServiceInterface paymentService;

    public PaymentController(PaymentServiceInterface paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> body) {
        try {
            String reference = body.get("reference");

            if (reference == null || reference.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Reference is required");
                return ResponseEntity.badRequest().body(error);
            }

            Payment payment = paymentService.verifyAndSave(reference);
            return ResponseEntity.ok(payment);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}