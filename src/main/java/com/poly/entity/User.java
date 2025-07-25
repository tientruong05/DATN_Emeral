package com.poly.entity;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "[User]") // Tên bảng là "User" có thể cần ngoặc vuông nếu là từ khóa SQL Server
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_nguoi_dung")
    private Long idNguoiDung;

    // Nếu tenNguoiDung cũng là duy nhất, hãy thêm unique = true
    // Và nullable = false nếu nó không được phép là NULL trong DB
    @NotBlank(message = "Tên người dùng không được để trống")
    @Column(name = "ten_nguoi_dung", unique = true, nullable = false)
    private String tenNguoiDung;

    @NotBlank(message = "Họ tên không được để trống")
    @Column(name = "ho_ten") // ho_ten thường không cần unique
    private String hoTen;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Column(name = "email", unique = true, nullable = false) // Rất quan trọng: email phải là duy nhất và không null
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    @Column(name = "mat_khau", nullable = false) // Mật khẩu không được null
    private String matKhau;

    @Column(name = "vai_tro")
    private Boolean vaiTro; // false là Học viên, true là Quản trị viên

    @Column(name = "status")
    private Boolean status; // BIT: 1 là "Hoạt động", 0 là "Không hoạt động"

    @Column(name = "anh_dai_dien")
    private String anhDaiDien;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "dia_chi")
    private String diaChi;

    @Column(name = "so_dien_thoai")
    private String soDienThoai;

    // Getter/setter cho vai_tro
    // Chỉnh sửa logic để trả về ROLE_ADMIN hoặc ROLE_USER cho Spring Security
    public String getVaiTroAsString() {
        return vaiTro != null && vaiTro ? "ADMIN" : "USER"; // Trả về "ADMIN" hoặc "USER"
    }

    public void setVaiTroAsString(String vaiTro) {
        this.vaiTro = "ADMIN".equalsIgnoreCase(vaiTro); // So sánh không phân biệt chữ hoa/thường
    }

    // Getter/setter cho status
    public String getStatusAsString() {
        return status != null && status ? "Hoạt động" : "Không hoạt động";
    }

    public void setStatusAsString(String status) {
        this.status = "Hoạt động".equalsIgnoreCase(status);
    }

    @OneToMany(mappedBy = "user")
    @JsonBackReference
    private List<Enrollment> enrollments;
}