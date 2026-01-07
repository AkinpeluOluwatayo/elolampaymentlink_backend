package enterprise.elroi.services;

import enterprise.elroi.model.Payment;

public interface PaymentServiceInterface {

    Payment verifyAndSave(String reference);
}
