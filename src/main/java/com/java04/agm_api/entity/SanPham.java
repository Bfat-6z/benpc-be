package com.java04.agm_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "SanPham")
public class SanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaSP")
    private Integer maSP;

    @Column(name = "TenSP", nullable = false, length = 255)
    private String tenSP;

    @Column(name = "Gia", nullable = false)
    private BigDecimal gia;

    @Column(name = "SoLuong")
    private Integer soLuong;

    @Column(name = "MoTa", length = 255)
    private String moTa;

    @Column(name = "HinhAnh", length = 255)
    private String hinhAnh;

    @ManyToOne
    @JoinColumn(name = "MaNCC")
    private NhaCungCap nhaCungCap;

    @ManyToOne
    @JoinColumn(name = "MaDanhMuc")
    private DanhMuc danhMuc;

    @OneToMany(mappedBy = "sanPham")
    @JsonIgnore
    private List<GioHang> gioHangList = new ArrayList<>();

    @OneToMany(mappedBy = "sanPham")
    @JsonIgnore
    private List<HoaDonChiTiet> danhSachHoaDonChiTiet = new ArrayList<>();

    @OneToMany(mappedBy = "sanPham")
    @JsonIgnore
    private List<ChiTietPhieuNhap> danhSachChiTietPhieuNhap = new ArrayList<>();

    @OneToMany(mappedBy = "sanPham")
    @JsonIgnore
    private List<ChiTietPhieuXuat> danhSachChiTietPhieuXuat = new ArrayList<>();
}
