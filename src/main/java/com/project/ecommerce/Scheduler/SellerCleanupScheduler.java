package com.project.ecommerce.Scheduler;

import com.project.ecommerce.Service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SellerCleanupScheduler {

    private final SellerService sellerService;

    /**
     * Chạy mỗi 1 giờ
     */
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void deleteExpiredUnverifiedSellers() {

        sellerService.deleteExpiredUnverifiedSellers();

    }
}