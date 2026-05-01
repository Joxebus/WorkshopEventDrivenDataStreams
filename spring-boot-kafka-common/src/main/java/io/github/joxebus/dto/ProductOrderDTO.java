package io.github.joxebus.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductOrderDTO {
    private String productId;
    private Integer quantity;
    private Double price;
    private Double total;
}
