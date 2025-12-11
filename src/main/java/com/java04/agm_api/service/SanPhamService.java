package com.java04.agm_api.service;

import com.java04.agm_api.entity.SanPham;
import com.java04.agm_api.repo.SanPhamRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class SanPhamService {

    private final SanPhamRepository sanPhamRepository;

    public SanPhamService(SanPhamRepository sanPhamRepository) {
        this.sanPhamRepository = sanPhamRepository;
    }

    public Page<SanPham> timKiem(String tuKhoa, Integer maDanhMuc, int trang, int kichThuoc) {
        Pageable pageable = PageRequest.of(trang, kichThuoc);

        if (tuKhoa != null && !tuKhoa.isBlank()) {
            return sanPhamRepository.findByTenSPContaining(tuKhoa, pageable);
        }

        if (maDanhMuc != null) {
            return sanPhamRepository.findByDanhMuc_MaDanhMuc(maDanhMuc, pageable);
        }

        return sanPhamRepository.findAll(pageable);
    }

    public SanPham timTheoMa(Integer maSP) {
        return sanPhamRepository.findById(maSP)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
    }
}
