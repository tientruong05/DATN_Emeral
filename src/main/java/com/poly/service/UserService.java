package com.poly.service;

import com.poly.entity.User;
import com.poly.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // @Autowired
    // private BCryptPasswordEncoder passwordEncoder; // Thêm để băm mật khẩu
    @Autowired
    private PasswordEncoder passwordEncoder;
    // Thêm phương thức này vào UserService
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    }

    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email đã tồn tại, vui lòng nhập email khác");
        }
        if (user.getTenNguoiDung() == null || user.getTenNguoiDung().trim().isEmpty()) {
            user.setTenNguoiDung(user.getEmail().split("@")[0]);
        }
        // Đảm bảo mật khẩu được băm trước khi lưu
        if (user.getMatKhau() != null && !user.getMatKhau().isEmpty() && !user.getMatKhau().startsWith("$2a$")) {
            user.setMatKhau(passwordEncoder.encode(user.getMatKhau()));
        }
        return userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        if (!user.getEmail().equals(userDetails.getEmail()) && userRepository.existsByEmail(userDetails.getEmail())) {
            throw new RuntimeException("Email đã tồn tại, vui lòng nhập email khác");
        }
        if (userDetails.getTenNguoiDung() == null) {
            userDetails.setTenNguoiDung(userDetails.getEmail().split("@")[0]);
        }
        user.setTenNguoiDung(userDetails.getTenNguoiDung());
        user.setHoTen(userDetails.getHoTen());
        user.setEmail(userDetails.getEmail());
        user.setMatKhau(userDetails.getMatKhau()); // Lưu mật khẩu dạng văn bản thô
        user.setVaiTro(userDetails.getVaiTro());
        user.setStatus(userDetails.getStatus());
        user.setNgaySinh(userDetails.getNgaySinh());
        user.setDiaChi(userDetails.getDiaChi());
        user.setSoDienThoai(userDetails.getSoDienThoai());
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByTenNguoiDung(username);
    }

    public String importUsersFromExcel(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File Excel không được để trống!");
        }

        List<User> users = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet Excel không tồn tại!");
            }
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) {
                rowIterator.next(); // Bỏ qua header
            }

            int rowNum = 1;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                rowNum++;
                User user = new User();
                try {
                    String tenNguoiDung = getCellValue(row.getCell(1));
                    String hoTen = getCellValue(row.getCell(2));
                    String email = getCellValue(row.getCell(3));
                    String matKhau = getCellValue(row.getCell(4));
                    String vaiTro = getCellValue(row.getCell(5));
                    String status = getCellValue(row.getCell(6));
                    String ngaySinh = getCellValue(row.getCell(7));
                    String diaChi = getCellValue(row.getCell(8));
                    String soDienThoai = getCellValue(row.getCell(9));

                    // Kiểm tra dữ liệu bắt buộc
                    if (email == null || email.trim().isEmpty()) {
                        throw new IllegalArgumentException("Email không được để trống tại dòng " + rowNum);
                    }
                    if (hoTen == null || hoTen.trim().isEmpty()) {
                        throw new IllegalArgumentException("Họ tên không được để trống tại dòng " + rowNum);
                    }
                    if (matKhau == null || matKhau.trim().isEmpty()) {
                        throw new IllegalArgumentException("Mật khẩu không được để trống tại dòng " + rowNum);
                    }

                    user.setTenNguoiDung(tenNguoiDung);
                    user.setHoTen(hoTen);
                    user.setEmail(email);
                    user.setMatKhau(passwordEncoder.encode(matKhau)); // Băm mật khẩu
                    // Xử lý ngay_sinh
                    if (ngaySinh != null && !ngaySinh.trim().isEmpty()) {
                        try {
                            user.setNgaySinh(parseDate(ngaySinh));
                        } catch (DateTimeParseException e) {
                            throw new IllegalArgumentException("Ngày sinh không hợp lệ tại dòng " + rowNum + ": " + ngaySinh);
                        }
                    }
                    user.setDiaChi(diaChi);
                    user.setSoDienThoai(soDienThoai);

                    // Xử lý vai trò
                    if (vaiTro != null && !vaiTro.trim().isEmpty()) {
                        if (vaiTro.equals("Học viên")) {
                            user.setVaiTro(false);
                        } else if (vaiTro.equals("Quản trị viên")) {
                            user.setVaiTro(true);
                        } else {
                            throw new IllegalArgumentException("Vai trò phải là 'Học viên' hoặc 'Quản trị viên' tại dòng " + rowNum);
                        }
                    } else {
                        user.setVaiTro(false);
                    }

                    // Xử lý trạng thái
                    if (status != null && !status.trim().isEmpty()) {
                        if (status.equals("Hoạt động")) {
                            user.setStatus(true);
                        } else if (status.equals("Không hoạt động")) {
                            user.setStatus(false);
                        } else {
                            throw new IllegalArgumentException("Trạng thái phải là 'Hoạt động' hoặc 'Không hoạt động' tại dòng " + rowNum);
                        }
                    } else {
                        user.setStatus(true);
                    }

                    users.add(user);
                } catch (IllegalArgumentException e) {
                    errors.add("Dòng " + rowNum + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new IOException("Lỗi khi đọc file Excel: " + e.getMessage(), e);
        }

        int successCount = 0;
        for (User user : users) {
            try {
                createUser(user);
                successCount++;
            } catch (RuntimeException e) {
                errors.add("Lỗi khi lưu user " + user.getEmail() + ": " + e.getMessage());
            }
        }

        if (errors.isEmpty()) {
            return "Nhập thành công " + successCount + " người dùng.";
        } else {
            return "Nhập thành công " + successCount + " người dùng. Lỗi: " + String.join("; ", errors);
        }
    }

    public byte[] exportUsersToExcel() throws IOException {
        List<User> users = userRepository.findAll();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users");

        Row headerRow = sheet.createRow(0);
        String[] columns = {"ID", "Tên người dùng", "Họ tên", "Email", "Mật khẩu", "Vai trò", "Trạng thái", "Ngày sinh", "Địa chỉ", "Số điện thoại"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
        }

        int rowNum = 1;
        for (User user : users) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(user.getIdNguoiDung() != null ? user.getIdNguoiDung() : 0);
            row.createCell(1).setCellValue(user.getTenNguoiDung() != null ? user.getTenNguoiDung() : "");
            row.createCell(2).setCellValue(user.getHoTen() != null ? user.getHoTen() : "");
            row.createCell(3).setCellValue(user.getEmail() != null ? user.getEmail() : "");
            row.createCell(4).setCellValue(""); // Không xuất mật khẩu
            row.createCell(5).setCellValue(user.getVaiTro() != null && user.getVaiTro() ? "Quản trị viên" : "Học viên");
            row.createCell(6).setCellValue(user.getStatus() != null && user.getStatus() ? "Hoạt động" : "Không hoạt động");
            row.createCell(7).setCellValue(user.getNgaySinh() != null ? user.getNgaySinh().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
            row.createCell(8).setCellValue(user.getDiaChi() != null ? user.getDiaChi() : "");
            row.createCell(9).setCellValue(user.getSoDienThoai() != null ? user.getSoDienThoai() : "");
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    }

    private LocalDate parseDate(String dateStr) throws DateTimeParseException {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        String[] formats = {"dd/MM/yyyy", "yyyy-MM-dd", "MM/dd/yyyy"};
        for (String format : formats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // Thử định dạng tiếp theo
            }
        }
        throw new DateTimeParseException("Không thể phân tích ngày: " + dateStr, dateStr, 0);
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    try {
                        LocalDate date = cell.getDateCellValue().toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate();
                        return DateTimeFormatter.ofPattern("dd/MM/yyyy").format(date);
                    } catch (Exception e) {
                        return null;
                    }
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }
}