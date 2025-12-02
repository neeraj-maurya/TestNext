package com.testnext.steps;

import com.testnext.annotation.TestStep;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class TradeSteps {

    @TestStep(name = "Book a Trade", description = "Book a trade with specified parameters", inputs = {
            "security_id:string:true", "quantity:number:true", "price:number:true", "side:string:true"
    })
    public Map<String, Object> bookTrade(Map<String, Object> params) {
        String securityId = (String) params.get("security_id");
        Integer quantity = params.get("quantity") instanceof Integer ? (Integer) params.get("quantity")
                : Integer.parseInt(params.get("quantity").toString());
        Double price = params.get("price") instanceof Double ? (Double) params.get("price")
                : Double.parseDouble(params.get("price").toString());
        String side = (String) params.get("side");

        // Simulate booking logic
        System.out.println("Booking trade: " + side + " " + quantity + " " + securityId + " @ " + price);

        return Map.of(
                "tradeId", "TRD-" + System.currentTimeMillis(),
                "status", "BOOKED",
                "message", "Trade booked successfully");
    }
}
