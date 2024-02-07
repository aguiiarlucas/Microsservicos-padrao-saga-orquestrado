package br.com.microservices.orchestrated.orchestratorservice.core.saga;


import br.com.microservices.orchestrated.orchestratorservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.orchestratorservice.core.dto.Event;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static br.com.microservices.orchestrated.orchestratorservice.core.saga.SagaHandler.*;
import static java.lang.String.format;
import static org.springframework.util.ObjectUtils.isEmpty;

@Component
@Slf4j
@AllArgsConstructor
public class SagaExecutionController {

    private static  final String SAGA_LOG_ID = "ORDER ID %s | TRANSACTION ID %s | EVENT ID %s";
    private static  final String SAGA_SUCCESS = "### SAGA: {}| SUCCESS | NEXT TOPIC {} | {}";
    private static  final String SAGA_ROLLBACK_PENDING = "### SAGA: {}| SENDING TO ROLLBACK CURRENT SERVER | NEXT TOPIC {} | {}";
    private static  final String SAGA_FAIL = "### SAGA: {}| SENDING TO ROLLBACK PREVIOUS SERVER | NEXT TOPIC {} | {}";

    public ETopics getNextTopic(Event event) {
        if (isEmpty(event.getSource()) || isEmpty(event.getStatus())) {
            throw new ValidationException("Source and Status must be informed");
        }
        var topic = findTopicBySourceAndStatus(event);
        logCurrentSaga(event,topic);
        return topic;
    }

    public ETopics findTopicBySourceAndStatus(Event event) {
        return (ETopics) Arrays.stream(SAGA_HANDLER)
                .filter(row -> isEventSourceAndStatusValid(event, row))
                .map(i -> i[TOPIC_INDEX])
                .findFirst()
                .orElseThrow(() -> new ValidationException("Topic not found"));
    }

    private boolean isEventSourceAndStatusValid(Event event, Object[] row) {
        var source = row[EVENT_SOURCE_INDEX];
        var status = row[SAGA_STATUS_INDEX];
        return event.getSource().equals(source) && event.getStatus().equals(status);
    }

    private void logCurrentSaga(Event event, ETopics topic) {
        var sagaId = createSagaId(event);
        var source = event.getSource();
        switch (event.getStatus()) {
            case SUCCESS ->
                    log.info(SAGA_SUCCESS, source, topic, sagaId);

            case ROLLBACK_PENDING ->
                    log.info(SAGA_ROLLBACK_PENDING, source, topic, sagaId);

            case FAIL ->
                    log.info(SAGA_FAIL, source, topic, sagaId);
        }

    }

    private String createSagaId(Event event) {
        return format(SAGA_LOG_ID,
                event.getPayload().getId(), event.getTransactionId(), event.getId());
    }


}
