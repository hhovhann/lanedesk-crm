package com.lanedesk.stats;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StatsController {

    private final StatsService stats;

    public StatsController(StatsService stats) {
        this.stats = stats;
    }

    @GetMapping("/stats")
    String stats(Model model) {
        model.addAttribute("v", stats.build());
        return "stats";
    }
}
