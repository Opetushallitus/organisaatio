CREATE TABLE organisaatio_koulutuslupa(
    organisaatio_id bigint NOT NULL REFERENCES organisaatio(id),
    koulutuskoodiarvo text NOT NULL REFERENCES koodisto_koulutus(koodiarvo),
    alkupvm date NOT NULL,
    loppupvm date,
    PRIMARY KEY (organisaatio_id, koulutuskoodiarvo)
)
