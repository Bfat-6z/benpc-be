package com.java04.agm_api.controller;

import com.java04.agm_api.entity.GioHang;
import com.java04.agm_api.entity.NguoiDung;
import com.java04.agm_api.service.GioHangService;
import com.java04.agm_api.service.NguoiDungService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/gio-hang")

public class GioHangApiController {

    private final GioHangService gioHangService;
    private final NguoiDungService nguoiDungService;

    public GioHangApiController(GioHangService gioHangService,
                                NguoiDungService nguoiDungService) {
        this.gioHangService = gioHangService;
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
        throw new RuntimeException("Cần đăng nhập để dùng giỏ hàng");
    }

    // GET /api/gio-hang
    @GetMapping
    public CartResponse xemGioHang(Principal principal,
                                   @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        NguoiDung nguoiDung = layNguoiDungHienTai(principal, userId);
        List<GioHang> gioHangList = gioHangService.layGioHangCuaNguoiDung(nguoiDung);
        BigDecimal tongTien = gioHangService.tinhTongTien(gioHangList);

        CartResponse res = new CartResponse();
        res.setItems(gioHangList);
        res.setTongTien(tongTien);
        return res;
    }

    // POST /api/gio-hang  { "maSP": 1, "soLuong": 2 }
    @PostMapping
    public void themVaoGio(@RequestBody CartAddRequest req,
                           Principal principal,
                           @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        NguoiDung nguoiDung = layNguoiDungHienTai(principal, userId);
        gioHangService.themVaoGioHang(nguoiDung, req.getMaSP(), req.getSoLuong());
    }

    // PUT /api/gio-hang/{maGH}  { "soLuong": 3 }
    @PutMapping("/{maGH}")
    public void capNhatSoLuong(@PathVariable Integer maGH,
                               @RequestBody CartUpdateRequest req,
                               Principal principal,
                               @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        NguoiDung nguoiDung = layNguoiDungHienTai(principal, userId);
        gioHangService.capNhatSoLuong(maGH, req.getSoLuong(), nguoiDung);
    }

    // DELETE /api/gio-hang/{maGH}
    @DeleteMapping("/{maGH}")
    public void xoaKhoiGio(@PathVariable Integer maGH,
                           Principal principal,
                           @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        NguoiDung nguoiDung = layNguoiDungHienTai(principal, userId);
        gioHangService.xoaKhoiGio(maGH, nguoiDung);
    }

    // ===== DTO =====

    public static class CartResponse {
        private List<GioHang> items;
        private BigDecimal tongTien;

        public List<GioHang> getItems() {
            return items;
        }

        public void setItems(List<GioHang> items) {
            this.items = items;
        }

        public BigDecimal getTongTien() {
            return tongTien;
        }

        public void setTongTien(BigDecimal tongTien) {
            this.tongTien = tongTien;
        }
    }

    public static class CartAddRequest {
        private Integer maSP;
        private Integer soLuong = 1;

        public Integer getMaSP() {
            return maSP;
        }

        public void setMaSP(Integer maSP) {
            this.maSP = maSP;
        }

        public Integer getSoLuong() {
            return soLuong;
        }

        public void setSoLuong(Integer soLuong) {
            this.soLuong = soLuong;
        }
    }

    public static class CartUpdateRequest {
        private Integer soLuong;

        public Integer getSoLuong() {
            return soLuong;
        }

        public void setSoLuong(Integer soLuong) {
            this.soLuong = soLuong;
        }
    }
}
