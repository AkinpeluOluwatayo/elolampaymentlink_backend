package enterprise.elroi.paystack;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Component
public class PaystackClient {

    private final String paystackSecretKey;
    private final RestTemplate restTemplate;

    public PaystackClient(@Value("${paystack.secret.key}") String paystackSecretKey) {
        this.paystackSecretKey = paystackSecretKey;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(10000);

        this.restTemplate = new RestTemplate(factory);
    }

    public PaystackResponse verifyTransaction(String reference) {
        String url = "https://api.paystack.co/transaction/verify/" + reference;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + paystackSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<PaystackResponse> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, PaystackResponse.class);

            if (response.getBody() == null) {
                throw new RuntimeException("Empty response from Paystack");
            }

            return response.getBody();

        } catch (HttpClientErrorException e) {
            throw new RuntimeException(
                    "Paystack verification failed: " + e.getResponseBodyAsString(), e
            );
        } catch (ResourceAccessException e) {
            throw new RuntimeException("Cannot reach Paystack API. Check your internet connection.", e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error calling Paystack: " + e.getMessage(), e);
        }
    }
}