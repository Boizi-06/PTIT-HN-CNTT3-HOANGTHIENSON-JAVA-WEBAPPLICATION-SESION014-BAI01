package com.re.session14_01;

import jakarta.websocket.Session;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.provider.HibernateUtils;

public void processPayment(Long orderId, Long walletId, double totalAmount) {
    Session session = HibernateUtils.getSessionFactory().openSession();

    // Khai báo Transaction ở ngoài try-catch để có thể gọi rollback trong catch
    Transaction tx = null;

    try {
        // 1. BẮT ĐẦU GIAO DỊCH
        tx = session.beginTransaction();

        // 2. Cập nhật trạng thái đơn hàng
        Order order = session.get(Order.class, orderId);
        order.setStatus("PAID");
        session.update(order);

        // Giả lập lỗi hệ thống bất ngờ
        if (true) throw new RuntimeException("Kết nối đến cổng thanh toán thất bại!");

        // 3. Trừ tiền trong ví khách hàng
        Wallet wallet = session.get(Wallet.class, walletId);
        wallet.setBalance(wallet.getBalance() - totalAmount);
        session.update(wallet);

        // 4. CHỐT GIAO DỊCH (Chỉ chạy được tới đây nếu không có lỗi nào văng ra)
        tx.commit();
        System.out.println("Thanh toán thành công!");

    } catch (Exception e) {
        System.out.println("Lỗi hệ thống: " + e.getMessage());

        // 5. QUAY XE (ROLLBACK)
        // Nếu tx đã được tạo ra, bắt buộc phải rollback để xóa bỏ trạng thái "PAID" của Order
        if (tx != null) {
            tx.rollback();
            System.out.println("Đã Rollback dữ liệu. Đơn hàng trở về trạng thái cũ!");
        }
    } finally {
        // Luôn luôn đóng session để giải phóng tài nguyên
        session.close();
    }
}