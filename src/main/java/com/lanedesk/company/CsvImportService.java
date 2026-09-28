package com.lanedesk.company;

import com.lanedesk.contact.Contact;
import com.lanedesk.contact.ContactRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Imports companies (and optionally one contact each) from CSV. Header row required; columns:
 * name, type, status, mc_number, city, state, equipment, website, notes,
 * contact_name, contact_title, contact_phone, contact_email, contact_time_zone.
 */
@Service
public class CsvImportService {

    public record Result(int imported, int skipped, List<String> problems) {}

    private final CompanyRepository companies;
    private final ContactRepository contacts;

    public CsvImportService(CompanyRepository companies, ContactRepository contacts) {
        this.companies = companies;
        this.contacts = contacts;
    }

    @Transactional
    public Result importCsv(Reader reader) throws IOException {
        List<List<String>> rows = parse(new BufferedReader(reader));
        if (rows.isEmpty()) {
            return new Result(0, 0, List.of("The file is empty"));
        }
        List<String> header = rows.get(0).stream().map(h -> h.trim().toLowerCase(Locale.ROOT)).toList();
        if (!header.contains("name")) {
            return new Result(0, 0, List.of("Header row must include a 'name' column"));
        }
        int imported = 0;
        int skipped = 0;
        List<String> problems = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            int line = i + 1;
            List<String> cells = rows.get(i);
            if (cells.stream().allMatch(String::isBlank)) {
                continue;
            }
            Map<String, String> r = new HashMap<>();
            for (int c = 0; c < header.size() && c < cells.size(); c++) {
                r.put(header.get(c), cells.get(c).trim());
            }
            try {
                if (importRow(r)) {
                    imported++;
                } else {
                    skipped++;
                    problems.add("Line " + line + ": " + r.get("name") + " already exists, skipped");
                }
            } catch (IllegalArgumentException e) {
                skipped++;
                problems.add("Line " + line + ": " + e.getMessage());
            }
        }
        return new Result(imported, skipped, problems);
    }

    private boolean importRow(Map<String, String> r) {
        String name = r.getOrDefault("name", "");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        String mc = blank(r.get("mc_number"));
        String state = blank(r.get("state"));
        if (state != null) {
            state = state.toUpperCase(Locale.ROOT);
            if (state.length() != 2) {
                throw new IllegalArgumentException("state '" + state + "' must be 2 letters");
            }
        }
        if ((mc != null && companies.existsByMcNumber(mc))
                || (state != null && companies.findByNameIgnoreCaseAndState(name, state).isPresent())) {
            return false;
        }
        Company c = new Company();
        c.name = name;
        c.type = enumOf(CompanyType.class, r.get("type"), CompanyType.SHIPPER, "type");
        c.status = enumOf(CompanyStatus.class, r.get("status"), CompanyStatus.PROSPECT, "status");
        c.equipment = enumOf(Equipment.class, r.get("equipment"), null, "equipment");
        c.mcNumber = mc;
        c.city = blank(r.get("city"));
        c.state = state;
        c.website = blank(r.get("website"));
        c.notes = blank(r.get("notes"));

        String contactName = blank(r.get("contact_name"));
        String phone = blank(r.get("contact_phone"));
        String email = blank(r.get("contact_email"));
        if (contactName != null && phone == null && email == null) {
            throw new IllegalArgumentException("contact " + contactName + " needs a phone or an email");
        }
        companies.save(c);
        if (contactName != null) {
            Contact ct = new Contact();
            ct.company = c;
            ct.name = contactName;
            ct.title = blank(r.get("contact_title"));
            ct.phone = phone;
            ct.email = email;
            String tz = blank(r.get("contact_time_zone"));
            if (tz != null) {
                ct.timeZone = tz;
            }
            contacts.save(ct);
        }
        return true;
    }

    private static <E extends Enum<E>> E enumOf(Class<E> type, String raw, E fallback, String column) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, raw.trim().toUpperCase(Locale.ROOT).replace(' ', '_'));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("unknown " + column + " '" + raw + "'");
        }
    }

    private static String blank(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    /** Minimal RFC 4180 parser: quoted fields, escaped quotes, commas and newlines inside quotes. */
    static List<List<String>> parse(BufferedReader in) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        int ch;
        while ((ch = in.read()) != -1) {
            char c = (char) ch;
            if (quoted) {
                if (c == '"') {
                    in.mark(1);
                    int next = in.read();
                    if (next == '"') {
                        cell.append('"');
                    } else {
                        quoted = false;
                        if (next != -1) {
                            in.reset();
                        }
                    }
                } else {
                    cell.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                row.add(cell.toString());
                cell.setLength(0);
            } else if (c == '\n' || c == '\r') {
                if (c == '\r') {
                    in.mark(1);
                    if (in.read() != '\n') {
                        in.reset();
                    }
                }
                row.add(cell.toString());
                cell.setLength(0);
                rows.add(row);
                row = new ArrayList<>();
            } else {
                cell.append(c);
            }
        }
        if (cell.length() > 0 || !row.isEmpty()) {
            row.add(cell.toString());
            rows.add(row);
        }
        return rows;
    }
}
