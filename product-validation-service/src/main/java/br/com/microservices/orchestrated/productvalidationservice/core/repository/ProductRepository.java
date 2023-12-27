package br.com.microservices.orchestrated.productvalidationservice.core.repository;

import br.com.microservices.orchestrated.productvalidationservice.core.dto.OrderProductDTO;
import br.com.microservices.orchestrated.productvalidationservice.core.dto.ProductDTO;
import br.com.microservices.orchestrated.productvalidationservice.core.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

@EnableJpaRepositories
public interface ProductRepository extends JpaRepository<Product, Integer> {

    Boolean existsByCode(String code);

}
