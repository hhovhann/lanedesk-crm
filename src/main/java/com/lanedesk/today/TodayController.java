package com.lanedesk.today;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TodayController {

    private final TodayService today;

    public TodayController(TodayService today) {
        this.today = today;
    }

    @GetMapping("/")
    String index(Model model) {
        model.addAttribute("v", today.build());
        return "today";
    }
}
