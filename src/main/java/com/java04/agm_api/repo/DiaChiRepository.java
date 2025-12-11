package com.java04.agm_api.repo;

import com.java04.agm_api.entity.DiaChi;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DiaChiRepository extends JpaRepository<DiaChi, Integer> {
    List<DiaChi> findByNguoiDung_MaNguoiDung(Integer maNguoiDung);
}
