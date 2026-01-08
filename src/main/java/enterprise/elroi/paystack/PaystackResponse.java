package enterprise.elroi.paystack;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.core.JsonParser;
import lombok.Data;
import java.io.IOException;

@Data
public class PaystackResponse {

    private boolean status;
    private String message;
    private PaystackData data;

    @Data
    public static class PaystackData {
        private String reference;
        private Integer amount;
        private String channel;

        @JsonProperty("paid_at")
        private String paidAt;

        private Customer customer;

        @JsonDeserialize(using = MetadataDeserializer.class)
        private Metadata metadata;
    }

    @Data
    public static class Customer {
        @JsonProperty("first_name")
        private String firstName;

        @JsonProperty("last_name")
        private String lastName;

        private String email;
        private String phone;
    }

    @Data
    public static class Metadata {
        @JsonProperty("child_s_full_name")
        private String studentName;

        @JsonProperty("duration_of_payment")
        private String paymentDuration;
    }

    public static class MetadataDeserializer extends JsonDeserializer<Metadata> {
        @Override
        public Metadata deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            try {
                return p.getCodec().readTree(p).isObject() ?
                        p.getCodec().treeToValue(ctxt.readTree(p), Metadata.class) : null;
            } catch (Exception e) {
                try {
                    String raw = p.getText();
                    if (raw == null || raw.isEmpty()) return null;
                    return p.getCodec().readValue(p, Metadata.class);
                } catch (Exception ex) {
                    return null;
                }
            }
        }
    }
}