package com.java04.agm_api.controller;

import com.java04.agm_api.entity.DiaChi;
import com.java04.agm_api.entity.GioHang;
import com.java04.agm_api.entity.HoaDon;
import com.java04.agm_api.entity.NguoiDung;
import com.java04.agm_api.repo.DiaChiRepository;
import com.java04.agm_api.service.GioHangService;
import com.java04.agm_api.service.HoaDonService;
import com.java04.agm_api.service.NguoiDungService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")

public class HoaDonApiController {

    private final HoaDonService hoaDonService;
    private final GioHangService gioHangService;
    private final DiaChiRepository diaChiRepository;
    private final NguoiDungService nguoiDungService;

    public HoaDonApiController(HoaDonService hoaDonService,
                               GioHangService gioHangService,
                               DiaChiRepository diaChiRepository,
                               NguoiDungService nguoiDungService) {
        this.hoaDonService = hoaDonService;
        this.gioHangService = gioHangService;
        this.diaChiRepository = diaChiRepository;
        this.nguoiDungService = nguoiDungService;
    }

    private NguoiDung layNguoiDungHienTai(Principal principal, @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        // Ưu tiên dùng userId từ header (cho frontend)
        if (userId != null) {
            return nguoiDungService.timTheoMa(userId);
        }
        // Fallback: dùng Principal nếu có (cho tương lai khi có security)
        if (principal != null) {
            return nguoiDungService.timTheoEmail(principal.getName());
        }
        throw new RuntimeException("Cần đăng nhập");
    }

    // GET /api/checkout-data
    @GetMapping("/checkout-data")
    public CheckoutDataResponse checkoutData(Principal principal,
                                            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        NguoiDung nguoiDung = layNguoiDungHienTai(principal, userId);

        List<GioHang> gioHangList = gioHangService.layGioHangCuaNguoiDung(nguoiDung);
        BigDecimal tongTien = gioHangService.tinhTongTien(gioHangList);

        List<DiaChi> diaChiList =
                diaChiRepository.findByNguoiDung_MaNguoiDung(nguoiDung.getMaNguoiDung());
        
        // Sắp xếp: địa chỉ mặc định (macDinh = true) lên đầu
        diaChiList.sort((a, b) -> {
            Boolean aDefault = a.getMacDinh() != null && a.getMacDinh();
            Boolean bDefault = b.getMacDinh() != null && b.getMacDinh();
            if (aDefault && !bDefault) return -1;
            if (!aDefault && bDefault) return 1;
            return 0;
        });

        CheckoutDataResponse res = new CheckoutDataResponse();
        res.setGioHangList(gioHangList);
        res.setTongTien(tongTien);
        res.setDiaChiList(diaChiList);

        return res;
    }

    // POST /api/dia-chi   { "diaChiChiTiet": "123 Đường ABC" }
    @PostMapping("/dia-chi")
    public DiaChi taoDiaChi(@RequestBody DiaChiCreateRequest req,
                            Principal principal,
                            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (req.getDiaChiChiTiet() == null || req.getDiaChiChiTiet().trim().isEmpty()) {
            throw new RuntimeException("Địa chỉ không được để trống");
        }
        
        NguoiDung nguoiDung = layNguoiDungHienTai(principal, userId);
        
        DiaChi diaChi = new DiaChi();
        diaChi.setNguoiDung(nguoiDung);
        diaChi.setDiaChiChiTiet(req.getDiaChiChiTiet().trim());
        diaChi.setMacDinh(false); // Mặc định không phải địa chỉ mặc định
        
        return diaChiRepository.save(diaChi);
    }

    // POST /api/hoa-don   { "maDiaChi": 1 } hoặc { "diaChiMoi": "123 Đường ABC" }
    @PostMapping("/hoa-don")
    public HoaDon datHang(@RequestBody OrderCreateRequest req,
                          Principal principal,
                          @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        NguoiDung nguoiDung = layNguoiDungHienTai(principal, userId);
        return hoaDonService.taoHoaDonTuGioHang(nguoiDung, req.getMaDiaChi(), req.getDiaChiMoi());
    }

    // GET /api/hoa-don
    @GetMapping("/hoa-don")
    public List<HoaDon> danhSachDonHang(Principal principal,
                                       @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        NguoiDung nguoiDung = layNguoiDungHienTai(principal, userId);
        return hoaDonService.layHoaDonCuaNguoiDung(nguoiDung);
    }

    // GET /api/hoa-don/{maHoaDon}
    @GetMapping("/hoa-don/{maHoaDon}")
    public HoaDon chiTietDon(@PathVariable Integer maHoaDon,
                             Principal principal,
                             @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        NguoiDung nguoiDung = layNguoiDungHienTai(principal, userId);
        return hoaDonService.xemChiTiet(maHoaDon, nguoiDung);
    }

    // ===== DTO =====

    public static class CheckoutDataResponse {
        private List<GioHang> gioHangList;
        private BigDecimal tongTien;
        private List<DiaChi> diaChiList;

        public List<GioHang> getGioHangList() {
            return gioHangList;
        }

        public void setGioHangList(List<GioHang> gioHangList) {
            this.gioHangList = gioHangList;
        }

        public BigDecimal getTongTien() {
            return tongTien;
        }

        public void setTongTien(BigDecimal tongTien) {
            this.tongTien = tongTien;
        }

        public List<DiaChi> getDiaChiList() {
            return diaChiList;
        }

        public void setDiaChiList(List<DiaChi> diaChiList) {
            this.diaChiList = diaChiList;
        }
    }

    public static class OrderCreateRequest {
        private Integer maDiaChi;
        private String diaChiMoi; // Địa chỉ mới nếu user nhập

        public Integer getMaDiaChi() {
            return maDiaChi;
        }

        public void setMaDiaChi(Integer maDiaChi) {
            this.maDiaChi = maDiaChi;
        }

        public String getDiaChiMoi() {
            return diaChiMoi;
        }

        public void setDiaChiMoi(String diaChiMoi) {
            this.diaChiMoi = diaChiMoi;
        }
    }

    public static class DiaChiCreateRequest {
        private String diaChiChiTiet;

        public String getDiaChiChiTiet() {
            return diaChiChiTiet;
        }

        public void setDiaChiChiTiet(String diaChiChiTiet) {
            this.diaChiChiTiet = diaChiChiTiet;
        }
    }
}
