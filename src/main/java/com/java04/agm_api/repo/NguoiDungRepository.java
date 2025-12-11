package com.java04.agm_api.repo;

import com.java04.agm_api.entity.NguoiDung;
import com.java04.agm_api.entity.Quyen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, Integer> {
    NguoiDung findByEmail(String email);
    
    Page<NguoiDung> findByTenNguoiDungContainingOrEmailContaining(String tenNguoiDung, String email, Pageable pageable);
    
    Page<NguoiDung> findByTenNguoiDungContainingOrEmailContainingAndQuyen(String tenNguoiDung, String email, Quyen quyen, Pageable pageable);
    
    Page<NguoiDung> findByQuyen(Quyen quyen, Pageable pageable);
}
