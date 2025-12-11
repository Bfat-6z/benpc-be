package com.java04.agm_api.controller;

import com.java04.agm_api.entity.*;
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
@RequestMapping("/api/admin")
public class AdminApiController {

    private final AdminService adminService;
    private final NguoiDungService nguoiDungService;

    public AdminApiController(AdminService adminService, NguoiDungService nguoiDungService) {
        this.adminService = adminService;
        this.nguoiDungService = nguoiDungService;
    }

    // Helper method để verify admin role
    private NguoiDung verifyAdmin(@RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            throw new RuntimeException("Cần đăng nhập để truy cập");
        }
        
        NguoiDung nguoiDung = nguoiDungService.timTheoMa(userId);
        if (nguoiDung == null) {
            throw new RuntimeException("Không tìm thấy người dùng");
        }
        
        // Kiểm tra role admin - hỗ trợ nhiều format
        Quyen quyen = nguoiDung.getQuyen();
        if (quyen == null || quyen.getTenQuyen() == null) {
            throw new RuntimeException("Bạn không có quyền truy cập trang này");
        }
        
        String tenQuyen = quyen.getTenQuyen().toLowerCase().trim();
        boolean isAdmin = tenQuyen.contains("admin") || tenQuyen.equals("admin");
        
        if (!isAdmin) {
            throw new RuntimeException("Bạn không có quyền truy cập trang này");
        }
        
