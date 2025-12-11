package com.java04.agm_api.service;

import com.java04.agm_api.entity.*;
import com.java04.agm_api.repo.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final NguoiDungRepository nguoiDungRepository;
    private final QuyenRepository quyenRepository;
    private final SanPhamRepository sanPhamRepository;
    private final DanhMucRepository danhMucRepository;
    private final HoaDonRepository hoaDonRepository;

    public AdminService(NguoiDungRepository nguoiDungRepository,
                       QuyenRepository quyenRepository,
                       SanPhamRepository sanPhamRepository,
                       DanhMucRepository danhMucRepository,
                       HoaDonRepository hoaDonRepository) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.quyenRepository = quyenRepository;
        this.sanPhamRepository = sanPhamRepository;
        this.danhMucRepository = danhMucRepository;
        this.hoaDonRepository = hoaDonRepository;
    }

    // ========== USER MANAGEMENT ==========

    public Page<NguoiDung> layDanhSachNguoiDung(String tuKhoa, String vaiTro, int trang, int kichThuoc) {
        Pageable pageable = PageRequest.of(trang, kichThuoc);
        
        // Map role từ frontend format sang database format
        Quyen quyen = null;
        if (vaiTro != null && !vaiTro.trim().isEmpty()) {
            String roleToSearch = vaiTro.trim().toLowerCase();
            
            // Lấy tất cả roles từ database để so sánh
            List<Quyen> allQuyens = quyenRepository.findAll();
            
            // Tìm role phù hợp - so sánh với tất cả roles trong DB
            for (Quyen q : allQuyens) {
                String tenQuyen = q.getTenQuyen();
                if (tenQuyen == null) continue;
                
                String tenQuyenLower = tenQuyen.toLowerCase().trim();
                
                // Map từ frontend format sang database format - dùng contains() để linh hoạt hơn
                boolean matches = false;
                switch (roleToSearch) {
                    case "user":
                        // Thử tất cả các format có thể có cho user - dùng contains để match "Khách hàng"
                        matches = tenQuyenLower.contains("user") || 
                                 tenQuyenLower.contains("khách") ||
                                 tenQuyenLower.contains("khach") ||
                                 tenQuyenLower.contains("customer") ||
                                 tenQuyenLower.contains("client");
                        break;
                    case "nhanvien":
                    case "nhan_vien":
                        // Thử tất cả các format có thể có cho nhân viên
                        matches = tenQuyenLower.contains("nhanvien") ||
                                 tenQuyenLower.contains("nhan_vien") ||
                                 tenQuyenLower.contains("nhân") ||
                                 tenQuyenLower.contains("nhan") ||
                                 tenQuyenLower.contains("staff") ||
                                 tenQuyenLower.contains("employee");
                        break;
                    case "admin":
                        // Thử tất cả các format có thể có cho admin
                        matches = tenQuyenLower.contains("admin") ||
                                 tenQuyenLower.contains("administrator");
                        break;
                    default:
                        // Nếu không match, thử so sánh trực tiếp hoặc contains
                        matches = tenQuyenLower.equals(roleToSearch) || 
                                 tenQuyenLower.contains(roleToSearch) ||
                                 tenQuyen.equals(vaiTro.trim());
                }
                
                if (matches) {
                    quyen = q;
                    break;
                }
            }
            
            // Debug: Log nếu không tìm thấy role
            if (quyen == null && !allQuyens.isEmpty()) {
                System.out.println("DEBUG: Không tìm thấy role '" + vaiTro + "'. Các role có trong DB:");
                for (Quyen q : allQuyens) {
                    System.out.println("  - " + q.getTenQuyen() + " (MaQuyen: " + q.getMaQuyen() + ")");
                }
            }
        }
        
        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            // Tìm theo tên hoặc email
            String keyword = tuKhoa.trim();
            if (quyen != null) {
                return nguoiDungRepository.findByTenNguoiDungContainingOrEmailContainingAndQuyen(
                    keyword, keyword, quyen, pageable);
            }
            return nguoiDungRepository.findByTenNguoiDungContainingOrEmailContaining(
                keyword, keyword, pageable);
        } else if (quyen != null) {
            return nguoiDungRepository.findByQuyen(quyen, pageable);
        }
        
        return nguoiDungRepository.findAll(pageable);
    }

    public NguoiDung capNhatRoleNguoiDung(Integer userId, String vaiTroMoi) {
        NguoiDung nguoiDung = nguoiDungRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Chặn thay đổi role của admin khác
        if (nguoiDung.getQuyen() != null) {
            String currentRole = nguoiDung.getQuyen().getTenQuyen();
            if (currentRole != null && currentRole.toLowerCase().contains("admin")) {
                throw new RuntimeException("Không thể thay đổi role của tài khoản admin.");
            }
        }
        
        // Lấy tất cả roles từ database để so sánh
        List<Quyen> allQuyens = quyenRepository.findAll();
        String roleToSearch = vaiTroMoi.trim().toLowerCase();
        Quyen quyen = null;
        
        // Tìm role phù hợp
        for (Quyen q : allQuyens) {
            String tenQuyen = q.getTenQuyen();
            if (tenQuyen == null) continue;
            
            String tenQuyenLower = tenQuyen.toLowerCase();
            
            // Map từ frontend format sang database format - dùng contains() để linh hoạt hơn
            boolean matches = false;
            switch (roleToSearch) {
                case "user":
                    matches = tenQuyenLower.contains("user") || 
                             tenQuyenLower.contains("khách") ||
                             tenQuyenLower.contains("khach") ||
                             tenQuyenLower.contains("customer") ||
                             tenQuyenLower.contains("client");
                    break;
                case "nhanvien":
                case "nhan_vien":
                    matches = tenQuyenLower.contains("nhanvien") ||
                             tenQuyenLower.contains("nhan_vien") ||
                             tenQuyenLower.contains("nhân") ||
                             tenQuyenLower.contains("nhan") ||
                             tenQuyenLower.contains("staff") ||
                             tenQuyenLower.contains("employee");
                    break;
                case "admin":
                    matches = tenQuyenLower.contains("admin") ||
                             tenQuyenLower.contains("administrator");
                    break;
                default:
                    // Nếu không match, thử so sánh trực tiếp hoặc contains
                    matches = tenQuyenLower.equals(roleToSearch) || 
                             tenQuyenLower.contains(roleToSearch) ||
                             tenQuyen.equals(vaiTroMoi.trim());
            }
            
            if (matches) {
                quyen = q;
                break;
            }
        }
        
        if (quyen == null) {
            throw new RuntimeException("Không tìm thấy quyền: " + vaiTroMoi);
        }
        
        nguoiDung.setQuyen(quyen);
        return nguoiDungRepository.save(nguoiDung);
    }

    public NguoiDung capNhatTrangThaiNguoiDung(Integer userId, Boolean trangThai) {
        NguoiDung nguoiDung = nguoiDungRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Chặn khóa/mở tài khoản admin
        if (nguoiDung.getQuyen() != null) {
            String currentRole = nguoiDung.getQuyen().getTenQuyen();
            if (currentRole != null && currentRole.toLowerCase().contains("admin")) {
                throw new RuntimeException("Không thể khóa hoặc mở khóa tài khoản admin.");
            }
        }

        nguoiDung.setTrangThai(trangThai);
        return nguoiDungRepository.save(nguoiDung);
    }

    // ========== PRODUCT MANAGEMENT ==========

    public Page<SanPham> layDanhSachSanPham(String tuKhoa, Integer maDanhMuc, int trang, int kichThuoc) {
        Pageable pageable = PageRequest.of(trang, kichThuoc);
        
        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            String keyword = tuKhoa.trim();
            if (maDanhMuc != null) {
                return sanPhamRepository.findByTenSPContainingAndDanhMuc_MaDanhMuc(keyword, maDanhMuc, pageable);
            }
            return sanPhamRepository.findByTenSPContaining(keyword, pageable);
        } else if (maDanhMuc != null) {
            return sanPhamRepository.findByDanhMuc_MaDanhMuc(maDanhMuc, pageable);
        }
        
        return sanPhamRepository.findAll(pageable);
    }

    public SanPham taoSanPham(SanPham sanPham) {
        return sanPhamRepository.save(sanPham);
    }

    public SanPham capNhatSanPham(Integer maSP, SanPham sanPhamMoi) {
        SanPham sanPham = sanPhamRepository.findById(maSP)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        
        if (sanPhamMoi.getTenSP() != null) {
            sanPham.setTenSP(sanPhamMoi.getTenSP());
        }
        if (sanPhamMoi.getGia() != null) {
            sanPham.setGia(sanPhamMoi.getGia());
        }
        if (sanPhamMoi.getSoLuong() != null) {
            sanPham.setSoLuong(sanPhamMoi.getSoLuong());
        }
        if (sanPhamMoi.getMoTa() != null) {
            sanPham.setMoTa(sanPhamMoi.getMoTa());
        }
        if (sanPhamMoi.getHinhAnh() != null) {
            sanPham.setHinhAnh(sanPhamMoi.getHinhAnh());
        }
        if (sanPhamMoi.getDanhMuc() != null) {
            sanPham.setDanhMuc(sanPhamMoi.getDanhMuc());
        }
        if (sanPhamMoi.getNhaCungCap() != null) {
            sanPham.setNhaCungCap(sanPhamMoi.getNhaCungCap());
        }
        
        return sanPhamRepository.save(sanPham);
    }

    public void xoaSanPham(Integer maSP) {
        if (!sanPhamRepository.existsById(maSP)) {
            throw new RuntimeException("Không tìm thấy sản phẩm");
        }
        sanPhamRepository.deleteById(maSP);
    }

    // ========== CATEGORY MANAGEMENT ==========

    public List<DanhMuc> layDanhSachDanhMuc() {
        return danhMucRepository.findAll();
    }

    public long demSoSanPhamTheoDanhMuc(Integer maDanhMuc) {
        return sanPhamRepository.countByDanhMuc_MaDanhMuc(maDanhMuc);
    }

    public DanhMuc taoDanhMuc(DanhMuc danhMuc) {
        return danhMucRepository.save(danhMuc);
    }

    public DanhMuc capNhatDanhMuc(Integer maDanhMuc, DanhMuc danhMucMoi) {
        DanhMuc danhMuc = danhMucRepository.findById(maDanhMuc)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
        
        if (danhMucMoi.getTenDanhMuc() != null) {
            danhMuc.setTenDanhMuc(danhMucMoi.getTenDanhMuc());
        }
        
        return danhMucRepository.save(danhMuc);
    }

    public void xoaDanhMuc(Integer maDanhMuc) {
        // Kiểm tra xem có sản phẩm nào đang dùng danh mục này không
        long soSanPham = sanPhamRepository.countByDanhMuc_MaDanhMuc(maDanhMuc);
        if (soSanPham > 0) {
            throw new RuntimeException("Không thể xóa danh mục vì còn " + soSanPham + " sản phẩm đang sử dụng");
        }
        
        if (!danhMucRepository.existsById(maDanhMuc)) {
            throw new RuntimeException("Không tìm thấy danh mục");
        }
        danhMucRepository.deleteById(maDanhMuc);
    }

    // ========== ORDER MANAGEMENT ==========

    public Page<HoaDon> layDanhSachHoaDon(String tuKhoa, String trangThai, int trang, int kichThuoc) {
        Pageable pageable = PageRequest.of(trang, kichThuoc);
        String normalizedStatus = normalizeTrangThai(trangThai);

        // Trường hợp không filter keyword, ta lọc trạng thái tại memory để tránh lệch format DB
        if ((tuKhoa == null || tuKhoa.trim().isEmpty()) && normalizedStatus != null && !normalizedStatus.isEmpty()) {
            List<HoaDon> all = hoaDonRepository.findAll();
            List<HoaDon> filtered = all.stream()
                .filter(hd -> normalizedStatus.equals(normalizeTrangThai(hd.getTrangThai())))
                .toList();
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), filtered.size());
            List<HoaDon> pageContent = start >= filtered.size() ? List.of() : filtered.subList(start, end);
            return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, filtered.size());
        }

        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            String keyword = tuKhoa.trim();
            try {
                Integer maHoaDon = Integer.parseInt(keyword);
                if (normalizedStatus != null && !normalizedStatus.isEmpty()) {
                    return hoaDonRepository.findByMaHoaDonAndTrangThai(maHoaDon, normalizedStatus, pageable);
                } else {
                    return hoaDonRepository.findByMaHoaDon(maHoaDon, pageable);
                }
            } catch (NumberFormatException e) {
                if (normalizedStatus != null && !normalizedStatus.isEmpty()) {
                    return hoaDonRepository.findByNguoiDung_TenNguoiDungContainingAndTrangThai(
                        keyword, normalizedStatus, pageable);
                }
                return hoaDonRepository.findByNguoiDung_TenNguoiDungContaining(keyword, pageable);
            }
        } else if (normalizedStatus != null && !normalizedStatus.isEmpty()) {
            return hoaDonRepository.findByTrangThai(normalizedStatus, pageable);
        }

        return hoaDonRepository.findAll(pageable);
    }

    private String normalizeTrangThai(String status) {
        if (status == null) return null;
        String s = status.trim();
        if (s.isEmpty()) return null;

        String key = s.toUpperCase();
        switch (key) {
            case "CHO_XAC_NHAN":
                return "Chờ xác nhận";
            case "DANG_XU_LY":
                return "Đang xử lý";
            case "DANG_GIAO":
                return "Đang giao";
            case "DA_GIAO":
                return "Đã giao";
            case "DA_HUY":
                return "Đã hủy";
            default:
                // Nếu backend đang lưu sẵn dạng code, giữ nguyên
                return s;
        }
    }

    public HoaDon xemChiTietHoaDon(Integer maHoaDon) {
        // Admin/Staff có thể xem bất kỳ đơn hàng nào, không cần check ownership
        return hoaDonRepository.findById(maHoaDon)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
    }

    public HoaDon capNhatTrangThaiHoaDon(Integer maHoaDon, String trangThaiMoi) {
        HoaDon hoaDon = hoaDonRepository.findById(maHoaDon)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        
        hoaDon.setTrangThai(trangThaiMoi);
        return hoaDonRepository.save(hoaDon);
    }

    public SanPham capNhatTonKho(Integer maSP, Integer tonKho) {
        SanPham sanPham = sanPhamRepository.findById(maSP)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        // Hệ thống đang dùng trường SoLuong cho tồn kho
        sanPham.setSoLuong(tonKho != null ? tonKho : 0);
        return sanPhamRepository.save(sanPham);
    }

    // ========== STATISTICS ==========

    public Map<String, Object> layThongKe() {
        Map<String, Object> stats = new HashMap<>();
        
        // Tổng doanh thu (tổng tongTien của tất cả đơn hàng đã giao)
        BigDecimal tongDoanhThu = hoaDonRepository.findAll().stream()
            .filter(hd -> "DA_GIAO".equals(hd.getTrangThai()))
            .map(HoaDon::getTongTien)
            .filter(t -> t != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalRevenue", tongDoanhThu);
        stats.put("tongDoanhThu", tongDoanhThu);
        
        // Tổng đơn hàng
        long tongDonHang = hoaDonRepository.count();
        stats.put("totalOrders", tongDonHang);
        stats.put("tongDonHang", tongDonHang);
        
        // Tổng sản phẩm
        long tongSanPham = sanPhamRepository.count();
        stats.put("totalProducts", tongSanPham);
        stats.put("tongSanPham", tongSanPham);
        
        // Tổng người dùng
        long tongNguoiDung = nguoiDungRepository.count();
        stats.put("totalUsers", tongNguoiDung);
        stats.put("tongNguoiDung", tongNguoiDung);
        
        return stats;
    }
}

