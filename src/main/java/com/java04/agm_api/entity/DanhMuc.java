package com.java04.agm_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "DanhMuc")
public class DanhMuc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDanhMuc")
    private Integer maDanhMuc;

    @Column(name = "TenDanhMuc", nullable = false, length = 100)
    private String tenDanhMuc;

    @OneToMany(mappedBy = "danhMuc")
    @JsonIgnore
    private List<SanPham> danhSachSanPham = new ArrayList<>();
}
