package com.java04.agm_api.repo;

import com.java04.agm_api.entity.Quyen;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuyenRepository extends JpaRepository<Quyen, Integer> {
    Quyen findByTenQuyen(String tenQuyen);
}

