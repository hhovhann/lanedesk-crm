-- Dev-only demo data (loaded by the `dev` profile). All dates are relative to "now" so the demo always looks current.
INSERT INTO company (name, type, status, mc_number, city, state, equipment, website, notes) VALUES
 ('Harbor Foods Distribution', 'SHIPPER', 'ACTIVE',    NULL, 'Newark',        'NJ', 'REEFER',  'harborfoods.example', 'Weekly reefer to Atlanta and Charlotte. Pays net 30.'),
 ('Lone Star Building Supply', 'SHIPPER', 'QUALIFIED', NULL, 'Dallas',        'TX', 'FLATBED', NULL, 'Lumber and steel. Needs tarps.'),
 ('Great Lakes Paper Co',      'SHIPPER', 'CONTACTED', NULL, 'Chicago',       'IL', 'DRY_VAN', NULL, NULL),
 ('Peach State Beverage',      'SHIPPER', 'PROSPECT',  NULL, 'Atlanta',       'GA', 'DRY_VAN', NULL, 'Referred by Harbor Foods.'),
 ('Rocky Mountain Outfitters', 'SHIPPER', 'PROSPECT',  NULL, 'Denver',        'CO', 'DRY_VAN', NULL, NULL),
 ('Pacific Rim Electronics',   'SHIPPER', 'CONTACTED', NULL, 'Los Angeles',   'CA', 'DRY_VAN', NULL, 'Import containers, drayage handled elsewhere.'),
 ('Sunbelt Produce',           'SHIPPER', 'QUALIFIED', NULL, 'Phoenix',       'AZ', 'REEFER',  NULL, 'Seasonal; peak Oct–Mar.'),
 ('Bayou Chemical Supply',     'SHIPPER', 'DO_NOT_CALL', NULL, 'Baton Rouge', 'LA', 'DRY_VAN', NULL, 'Asked us to stop calling. Do not contact.'),
 ('Northwest Timber Group',    'SHIPPER', 'PROSPECT',  NULL, 'Seattle',       'WA', 'FLATBED', NULL, NULL),
 ('Aloha Island Goods',        'SHIPPER', 'PROSPECT',  NULL, 'Honolulu',      'HI', 'DRY_VAN', NULL, NULL),
 ('Meridian Freight Brokers',  'BROKER',  'CONTACTED', 'MC-812345', 'Nashville', 'TN', NULL, NULL, 'Overflow loads; good on flatbed.'),
 ('Coastal Logistics Partners','BROKER',  'PROSPECT',  'MC-774210', 'Jacksonville','FL', NULL, NULL, NULL),
 ('Summit Load Solutions',     'BROKER',  'ACTIVE',    'MC-690033', 'Kansas City', 'MO', NULL, NULL, NULL),
 ('Redline Transport',         'CARRIER', 'ACTIVE',    'MC-501122', 'Columbus',   'OH', 'DRY_VAN', NULL, 'Reliable; 12 trucks.'),
 ('Cold Chain Carriers',       'CARRIER', 'ACTIVE',    'MC-620987', 'Atlanta',    'GA', 'REEFER',  NULL, 'Reefers, southeast lanes.'),
 ('Flatland Hauling',          'CARRIER', 'ACTIVE',    'MC-455670', 'Oklahoma City','OK','FLATBED', NULL, NULL),
 ('Mesa Trucking',             'CARRIER', 'CONTACTED', 'MC-733901', 'Phoenix',    'AZ', 'REEFER',  NULL, NULL),
 ('Two Rivers Express',        'CARRIER', 'PROSPECT',  'MC-388120', 'Memphis',    'TN', 'DRY_VAN', NULL, NULL);

