package com.java04.agm_api.controller;

import com.java04.agm_api.entity.DanhMuc;
import com.java04.agm_api.entity.HoaDon;
import com.java04.agm_api.entity.NguoiDung;
import com.java04.agm_api.entity.SanPham;
import com.java04.agm_api.service.AdminService;
import com.java04.agm_api.service.NguoiDungService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
public class StaffApiController {

    private final AdminService adminService;
    private final NguoiDungService nguoiDungService;

    public StaffApiController(AdminService adminService, NguoiDungService nguoiDungService) {
        this.adminService = adminService;
        this.nguoiDungService = nguoiDungService;
    }

    // Helper: verify staff or admin
    private NguoiDung verifyStaff(@RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            throw new RuntimeException("Cần đăng nhập để truy cập");
        }
        NguoiDung nguoiDung = nguoiDungService.timTheoMa(userId);
        if (nguoiDung == null) {
            throw new RuntimeException("Không tìm thấy người dùng");
        }
        String tenQuyen = nguoiDung.getQuyen() != null ? nguoiDung.getQuyen().getTenQuyen() : "";
        String roleLower = tenQuyen == null ? "" : tenQuyen.toLowerCase();
        boolean isStaff = roleLower.contains("nhan") || roleLower.contains("viên") || roleLower.contains("staff");
        boolean isAdmin = roleLower.contains("admin");
        if (!isStaff && !isAdmin) {
            throw new RuntimeException("Bạn không có quyền truy cập trang này");
        }
        return nguoiDung;
    }

    // ========== ORDERS ==========
    @GetMapping("/hoa-don")
    public ResponseEntity<?> layDanhSachHoaDon(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            @RequestParam(required = false) String tuKhoa,
            @RequestParam(required = false) String trangThai,
            @RequestParam(defaultValue = "0") int trang,
            @RequestParam(defaultValue = "100") int kichThuoc) {
        try {
            verifyStaff(userId);
            String normalizedStatus = normalizeStatus(trangThai);
            Page<HoaDon> page = adminService.layDanhSachHoaDon(tuKhoa, normalizedStatus, trang, kichThuoc);
            Map<String, Object> response = new HashMap<>();
            response.put("content", page.getContent());
            response.put("totalElements", page.getTotalElements());
            response.put("totalPages", page.getTotalPages());
            response.put("number", page.getNumber());
            response.put("size", page.getSize());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/hoa-don/{maHoaDon}/status")
    public ResponseEntity<?> capNhatTrangThaiHoaDon(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            @PathVariable Integer maHoaDon,
            @RequestBody AdminApiController.UpdateStatusRequest request) {
        try {
            verifyStaff(userId);
            HoaDon updated = adminService.capNhatTrangThaiHoaDon(maHoaDon, request.getTrangThai());
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // ========== PRODUCTS ==========
    @GetMapping("/san-pham")
    public ResponseEntity<?> layDanhSachSanPham(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            @RequestParam(required = false) String tuKhoa,
            @RequestParam(required = false) Integer maDanhMuc,
            @RequestParam(defaultValue = "0") int trang,
            @RequestParam(defaultValue = "1000") int kichThuoc) {
        try {
            verifyStaff(userId);
            Page<SanPham> page = adminService.layDanhSachSanPham(tuKhoa, maDanhMuc, trang, kichThuoc);
            Map<String, Object> response = new HashMap<>();
            response.put("content", page.getContent());
            response.put("totalElements", page.getTotalElements());
            response.put("totalPages", page.getTotalPages());
            response.put("number", page.getNumber());
            response.put("size", page.getSize());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/san-pham/{maSP}/stock")
    public ResponseEntity<?> capNhatTonKho(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            @PathVariable Integer maSP,
            @RequestBody UpdateStockRequest request) {
        try {
            verifyStaff(userId);
            SanPham updated = adminService.capNhatTonKho(maSP, request.getTonKho());
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/san-pham")
    public ResponseEntity<?> taoSanPham(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            @RequestBody SanPham sanPham) {
        try {
            verifyStaff(userId);
            SanPham created = adminService.taoSanPham(sanPham);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/san-pham/{maSP}")
    public ResponseEntity<?> capNhatSanPham(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            @PathVariable Integer maSP,
            @RequestBody SanPham sanPham) {
        try {
            verifyStaff(userId);
            SanPham updated = adminService.capNhatSanPham(maSP, sanPham);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // ========== CATEGORIES ==========
    @GetMapping("/danh-muc")
    public ResponseEntity<?> layDanhSachDanhMuc(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        try {
            verifyStaff(userId);
            List<DanhMucResponse> danhMucs = adminService.layDanhSachDanhMuc().stream()
                .map(dm -> {
                    DanhMucResponse res = new DanhMucResponse();
                    res.setMaDanhMuc(dm.getMaDanhMuc());
                    res.setTenDanhMuc(dm.getTenDanhMuc());
                    res.setSoSanPham(adminService.demSoSanPhamTheoDanhMuc(dm.getMaDanhMuc()));
                    return res;
                })
                .toList();
            return ResponseEntity.ok(danhMucs);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/danh-muc")
    public ResponseEntity<?> taoDanhMuc(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            @RequestBody DanhMuc danhMuc) {
        try {
            verifyStaff(userId);
            DanhMuc created = adminService.taoDanhMuc(danhMuc);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/danh-muc/{maDanhMuc}")
    public ResponseEntity<?> capNhatDanhMuc(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            @PathVariable Integer maDanhMuc,
            @RequestBody DanhMuc danhMuc) {
        try {
            verifyStaff(userId);
            DanhMuc updated = adminService.capNhatDanhMuc(maDanhMuc, danhMuc);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // ========== DTO ==========
    public static class UpdateStockRequest {
        private Integer tonKho;
        public Integer getTonKho() { return tonKho; }
        public void setTonKho(Integer tonKho) { this.tonKho = tonKho; }
    }

    public static class DanhMucResponse {
        private Integer maDanhMuc;
        private String tenDanhMuc;
        private Long soSanPham;

        public Integer getMaDanhMuc() { return maDanhMuc; }
        public void setMaDanhMuc(Integer maDanhMuc) { this.maDanhMuc = maDanhMuc; }
        public String getTenDanhMuc() { return tenDanhMuc; }
        public void setTenDanhMuc(String tenDanhMuc) { this.tenDanhMuc = tenDanhMuc; }
        public Long getSoSanPham() { return soSanPham; }
        public void setSoSanPham(Long soSanPham) { this.soSanPham = soSanPham; }
    }

    // Helper: normalize Vietnamese/English status to backend enum codes
    private String normalizeStatus(String input) {
        if (input == null) return null;
        String s = removeDiacritics(input).trim().toLowerCase().replaceAll("[\\s\\-_]+", "");
        if (s.isEmpty()) return null;
        if (s.contains("choxacnhan") || s.equals("cho")) return "CHO_XAC_NHAN";
        if (s.contains("dangxuly") || s.contains("xuly")) return "DANG_XU_LY";
        if (s.contains("danggiao") || s.contains("giaohang")) return "DANG_GIAO";
        if (s.contains("dagiao") || s.equals("giao")) return "DA_GIAO";
        if (s.contains("dahuy") || s.contains("huy")) return "DA_HUY";
        // Already in correct form?
        if (s.equals("cho_xac_nhan") || s.equals("cho_xacnhan") || s.equals("chờxácnhận"))
            return "CHO_XAC_NHAN";
        if (s.equals("dang_xu_ly") || s.equals("dangxuly")) return "DANG_XU_LY";
        if (s.equals("dang_giao") || s.equals("danggiao")) return "DANG_GIAO";
        if (s.equals("da_giao") || s.equals("dagiao")) return "DA_GIAO";
        if (s.equals("da_huy") || s.equals("dahuy")) return "DA_HUY";
        return input; // fallback: keep original
    }

    // Remove Vietnamese accents
    private String removeDiacritics(String str) {
        if (str == null) return "";
        return java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}

