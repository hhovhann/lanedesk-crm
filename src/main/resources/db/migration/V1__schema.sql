CREATE TABLE company (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    type        VARCHAR(20)  NOT NULL CHECK (type IN ('SHIPPER', 'BROKER', 'CARRIER')),
    status      VARCHAR(20)  NOT NULL DEFAULT 'PROSPECT'
                CHECK (status IN ('PROSPECT', 'CONTACTED', 'QUALIFIED', 'ACTIVE', 'INACTIVE', 'DO_NOT_CALL')),
    mc_number   VARCHAR(20)  UNIQUE,
    city        VARCHAR(100),
    state       VARCHAR(2),
    equipment   VARCHAR(20)  CHECK (equipment IN ('DRY_VAN', 'REEFER', 'FLATBED', 'STEP_DECK', 'POWER_ONLY')),
    website     VARCHAR(200),
    notes       TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE contact (
    id          BIGSERIAL PRIMARY KEY,
    company_id  BIGINT       NOT NULL REFERENCES company (id) ON DELETE CASCADE,
    name        VARCHAR(150) NOT NULL,
    title       VARCHAR(100),
    phone       VARCHAR(40),
    email       VARCHAR(200),
    time_zone   VARCHAR(50)  NOT NULL DEFAULT 'America/Chicago',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT contact_phone_or_email CHECK (phone IS NOT NULL OR email IS NOT NULL)
);

CREATE TABLE activity (
    id                BIGSERIAL PRIMARY KEY,
    company_id        BIGINT      NOT NULL REFERENCES company (id) ON DELETE CASCADE,
    contact_id        BIGINT      REFERENCES contact (id) ON DELETE SET NULL,
    type              VARCHAR(20) NOT NULL CHECK (type IN ('CALL', 'EMAIL', 'NOTE')),
    outcome           VARCHAR(20) CHECK (outcome IN ('NO_ANSWER', 'VOICEMAIL', 'CONVERSATION', 'NOT_INTERESTED')),
    notes             TEXT,
    occurred_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    next_follow_up_at TIMESTAMPTZ,
    follow_up_done    BOOLEAN     NOT NULL DEFAULT FALSE
);
CREATE INDEX activity_occurred_at_idx ON activity (occurred_at);
CREATE INDEX activity_company_idx ON activity (company_id, occurred_at DESC);
CREATE INDEX activity_follow_up_idx ON activity (next_follow_up_at) WHERE next_follow_up_at IS NOT NULL AND NOT follow_up_done;

CREATE TABLE shipment (
    id            BIGSERIAL PRIMARY KEY,
    shipper_id    BIGINT        NOT NULL REFERENCES company (id),
    carrier_id    BIGINT        REFERENCES company (id),
    status        VARCHAR(20)   NOT NULL DEFAULT 'QUOTED'
                  CHECK (status IN ('QUOTED', 'WON', 'COVERED', 'IN_TRANSIT', 'DELIVERED', 'LOST')),
    origin_city   VARCHAR(100)  NOT NULL,
    origin_state  VARCHAR(2)       NOT NULL,
    dest_city     VARCHAR(100)  NOT NULL,
    dest_state    VARCHAR(2)       NOT NULL,
    equipment     VARCHAR(20)   NOT NULL CHECK (equipment IN ('DRY_VAN', 'REEFER', 'FLATBED', 'STEP_DECK', 'POWER_ONLY')),
    weight_lbs    INTEGER       NOT NULL CHECK (weight_lbs > 0 AND weight_lbs <= 48000),
    pickup_at     TIMESTAMPTZ   NOT NULL,
    delivery_at   TIMESTAMPTZ   NOT NULL,
    customer_rate NUMERIC(10,2) NOT NULL CHECK (customer_rate >= 0),
    carrier_cost  NUMERIC(10,2) CHECK (carrier_cost >= 0),
    margin        NUMERIC(10,2) GENERATED ALWAYS AS (customer_rate - carrier_cost) STORED,
    lost_reason   VARCHAR(200),
    quoted_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT shipment_delivery_after_pickup CHECK (delivery_at >= pickup_at),
    CONSTRAINT shipment_lost_reason CHECK (status <> 'LOST' OR lost_reason IS NOT NULL),
    CONSTRAINT shipment_carrier_needed CHECK (status NOT IN ('COVERED', 'IN_TRANSIT', 'DELIVERED')
                                               OR (carrier_id IS NOT NULL AND carrier_cost IS NOT NULL))
);
CREATE INDEX shipment_status_idx ON shipment (status);