INSERT INTO contact (company_id, name, title, phone, email, time_zone)
SELECT c.id, v.name, v.title, v.phone, v.email, v.tz
FROM (VALUES
 ('Harbor Foods Distribution', 'Maria Lopez',   'Logistics Manager', '201-555-0101', 'maria@harborfoods.example', 'America/New_York'),
 ('Lone Star Building Supply', 'Dale Whitaker', 'Dispatch Lead',     '214-555-0102', NULL,                        'America/Chicago'),
 ('Great Lakes Paper Co',      'Priya Shah',    'Transportation Buyer','312-555-0103','priya@glp.example',         'America/Chicago'),
 ('Peach State Beverage',      'Tom Reyes',     'Shipping Supervisor','404-555-0104', NULL,                        'America/New_York'),
 ('Rocky Mountain Outfitters', 'Jenna Cole',    'Ops Director',      '303-555-0105', 'jenna@rmo.example',         'America/Denver'),
 ('Pacific Rim Electronics',   'Kevin Tran',    'Logistics Analyst', '310-555-0106', 'kevin@pacrim.example',      'America/Los_Angeles'),
 ('Sunbelt Produce',           'Rosa Martinez', 'Sales Coordinator', '602-555-0107', NULL,                        'America/Phoenix'),
 ('Bayou Chemical Supply',     'Gene Boudreaux','Owner',             '225-555-0108', NULL,                        'America/Chicago'),
 ('Northwest Timber Group',    'Ingrid Olsen',  'Traffic Manager',   '206-555-0109', 'ingrid@nwtimber.example',   'America/Los_Angeles'),
 ('Aloha Island Goods',        'Kai Nakamura',  'Owner',             '808-555-0110', NULL,                        'Pacific/Honolulu'),
 ('Meridian Freight Brokers',  'Sam Ortiz',     'Carrier Rep',       '615-555-0111', 'sam@meridian.example',      'America/Chicago'),
 ('Coastal Logistics Partners','Beth Hall',     'Capacity Manager',  '904-555-0112', NULL,                        'America/New_York'),
 ('Summit Load Solutions',     'Nate Brooks',   'Broker',            '816-555-0113', 'nate@summit.example',       'America/Chicago'),
 ('Redline Transport',         'Carl Dobbs',    'Dispatcher',        '614-555-0114', NULL,                        'America/New_York'),
 ('Cold Chain Carriers',       'Alicia Ford',   'Dispatcher',        '470-555-0115', 'alicia@coldchain.example',  'America/New_York'),
 ('Flatland Hauling',          'Buck Harlan',   'Owner-Operator',    '405-555-0116', NULL,                        'America/Chicago'),
 ('Mesa Trucking',             'Luis Ramirez',  'Dispatcher',        '480-555-0117', NULL,                        'America/Phoenix'),
 ('Two Rivers Express',        'Wanda Price',   'Fleet Manager',     '901-555-0118', 'wanda@tworivers.example',   'America/Chicago')
) AS v(company, name, title, phone, email, tz) JOIN company c ON c.name = v.company;

-- Activity history (calls over the past week, some open follow-ups already due)
INSERT INTO activity (company_id, contact_id, type, outcome, notes, occurred_at, next_follow_up_at, follow_up_done)
SELECT c.id, ct.id, 'CALL', v.outcome, v.notes, now() - v.ago, now() - v.due, v.done
FROM (VALUES
 ('Harbor Foods Distribution', 'CONVERSATION', 'Confirmed weekly Newark→Atlanta reefer, wants rate review in Q4.', interval '6 days', interval '-20 days', true),
 ('Harbor Foods Distribution', 'CONVERSATION', 'Added Charlotte lane. Happy with service.',                         interval '2 days', interval '-5 days',  false),
 ('Lone Star Building Supply', 'CONVERSATION', 'Needs 2 flatbeds/week starting next month. Send quote.',            interval '3 days', interval '1 day',    false),
 ('Great Lakes Paper Co',      'VOICEMAIL',    'Left voicemail about dry van capacity.',                            interval '4 days', interval '2 days',   false),
 ('Pacific Rim Electronics',   'NO_ANSWER',    NULL,                                                                interval '2 days', interval '30 minutes', false),
 ('Sunbelt Produce',           'CONVERSATION', 'Reefer season starts in October, very interested.',                 interval '5 days', interval '-2 days',  false),
 ('Meridian Freight Brokers',  'CONVERSATION', 'Will send overflow flatbed loads.',                                 interval '1 day',  interval '-6 days',  false),
 ('Bayou Chemical Supply',     'NOT_INTERESTED','Asked to be removed from call list.',                              interval '10 days', NULL,               true)
) AS v(company, outcome, notes, ago, due, done)
JOIN company c ON c.name = v.company
LEFT JOIN LATERAL (SELECT id FROM contact WHERE company_id = c.id ORDER BY id LIMIT 1) ct ON true;

