package com.testnext.steps;

import com.testnext.annotation.TestStep;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class TradeSteps {

        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TradeSteps.class);

        @TestStep(id = "trade.bookTrade", name = "Book a Trade", description = "Book a trade with specified parameters")
        public Map<String, Object> bookTrade(Map<String, Object> params) {
                String securityId = (String) params.get("security_id");
                Integer quantity = params.get("quantity") instanceof Integer ? (Integer) params.get("quantity")
                                : Integer.parseInt(params.get("quantity").toString());
                Double price = params.get("price") instanceof Double ? (Double) params.get("price")
                                : Double.parseDouble(params.get("price").toString());
                String side = (String) params.get("side");

                // Simulate booking logic
                log.debug("Booking trade: {} {} {} @ {}", side, quantity, securityId, price);

                return Map.of(
                                "tradeId", "TRD-" + System.currentTimeMillis(),
                                "status", "BOOKED",
                                "message", "Trade booked successfully");
        }
}
