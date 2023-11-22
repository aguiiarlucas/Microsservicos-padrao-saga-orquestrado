package br.com.microservices.orchestrated.inventoryservice.core.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderProduct {

    private Product product;
    private Integer quantity;
}
