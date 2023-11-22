package br.com.microservices.orchestrated.productvalidationservice.core.dto;



import br.com.microservices.orchestrated.productvalidationservice.core.enums.ESagaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.protocol.types.Field;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class History {

    private Field.Str source;
    private ESagaStatus status;
    private String message;
    private LocalDateTime createdAt;
}
