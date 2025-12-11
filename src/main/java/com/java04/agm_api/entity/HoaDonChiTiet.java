package com.java04.agm_api.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "HoaDonChiTiet")
public class HoaDonChiTiet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaCT")
    private Integer maCT;

    @ManyToOne
    @JoinColumn(name = "MaHoaDon", nullable = false)
    @JsonBackReference
    private HoaDon hoaDon;

    @ManyToOne
    @JoinColumn(name = "MaSP", nullable = false)
    private SanPham sanPham;

    @Column(name = "SoLuong", nullable = false)
    private Integer soLuong;

    @Column(name = "DonGia", nullable = false)
    private BigDecimal donGia;
}
