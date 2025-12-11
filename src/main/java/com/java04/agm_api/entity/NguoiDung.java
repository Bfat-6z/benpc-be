package com.java04.agm_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "NguoiDung")
public class NguoiDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaNguoiDung")
    private Integer maNguoiDung;

    @Column(name = "TenNguoiDung", nullable = false, length = 100)
    private String tenNguoiDung;

    @Column(name = "GioiTinh")
    private Boolean gioiTinh;

    @Column(name = "DienThoai", length = 15)
    private String dienThoai;

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "MatKhau", length = 255)
    private String matKhau;

    @Column(name = "Avatar", length = 500)
    private String avatar;

    @Column(name = "TrangThai")
    private Boolean trangThai = true; // true: hoạt động, false: khóa

    @ManyToOne
    @JoinColumn(name = "MaQuyen", nullable = false)
    private Quyen quyen;

    @OneToMany(mappedBy = "nguoiDung")
    @JsonIgnore
    private List<DiaChi> danhSachDiaChi = new ArrayList<>();

    @OneToMany(mappedBy = "nguoiDung")
    @JsonIgnore
    private List<GioHang> gioHangList = new ArrayList<>();

    @OneToMany(mappedBy = "nguoiDung")
    @JsonIgnore
    private List<HoaDon> danhSachHoaDon = new ArrayList<>();

    @OneToMany(mappedBy = "nguoiDung")
    @JsonIgnore
    private List<PhieuXuat> danhSachPhieuXuat = new ArrayList<>();
}
