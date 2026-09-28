package com.lanedesk.company;

import com.lanedesk.activity.ActivityRepository;
import com.lanedesk.activity.Outcome;
import com.lanedesk.contact.ContactRepository;
import com.lanedesk.shipment.ShipmentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyRepository companies;
    private final ContactRepository contacts;
    private final ActivityRepository activities;
    private final ShipmentRepository shipments;

    public CompanyController(CompanyRepository companies, ContactRepository contacts,
                             ActivityRepository activities, ShipmentRepository shipments) {
        this.companies = companies;
        this.contacts = contacts;
        this.activities = activities;
        this.shipments = shipments;
    }

    @ModelAttribute
    void enums(Model model) {
        model.addAttribute("types", CompanyType.values());
        model.addAttribute("statuses", CompanyStatus.values());
        model.addAttribute("equipments", Equipment.values());
    }

    @GetMapping
    String list(@RequestParam(required = false) CompanyType type, @RequestParam(required = false) CompanyStatus status,
                @RequestParam(required = false) String state, @RequestParam(required = false) Equipment equipment,
                @RequestParam(required = false) String q, Model model) {
        model.addAttribute("companies", companies.search(type, status, blankToNull(state, true), equipment, blankToNull(q, false)));
        model.addAttribute("f", new Filters(type, status, state, equipment, q));
        return "companies";
    }

    record Filters(CompanyType type, CompanyStatus status, String state, Equipment equipment, String q) {}

    @GetMapping("/{id}")
    String detail(@PathVariable Long id, Model model) {
        Company company = companies.findById(id).orElseThrow();
        model.addAttribute("company", company);
        model.addAttribute("contacts", contacts.findByCompanyIdOrderByName(id));
        model.addAttribute("activities", activities.findByCompanyIdOrderByOccurredAtDesc(id));
        model.addAttribute("shipments", shipments.findByShipperIdOrderByQuotedAtDesc(id));
        model.addAttribute("outcomes", Outcome.values());
        return "company-detail";
    }

    @GetMapping("/new")
    String newForm(Model model) {
        model.addAttribute("company", new Company());
        return "company-form";
    }

    @GetMapping("/{id}/edit")
    String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("company", companies.findById(id).orElseThrow());
        return "company-form";
    }

    @PostMapping
    String create(@RequestParam String name, @RequestParam CompanyType type, @RequestParam CompanyStatus status,
                  @RequestParam(required = false) String mcNumber, @RequestParam(required = false) String city,
                  @RequestParam(required = false) String state, @RequestParam(required = false) Equipment equipment,
                  @RequestParam(required = false) String website, @RequestParam(required = false) String notes,
                  RedirectAttributes flash) {
        Company c = new Company();
        String error = apply(c, name, type, status, mcNumber, city, state, equipment, website, notes);
        if (error != null) {
            flash.addFlashAttribute("error", error);
            return "redirect:/companies/new";
        }
        return "redirect:/companies/" + companies.save(c).id;
    }

    @PostMapping("/{id}")
    String update(@PathVariable Long id, @RequestParam String name, @RequestParam CompanyType type,
                  @RequestParam CompanyStatus status, @RequestParam(required = false) String mcNumber,
                  @RequestParam(required = false) String city, @RequestParam(required = false) String state,
                  @RequestParam(required = false) Equipment equipment, @RequestParam(required = false) String website,
                  @RequestParam(required = false) String notes, RedirectAttributes flash) {
        Company c = companies.findById(id).orElseThrow();
        String error = apply(c, name, type, status, mcNumber, city, state, equipment, website, notes);
        if (error != null) {
            flash.addFlashAttribute("error", error);
            return "redirect:/companies/" + id + "/edit";
        }
        companies.save(c);
        return "redirect:/companies/" + id;
    }

    private String apply(Company c, String name, CompanyType type, CompanyStatus status, String mcNumber, String city,
                         String state, Equipment equipment, String website, String notes) {
        if (name == null || name.isBlank()) {
            return "Company name is required";
        }
        String mc = blankToNull(mcNumber, false);
        if (mc != null) {
            boolean taken = companies.findAll().stream().anyMatch(o -> mc.equals(o.mcNumber) && !o.id.equals(c.id));
            if (taken) {
                return "MC number " + mc + " already belongs to another company";
            }
        }
        String st = blankToNull(state, true);
        if (st != null && st.length() != 2) {
            return "State must be a 2-letter code";
        }
        c.name = name.trim();
        c.type = type;
        c.status = status;
        c.mcNumber = mc;
        c.city = blankToNull(city, false);
        c.state = st;
        c.equipment = equipment;
        c.website = blankToNull(website, false);
        c.notes = blankToNull(notes, false);
        return null;
    }

    static String blankToNull(String s, boolean upper) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return upper ? s.trim().toUpperCase() : s.trim();
    }
}
