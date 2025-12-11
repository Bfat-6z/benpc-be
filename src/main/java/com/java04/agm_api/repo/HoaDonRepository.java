package com.java04.agm_api.repo;

import com.java04.agm_api.entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    List<HoaDon> findByNguoiDung_MaNguoiDungOrderByNgayLapDesc(Integer maNguoiDung);
    
    Page<HoaDon> findByNguoiDung_TenNguoiDungContaining(String tenNguoiDung, Pageable pageable);
    
    Page<HoaDon> findByTrangThai(String trangThai, Pageable pageable);
    
    Page<HoaDon> findByNguoiDung_TenNguoiDungContainingAndTrangThai(String tenNguoiDung, String trangThai, Pageable pageable);
    
    // Tìm theo mã đơn hàng
    Page<HoaDon> findByMaHoaDon(Integer maHoaDon, Pageable pageable);
    
    Page<HoaDon> findByMaHoaDonAndTrangThai(Integer maHoaDon, String trangThai, Pageable pageable);
}
