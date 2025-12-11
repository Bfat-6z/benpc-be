package com.java04.agm_api.service;

import com.java04.agm_api.entity.GioHang;
import com.java04.agm_api.entity.NguoiDung;
import com.java04.agm_api.entity.SanPham;
import com.java04.agm_api.repo.GioHangRepository;
import com.java04.agm_api.repo.SanPhamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class GioHangService {

    private final GioHangRepository gioHangRepository;
    private final SanPhamRepository sanPhamRepository;

    public GioHangService(GioHangRepository gioHangRepository,
                          SanPhamRepository sanPhamRepository) {
        this.gioHangRepository = gioHangRepository;
        this.sanPhamRepository = sanPhamRepository;
    }

    @Transactional
    public void themVaoGioHang(NguoiDung nguoiDung, Integer maSP, Integer soLuong) {
        SanPham sanPham = sanPhamRepository.findById(maSP)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        var optional = gioHangRepository
                .findByNguoiDung_MaNguoiDungAndSanPham_MaSP(
                        nguoiDung.getMaNguoiDung(), maSP);

        GioHang gioHang;
        if (optional.isPresent()) {
            gioHang = optional.get();
            gioHang.setSoLuong(gioHang.getSoLuong() + soLuong);
        } else {
            gioHang = new GioHang();
            gioHang.setNguoiDung(nguoiDung);
            gioHang.setSanPham(sanPham);
            gioHang.setSoLuong(soLuong);
            gioHang.setDonGia(sanPham.getGia()); // giá tại thời điểm thêm
        }
        gioHangRepository.save(gioHang);
    }

    public List<GioHang> layGioHangCuaNguoiDung(NguoiDung nguoiDung) {
        return gioHangRepository.findByNguoiDung_MaNguoiDung(nguoiDung.getMaNguoiDung());
    }

    @Transactional
    public void capNhatSoLuong(Integer maGH, Integer soLuongMoi, NguoiDung nguoiDung) {
        GioHang gh = gioHangRepository.findById(maGH)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mục giỏ hàng"));

        if (!gh.getNguoiDung().getMaNguoiDung().equals(nguoiDung.getMaNguoiDung())) {
            throw new RuntimeException("Không có quyền sửa giỏ hàng của người khác");
        }

        if (soLuongMoi <= 0) {
            gioHangRepository.delete(gh);
        } else {
            gh.setSoLuong(soLuongMoi);
            gioHangRepository.save(gh);
        }
    }

    @Transactional
    public void xoaKhoiGio(Integer maGH, NguoiDung nguoiDung) {
        GioHang gh = gioHangRepository.findById(maGH)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mục giỏ hàng"));

        if (!gh.getNguoiDung().getMaNguoiDung().equals(nguoiDung.getMaNguoiDung())) {
            throw new RuntimeException("Không có quyền xoá");
        }
        gioHangRepository.delete(gh);
    }

    public BigDecimal tinhTongTien(List<GioHang> gioHangList) {
        return gioHangList.stream()
                .map(i -> i.getDonGia()
                        .multiply(BigDecimal.valueOf(i.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
