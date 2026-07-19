package com.dunghaiquyen.ecommerce.modules.replenishment.service;

import java.util.ArrayList;
import java.util.List;

public class ReplenishmentSimulationService {

    public record SimulationConfig(
            int initialStock,
            int leadTimeDays,
            int reorderPoint,
            int targetStock,
            int packSize,
            int moq) {}

    public record SimulationMetrics(
            int stockoutDays,
            double stockoutRate,
            int unitsShort,
            double fillRate,
            double averageOnHand,
            int totalOrders,
            int totalUnitsOrdered) {}

    public SimulationMetrics simulate(List<Integer> dailyDemand, SimulationConfig config) {
        int onHand = config.initialStock();
        int incoming = 0;
        int stockoutDays = 0;
        int unitsShort = 0;
        int totalDemand = 0;
        int totalOrders = 0;
        int totalUnitsOrdered = 0;
        long sumOnHand = 0;
        
        List<Integer> arrivals = new ArrayList<>();
        for (int i = 0; i < dailyDemand.size() + config.leadTimeDays(); i++) {
            arrivals.add(0);
        }

        for (int day = 0; day < dailyDemand.size(); day++) {
            // 1. Receive arrivals
            int arrivedToday = arrivals.get(day);
            onHand += arrivedToday;
            incoming -= arrivedToday;

            // 2. Satisfy demand
            int demand = dailyDemand.get(day);
            totalDemand += demand;
            
            if (demand > onHand) {
                stockoutDays++;
                unitsShort += (demand - onHand);
                onHand = 0; // all stock depleted
            } else {
                onHand -= demand;
            }

            // 3. Record end of day stock
            sumOnHand += onHand;

            // 4. Place orders at end of day
            int available = onHand + incoming;
            if (available <= config.reorderPoint()) {
                int qtyNeeded = config.targetStock() - available;
                if (qtyNeeded < config.moq()) {
                    qtyNeeded = config.moq();
                }
                // apply pack size
                if (config.packSize() > 0) {
                    int remainder = qtyNeeded % config.packSize();
                    if (remainder != 0) {
                        qtyNeeded += (config.packSize() - remainder);
                    }
                }
                
                if (qtyNeeded > 0) {
                    arrivals.set(day + config.leadTimeDays(), arrivals.get(day + config.leadTimeDays()) + qtyNeeded);
                    incoming += qtyNeeded;
                    totalOrders++;
                    totalUnitsOrdered += qtyNeeded;
                }
            }
        }

        double stockoutRate = (double) stockoutDays / dailyDemand.size();
        double fillRate = totalDemand == 0 ? 1.0 : 1.0 - ((double) unitsShort / totalDemand);
        double averageOnHand = (double) sumOnHand / dailyDemand.size();

        return new SimulationMetrics(
                stockoutDays, stockoutRate, unitsShort, fillRate, averageOnHand, totalOrders, totalUnitsOrdered);
    }
}
