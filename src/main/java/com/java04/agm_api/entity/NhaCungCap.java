package com.java04.agm_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "NhaCungCap")
public class NhaCungCap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaNCC")
    private Integer maNCC;

    @Column(name = "TenNCC", nullable = false, length = 150)
    private String tenNCC;

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "DienThoai", length = 15)
    private String dienThoai;

    @Column(name = "DiaChi", length = 255)
    private String diaChi;

    @OneToMany(mappedBy = "nhaCungCap")
    @JsonIgnore
    private List<SanPham> danhSachSanPham = new ArrayList<>();

    @OneToMany(mappedBy = "nhaCungCap")
    @JsonIgnore
    private List<PhieuNhap> danhSachPhieuNhap = new ArrayList<>();
}