        return nguoiDung;
    }

    // Helper method để verify admin hoặc nhân viên role
    private NguoiDung verifyAdminOrStaff(@RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            throw new RuntimeException("Cần đăng nhập để truy cập");
        }
        
        NguoiDung nguoiDung = nguoiDungService.timTheoMa(userId);
        if (nguoiDung == null) {
            throw new RuntimeException("Không tìm thấy người dùng");
        }
        
        // Kiểm tra role admin hoặc nhân viên - hỗ trợ nhiều format
        Quyen quyen = nguoiDung.getQuyen();
        if (quyen == null || quyen.getTenQuyen() == null) {
            throw new RuntimeException("Bạn không có quyền truy cập trang này");
        }
        
        String tenQuyen = quyen.getTenQuyen().toLowerCase().trim();
        boolean isAdmin = tenQuyen.contains("admin") || tenQuyen.equals("admin");
        boolean isNhanVien = tenQuyen.contains("nhan") || tenQuyen.contains("viên") || 
                            tenQuyen.contains("staff") || tenQuyen.contains("employee") ||
                            tenQuyen.contains("nhanvien") || tenQuyen.contains("nhan_vien");
        
        if (!isAdmin && !isNhanVien) {
            throw new RuntimeException("Bạn không có quyền truy cập trang này");
        }
        
        return nguoiDung;
    }

    // ========== USER MANAGEMENT ==========

    @GetMapping("/nguoi-dung")
    public ResponseEntity<?> layDanhSachNguoiDung(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            @RequestParam(required = false) String tuKhoa,
            @RequestParam(required = false) String vaiTro,
            @RequestParam(defaultValue = "0") int trang,
            @RequestParam(defaultValue = "20") int kichThuoc) {
        try {
            verifyAdmin(userId);
            
            Page<NguoiDung> page = adminService.layDanhSachNguoiDung(tuKhoa, vaiTro, trang, kichThuoc);
            
            // Convert to response format
            Map<String, Object> response = new HashMap<>();
            response.put("content", page.getContent().stream()
                .map(this::toNguoiDungResponse)
                .toList());
            response.put("totalElements", page.getTotalElements());
            response.put("totalPages", page.getTotalPages());
            response.put("number", page.getNumber());
            response.put("size", page.getSize());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/nguoi-dung/{userId}/role")
    public ResponseEntity<?> capNhatRoleNguoiDung(
            @RequestHeader(value = "X-User-Id", required = false) Integer adminUserId,
            @PathVariable Integer userId,
            @RequestBody UpdateRoleRequest request) {
        try {
            verifyAdmin(adminUserId);
            
            NguoiDung updated = adminService.capNhatRoleNguoiDung(userId, request.getVaiTro());
            return ResponseEntity.ok(toNguoiDungResponse(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/nguoi-dung/{userId}/status")
    public ResponseEntity<?> capNhatTrangThaiNguoiDung(
            @RequestHeader(value = "X-User-Id", required = false) Integer adminUserId,
            @PathVariable Integer userId,
            @RequestBody UpdateUserStatusRequest request) {
        try {
            verifyAdmin(adminUserId);

            NguoiDung updated = adminService.capNhatTrangThaiNguoiDung(userId, request.getTrangThai());
            return ResponseEntity.ok(toNguoiDungResponse(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== PRODUCT MANAGEMENT ==========

    @GetMapping("/san-pham")
    public ResponseEntity<?> layDanhSachSanPham(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            @RequestParam(required = false) String tuKhoa,
            @RequestParam(required = false) Integer maDanhMuc,
            @RequestParam(defaultValue = "0") int trang,
            @RequestParam(defaultValue = "20") int kichThuoc) {
        try {
            verifyAdmin(userId);
            
            Page<SanPham> page = adminService.layDanhSachSanPham(tuKhoa, maDanhMuc, trang, kichThuoc);
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", page.getContent());
            response.put("totalElements", page.getTotalElements());
            response.put("totalPages", page.getTotalPages());
            response.put("number", page.getNumber());
            response.put("size", page.getSize());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/san-pham")
    public ResponseEntity<?> taoSanPham(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            @RequestBody SanPham sanPham) {
        try {
            verifyAdmin(userId);
            
            SanPham saved = adminService.taoSanPham(sanPham);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/san-pham/{maSP}")
    public ResponseEntity<?> capNhatSanPham(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            @PathVariable Integer maSP,
            @RequestBody SanPham sanPhamMoi) {
        try {
            verifyAdmin(userId);
            
            SanPham updated = adminService.capNhatSanPham(maSP, sanPhamMoi);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/san-pham/{maSP}")
    public ResponseEntity<?> xoaSanPham(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            @PathVariable Integer maSP) {
        try {
            verifyAdmin(userId);
            
            adminService.xoaSanPham(maSP);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== CATEGORY MANAGEMENT ==========

    @GetMapping("/danh-muc")
    public ResponseEntity<?> layDanhSachDanhMuc(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        try {
            verifyAdmin(userId);
            
            List<DanhMucResponse> danhMucs = adminService.layDanhSachDanhMuc().stream()
                .map(dm -> {
                    DanhMucResponse response = new DanhMucResponse();
                    response.setMaDanhMuc(dm.getMaDanhMuc());
                    response.setTenDanhMuc(dm.getTenDanhMuc());
                    response.setSoSanPham(adminService.demSoSanPhamTheoDanhMuc(dm.getMaDanhMuc()));
                    return response;
                })
                .toList();
            return ResponseEntity.ok(danhMucs);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/danh-muc")
    public ResponseEntity<?> taoDanhMuc(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            @RequestBody DanhMuc danhMuc) {
        try {
            verifyAdmin(userId);
            
            DanhMuc saved = adminService.taoDanhMuc(danhMuc);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/danh-muc/{maDanhMuc}")
    public ResponseEntity<?> capNhatDanhMuc(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            @PathVariable Integer maDanhMuc,
            @RequestBody DanhMuc danhMucMoi) {
        try {
            verifyAdmin(userId);
            
            DanhMuc updated = adminService.capNhatDanhMuc(maDanhMuc, danhMucMoi);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/danh-muc/{maDanhMuc}")
    public ResponseEntity<?> xoaDanhMuc(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            @PathVariable Integer maDanhMuc) {
        try {
            verifyAdmin(userId);
            
            adminService.xoaDanhMuc(maDanhMuc);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== ORDER MANAGEMENT ==========

    @GetMapping("/hoa-don")
    public ResponseEntity<?> layDanhSachHoaDon(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            @RequestParam(required = false) String tuKhoa,
            @RequestParam(required = false) String trangThai,
            @RequestParam(defaultValue = "0") int trang,
            @RequestParam(defaultValue = "20") int kichThuoc) {
        try {
            verifyAdmin(userId);
            
            Page<HoaDon> page = adminService.layDanhSachHoaDon(tuKhoa, trangThai, trang, kichThuoc);
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", page.getContent());
            response.put("totalElements", page.getTotalElements());
            response.put("totalPages", page.getTotalPages());
            response.put("number", page.getNumber());
            response.put("size", page.getSize());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/hoa-don/{maHoaDon}")
    public ResponseEntity<?> xemChiTietHoaDon(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            @PathVariable Integer maHoaDon) {
        try {
            System.out.println("DEBUG AdminApiController.xemChiTietHoaDon: userId=" + userId + ", maHoaDon=" + maHoaDon);
            NguoiDung nguoiDung = verifyAdminOrStaff(userId);
            System.out.println("DEBUG AdminApiController.xemChiTietHoaDon: User verified, role=" + 
                             (nguoiDung.getQuyen() != null ? nguoiDung.getQuyen().getTenQuyen() : "null"));
            
            // Admin/Staff có thể xem bất kỳ đơn hàng nào
            HoaDon hoaDon = adminService.xemChiTietHoaDon(maHoaDon);
            System.out.println("DEBUG AdminApiController.xemChiTietHoaDon: Order found, returning");
            return ResponseEntity.ok(hoaDon);
        } catch (RuntimeException e) {
            System.out.println("DEBUG AdminApiController.xemChiTietHoaDon: Error - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/hoa-don/{maHoaDon}/status")
    public ResponseEntity<?> capNhatTrangThaiHoaDon(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            @PathVariable Integer maHoaDon,
            @RequestBody UpdateStatusRequest request) {
        try {
            verifyAdmin(userId);
            
            HoaDon updated = adminService.capNhatTrangThaiHoaDon(maHoaDon, request.getTrangThai());
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== STATISTICS ==========

    @GetMapping("/thong-ke")
    public ResponseEntity<?> layThongKe(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        try {
            verifyAdmin(userId);
            
            Map<String, Object> stats = adminService.layThongKe();
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== HELPER METHODS ==========

    private NguoiDungResponse toNguoiDungResponse(NguoiDung nguoiDung) {
        NguoiDungResponse response = new NguoiDungResponse();
        response.setMaNguoiDung(nguoiDung.getMaNguoiDung());
        response.setTenNguoiDung(nguoiDung.getTenNguoiDung());
        response.setEmail(nguoiDung.getEmail());
        response.setDienThoai(nguoiDung.getDienThoai());
        response.setGioiTinh(nguoiDung.getGioiTinh());
        response.setAvatar(nguoiDung.getAvatar());
        response.setTrangThai(nguoiDung.getTrangThai());
        if (nguoiDung.getQuyen() != null) {
            response.setVaiTro(nguoiDung.getQuyen().getTenQuyen());
            response.setQuyen(new QuyenResponse(
                nguoiDung.getQuyen().getMaQuyen(),
                nguoiDung.getQuyen().getTenQuyen()
            ));
        }
        return response;
    }

    // ========== DTOs ==========

    public static class UpdateRoleRequest {
        private String vaiTro;

        public String getVaiTro() { return vaiTro; }
        public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }
    }

    public static class UpdateUserStatusRequest {
        private Boolean trangThai;

        public Boolean getTrangThai() { return trangThai; }
        public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }
    }

    public static class UpdateStatusRequest {
        private String trangThai;

        public String getTrangThai() { return trangThai; }
        public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    }

    public static class NguoiDungResponse {
        private Integer maNguoiDung;
        private String tenNguoiDung;
        private String email;
        private String dienThoai;
        private Boolean gioiTinh;
        private String avatar;
        private String vaiTro;
        private QuyenResponse quyen;
        private Boolean trangThai;

        // Getters and Setters
        public Integer getMaNguoiDung() { return maNguoiDung; }
        public void setMaNguoiDung(Integer maNguoiDung) { this.maNguoiDung = maNguoiDung; }
        public String getTenNguoiDung() { return tenNguoiDung; }
        public void setTenNguoiDung(String tenNguoiDung) { this.tenNguoiDung = tenNguoiDung; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getDienThoai() { return dienThoai; }
        public void setDienThoai(String dienThoai) { this.dienThoai = dienThoai; }
        public Boolean getGioiTinh() { return gioiTinh; }
        public void setGioiTinh(Boolean gioiTinh) { this.gioiTinh = gioiTinh; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
        public String getVaiTro() { return vaiTro; }
        public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }
        public QuyenResponse getQuyen() { return quyen; }
        public void setQuyen(QuyenResponse quyen) { this.quyen = quyen; }
        public Boolean getTrangThai() { return trangThai; }
        public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }
    }

    public static class QuyenResponse {
        private Integer maQuyen;
        private String tenQuyen;

        public QuyenResponse() {}
        
        public QuyenResponse(Integer maQuyen, String tenQuyen) {
            this.maQuyen = maQuyen;
            this.tenQuyen = tenQuyen;
        }

        public Integer getMaQuyen() { return maQuyen; }
        public void setMaQuyen(Integer maQuyen) { this.maQuyen = maQuyen; }
        public String getTenQuyen() { return tenQuyen; }
        public void setTenQuyen(String tenQuyen) { this.tenQuyen = tenQuyen; }
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
}

