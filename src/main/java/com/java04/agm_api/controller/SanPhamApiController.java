package com.java04.agm_api.controller;

import com.java04.agm_api.entity.SanPham;
import com.java04.agm_api.service.SanPhamService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/san-pham")

public class SanPhamApiController {

    private final SanPhamService sanPhamService;

    public SanPhamApiController(SanPhamService sanPhamService) {
        this.sanPhamService = sanPhamService;
    }

    // GET /api/san-pham?tuKhoa=&maDanhMuc=&trang=0&kichThuoc=12
    @GetMapping
    public Page<SanPham> danhSachSanPham(
            @RequestParam(required = false) String tuKhoa,
            @RequestParam(required = false) Integer maDanhMuc,
            @RequestParam(defaultValue = "0") int trang,
            @RequestParam(defaultValue = "12") int kichThuoc
    ) {
        return sanPhamService.timKiem(tuKhoa, maDanhMuc, trang, kichThuoc);
    }

    // GET /api/san-pham/{maSP}
    @GetMapping("/{maSP}")
    public SanPham chiTietSanPham(@PathVariable Integer maSP) {
        return sanPhamService.timTheoMa(maSP);
    }
}
