package com.java04.agm_api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java04.agm_api.entity.NguoiDung;
import com.java04.agm_api.entity.Quyen;
import com.java04.agm_api.repo.NguoiDungRepository;
import com.java04.agm_api.repo.QuyenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class NguoiDungService {

    private final NguoiDungRepository nguoiDungRepository;
    private final QuyenRepository quyenRepository;
    private final PasswordEncoder passwordEncoder;

    public NguoiDungService(NguoiDungRepository nguoiDungRepository,
                           QuyenRepository quyenRepository,
                           PasswordEncoder passwordEncoder) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.quyenRepository = quyenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public NguoiDung timTheoEmail(String email) {
        return nguoiDungRepository.findByEmail(email);
    }

    public NguoiDung timTheoMa(Integer maNguoiDung) {
        NguoiDung nd = nguoiDungRepository.findById(maNguoiDung)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        if (nd.getTrangThai() != null && !nd.getTrangThai()) {
            throw new RuntimeException("Tài khoản đã bị khóa, vui lòng liên hệ quản trị.");
        }
        return nd;
    }

    public NguoiDung dangKy(NguoiDung nguoiDung) {
        // Kiểm tra email đã tồn tại chưa
        if (nguoiDungRepository.findByEmail(nguoiDung.getEmail()) != null) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        // Hash password
        if (nguoiDung.getMatKhau() != null && !nguoiDung.getMatKhau().isEmpty()) {
            nguoiDung.setMatKhau(passwordEncoder.encode(nguoiDung.getMatKhau()));
        }

        // Set trạng thái mặc định: hoạt động
        nguoiDung.setTrangThai(true);

        // Set quyền mặc định là "USER" (hoặc lấy từ DB)
        Quyen quyenUser = quyenRepository.findByTenQuyen("USER");
        if (quyenUser == null) {
            // Nếu chưa có quyền USER, tạo mới hoặc dùng quyền đầu tiên
            quyenUser = quyenRepository.findAll().stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy quyền nào trong hệ thống"));
        }
        nguoiDung.setQuyen(quyenUser);

        return nguoiDungRepository.save(nguoiDung);
    }

    public NguoiDung dangNhap(String email, String matKhau) {
        NguoiDung nguoiDung = nguoiDungRepository.findByEmail(email);
        if (nguoiDung == null) {
            throw new RuntimeException("Email hoặc mật khẩu không đúng");
        }

        if (Boolean.FALSE.equals(nguoiDung.getTrangThai())) {
            throw new RuntimeException("Tài khoản đã bị khóa, vui lòng liên hệ quản trị.");
        }

        // Kiểm tra password (có thể null nếu đăng nhập bằng Google)
        if (nguoiDung.getMatKhau() != null && !nguoiDung.getMatKhau().isEmpty()) {
            if (!passwordEncoder.matches(matKhau, nguoiDung.getMatKhau())) {
                throw new RuntimeException("Email hoặc mật khẩu không đúng");
            }
        } else {
            // Nếu không có password (đăng nhập bằng Google), không kiểm tra
            // Nhưng nếu có password trong request thì vẫn phải đúng
            if (matKhau != null && !matKhau.isEmpty()) {
                throw new RuntimeException("Email hoặc mật khẩu không đúng");
            }
        }

        return nguoiDung;
    }

    public NguoiDung capNhatThongTin(Integer maNguoiDung, NguoiDung thongTinMoi) {
        NguoiDung nguoiDung = timTheoMa(maNguoiDung);
        
        if (thongTinMoi.getTenNguoiDung() != null) {
            nguoiDung.setTenNguoiDung(thongTinMoi.getTenNguoiDung());
        }
        if (thongTinMoi.getDienThoai() != null) {
            nguoiDung.setDienThoai(thongTinMoi.getDienThoai());
        }
        if (thongTinMoi.getGioiTinh() != null) {
            nguoiDung.setGioiTinh(thongTinMoi.getGioiTinh());
        }
        if (thongTinMoi.getAvatar() != null) {
            nguoiDung.setAvatar(thongTinMoi.getAvatar());
        }

        return nguoiDungRepository.save(nguoiDung);
    }

    public void doiMatKhau(Integer maNguoiDung, String matKhauCu, String matKhauMoi) {
        NguoiDung nguoiDung = timTheoMa(maNguoiDung);
        
        // Kiểm tra mật khẩu cũ
        if (nguoiDung.getMatKhau() != null && !nguoiDung.getMatKhau().isEmpty()) {
            if (!passwordEncoder.matches(matKhauCu, nguoiDung.getMatKhau())) {
                throw new RuntimeException("Mật khẩu cũ không đúng");
            }
        }

        // Cập nhật mật khẩu mới
        nguoiDung.setMatKhau(passwordEncoder.encode(matKhauMoi));
        nguoiDungRepository.save(nguoiDung);
    }

    public NguoiDung dangNhapGoogle(String credential) {
        try {
            // Verify Google token bằng cách gọi Google API
            String googleVerifyUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + credential;
            URL url = new URL(googleVerifyUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Token Google không hợp lệ");
            }
            
            // Đọc response
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            // Parse JSON response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response.toString());
            
            String email = jsonNode.get("email").asText();
            String name = jsonNode.has("name") ? jsonNode.get("name").asText() : email;
            String picture = jsonNode.has("picture") ? jsonNode.get("picture").asText() : null;
            
            // Tìm hoặc tạo user
            NguoiDung nguoiDung = nguoiDungRepository.findByEmail(email);
            
            if (nguoiDung == null) {
                // Tạo user mới nếu chưa có
                nguoiDung = new NguoiDung();
                nguoiDung.setEmail(email);
                nguoiDung.setTenNguoiDung(name);
                nguoiDung.setAvatar(picture);
                nguoiDung.setMatKhau(null); // Không có password cho Google login
                
                // Set quyền mặc định
                Quyen quyenUser = quyenRepository.findByTenQuyen("USER");
                if (quyenUser == null) {
                    quyenUser = quyenRepository.findAll().stream()
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy quyền nào trong hệ thống"));
                }
                nguoiDung.setQuyen(quyenUser);
                
                nguoiDung = nguoiDungRepository.save(nguoiDung);
            } else {
                // Cập nhật thông tin nếu có thay đổi
                boolean updated = false;
                if (nguoiDung.getTenNguoiDung() == null || !nguoiDung.getTenNguoiDung().equals(name)) {
                    nguoiDung.setTenNguoiDung(name);
                    updated = true;
                }
                if (picture != null && (nguoiDung.getAvatar() == null || !nguoiDung.getAvatar().equals(picture))) {
                    nguoiDung.setAvatar(picture);
                    updated = true;
                }
                if (updated) {
                    nguoiDung = nguoiDungRepository.save(nguoiDung);
                }
            }
            
            return nguoiDung;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xác thực Google: " + e.getMessage());
        }
    }
}
