package com.java04.agm_api.repo;

import com.java04.agm_api.entity.GioHang;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GioHangRepository extends JpaRepository<GioHang, Integer> {

    List<GioHang> findByNguoiDung_MaNguoiDung(Integer maNguoiDung);

    Optional<GioHang> findByNguoiDung_MaNguoiDungAndSanPham_MaSP(
            Integer maNguoiDung, Integer maSP);
}
