package com.poly.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Course")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_khoa_hoc")
    private Long ID_khoa_hoc;

    @Column(name = "ten_khoa_hoc", nullable = false)
    private String ten_khoa_hoc;
    
    private String mo_ta;
    
    @Column(name = "diem_dat", nullable = false)
    private Double diem_dat;

    @ManyToOne
    @JoinColumn(name = "danh_muc_ID")
    private Category category;

    @Column(name = "gia_tien", nullable = false)
    private Double gia_tien;
    private String anh_dai_dien;

    @Column(name = "Status")
    private boolean status = true;


    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonBackReference // Ngăn serialize ngược từ Course -> Questions
    private List<Question> questions;

    @OneToMany(mappedBy = "course")
    @JsonBackReference
    private List<Video> videos;

    @OneToMany(mappedBy = "course")
    @JsonBackReference
    private List<Enrollment> enrollments;

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<DiscountDetail> discountDetails;
    
 // Tính giá sau khi giảm giá
    @Transient
    public Double getDiscountedPrice() {
        Double percent = getDiscountPercentage();
        if (percent != null) {
            return gia_tien - (gia_tien * percent / 100);
        }
        return gia_tien;
    }


    @Transient
    public Double getDiscountPercentage() {
        List<DiscountDetail> allApplicableDiscounts = new ArrayList<>();

        // 1. Lấy các giảm giá áp dụng cho khóa học này
        if (discountDetails != null) {
            for (DiscountDetail detail : discountDetails) {
                Discount discount = detail.getDiscount();
                if (discount != null
                        && discount.isActive()
                        && Boolean.TRUE.equals(detail.getStatus())
                        && discount.isValidNow()) {
                    allApplicableDiscounts.add(detail);
                }
            }
        }

        // 2. Lấy các giảm giá theo danh mục nếu có
        if (this.category != null && this.category.getDiscountDetails() != null) {
            for (DiscountDetail detail : this.category.getDiscountDetails()) {
                Discount discount = detail.getDiscount();
                if (discount != null
                        && discount.isActive()
                        && Boolean.TRUE.equals(detail.getStatus())
                        && discount.isValidNow()) {
                    allApplicableDiscounts.add(detail);
                }
            }
        }

        // 3. Nếu không có giảm giá hợp lệ
        if (allApplicableDiscounts.isEmpty()) {
            return null;
        }

        // 4. Chọn giảm giá mới nhất theo start_date hoặc id
        DiscountDetail latest = allApplicableDiscounts.stream()
                .max(Comparator.comparing((DiscountDetail d) -> d.getDiscount().getStart_date())
                        .thenComparing(d -> d.getDiscount().getId()))
                .orElse(null);

        return latest != null ? latest.getDiscount().getDiscount_value() : null;
    }


    // Kiểm tra có giảm giá hay không
    @Transient
    public boolean isDiscounted() {
        return getDiscountPercentage() != null;
    }
    
}