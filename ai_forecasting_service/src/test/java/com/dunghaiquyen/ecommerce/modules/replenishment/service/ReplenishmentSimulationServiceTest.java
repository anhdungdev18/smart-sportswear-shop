package com.dunghaiquyen.ecommerce.modules.replenishment.service;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReplenishmentSimulationServiceTest {

    @Test
    void testBasicSimulation() {
        ReplenishmentSimulationService service = new ReplenishmentSimulationService();
        
        // 10 days of demand: 2 units per day
        List<Integer> demand = List.of(2, 2, 2, 2, 2, 2, 2, 2, 2, 2);
        
        // Initial stock 5, lead time 2, ROP 3, Target 10
        ReplenishmentSimulationService.SimulationConfig config = new ReplenishmentSimulationService.SimulationConfig(
                5, 2, 3, 10, 1, 1);
                
        ReplenishmentSimulationService.SimulationMetrics metrics = service.simulate(demand, config);
        
        // Day 1: onHand = 5, demand = 2 => end of day onHand = 3. Available = 3 <= 3 (ROP). Place order of 7 (10-3). Arrives at end of day 3.
        // Day 2: onHand = 3, demand = 2 => end of day onHand = 1.
        // Day 3: onHand = 1, demand = 2 => stockout! short = 1. onHand = 0. Order arrives: +7.
        // Day 4: onHand = 7, demand = 2 => end of day onHand = 5.
        // ...
        
        System.out.println(metrics);
        assertEquals(0, metrics.stockoutDays());
        assertEquals(0, metrics.unitsShort());
    }
    
    @Test
    void printBaselineVsProposedSimulation() {
        ReplenishmentSimulationService service = new ReplenishmentSimulationService();
        
        // Example SKU profile (intermittent demand) over 180 days
        List<Integer> demand = new java.util.ArrayList<>();
        java.util.Random rnd = new java.util.Random(2026);
        for (int i = 0; i < 180; i++) {
            demand.add(rnd.nextDouble() > 0.8 ? rnd.nextInt(5) + 1 : 0);
        }
        
        // Baseline: Wait until 5, then order 20
        ReplenishmentSimulationService.SimulationConfig baseline = new ReplenishmentSimulationService.SimulationConfig(
                20, 7, 5, 25, 1, 1);
                
        // Proposed AI: ROP = 8 (Lead time demand + safety stock), Target = 30
        ReplenishmentSimulationService.SimulationConfig proposed = new ReplenishmentSimulationService.SimulationConfig(
                20, 7, 8, 30, 1, 1);
                
        var bMetrics = service.simulate(demand, baseline);
        var pMetrics = service.simulate(demand, proposed);
        
        System.out.println("=== BASELINE VS PROPOSED ===");
        System.out.println("Metric | Baseline | Proposed");
        System.out.println("---|---|---");
        System.out.printf("Stockout Days | %d | %d%n", bMetrics.stockoutDays(), pMetrics.stockoutDays());
        System.out.printf("Fill Rate | %.2f%% | %.2f%%%n", bMetrics.fillRate() * 100, pMetrics.fillRate() * 100);
        System.out.printf("Average On Hand | %.1f | %.1f%n", bMetrics.averageOnHand(), pMetrics.averageOnHand());
        System.out.printf("Total Orders | %d | %d%n", bMetrics.totalOrders(), pMetrics.totalOrders());
        
        // Ensure that we can run this successfully
    }
}
