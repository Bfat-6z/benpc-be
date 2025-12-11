package com.java04.agm_api.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "HoaDon")
public class HoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaHoaDon")
    private Integer maHoaDon;

    @ManyToOne
    @JoinColumn(name = "MaNguoiDung", nullable = false)
    private NguoiDung nguoiDung;

    @ManyToOne
    @JoinColumn(name = "MaDiaChi", nullable = false)
    private DiaChi diaChi;

    @Column(name = "NgayLap")
    private LocalDate ngayLap;

    @Column(name = "TongTien")
    private BigDecimal tongTien;

    @Column(name = "TrangThai", length = 50)
    private String trangThai;

    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<HoaDonChiTiet> danhSachChiTiet = new ArrayList<>();
}
