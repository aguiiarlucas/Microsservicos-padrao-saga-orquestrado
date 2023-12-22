package br.com.microservices.orchestrated.productvalidationservice.core.dto;


import br.com.microservices.orchestrated.productvalidationservice.core.enums.ESagaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventDTO {

    private String id;
    private String transactionId;
    private String orderId;
    private OrderDTO payload;
    private String source;
    private ESagaStatus status;
    private List<HistoryDTO> eventHistoryDTO;
    private LocalDateTime createdAt;

    public void addToHistory(HistoryDTO historyDTO){
        if(eventHistoryDTO.isEmpty()){
            eventHistoryDTO = new ArrayList<>();
        }
        eventHistoryDTO.add(historyDTO);
    }


}
