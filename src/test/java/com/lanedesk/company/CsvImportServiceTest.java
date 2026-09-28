package com.lanedesk.company;

import static org.assertj.core.api.Assertions.assertThat;

import com.lanedesk.AbstractIntegrationTest;
import com.lanedesk.contact.ContactRepository;
import java.io.StringReader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CsvImportServiceTest extends AbstractIntegrationTest {

    @Autowired CsvImportService importer;
    @Autowired CompanyRepository companies;
    @Autowired ContactRepository contacts;

    @Test
    void importsQuotedFieldsAndContactsAndSkipsDuplicatesAndBadRows() throws Exception {
        String csv = """
                name,type,status,mc_number,state,equipment,contact_name,contact_phone,contact_time_zone
                "Acme, Inc.",shipper,,,tx,dry van,Jo Smith,555-1111,America/Chicago
                Acme Carriers,carrier,active,MC-1,OH,,,,
                Dup Carrier,carrier,,MC-1,OH,,,,
                Bad State,shipper,,,Texas,,,,
                Bad Type,widget,,,TX,,,,
                """;
        var result = importer.importCsv(new StringReader(csv));

        assertThat(result.imported()).isEqualTo(2);
        assertThat(result.skipped()).isEqualTo(3);
        var acme = companies.findByNameIgnoreCaseAndState("Acme, Inc.", "TX").orElseThrow();
        assertThat(acme.equipment).isEqualTo(Equipment.DRY_VAN);
        assertThat(contacts.findByCompanyIdOrderByName(acme.id)).extracting(c -> c.name).containsExactly("Jo Smith");
    }

    @Test
    void reimportingSameFileCreatesNothing() throws Exception {
        String csv = "name,state\nRepeat Co,TX\n";
        importer.importCsv(new StringReader(csv));
        assertThat(importer.importCsv(new StringReader(csv)).imported()).isZero();
    }
}
