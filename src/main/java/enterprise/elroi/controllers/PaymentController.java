package enterprise.elroi.controllers;

import enterprise.elroi.model.Payment;
import enterprise.elroi.services.PaymentServiceInterface;
import enterprise.elroi.services.pdfGeneratorService.PdfGeneratorService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/elolam")

@CrossOrigin(origins = {"http://localhost:3000", "https://elolamtuitionpaymentlink.vercel.app"})
public class PaymentController {

    private final PaymentServiceInterface paymentService;
    private final PdfGeneratorService pdfGeneratorService;

    public PaymentController(PaymentServiceInterface paymentService,
                             PdfGeneratorService pdfGeneratorService) {
        this.paymentService = paymentService;
        this.pdfGeneratorService = pdfGeneratorService;
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

    @GetMapping("/payments/receipt/{reference}")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable String reference) {
        try {
            Payment payment = paymentService.verifyAndSave(reference);
            byte[] pdfBytes = pdfGeneratorService.generatePaymentReceipt(payment);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "ElOlam_Receipt_" + reference + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error serving PDF: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}