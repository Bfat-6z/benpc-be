package com.java04.agm_api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ChiTietPhieuNhap")
public class ChiTietPhieuNhap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaCT")
    private Integer maCT;

    @ManyToOne
    @JoinColumn(name = "MaPhieuNhap", nullable = false)
    private PhieuNhap phieuNhap;

    @ManyToOne
    @JoinColumn(name = "MaSP", nullable = false)
    private SanPham sanPham;

    @Column(name = "SoLuong", nullable = false)
    private Integer soLuong;

    @Column(name = "DonGia", nullable = false)
    private BigDecimal donGia;
}
