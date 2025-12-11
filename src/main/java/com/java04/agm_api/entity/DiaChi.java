package com.java04.agm_api.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "DiaChi")
public class DiaChi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDiaChi")
    private Integer maDiaChi;

    @ManyToOne
    @JoinColumn(name = "MaNguoiDung", nullable = false)
    private NguoiDung nguoiDung;

    @Column(name = "DiaChiChiTiet", nullable = false, length = 255)
    private String diaChiChiTiet;

    @Column(name = "MacDinh")
    private Boolean macDinh;
}