-- Extra calls today and earlier this week so Stats has a shape
INSERT INTO activity (company_id, contact_id, type, outcome, notes, occurred_at)
SELECT c.id, ct.id, 'CALL', (ARRAY['NO_ANSWER','VOICEMAIL','CONVERSATION','NO_ANSWER'])[1 + (g % 4)],
       NULL, date_trunc('day', now()) - (g % 6) * interval '1 day' + interval '1 hour' * (g % 8)
FROM generate_series(1, 36) g
JOIN LATERAL (SELECT id FROM company WHERE type = 'SHIPPER' AND status <> 'DO_NOT_CALL' ORDER BY id OFFSET (g % 9) LIMIT 1) c ON true
LEFT JOIN LATERAL (SELECT id FROM contact WHERE company_id = c.id LIMIT 1) ct ON true;

INSERT INTO activity (company_id, type, notes, occurred_at)
SELECT id, 'NOTE', 'Met at the Southeast Produce Expo — follow up on reefer lanes.', now() - interval '8 days' FROM company WHERE name = 'Sunbelt Produce';

-- Loads in every status
INSERT INTO shipment (shipper_id, carrier_id, status, origin_city, origin_state, dest_city, dest_state, equipment, weight_lbs,
                      pickup_at, delivery_at, customer_rate, carrier_cost, lost_reason, quoted_at)
SELECT s.id, ca.id, v.status, v.oc, v.os, v.dc, v.ds, v.eq, v.wt, now() + v.pick, now() + v.pick + v.transit, v.rate, v.cost, v.lost, now() + v.pick - interval '4 days'
FROM (VALUES
 ('Lone Star Building Supply', NULL,                 'QUOTED',     'Dallas','TX','Denver','CO','FLATBED', 42000, interval '3 days',  interval '2 days', 2650.00, NULL::numeric, NULL),
 ('Great Lakes Paper Co',      NULL,                 'QUOTED',     'Chicago','IL','Columbus','OH','DRY_VAN', 38000, interval '4 days', interval '1 day',  1450.00, NULL, NULL),
 ('Harbor Foods Distribution', NULL,                 'WON',        'Newark','NJ','Charlotte','NC','REEFER', 36000, interval '2 days',  interval '1 day',  2100.00, NULL, NULL),
 ('Harbor Foods Distribution', 'Cold Chain Carriers','COVERED',    'Newark','NJ','Atlanta','GA','REEFER',   38000, interval '1 day',   interval '2 days', 3100.00, 2450.00, NULL),
 ('Sunbelt Produce',           'Mesa Trucking',      'IN_TRANSIT', 'Phoenix','AZ','Dallas','TX','REEFER',   40000, interval '-1 day',  interval '2 days', 3400.00, 2750.00, NULL),
 ('Harbor Foods Distribution', 'Cold Chain Carriers','DELIVERED',  'Newark','NJ','Atlanta','GA','REEFER',   37000, interval '-6 days', interval '2 days', 3050.00, 2400.00, NULL),
 ('Lone Star Building Supply', 'Flatland Hauling',   'DELIVERED',  'Dallas','TX','Memphis','TN','FLATBED', 44000, interval '-3 days', interval '1 day',  2300.00, 1850.00, NULL),
 ('Pacific Rim Electronics',   'Redline Transport',  'DELIVERED',  'Los Angeles','CA','Phoenix','AZ','DRY_VAN', 30000, interval '-2 days', interval '1 day', 1300.00, 1050.00, NULL),
 ('Great Lakes Paper Co',      NULL,                 'LOST',       'Chicago','IL','Atlanta','GA','DRY_VAN', 41000, interval '-5 days', interval '2 days', 2200.00, NULL, 'Went with incumbent, $150 cheaper')
) AS v(shipper, carrier, status, oc, os, dc, ds, eq, wt, pick, transit, rate, cost, lost)
JOIN company s ON s.name = v.shipper
LEFT JOIN company ca ON ca.name = v.carrier;
