package br.com.microservices.orchestrated.paymentservice.core.service;


import br.com.microservices.orchestrated.paymentservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.paymentservice.core.dto.EventDTO;
import br.com.microservices.orchestrated.paymentservice.core.dto.HistoryDTO;
import br.com.microservices.orchestrated.paymentservice.core.dto.OrderProduct;
import br.com.microservices.orchestrated.paymentservice.core.enums.EPaymentStatus;
import br.com.microservices.orchestrated.paymentservice.core.model.Payment;
import br.com.microservices.orchestrated.paymentservice.core.producer.KafkaProducer;
import br.com.microservices.orchestrated.paymentservice.core.repository.PaymentRepository;
import br.com.microservices.orchestrated.paymentservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.events.Event;

import java.time.LocalDateTime;

import static br.com.microservices.orchestrated.paymentservice.core.enums.ESagaStatus.SUCCESS;

@Slf4j
@Service
@AllArgsConstructor
public class PaymentService {

    private static final String CURRENT_SOURCE = "PAYMENT_SERVICE";
    private static final Double REDUCE_SUM_VALUE = 0.0;
    private static final Double MIN_AMOUNT_VALUE = 0.1;
    private final JsonUtil jsonUtil;
    private final KafkaProducer producer;
    private final PaymentRepository paymentRepository;


    public void realizePayment(EventDTO event) {
        try {
            checkCurrentValidation(event );
            createPendingPayment(event);
            var payment = findByOrderIdAndTransactionId(event);
            validateAmount(payment.getTotalAmount());
            changePaymentSuccess(payment);
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

    private void save(Payment payment) {
        paymentRepository.save(payment);
    }

    private void validateAmount(double amount) {
        if (amount < MIN_AMOUNT_VALUE) {
            throw new ValidationException("The minimum  amount available is"
                    .concat(MIN_AMOUNT_VALUE.toString()));
        }
    }
    private void changePaymentSuccess(Payment payment){
        payment.setStatus(EPaymentStatus.SUCCESS);
        save(payment);
    }
    private Payment findByOrderIdAndTransactionId(EventDTO event) {
        return paymentRepository
                .findByOrderIdAndTransactionId(event.getPayload().getId(), event.getPayload().getTransactionId())
                .orElseThrow(() -> new ValidationException("Payment not found by OrderId and TransactionId"));
    }
    private void handleSuccess(EventDTO event) {
        event.setStatus(SUCCESS);
        event.setSource(CURRENT_SOURCE);
        addHistory(event, "Payment realized successfully!");
    }
    private void addHistory(EventDTO event, String message) {
        var history = HistoryDTO
                .builder()
                .source(event.getSource())
                .status(event.getStatus())
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
        event.addToHistory(history);
    }
}


