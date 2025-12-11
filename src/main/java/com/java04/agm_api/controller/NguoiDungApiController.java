package com.java04.agm_api.controller;

import com.java04.agm_api.entity.NguoiDung;
import com.java04.agm_api.service.NguoiDungService;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/nguoi-dung")
public class NguoiDungApiController {

    private static final Logger logger = LoggerFactory.getLogger(NguoiDungApiController.class);

    private final NguoiDungService nguoiDungService;

    public NguoiDungApiController(NguoiDungService nguoiDungService) {
        this.nguoiDungService = nguoiDungService;
        logger.info("NguoiDungApiController initialized");
    }

    // GET /api/nguoi-dung/test - Test endpoint để verify controller được load
    @GetMapping("/test")
    public String test() {
        logger.info("Test endpoint called");
        return "NguoiDungApiController is working!";
    }

    // POST /api/nguoi-dung/register
    @PostMapping("/register")
    public NguoiDungResponse dangKy(@RequestBody RegisterRequest request) {
        logger.info("Register endpoint called with email: {}", request.getEmail());
        try {
            // Validate input
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                throw new RuntimeException("Email không được để trống");
            }
            if (request.getTen() == null || request.getTen().trim().isEmpty()) {
                throw new RuntimeException("Tên không được để trống");
            }
            if (request.getPassword() == null || request.getPassword().length() < 6) {
                throw new RuntimeException("Mật khẩu phải có ít nhất 6 ký tự");
            }

            NguoiDung nguoiDung = new NguoiDung();
            String tenNguoiDung = (request.getHo() != null && !request.getHo().trim().isEmpty() 
                ? request.getHo().trim() + " " : "") + (request.getTen() != null ? request.getTen().trim() : "");
            nguoiDung.setTenNguoiDung(tenNguoiDung.trim());
            nguoiDung.setEmail(request.getEmail().trim());
            nguoiDung.setDienThoai(request.getSoDienThoai());
            nguoiDung.setMatKhau(request.getPassword());
            nguoiDung.setAvatar(request.getAvatar());

            NguoiDung saved = nguoiDungService.dangKy(nguoiDung);
            return toResponse(saved);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi đăng ký: " + e.getMessage());
        }
    }

    // POST /api/nguoi-dung/login
    @PostMapping("/login")
    public NguoiDungResponse dangNhap(@RequestBody LoginRequest request) {
        NguoiDung nguoiDung = nguoiDungService.dangNhap(request.getEmail(), request.getPassword());
        return toResponse(nguoiDung);
    }

    // POST /api/nguoi-dung/google-login
    @PostMapping("/google-login")
    public NguoiDungResponse dangNhapGoogle(@RequestBody GoogleLoginRequest request) {
        logger.info("Google login endpoint called");
        try {
            if (request.getCredential() == null || request.getCredential().trim().isEmpty()) {
                throw new RuntimeException("Credential không được để trống");
            }
            NguoiDung nguoiDung = nguoiDungService.dangNhapGoogle(request.getCredential());
            return toResponse(nguoiDung);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            logger.error("Error in Google login: ", e);
            throw new RuntimeException("Lỗi khi đăng nhập bằng Google: " + e.getMessage());
        }
    }

    // GET /api/nguoi-dung/current?userId=1
    @GetMapping("/current")
    public NguoiDungResponse layThongTinHienTai(@RequestParam Integer userId) {
        NguoiDung nguoiDung = nguoiDungService.timTheoMa(userId);
        return toResponse(nguoiDung);
    }

    // PUT /api/nguoi-dung/update-profile
    @PutMapping("/update-profile")
    public NguoiDungResponse capNhatThongTin(@RequestParam Integer userId,
                                             @RequestBody UpdateProfileRequest request) {
        NguoiDung thongTinMoi = new NguoiDung();
        thongTinMoi.setTenNguoiDung(request.getTenNguoiDung());
        thongTinMoi.setDienThoai(request.getDienThoai());
        thongTinMoi.setGioiTinh(request.getGioiTinh());

        NguoiDung updated = nguoiDungService.capNhatThongTin(userId, thongTinMoi);
        return toResponse(updated);
    }

    // PUT /api/nguoi-dung/change-password
    @PutMapping("/change-password")
    public MessageResponse doiMatKhau(@RequestParam Integer userId,
                                     @RequestBody ChangePasswordRequest request) {
        nguoiDungService.doiMatKhau(userId, request.getMatKhauCu(), request.getMatKhauMoi());
        return new MessageResponse("Đổi mật khẩu thành công");
    }

    // PUT /api/nguoi-dung/update-avatar
    @PutMapping("/update-avatar")
    public NguoiDungResponse capNhatAvatar(@RequestParam Integer userId,
                                          @RequestBody UpdateAvatarRequest request) {
        NguoiDung thongTinMoi = new NguoiDung();
        thongTinMoi.setAvatar(request.getAvatar());

        NguoiDung updated = nguoiDungService.capNhatThongTin(userId, thongTinMoi);
        return toResponse(updated);
    }

    // Helper method để loại bỏ password khỏi response
    private NguoiDungResponse toResponse(NguoiDung nguoiDung) {
        NguoiDungResponse response = new NguoiDungResponse();
        response.setMaNguoiDung(nguoiDung.getMaNguoiDung());
        response.setTenNguoiDung(nguoiDung.getTenNguoiDung());
        response.setEmail(nguoiDung.getEmail());
        response.setDienThoai(nguoiDung.getDienThoai());
        response.setGioiTinh(nguoiDung.getGioiTinh());
        response.setAvatar(nguoiDung.getAvatar());
        if (nguoiDung.getQuyen() != null) {
            response.setMaQuyen(nguoiDung.getQuyen().getMaQuyen());
            response.setTenQuyen(nguoiDung.getQuyen().getTenQuyen());
        }
        return response;
    }

    // ===== DTOs =====

    public static class RegisterRequest {
        private String ho;
        private String ten;
        private String email;
        private String soDienThoai;
        private String password;
        private String diaChi;
        private String avatar;

        // Getters and Setters
        public String getHo() { return ho; }
        public void setHo(String ho) { this.ho = ho; }
        public String getTen() { return ten; }
        public void setTen(String ten) { this.ten = ten; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getSoDienThoai() { return soDienThoai; }
        public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getDiaChi() { return diaChi; }
        public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
    }

    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class NguoiDungResponse {
        private Integer maNguoiDung;
        private String tenNguoiDung;
        private String email;
        private String dienThoai;
        private Boolean gioiTinh;
        private String avatar;
        private Integer maQuyen;
        private String tenQuyen;

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
        public Integer getMaQuyen() { return maQuyen; }
        public void setMaQuyen(Integer maQuyen) { this.maQuyen = maQuyen; }
        public String getTenQuyen() { return tenQuyen; }
        public void setTenQuyen(String tenQuyen) { this.tenQuyen = tenQuyen; }
    }

    public static class UpdateProfileRequest {
        private String tenNguoiDung;
        private String dienThoai;
        private Boolean gioiTinh;

        public String getTenNguoiDung() { return tenNguoiDung; }
        public void setTenNguoiDung(String tenNguoiDung) { this.tenNguoiDung = tenNguoiDung; }
        public String getDienThoai() { return dienThoai; }
        public void setDienThoai(String dienThoai) { this.dienThoai = dienThoai; }
        public Boolean getGioiTinh() { return gioiTinh; }
        public void setGioiTinh(Boolean gioiTinh) { this.gioiTinh = gioiTinh; }
    }

    public static class ChangePasswordRequest {
        private String matKhauCu;
        private String matKhauMoi;

        public String getMatKhauCu() { return matKhauCu; }
        public void setMatKhauCu(String matKhauCu) { this.matKhauCu = matKhauCu; }
        public String getMatKhauMoi() { return matKhauMoi; }
        public void setMatKhauMoi(String matKhauMoi) { this.matKhauMoi = matKhauMoi; }
    }

    public static class UpdateAvatarRequest {
        private String avatar;

        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
    }

    public static class GoogleLoginRequest {
        private String credential;

        public String getCredential() { return credential; }
        public void setCredential(String credential) { this.credential = credential; }
    }

    public static class MessageResponse {
        private String message;

        public MessageResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}

