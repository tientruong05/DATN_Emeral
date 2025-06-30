package com.poly.service;

import com.poly.dto.DiscountDTO;
import com.poly.entity.Category;
import com.poly.entity.Course;
import com.poly.entity.Discount;
import com.poly.entity.DiscountDetail;
import com.poly.repository.CategoryRepository;
import com.poly.repository.CourseRepository;
import com.poly.repository.DiscountDetailRepository;
import com.poly.repository.DiscountRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiscountServiceImpl implements DiscountService {

    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private DiscountDetailRepository discountDetailRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CourseRepository courseRepository;

    // No explicit constructor to avoid instantiation issues
    // Spring will use the default no-arg constructor

    @Override
    public List<DiscountDTO> getAllDiscounts() {
        List<Discount> discounts = discountRepository.findAll();
        return discounts.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DiscountDTO getDiscountById(Long id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá với ID: " + id));
        return mapToDTO(discount);
    }

    @Override
    @Transactional
    public DiscountDTO createDiscount(DiscountDTO discountDTO) {
        Discount discount = new Discount();
        discount.setDiscount_name(discountDTO.discountName());
        discount.setDiscount_value(discountDTO.discountValue());
        discount.setStart_date(discountDTO.startDate());
        discount.setEnd_date(discountDTO.endDate());
        discount.setStatus(discountDTO.status() != null ? discountDTO.status() : true);

        Discount savedDiscount = discountRepository.save(discount);

        if (discountDTO.categoryIds() != null && !discountDTO.categoryIds().isEmpty()) {
            for (Long categoryId : discountDTO.categoryIds()) {
                Category category = categoryRepository.findById(categoryId.intValue())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + categoryId));
                DiscountDetail detail = new DiscountDetail();
                detail.setDiscount(savedDiscount);
                detail.setCategory(category);
                detail.setStatus("1");
                discountDetailRepository.save(detail);
            }
        }

        if (discountDTO.courseIds() != null && !discountDTO.courseIds().isEmpty()) {
            for (Long courseId : discountDTO.courseIds()) {
                Course course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học với ID: " + courseId));
                DiscountDetail detail = new DiscountDetail();
                detail.setDiscount(savedDiscount);
                detail.setCourse(course);
                detail.setStatus("1");
                discountDetailRepository.save(detail);
            }
        }

        return mapToDTO(savedDiscount);
    }

    @Override
    @Transactional
    public DiscountDTO updateDiscount(Long id, DiscountDTO discountDTO) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá với ID: " + id));

        discount.setDiscount_name(discountDTO.discountName());
        discount.setDiscount_value(discountDTO.discountValue());
        discount.setStart_date(discountDTO.startDate());
        discount.setEnd_date(discountDTO.endDate());
        discount.setStatus(discountDTO.status() != null ? discountDTO.status() : true);

        Discount updatedDiscount = discountRepository.save(discount);

        List<DiscountDetail> details = discountDetailRepository.findAllByDiscountId(id);
        discountDetailRepository.deleteAll(details);

        if (discountDTO.categoryIds() != null && !discountDTO.categoryIds().isEmpty()) {
            for (Long categoryId : discountDTO.categoryIds()) {
                Category category = categoryRepository.findById(categoryId.intValue())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + categoryId));
                DiscountDetail detail = new DiscountDetail();
                detail.setDiscount(updatedDiscount);
                detail.setCategory(category);
                detail.setStatus("1");
                discountDetailRepository.save(detail);
            }
        }

        if (discountDTO.courseIds() != null && !discountDTO.courseIds().isEmpty()) {
            for (Long courseId : discountDTO.courseIds()) {
                Course course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học với ID: " + courseId));
                DiscountDetail detail = new DiscountDetail();
                detail.setDiscount(updatedDiscount);
                detail.setCourse(course);
                detail.setStatus("1");
                discountDetailRepository.save(detail);
            }
        }

        return mapToDTO(updatedDiscount);
    }

    @Override
    @Transactional
    public void deleteDiscount(Long id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá với ID: " + id));
        List<DiscountDetail> details = discountDetailRepository.findAllByDiscountId(id);
        discountDetailRepository.deleteAll(details);
        discountRepository.delete(discount);
    }

    @Override
    public List<DiscountDTO> getActiveDiscounts() {
        LocalDate today = LocalDate.now();
        List<Discount> activeDiscounts = discountRepository.findActiveDiscounts(today);
        return activeDiscounts.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private DiscountDTO mapToDTO(Discount discount) {
        List<DiscountDetail> details = discountDetailRepository.findAllByDiscountId(discount.getId());
        List<Long> categoryIds = new ArrayList<>();
        List<Long> courseIds = new ArrayList<>();

        for (DiscountDetail detail : details) {
            if (detail.getCategory() != null) {
                categoryIds.add(Long.valueOf(detail.getCategory().getId()));
            }
            if (detail.getCourse() != null) {
                courseIds.add(detail.getCourse().getID_khoa_hoc());
            }
        }

        return new DiscountDTO(
                discount.getId(),
                discount.getDiscount_name(),
                discount.getDiscount_value(),
                discount.getStart_date(),
                discount.getEnd_date(),
                discount.getStatus(),
                categoryIds,
                courseIds
        );
    }

    @Override
    public Workbook exportDiscountsToExcel() {
        List<DiscountDTO> discounts = getAllDiscounts();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Discounts");

        Row headerRow = sheet.createRow(0);
        String[] columns = {
                "Tên chương trình", "Giá trị giảm (%)", "Ngày bắt đầu", "Ngày kết thúc",
                "Trạng thái", "Loại giảm giá", "Danh mục áp dụng", "Khóa học áp dụng"
        };
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
        }

        int rowNum = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (DiscountDTO discount : discounts) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(discount.discountName());
            row.createCell(1).setCellValue(discount.discountValue());
            row.createCell(2).setCellValue(discount.startDate().format(formatter));
            row.createCell(3).setCellValue(discount.endDate().format(formatter));
            row.createCell(4).setCellValue(discount.status() ? "Hoạt động" : "Không hoạt động");

            String discountType = discount.categoryIds() != null && !discount.categoryIds().isEmpty()
                    ? "Theo danh mục" : "Theo khóa học";
            row.createCell(5).setCellValue(discountType);

            String categories = discount.categoryIds() != null && !discount.categoryIds().isEmpty()
                    ? discount.categoryIds().stream()
                    .map(id -> categoryRepository.findById(id.intValue())
                            .map(Category::getTenDanhMuc)
                            .orElse("N/A"))
                    .collect(Collectors.joining(", "))
                    : "N/A";
            row.createCell(6).setCellValue(categories);

            String courses = discount.courseIds() != null && !discount.courseIds().isEmpty()
                    ? discount.courseIds().stream()
                    .map(id -> courseRepository.findById(id)
                            .map(Course::getTen_khoa_hoc)
                            .orElse("N/A"))
                    .collect(Collectors.joining(", "))
                    : "N/A";
            row.createCell(7).setCellValue(courses);
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }

    @Override
    @Transactional
    public List<DiscountDTO> importDiscountsFromExcel(MultipartFile file) throws Exception {
        List<DiscountDTO> importedDiscounts = new ArrayList<>();

        if (file == null || file.isEmpty() || (!file.getOriginalFilename().endsWith(".xlsx") && !file.getOriginalFilename().endsWith(".xls"))) {
            throw new IllegalArgumentException("Vui lòng chọn một tệp Excel (.xlsx hoặc .xls) hợp lệ!");
        }

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowNum = 0;

            for (Row row : sheet) {
                rowNum++;
                if (rowNum == 1 || isRowEmpty(row)) continue; // Bỏ qua dòng tiêu đề hoặc dòng trống

                try {
                    String discountName = getCellValueAsString(row.getCell(0));
                    if (discountName == null || discountName.trim().isEmpty()) {
                        throw new IllegalArgumentException("Tên chương trình không được để trống");
                    }

                    double discountValue = getCellValueAsDouble(row.getCell(1), 0.0);
                    if (discountValue < 0 || discountValue > 100) {
                        throw new IllegalArgumentException("Giá trị giảm phải từ 0 đến 100");
                    }

                    LocalDate startDate = parseDateCell(row.getCell(2));
                    LocalDate endDate = parseDateCell(row.getCell(3));
                    if (startDate == null || endDate == null) {
                        throw new IllegalArgumentException("Ngày bắt đầu hoặc ngày kết thúc không hợp lệ");
                    }
                    if (startDate.isAfter(endDate) || startDate.equals(endDate)) {
                        throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
                    }
                    if (startDate.isBefore(LocalDate.now())) {
                        throw new IllegalArgumentException("Ngày bắt đầu không được trong quá khứ");
                    }

                    LocalDateTime startDateTime = startDate.atStartOfDay();
                    LocalDateTime endDateTime = endDate.atStartOfDay();

                    boolean status = "Hoạt động".equalsIgnoreCase(getCellValueAsString(row.getCell(4)));

                    String discountType = getCellValueAsString(row.getCell(5));
                    if (!discountType.equals("Theo danh mục") && !discountType.equals("Theo khóa học")) {
                        throw new IllegalArgumentException("Loại giảm giá phải là 'Theo danh mục' hoặc 'Theo khóa học'");
                    }

                    List<Long> categoryIds = new ArrayList<>();
                    List<Long> courseIds = new ArrayList<>();

                    String categoriesStr = getCellValueAsString(row.getCell(6));
                    if (categoriesStr != null && !categoriesStr.trim().isEmpty() && !categoriesStr.equals("N/A")) {
                        String[] categories = categoriesStr.split(",");
                        for (String catName : categories) {
                            String trimmedCatName = catName.trim(); // Use a new variable to avoid modifying loop variable
                            Category category = categoryRepository.findByTenDanhMuc(trimmedCatName)
                                    .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại: " + trimmedCatName));
                            categoryIds.add((long) category.getId());
                        }
                    }

                    String coursesStr = getCellValueAsString(row.getCell(7));
                    if (coursesStr != null && !coursesStr.trim().isEmpty() && !coursesStr.equals("N/A")) {
                        String[] courses = coursesStr.split(",");
                        for (String courseName : courses) {
                            String trimmedCourseName = courseName.trim(); // Use a new variable
                            Course course = courseRepository.findByTen_khoa_hoc(trimmedCourseName)
                                    .orElseThrow(() -> new IllegalArgumentException("Khóa học không tồn tại: " + trimmedCourseName));
                            courseIds.add(course.getID_khoa_hoc());
                        }
                    }

                    if (discountType.equals("Theo danh mục") && categoryIds.isEmpty()) {
                        throw new IllegalArgumentException("Phải chọn ít nhất một danh mục khi loại giảm giá là 'Theo danh mục'");
                    }
                    if (discountType.equals("Theo khóa học") && courseIds.isEmpty()) {
                        throw new IllegalArgumentException("Phải chọn ít nhất một khóa học khi loại giảm giá là 'Theo khóa học'");
                    }

                    DiscountDTO discountDTO = new DiscountDTO(
                            null, discountName, discountValue, startDateTime, endDateTime, status,
                            discountType.equals("Theo danh mục") ? categoryIds : null,
                            discountType.equals("Theo khóa học") ? courseIds : null
                    );

                    DiscountDTO savedDiscount = createDiscount(discountDTO);
                    importedDiscounts.add(savedDiscount);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Dòng " + rowNum + ": " + e.getMessage());
                } catch (Exception e) {
                    throw new Exception("Dòng " + rowNum + ": Lỗi không xác định - " + e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            throw new Exception("Lỗi khi nhập tệp Excel: " + e.getMessage(), e);
        }

        return importedDiscounts;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }
                yield String.valueOf((long) cell.getNumericCellValue());
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private double getCellValueAsDouble(Cell cell, double defaultValue) {
        if (cell == null || cell.getCellType() == CellType.BLANK) return defaultValue;
        return cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : defaultValue;
    }

    private LocalDate parseDateCell(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        String cellValue = getCellValueAsString(cell);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try {
            return LocalDate.parse(cellValue, formatter);
        } catch (Exception e) {
            throw new IllegalArgumentException("Định dạng ngày không hợp lệ: " + cellValue);
        }
    }
}