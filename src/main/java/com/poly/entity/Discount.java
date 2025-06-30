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
    private LocalDate start_date;
    private LocalDate end_date;
    private Boolean status;

    @OneToMany(mappedBy = "discount")
    private List<DiscountDetail> discountDetails;
    
    public boolean isActive() {
        return Boolean.TRUE.equals(this.status);
    }

    public boolean isValidNow() {
        LocalDate today = LocalDate.now();
        return (start_date != null && end_date != null
                && !today.isBefore(start_date)
                && !today.isAfter(end_date));
    }
}
