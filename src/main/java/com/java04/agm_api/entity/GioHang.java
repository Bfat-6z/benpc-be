package com.java04.agm_api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "GioHang")
public class GioHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaGH")
    private Integer maGH;

    @ManyToOne
    @JoinColumn(name = "MaNguoiDung", nullable = false)
    private NguoiDung nguoiDung;

    @ManyToOne
    @JoinColumn(name = "MaSP", nullable = false)
    private SanPham sanPham;

    @Column(name = "SoLuong", nullable = false)
    private Integer soLuong;

    @Column(name = "DonGia", nullable = false)
    private BigDecimal donGia;
}
