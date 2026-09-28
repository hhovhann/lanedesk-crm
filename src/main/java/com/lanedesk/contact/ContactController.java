package com.lanedesk.contact;

import com.lanedesk.company.CompanyRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ContactController {

    private final ContactRepository contacts;
    private final CompanyRepository companies;

    public ContactController(ContactRepository contacts, CompanyRepository companies) {
        this.contacts = contacts;
        this.companies = companies;
    }

    @PostMapping("/companies/{companyId}/contacts")
    String add(@PathVariable Long companyId, @RequestParam String name, @RequestParam(required = false) String title,
               @RequestParam(required = false) String phone, @RequestParam(required = false) String email,
               @RequestParam String timeZone, RedirectAttributes flash) {
        boolean noPhone = phone == null || phone.isBlank();
        boolean noEmail = email == null || email.isBlank();
        if (name.isBlank() || (noPhone && noEmail)) {
            flash.addFlashAttribute("error", "A contact needs a name and a phone or an email");
            return "redirect:/companies/" + companyId;
        }
        Contact c = new Contact();
        c.company = companies.findById(companyId).orElseThrow();
        c.name = name.trim();
        c.title = title == null || title.isBlank() ? null : title.trim();
        c.phone = noPhone ? null : phone.trim();
        c.email = noEmail ? null : email.trim();
        c.timeZone = timeZone;
        contacts.save(c);
        return "redirect:/companies/" + companyId;
    }
}
