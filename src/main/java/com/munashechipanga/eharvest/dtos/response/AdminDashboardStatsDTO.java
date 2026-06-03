package com.munashechipanga.eharvest.dtos.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminDashboardStatsDTO {
    private long totalUsers;
    private long totalFarmers;
    private long totalBuyers;
    private long totalLogisticsProviders;
    private long totalAdmins;
    private long totalProduce;
    private long totalVehicles;
    private long totalOrders;
    private long totalReviews;
    private long totalDisputeReports;
    private long unattendedDisputeReports;
    private long unverifiedUsers;
}
