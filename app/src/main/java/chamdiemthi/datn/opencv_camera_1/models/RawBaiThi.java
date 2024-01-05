package chamdiemthi.datn.opencv_camera_1.models;

import org.msgpack.annotation.Message;


@Message
public class RawBaiThi {

    public int maBaiThi;
    public DeThi[] dsDeThi;
    public long ngayTao;
    public String tenBaiThi;
    public int soCau;
    public int heDiem;

    public RawBaiThi() {
    }

    public static RawBaiThi encode(BaiThi baiThi) {
        RawBaiThi raw = new RawBaiThi();
        raw.maBaiThi = baiThi.maBaiThi;
        raw.dsDeThi = baiThi.dsDeThi.toArray(new DeThi[baiThi.dsDeThi.size()]);
        raw.ngayTao = baiThi.getLNgayTao();
        raw.tenBaiThi = baiThi.tenBaiThi;
        raw.soCau = baiThi.soCau;
        raw.heDiem = baiThi.heDiem;
        return raw;
    }

    public static BaiThi decode(RawBaiThi raw) {
        BaiThi baiThi = new BaiThi(raw.maBaiThi, raw.ngayTao, raw.tenBaiThi, raw.soCau, raw.heDiem);
        for (int i = 0; i < raw.dsDeThi.length; i++) baiThi.dsDeThi.add(raw.dsDeThi[i]);
        return baiThi;
    }

}
