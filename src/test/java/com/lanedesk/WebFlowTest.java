package com.lanedesk;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lanedesk.company.Company;
import com.lanedesk.company.CompanyRepository;
import com.lanedesk.company.CompanyType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class WebFlowTest extends AbstractIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired CompanyRepository companies;

    @Test
    void anonymousUsersAreSentToLogin() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/login")).andExpect(status().isOk());
    }

    @Test
    void allPagesRender() throws Exception {
        for (String url : new String[]{"/", "/companies", "/companies/new", "/companies/import", "/pipeline", "/shipments/new", "/stats"}) {
            mvc.perform(get(url).with(user("test"))).andExpect(status().isOk());
        }
    }

    @Test
    void quickLogCallOnCompanyPageReturnsTimelineFragment() throws Exception {
        Company c = new Company();
        c.name = "Fragment Co";
        c.type = CompanyType.SHIPPER;
        companies.save(c);
        mvc.perform(get("/companies/" + c.id).with(user("test"))).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Log a call")));
        mvc.perform(post("/companies/" + c.id + "/calls").with(user("test")).with(csrf())
                        .param("outcome", "VOICEMAIL").param("followUpDays", "2").param("notes", "left vm"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("left vm")));
    }
}
