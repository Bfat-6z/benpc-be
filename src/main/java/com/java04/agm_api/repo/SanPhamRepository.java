package com.java04.agm_api.repo;

import com.java04.agm_api.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {

    Page<SanPham> findByTenSPContaining(String tenSP, Pageable pageable);

    Page<SanPham> findByDanhMuc_MaDanhMuc(Integer maDanhMuc, Pageable pageable);
    
    Page<SanPham> findByTenSPContainingAndDanhMuc_MaDanhMuc(String tenSP, Integer maDanhMuc, Pageable pageable);
    
    long countByDanhMuc_MaDanhMuc(Integer maDanhMuc);
}
