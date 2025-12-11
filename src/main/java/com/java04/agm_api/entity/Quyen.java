package com.java04.agm_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Quyen")
public class Quyen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaQuyen")
    private Integer maQuyen;

    @Column(name = "TenQuyen", nullable = false, length = 50)
    private String tenQuyen;

    @OneToMany(mappedBy = "quyen")
    @JsonIgnore
    private List<NguoiDung> danhSachNguoiDung = new ArrayList<>();
}
