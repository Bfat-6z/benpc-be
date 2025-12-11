package com.java04.agm_api.service;

import com.java04.agm_api.entity.*;
import com.java04.agm_api.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class HoaDonService {

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final GioHangRepository gioHangRepository;
    private final DiaChiRepository diaChiRepository;
    private final SanPhamRepository sanPhamRepository;

    public HoaDonService(HoaDonRepository hoaDonRepository,
                         HoaDonChiTietRepository hoaDonChiTietRepository,
                         GioHangRepository gioHangRepository,
                         DiaChiRepository diaChiRepository,
                         SanPhamRepository sanPhamRepository) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.gioHangRepository = gioHangRepository;
        this.diaChiRepository = diaChiRepository;
        this.sanPhamRepository = sanPhamRepository;
    }

    @Transactional
    public HoaDon taoHoaDonTuGioHang(NguoiDung nguoiDung, Integer maDiaChi, String diaChiMoi) {

        List<GioHang> gioHangList = gioHangRepository
                .findByNguoiDung_MaNguoiDung(nguoiDung.getMaNguoiDung());

        if (gioHangList.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống");
        }

        DiaChi diaChi;
        
        // Nếu có maDiaChi (không null), dùng địa chỉ đó
        if (maDiaChi != null && maDiaChi > 0) {
            diaChi = diaChiRepository.findById(maDiaChi)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ với mã: " + maDiaChi));

            if (!diaChi.getNguoiDung().getMaNguoiDung().equals(nguoiDung.getMaNguoiDung())) {
                throw new RuntimeException("Địa chỉ không thuộc về tài khoản này");
            }
        } else if (diaChiMoi != null && !diaChiMoi.trim().isEmpty()) {
            // Nếu không có maDiaChi nhưng có địa chỉ mới, tạo địa chỉ mới
            // Kiểm tra xem user đã có địa chỉ nào chưa
            List<DiaChi> existingAddresses = diaChiRepository.findByNguoiDung_MaNguoiDung(nguoiDung.getMaNguoiDung());
            boolean isFirstAddress = existingAddresses.isEmpty();
            
            diaChi = new DiaChi();
            diaChi.setNguoiDung(nguoiDung);
            diaChi.setDiaChiChiTiet(diaChiMoi.trim());
            // Nếu đây là địa chỉ đầu tiên của user, set làm mặc định
            diaChi.setMacDinh(isFirstAddress);
            diaChi = diaChiRepository.save(diaChi);
        } else {
            throw new RuntimeException("Vui lòng chọn hoặc nhập địa chỉ giao hàng (maDiaChi=" + maDiaChi + ", diaChiMoi=" + diaChiMoi + ")");
        }

        HoaDon hoaDon = new HoaDon();
        hoaDon.setNguoiDung(nguoiDung);
        hoaDon.setDiaChi(diaChi);
        hoaDon.setNgayLap(LocalDate.now());
        hoaDon.setTrangThai("Chờ xác nhận");

        BigDecimal tongTien = BigDecimal.ZERO;
        List<HoaDonChiTiet> chiTietList = new ArrayList<>();

        for (GioHang gh : gioHangList) {
            SanPham sp = gh.getSanPham();

            // KHÔNG cập nhật số lượng tồn kho ở đây để tránh vi phạm constraint
            // Số lượng tồn kho sẽ được quản lý riêng bởi admin hoặc hệ thống khác
            // Chỉ tạo chi tiết hóa đơn mà không động vào số lượng tồn kho

            HoaDonChiTiet ct = new HoaDonChiTiet();
            ct.setHoaDon(hoaDon);
            ct.setSanPham(sp);
            ct.setSoLuong(gh.getSoLuong());
            ct.setDonGia(gh.getDonGia());

            tongTien = tongTien.add(
                    gh.getDonGia().multiply(BigDecimal.valueOf(gh.getSoLuong()))
            );
            chiTietList.add(ct);
        }

        hoaDon.setTongTien(tongTien);
        hoaDon.setDanhSachChiTiet(chiTietList);

        HoaDon hoaDonDaLuu = hoaDonRepository.save(hoaDon);
        hoaDonChiTietRepository.saveAll(chiTietList);

        gioHangRepository.deleteAll(gioHangList);

        return hoaDonDaLuu;
    }

    public List<HoaDon> layHoaDonCuaNguoiDung(NguoiDung nguoiDung) {
        return hoaDonRepository
                .findByNguoiDung_MaNguoiDungOrderByNgayLapDesc(
                        nguoiDung.getMaNguoiDung());
    }

    public HoaDon xemChiTiet(Integer maHoaDon, NguoiDung nguoiDung) {
        HoaDon hd = hoaDonRepository.findById(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hoá đơn"));
        
        // Debug: Log thông tin user và quyen
        System.out.println("DEBUG xemChiTiet: UserId=" + nguoiDung.getMaNguoiDung() + 
                           ", Quyen=" + (nguoiDung.getQuyen() != null ? nguoiDung.getQuyen().getTenQuyen() : "null"));
        
        // Admin hoặc Nhân viên có thể xem tất cả đơn hàng
        if (nguoiDung.getQuyen() != null && nguoiDung.getQuyen().getTenQuyen() != null) {
            String tenQuyen = nguoiDung.getQuyen().getTenQuyen();
            String tenQuyenLower = tenQuyen.toLowerCase().trim();
            
            // Kiểm tra admin - thử nhiều format (case-insensitive)
            boolean isAdmin = tenQuyenLower.contains("admin") || 
                             tenQuyenLower.equals("admin") ||
                             tenQuyenLower.contains("administrator");
            
            // Kiểm tra nhân viên - thử nhiều format (tiếng Việt và tiếng Anh)
            boolean isNhanVien = tenQuyenLower.contains("nhanvien") || 
                                tenQuyenLower.contains("nhan_vien") ||
                                tenQuyenLower.contains("nhân") ||
                                tenQuyenLower.contains("nhan") ||
                                tenQuyenLower.contains("viên") ||
                                tenQuyenLower.contains("staff") ||
                                tenQuyenLower.contains("employee");
            
            System.out.println("DEBUG xemChiTiet: tenQuyen='" + tenQuyen + 
                             "', tenQuyenLower='" + tenQuyenLower + 
                             "', isAdmin=" + isAdmin + 
                             ", isNhanVien=" + isNhanVien);
            
            if (isAdmin || isNhanVien) {
                System.out.println("DEBUG xemChiTiet: Admin/NhanVien được phép xem đơn hàng #" + maHoaDon);
                return hd;
            }
        }
        
        // User chỉ có thể xem đơn hàng của mình
        if (hd.getNguoiDung() == null || !hd.getNguoiDung().getMaNguoiDung().equals(nguoiDung.getMaNguoiDung())) {
            System.out.println("DEBUG xemChiTiet: User không có quyền xem đơn hàng này. " +
                             "Order userId=" + (hd.getNguoiDung() != null ? hd.getNguoiDung().getMaNguoiDung() : "null") +
                             ", Current userId=" + nguoiDung.getMaNguoiDung());
            throw new RuntimeException("Không có quyền xem hoá đơn này");
        }
        return hd;
    }
}
