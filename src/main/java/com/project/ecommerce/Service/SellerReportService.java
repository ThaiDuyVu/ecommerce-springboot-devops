package com.project.ecommerce.Service;


import com.project.ecommerce.Model.Seller;
import com.project.ecommerce.Model.SellerReport;

public interface SellerReportService {
    SellerReport getSellerReport(Seller seller);
    SellerReport updateSellerReport(SellerReport sellerReport);
}
