package br.com.microservices.orchestrated.orchestratorservice.core.service;


import br.com.microservices.orchestrated.orchestratorservice.core.dto.Event;
import br.com.microservices.orchestrated.orchestratorservice.core.dto.History;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.EEventSource;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics;
import br.com.microservices.orchestrated.orchestratorservice.core.producer.SagaOrchestratorProducer;
import br.com.microservices.orchestrated.orchestratorservice.core.saga.SagaExecutionController;
import br.com.microservices.orchestrated.orchestratorservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static br.com.microservices.orchestrated.orchestratorservice.core.enums.EEventSource.ORCHESTRATOR;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus.FAIL;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus.SUCCESS;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics.NOTIFY_ENDING;

@Service
@Slf4j
@AllArgsConstructor
public class OrchestratorService {

    private final SagaOrchestratorProducer producer;
    private final JsonUtil jsonUtil;
    private final SagaExecutionController executionController;

    public void startSaga(Event event) {
        event.setSource(ORCHESTRATOR);
        event.setStatus(SUCCESS);
        var topic = getTopic(event);
        log.info("SAGA STARTED");
        addHistory(event,"Saga started!");
        producer.sendEvent(jsonUtil.toJson(event),topic.getTopic());
    }


    public void finishSagaSuccess(Event event) {
        event.setSource(ORCHESTRATOR);
        event.setStatus(SUCCESS);
        log.info("SAGA FINISHED SUCCESSFULLY FOR EVENT {}",event.getId());
        addHistory(event,"Saga finish successfully!");
        notifyFinishedSaga(event);
    }

    public void finishSagaFail(Event event) {
        event.setSource(ORCHESTRATOR);
        event.setStatus(FAIL);
        log.info("SAGA FINISHED WHIT ERRORS FOR EVENT {}",event.getId());
        addHistory(event,"Saga finish errors!");
        notifyFinishedSaga(event);
    }

    public void continueSaga(Event event) {
      var topic = getTopic(event);
      log.info("SAGA CONTINUE FOR EVENT {}",getTopic(event));
      producer.sendEvent(jsonUtil.toJson(event),topic.getTopic());

    }

    private ETopics getTopic(Event event) {
        return executionController.getNextTopic(event);
    }

    private void addHistory(Event event, String message) {
        var history = History
                .builder()
                .source(event.getSource())
                .status(event.getStatus())
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
        event.addToHistory(history);
    }
    private void notifyFinishedSaga(Event event){
        producer.sendEvent(jsonUtil.toJson(event),NOTIFY_ENDING.getTopic() );
    }
}
