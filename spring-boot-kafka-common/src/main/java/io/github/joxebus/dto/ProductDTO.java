package io.github.joxebus.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductDTO {
    private String id;
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private String category;
}
