package com.java04.agm_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PhieuNhap")
public class PhieuNhap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaPhieuNhap")
    private Integer maPhieuNhap;

    @ManyToOne
    @JoinColumn(name = "MaNCC", nullable = false)
    private NhaCungCap nhaCungCap;

    @Column(name = "NgayNhap")
    private LocalDate ngayNhap;

    @Column(name = "TongTien")
    private BigDecimal tongTien;

    @OneToMany(mappedBy = "phieuNhap", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<ChiTietPhieuNhap> danhSachChiTiet = new ArrayList<>();
}
