package com.poly.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "discounts")
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String discount_name;
    private Double discount_value;
    private LocalDateTime start_date;
    private LocalDateTime end_date;
    private Boolean status;

    @OneToMany(mappedBy = "discount")
    private List<DiscountDetail> discountDetails;
}
