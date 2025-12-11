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
@Table(name = "PhieuXuat")
public class PhieuXuat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaPhieuXuat")
    private Integer maPhieuXuat;

    @ManyToOne
    @JoinColumn(name = "MaNguoiDung", nullable = false)
    private NguoiDung nguoiDung;

    @Column(name = "NgayXuat")
    private LocalDate ngayXuat;

    @Column(name = "TongTien")
    private BigDecimal tongTien;

    @OneToMany(mappedBy = "phieuXuat", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<ChiTietPhieuXuat> danhSachChiTiet = new ArrayList<>();
}
