package com.poly.controller;

import com.poly.dto.DiscountDTO;
import com.poly.entity.Category;
import com.poly.entity.Course;
import com.poly.service.CategoryService;
import com.poly.service.CourseService;
import com.poly.service.DiscountService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/discount")
public class DiscountController {

    @Autowired
    private DiscountService discountService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CourseService courseService;

    @GetMapping("")
    public String showDiscountPage(Model model) {
        try {
            List<DiscountDTO> discounts = discountService.getAllDiscounts();
            List<Category> categories = categoryService.getAllCategories();
            List<Course> courses = courseService.findAll();

            model.addAttribute("discounts", discounts);
            model.addAttribute("categories", categories);
            model.addAttribute("courses", courses);
            model.addAttribute("isEdit", false);

            return "Crud_Discount";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi tải trang: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/save")
    public String saveDiscount(
            @RequestParam("discountName") String discountName,
            @RequestParam("discountValue") Double discountValue,
            @RequestParam("startDate") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate endDate,
            @RequestParam("discountType") String discountType,
            @RequestParam(value = "categoryIds[]", required = false) List<Long> categoryIds,
            @RequestParam(value = "courseIds[]", required = false) List<Long> courseIds,
            RedirectAttributes redirectAttributes
    ) {
        try {
            DiscountDTO discountDTO = new DiscountDTO(
                    null,
                    discountName,
                    discountValue,
                    startDate.atStartOfDay(), // Convert LocalDate to LocalDateTime
                    endDate.atTime(23, 59, 59), // Set end of day
                    true,
                    discountType.equals("category") ? categoryIds : null,
                    discountType.equals("course") ? courseIds : null
            );
            discountService.createDiscount(discountDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm chương trình giảm giá thành công!");
            return "redirect:/admin/discount";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu giảm giá: " + e.getMessage());
            redirectAttributes.addFlashAttribute("discountName", discountName);
            redirectAttributes.addFlashAttribute("discountValue", discountValue);
            redirectAttributes.addFlashAttribute("startDate", startDate);
            redirectAttributes.addFlashAttribute("endDate", endDate);
            redirectAttributes.addFlashAttribute("discountType", discountType);
            return "redirect:/admin/discount";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            DiscountDTO discount = discountService.getDiscountById(id);
            List<DiscountDTO> discounts = discountService.getAllDiscounts();
            List<Category> categories = categoryService.getAllCategories();
            List<Course> courses = courseService.findAll();

            model.addAttribute("discount", discount);
            model.addAttribute("discounts", discounts);
            model.addAttribute("categories", categories);
            model.addAttribute("courses", courses);
            model.addAttribute("isEdit", true);

            String discountType = "";
            if (discount.categoryIds() != null && !discount.categoryIds().isEmpty()) {
                discountType = "category";
            } else if (discount.courseIds() != null && !discount.courseIds().isEmpty()) {
                discountType = "course";
            }
            model.addAttribute("discountType", discountType);

            return "Crud_Discount";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi tải thông tin giảm giá: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/update/{id}")
    public String updateDiscount(
            @PathVariable Long id,
            @RequestParam("discountName") String discountName,
            @RequestParam("discountValue") Double discountValue,
            @RequestParam("startDate") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate endDate,
            @RequestParam("discountType") String discountType,
            @RequestParam(value = "categoryIds[]", required = false) List<Long> categoryIds,
            @RequestParam(value = "courseIds[]", required = false) List<Long> courseIds,
            @RequestParam(value = "status", required = false) Boolean status,
            RedirectAttributes redirectAttributes
    ) {
        try {
            DiscountDTO discountDTO = new DiscountDTO(
                    id,
                    discountName,
                    discountValue,
                    startDate.atStartOfDay(), // Convert LocalDate to LocalDateTime
                    endDate.atTime(23, 59, 59), // Set end of day
                    status != null ? status : true,
                    discountType.equals("category") ? categoryIds : null,
                    discountType.equals("course") ? courseIds : null
            );
            discountService.updateDiscount(id, discountDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật chương trình giảm giá thành công!");
            return "redirect:/admin/discount";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật giảm giá: " + e.getMessage());
            return "redirect:/admin/discount/edit/" + id;
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteDiscount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            discountService.deleteDiscount(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa chương trình giảm giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa giảm giá: " + e.getMessage());
        }
        return "redirect:/admin/discount";
    }

    @GetMapping("/active")
    @ResponseBody
    public List<DiscountDTO> getActiveDiscounts() {
        return discountService.getActiveDiscounts();
    }

    @GetMapping("/export")
    public void exportDiscountsToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=discounts.xlsx");

        Workbook workbook = discountService.exportDiscountsToExcel();
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @PostMapping("/import")
    public String importDiscountsFromExcel(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn một tệp Excel để nhập!");
                return "redirect:/admin/discount";
            }
            if (!file.getOriginalFilename().endsWith(".xlsx") && !file.getOriginalFilename().endsWith(".xls")) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn một tệp Excel (.xlsx hoặc .xls) hợp lệ!");
                return "redirect:/admin/discount";
            }
            List<DiscountDTO> importedDiscounts = discountService.importDiscountsFromExcel(file);
            redirectAttributes.addFlashAttribute("successMessage", "Đã nhập " + importedDiscounts.size() + " chương trình giảm giá từ Excel!");
            return "redirect:/admin/discount";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu trong tệp không hợp lệ: " + e.getMessage());
            return "redirect:/admin/discount";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi nhập Excel: " + e.getMessage());
            return "redirect:/admin/discount";
        }
    }
}