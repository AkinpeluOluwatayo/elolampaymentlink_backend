package enterprise.elroi.controllers;

import enterprise.elroi.model.Payment;
import enterprise.elroi.services.PaymentServiceInterface;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/elolam")
@CrossOrigin(origins = {"http://localhost:3000", "https://elolamtuitionpaymentlink.vercel.app"})
public class PaymentController {

    private final PaymentServiceInterface paymentService;

    // Clean Constructor: Removed PdfGeneratorService
    public PaymentController(PaymentServiceInterface paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> body) {
        try {
            String reference = body.get("reference");
            if (reference == null || reference.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Reference is required"));
            }


            Payment payment = paymentService.verifyAndSave(reference);


            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}