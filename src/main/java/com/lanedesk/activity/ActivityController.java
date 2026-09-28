package com.lanedesk.activity;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ActivityController {

    private final ActivityService service;
    private final ActivityRepository activities;

    public ActivityController(ActivityService service, ActivityRepository activities) {
        this.service = service;
        this.activities = activities;
    }

    /**
     * Log a call in one click: the outcome button is the submit. From the company page it swaps the timeline;
     * from the Today list ({@code quick=true}) it just removes the row.
     */
    @PostMapping("/companies/{companyId}/calls")
    Object logCall(@PathVariable Long companyId, @RequestParam(required = false) Long contactId,
                   @RequestParam Outcome outcome, @RequestParam(required = false) String notes,
                   @RequestParam(required = false) Integer followUpDays, @RequestParam(defaultValue = "false") boolean quick,
                   Model model) {
        service.logCall(companyId, contactId, outcome, notes, followUpDays);
        if (quick) {
            return ResponseEntity.ok("");
        }
        model.addAttribute("activities", activities.findByCompanyIdOrderByOccurredAtDesc(companyId));
        model.addAttribute("companyId", companyId);
        return "fragments/timeline :: timeline";
    }

    @PostMapping("/companies/{companyId}/notes")
    Object addNote(@PathVariable Long companyId, @RequestParam String notes, Model model) {
        if (!notes.isBlank()) {
            service.addNote(companyId, notes.trim());
        }
        model.addAttribute("activities", activities.findByCompanyIdOrderByOccurredAtDesc(companyId));
        model.addAttribute("companyId", companyId);
        return "fragments/timeline :: timeline";
    }
}
