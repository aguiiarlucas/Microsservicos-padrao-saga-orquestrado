package br.com.microservices.orchestrated.paymentservice.core.service;


import br.com.microservices.orchestrated.paymentservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.paymentservice.core.dto.EventDTO;
import br.com.microservices.orchestrated.paymentservice.core.dto.OrderProduct;
import br.com.microservices.orchestrated.paymentservice.core.model.Payment;
import br.com.microservices.orchestrated.paymentservice.core.producer.KafkaProducer;
import br.com.microservices.orchestrated.paymentservice.core.repository.PaymentRepository;
import br.com.microservices.orchestrated.paymentservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.events.Event;

@Slf4j
@Service
@AllArgsConstructor
public class PaymentService {

    private static final String CURRENT_SOURCE = "PAYMENT_SERVICE";
    private static final Double REDUCE_SUM_VALUE = 0.0;
    private final JsonUtil jsonUtil;
    private final KafkaProducer producer;
    private final PaymentRepository paymentRepository;


    public void realizePayment(EventDTO event) {
        try {
            checkCurrentValidation(event);
            createPendingPayment(event);
        } catch (Exception ex) {
            log.error("Error trying to validate product: ", ex);
        }
        producer.sendEvent(jsonUtil.toJson(event));

    }

    private void checkCurrentValidation(EventDTO event) {
        if (paymentRepository.existsByOrderIdAndTransactionId(event.getPayload().getId(), event.getTransactionId()))
            throw new ValidationException("There's another transactionId for this validation.");
    }

    private void createPendingPayment(EventDTO event) {
        var totalAmount = calculateAmount(event);
        var totalItems = calculateItem(event);
        var payment = Payment
                .builder()
                .orderId(event.getPayload().getId())
                .transactionId(event.getTransactionId())
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .build();
        save(payment);
        setEventAmountItems(event, payment);
    }

    private void save(Payment payment) {
        paymentRepository.save(payment);
    }

    private double calculateAmount(EventDTO event) {
        return event.getPayload()
                .getProducts()
                .stream()
                .map(product -> product.getQuantity() * product.getProduct().getUnitValue())
                .reduce(REDUCE_SUM_VALUE, Double::sum);
    }

    private int calculateItem(EventDTO event) {
        return event.getPayload()
                .getProducts()
                .stream()
                .map(OrderProduct::getQuantity)
                .reduce(REDUCE_SUM_VALUE.intValue(), Integer::sum);
    }

    private void setEventAmountItems(EventDTO event, Payment payment) {
        event.getPayload().setTotalAmount(payment.getTotalAmount());
        event.getPayload().setTotalItems(payment.getTotalItems());
    }
}


