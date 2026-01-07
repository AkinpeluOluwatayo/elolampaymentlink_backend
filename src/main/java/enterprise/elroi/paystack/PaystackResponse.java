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

        @JsonProperty("paid_at")
        private String paidAt;

        @JsonDeserialize(using = MetadataDeserializer.class)
        private Metadata metadata;
    }

    @Data
    public static class Metadata {
        private String studentName;
        private String paymentDuration;
    }


    public static class MetadataDeserializer extends JsonDeserializer<Metadata> {
        @Override
        public Metadata deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getText();
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            return p.readValueAs(Metadata.class);
        }
    }
}