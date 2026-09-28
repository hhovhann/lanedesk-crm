package com.lanedesk.shipment;

import com.lanedesk.company.CompanyRepository;
import com.lanedesk.company.CompanyStatus;
import com.lanedesk.company.CompanyType;
import com.lanedesk.company.Equipment;
import com.lanedesk.config.LaneDeskProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ShipmentController {

    private static final List<ShipmentStatus> COLUMNS = List.of(ShipmentStatus.QUOTED, ShipmentStatus.WON,
            ShipmentStatus.COVERED, ShipmentStatus.IN_TRANSIT, ShipmentStatus.DELIVERED, ShipmentStatus.LOST);

    private final ShipmentService service;
    private final ShipmentRepository shipments;
    private final CompanyRepository companies;
    private final LaneDeskProperties props;

    public ShipmentController(ShipmentService service, ShipmentRepository shipments, CompanyRepository companies,
                              LaneDeskProperties props) {
        this.service = service;
        this.shipments = shipments;
        this.companies = companies;
        this.props = props;
    }

    @GetMapping("/pipeline")
    String pipeline(Model model) {
        Map<ShipmentStatus, List<Shipment>> board = new LinkedHashMap<>();
        for (ShipmentStatus s : COLUMNS) {
            board.put(s, shipments.findByStatusOrderByPickupAt(s));
        }
        model.addAttribute("board", board);
        model.addAttribute("carriers", companies.findByTypeAndStatusNotOrderByName(CompanyType.CARRIER, CompanyStatus.DO_NOT_CALL));
        return "pipeline";
    }

    @GetMapping("/shipments/new")
    String newForm(@RequestParam(required = false) Long shipperId, Model model) {
        model.addAttribute("shippers", companies.findByTypeAndStatusNotOrderByName(CompanyType.SHIPPER, CompanyStatus.DO_NOT_CALL));
        model.addAttribute("shipperId", shipperId);
        model.addAttribute("equipments", Equipment.values());
        return "shipment-form";
    }

    @PostMapping("/shipments")
    String quote(@RequestParam Long shipperId, @RequestParam String originCity, @RequestParam String originState,
                 @RequestParam String destCity, @RequestParam String destState, @RequestParam Equipment equipment,
                 @RequestParam int weightLbs, @RequestParam LocalDateTime pickupAt, @RequestParam LocalDateTime deliveryAt,
                 @RequestParam BigDecimal customerRate, RedirectAttributes flash) {
        try {
            service.quote(shipperId, originCity, originState, destCity, destState, equipment, weightLbs,
                    pickupAt.atZone(props.agentTimeZone()).toInstant(), deliveryAt.atZone(props.agentTimeZone()).toInstant(),
                    customerRate);
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/shipments/new?shipperId=" + shipperId;
        }
        flash.addFlashAttribute("message", "Quote created");
        return "redirect:/pipeline";
    }

    @PostMapping("/shipments/{id}/advance")
    String advance(@PathVariable Long id, @RequestParam(required = false) Long carrierId,
                   @RequestParam(required = false) BigDecimal carrierCost, RedirectAttributes flash) {
        try {
            Shipment s = service.advance(id, carrierId, carrierCost);
            flash.addFlashAttribute("message", "Load #" + id + " is now " + s.status);
        } catch (IllegalArgumentException | IllegalStateException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pipeline";
    }

    @PostMapping("/shipments/{id}/lost")
    String lost(@PathVariable Long id, @RequestParam(required = false) String reason, RedirectAttributes flash) {
        try {
            service.lose(id, reason);
            flash.addFlashAttribute("message", "Load #" + id + " marked lost");
        } catch (IllegalArgumentException | IllegalStateException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pipeline";
    }
}
