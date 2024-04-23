/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 */
package fi.vm.sade.organisaatio.business.impl;

import com.google.common.collect.Maps;
import fi.vm.sade.oid.ExceptionMessage;
import fi.vm.sade.oid.OIDService;
import fi.vm.sade.oid.NodeClassCode;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioKoodisto;
import fi.vm.sade.organisaatio.business.OrganisaatioValidationService;
import fi.vm.sade.organisaatio.business.exception.*;
import fi.vm.sade.organisaatio.dto.OrganisaatioNimiDTO;
import fi.vm.sade.organisaatio.dto.OrganisaatioNimiUpdateDTO;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioNimiModelMapper;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioMuokkausTiedotDTO;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.dto.v4.ResultRDTOV4;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.repository.*;
import fi.vm.sade.organisaatio.resource.OrganisaatioResourceException;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.organisaatio.service.KoodistoService;
import fi.vm.sade.organisaatio.service.OrganisationDateValidator;
import fi.vm.sade.organisaatio.service.util.OrganisaatioNimiUtil;
import fi.vm.sade.organisaatio.service.util.OrganisaatioUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ValidationException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@Service("organisaatioBusinessService")
public class OrganisaatioBusinessServiceImpl implements OrganisaatioBusinessService {


    @Autowired
    private OrganisaatioRepository organisaatioRepository;

    @Autowired
    private OrganisaatioSuhdeRepository organisaatioSuhdeRepository;

    @Autowired
    protected YhteystietoArvoRepository yhteystietoArvoRepository;

    @Autowired
    protected OrganisaatioMetaDataRepository organisaatioMetaDataRepository;

    @Autowired
    private YhteystietojenTyyppiRepository yhteystietojenTyyppiRepository;

    @Autowired
    protected YhteystietoElementtiRepository yhteystietoElementtiRepository;

    @Autowired
    protected OrganisaatioNimiRepository organisaatioNimiRepository;

    @Autowired
    private OrganisaatioNimiModelMapper organisaatioNimiModelMapper;

    @Autowired
    private OrganisaatioBusinessChecker checker;

    @Autowired
    private OIDService oidService;

    @Autowired
    private ConversionService conversionService;

    @Autowired
    @Lazy
    private KoodistoService koodistoService;

    @Autowired
    private OrganisaatioTarjonta organisaatioTarjonta;

    @Autowired
    private OrganisaatioKoodisto organisaatioKoodisto;

    @Autowired
    private LisatietoTyyppiRepository lisatietoTyyppiRepository;

    @Autowired
    private OrganisaatioValidationService organisaatioValidationService;

    @Value("${root.organisaatio.oid}")
    private String rootOrganisaatioOid;

    private static final String PARENT_SEPARATOR = "|";

    private void mergeAuxData(Organisaatio entity, Organisaatio orgEntity) {
        try {
            if (orgEntity.getKuvaus2() != null) {
                entity.getKuvaus2().setId(orgEntity.getKuvaus2().getId());
                entity.getKuvaus2().setVersion(orgEntity.getKuvaus2().getVersion());
            }
            OrganisaatioMetaData metadata = entity.getMetadata();
            OrganisaatioMetaData orgMetadata = orgEntity.getMetadata();
            if (metadata != null && orgMetadata != null) {
                metadata.setId(orgMetadata.getId());
                metadata.setVersion(orgMetadata.getVersion());
                if (orgMetadata.getHakutoimistoNimi() != null) {
                    metadata.getHakutoimistoNimi().setId(orgMetadata.getHakutoimistoNimi().getId());
                    metadata.getHakutoimistoNimi().setVersion(orgMetadata.getHakutoimistoNimi().getVersion());
                }
                if (orgMetadata.getNimi() != null) {
                    metadata.getNimi().setId(orgMetadata.getNimi().getId());
                    metadata.getNimi().setVersion(orgMetadata.getNimi().getVersion());
                }
                if (orgMetadata.getHakutoimistoEctsNimi() != null) {
                    metadata.getHakutoimistoEctsNimi().setId(orgMetadata.getHakutoimistoEctsNimi().getId());
                    metadata.getHakutoimistoEctsNimi().setVersion(orgMetadata.getHakutoimistoEctsNimi().getVersion());
                }
                if (orgMetadata.getHakutoimistoEctsEmail() != null) {
                    metadata.getHakutoimistoEctsEmail().setId(orgMetadata.getHakutoimistoEctsEmail().getId());
                    metadata.getHakutoimistoEctsEmail().setVersion(orgMetadata.getHakutoimistoEctsEmail().getVersion());
                }
                if (orgMetadata.getHakutoimistoEctsPuhelin() != null) {
                    metadata.getHakutoimistoEctsPuhelin().setId(orgMetadata.getHakutoimistoEctsPuhelin().getId());
                    metadata.getHakutoimistoEctsPuhelin().setVersion(orgMetadata.getHakutoimistoEctsPuhelin().getVersion());
                }
                if (orgMetadata.getHakutoimistoEctsTehtavanimike() != null) {
                    metadata.getHakutoimistoEctsTehtavanimike().setId(orgMetadata.getHakutoimistoEctsTehtavanimike().getId());
                    metadata.getHakutoimistoEctsTehtavanimike().setVersion(orgMetadata.getHakutoimistoEctsTehtavanimike().getVersion());
                }
                for (NamedMonikielinenTeksti value : metadata.getValues()) {
                    MonikielinenTeksti mkt = orgMetadata.getNamedValue(value.getKey());
                    if (mkt != null) {
                        value.getValue().setId(mkt.getId());
                        value.getValue().setVersion(mkt.getVersion());
                    }
                }
                metadata = organisaatioMetaDataRepository.save(metadata);
                entity.setMetadata(metadata);

            }
        } catch (Exception ex) {
            throw new OrganisaatioResourceException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), "organisaatio.error.merge.aux.data");
        }
    }

    private String getCurrentUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Override
    public OrganisaatioResult saveOrUpdate(OrganisaatioRDTO model) throws ValidationException {
        // Luodaan tallennettava entity objekti
        Organisaatio entity = conversionService.convert(model, Organisaatio.class); //this entity is populated with new data
        if (entity == null) {
            throw new ValidationException("validation.organisaatio.convert.error");
        }
        if (entity.getOid() != null) {
            return update(entity, model.getParentOid());
        }
        return save(entity, model.getParentOid());
    }

    @Override
    public ResultRDTOV4 saveOrUpdate(OrganisaatioRDTOV4 model) throws ValidationException {
        // Luodaan tallennettava entity objekti
        Organisaatio entity = conversionService.convert(model, Organisaatio.class); //this entity is populated with new data
        if (entity == null) {
            throw new ValidationException("validation.organisaatio.convert.error");
        }
        OrganisaatioResult organisaatioResult;
        if (entity.getOid() != null) {
            organisaatioResult = update(entity, model.getParentOid());
        } else {
            organisaatioResult = save(entity, model.getParentOid());
        }
        return new ResultRDTOV4(this.conversionService.convert(organisaatioResult.getOrganisaatio(), OrganisaatioRDTOV4.class), organisaatioResult.getInfo());
    }

    private OrganisaatioResult update(Organisaatio entity, String parentOid) {

        Organisaatio parentOrg = fetchParentOrg(parentOid);

        // Validate (throws exception)
        this.organisaatioValidationService.validateOrganisation(entity, parentOid, parentOrg);

        // Validate and persist lisatietotyypit
        if (!CollectionUtils.isEmpty(entity.getOrganisaatioLisatietotyypit())) {
            persistOrganisaatioLisatietotyyppis(entity);
        }

        Organisaatio oldOrg = organisaatioRepository.findFirstByOid(entity.getOid());
        if (oldOrg.isOrganisaatioPoistettu()) {
            throw new ValidationException("validation.Organisaatio.poistettu");
        }

        // Asetetaan parent path
        setParentPath(entity, parentOid);

        Organisaatio oldParent = validateHierarchy(parentOid, entity, oldOrg);

        mergeAuxData(entity, oldOrg);
        updateExistingYhteystiedot(entity, oldOrg);

        entity.setId(oldOrg.getId());
        entity.setOpetuspisteenJarjNro(oldOrg.getOpetuspisteenJarjNro());
        entity.setVersion(oldOrg.getVersion());

        // Generoidaan oidit
        try {
            generateOids(entity);
            generateOidsMetadata(entity.getMetadata());
        } catch (ExceptionMessage em) {
            throw new OrganisaatioResourceException(HttpStatus.INTERNAL_SERVER_ERROR, em.getMessage());
        }

        String opJarjNro;
        if (oldOrg != null && isEmpty(oldOrg.getOpetuspisteenJarjNro()) && isEmpty(oldOrg.getToimipisteKoodi())) {
            opJarjNro = generateOpetuspisteenJarjNro(entity, parentOrg, entity.getTyypit());
            entity.setOpetuspisteenJarjNro(opJarjNro);
        } else {
            opJarjNro = entity.getOpetuspisteenJarjNro();
        }

        boolean isVarhaiskasvatuksenToimipaikka = checkIfVarhaiskasvatuksenToimipaikka(entity);
        setVarhaiskasvatuksenToimipaikkaTietoRelations(entity, isVarhaiskasvatuksenToimipaikka);
        entity.setOrganisaatioPoistettu(false);

        setPaivittajaData(entity);

        checkDateConstraints(entity, parentOrg);

        setYhteystietoArvot(entity, true);

        generateToimipistekoodi(entity, oldOrg, parentOrg);

        try {
            entity = organisaatioRepository.saveAndFlush(entity);
        } catch (OptimisticLockException ole) {
            throw new OrganisaatioModifiedException(ole);
        }

        // Saving the parent relationship
        entity = saveParentSuhteet(entity, parentOrg, opJarjNro);

        // Parent changed update children and reindex old parent.
        if (oldParent != null) {
            updateParentForChildrenRecursive(entity);
        }

        setLakkautusPvmForOppilaitosToimipistesRecursive(entity, entity.getLakkautusPvm());

        // Päivitä tiedot koodistoon.
        // organisaation päivittäminen koodistoon tehdään taustalla
        // jotta organisaation muokkaus olisi nopeampaa
        String info = null;
        koodistoService.addKoodistoSyncByOid(entity.getOid());
        return new OrganisaatioResult(entity, info);
    }

    private void updateExistingYhteystiedot(Organisaatio entity, Organisaatio oldOrg) {
        for (var y : entity.getYhteystiedot()) {
            for (var old : oldOrg.getYhteystiedot()) {
                if (y.getId() == old.getId()) {
                    y.setVersion(old.getVersion());
                }
            }
        }
    }

    private OrganisaatioResult save(Organisaatio entity, String parentOid) {

        if ((entity.getOid() != null) && (organisaatioRepository.findFirstByOid(entity.getOid()) != null)) {
            throw new OrganisaatioExistsException(entity.getOid());
        }

        if (entity.getOppilaitosKoodi() != null && entity.getOppilaitosKoodi().length() > 0) {
            if (checker.checkLearningInstitutionCodeIsUniqueAndNotUsed(entity)) {
            throw new LearningInstitutionExistsException("organisaatio.oppilaitos.exists.with.code");
            }
        }
        Organisaatio parentOrg = fetchParentOrg(parentOid);

        // Validate (throws exception)
        this.organisaatioValidationService.validateOrganisation(entity, parentOid, parentOrg);

        // Validate and persist lisatietotyypit
        if (!CollectionUtils.isEmpty(entity.getOrganisaatioLisatietotyypit())) {
            persistOrganisaatioLisatietotyyppis(entity);
        }

        boolean isVarhaiskasvatuksenToimipaikka = checkIfVarhaiskasvatuksenToimipaikka(entity);
        setVarhaiskasvatuksenToimipaikkaTietoRelations(entity, isVarhaiskasvatuksenToimipaikka);

        Organisaatio oldOrg = null;

        // Asetetaan parent path
        setParentPath(entity, parentOid);


        setPaivittajaData(entity);

        // Tarkistetaan organisaatio hierarkia
        checker.checkOrganisaatioHierarchy(entity, parentOid);

        // Generoidaan oidit
        try {
            generateOids(entity);
            generateOidsMetadata(entity.getMetadata());
        } catch (ExceptionMessage em) {
            throw new OrganisaatioResourceException(HttpStatus.INTERNAL_SERVER_ERROR, em.getMessage());
        }

        // Generoidaan opetuspiteenJarjNro
        String opJarjNro;
        opJarjNro = generateOpetuspisteenJarjNro(entity, parentOrg, entity.getTyypit());
        entity.setOpetuspisteenJarjNro(opJarjNro);

        // If inserting, check if ytunnus allready exists in the database
        if (entity.getYtunnus() != null) {
            checker.checkYtunnusIsUniqueAndNotUsed(entity.getYtunnus());
        }

        entity.setOrganisaatioPoistettu(false);

        checkDateConstraints(entity, parentOrg);

        setYhteystietoArvot(entity, false);

        generateToimipistekoodi(entity, oldOrg, parentOrg);

        //save org
        entity = organisaatioRepository.saveAndFlush(entity);
        // Saving the parent relationship
        entity = saveParentSuhteet(entity, parentOrg, opJarjNro);

        // Päivitä tiedot koodistoon.
        // organisaation päivittäminen koodistoon tehdään taustalla
        // jotta organisaation muokkaus olisi nopeampaa
        String info = null;
        koodistoService.addKoodistoSyncByOid(entity.getOid());
        return new OrganisaatioResult(entity, info);
    }

    private void persistOrganisaatioLisatietotyyppis(Organisaatio entity) {
        Set<OrganisaatioLisatietotyyppi> persistedLisatietotyypit = entity.getOrganisaatioLisatietotyypit().stream().map(lisatietotyyppi -> this.lisatietoTyyppiRepository.findByNimi(lisatietotyyppi.getLisatietotyyppi().getNimi()).orElseThrow(() -> new ValidationException(String.format("Lisätietoa %s ei löytynyt", lisatietotyyppi.getLisatietotyyppi().getNimi())))).map(lisatietotyyppi -> {
            OrganisaatioLisatietotyyppi organisaatioLisatietotyyppi = new OrganisaatioLisatietotyyppi();
            organisaatioLisatietotyyppi.setLisatietotyyppi(lisatietotyyppi);
            organisaatioLisatietotyyppi.setOrganisaatio(entity);
            return organisaatioLisatietotyyppi;
        }).collect(Collectors.toSet());
        entity.setOrganisaatioLisatietotyypit(persistedLisatietotyypit);
    }

    private void setVarhaiskasvatuksenToimipaikkaTietoRelations(Organisaatio entity, boolean isVarhaiskasvatuksenToimipaikka) {
        if (isVarhaiskasvatuksenToimipaikka) {
            entity.getVarhaiskasvatuksenToimipaikkaTiedot().getVarhaiskasvatuksenKielipainotukset().forEach(kielipainotus -> kielipainotus.setVarhaiskasvatuksenToimipaikkaTiedot(entity.getVarhaiskasvatuksenToimipaikkaTiedot()));
            entity.getVarhaiskasvatuksenToimipaikkaTiedot().getVarhaiskasvatuksenToiminnallinenpainotukset().forEach(toimintamuoto -> toimintamuoto.setVarhaiskasvatuksenToimipaikkaTiedot(entity.getVarhaiskasvatuksenToimipaikkaTiedot()));
        }
    }

    private Organisaatio validateHierarchy(String parentOid, Organisaatio entity, Organisaatio oldOrg) {
        Organisaatio oldParent = null;

        // Tarkistetaan organisaatiohierarkia jos hierarkia muuttunut (onko parent muuttunut)
        if (!parentOid.equals(oldOrg.getParent().getOid())) {
            log.info("Hierarkia muuttunut, tarkastetaan hierarkia.");
            checker.checkOrganisaatioHierarchy(entity, parentOid);
            oldParent = oldOrg.getParent();
        }

        // Tarkistetaan organisaatiohierarkia jos organisaatiotyypit muutuneet
        if (!entity.getTyypit().containsAll(oldOrg.getTyypit()) || !oldOrg.getTyypit().containsAll(entity.getTyypit())) {
            log.info("Organisaation tyypit muuttuneet, tarkastetaan hierarkia.");
            checker.checkOrganisaatioHierarchy(entity, parentOid);
        }

        // Tarkistetaan ettei lakkautuspäivämäärän jälkeen ole alkavia koulutuksia
        if (!OrganisaatioUtil.isSameDay(entity.getLakkautusPvm(), oldOrg.getLakkautusPvm())) {
            log.info("Lakkautuspäivämäärä muuttunut, tarkastetaan alkavat koulutukset.");
            checker.checkLakkautusAlkavatKoulutukset(entity);
        }
        return oldParent;
    }

    private Organisaatio saveParentSuhde(Organisaatio child, Organisaatio parent, String opJarjNro) {
        OrganisaatioSuhde curSuhde = organisaatioSuhdeRepository.findParentTo(child.getId(), null);
        if (parent != null && (curSuhde == null || !curSuhde.getParent().getId().equals(parent.getId()))) {
            if (curSuhde != null) {
                // Set end date for current parent relation before create new one.
                curSuhde.setLoppuPvm(new Date());
                organisaatioSuhdeRepository.save(curSuhde);
            }
            addChild(parent, child, Calendar.getInstance().getTime(), opJarjNro);
        }
        child.setParentSuhteet(organisaatioSuhdeRepository.findByChild(child));
        return this.organisaatioRepository.findFirstByOid(child.getOid());
    }

    private Set<YhteystietoArvo> mergeYhteystietoArvos(Organisaatio org, Set<YhteystietoArvo> nys, boolean updating) {

        Map<String, YhteystietoArvo> ov = new HashMap<>();

        for (YhteystietoArvo ya : yhteystietoArvoRepository.findByOrganisaatio(org)) {
            if (!isAllowed(org, ya.getKentta().getYhteystietojenTyyppi())) {
                yhteystietoArvoRepository.delete(ya);
            } else {
                ov.put(ya.getKentta().getOid() + ya.getKieli(), ya);
            }
        }

        Set<YhteystietoArvo> ret = new HashSet<>();

        for (YhteystietoArvo ya : nys) {
            List<YhteystietojenTyyppi> yt = yhteystietojenTyyppiRepository.findByOid(ya.getKentta().getYhteystietojenTyyppi().getOid());
            if (yt.isEmpty()) {
                continue;
            }
            List<YhteystietoElementti> kentat = yhteystietoElementtiRepository.findByOid(ya.getKentta().getOid());
            if (kentat.isEmpty()) {
                continue;
            }
            ya.setKentta(kentat.get(0));
            if (!isAllowed(org, yt.get(0))) {
                continue;
            }
            YhteystietoArvo o = ov.get(ya.getKentta().getOid() + ya.getKieli());
            if (o != null) {
                o.setArvoText(ya.getArvoText());
                o.setKieli(ya.getKieli());
                yhteystietoArvoRepository.save(o); // TODO check if works?
                ret.add(o);
            } else {
                ya.setOrganisaatio(org);
                try {
                    ya.setYhteystietoArvoOid(oidService.newOid(NodeClassCode.TEKN_5));
                } catch (ExceptionMessage em) {
                    throw new OrganisaatioResourceException(HttpStatus.INTERNAL_SERVER_ERROR, em.getMessage());
                }
                if (updating) {
                    yhteystietoArvoRepository.save(ya); // TODO works?
                }
                ret.add(ya);
            }
        }

        return ret;
    }

    /**
     * Lasketaan opetuspisteen / toimipisteen koodi.
     * Lisätään parent oppilaitoksen oppilaitoskoodiin opetuspisteen järjestysnumero.
     *
     * @param org Toimipiste
     * @return Toimipistekoodi
     */
    public String calculateToimipisteKoodi(Organisaatio org, Organisaatio parent) {
        if (parent == null) {
            parent = org.getParent();
        }
        if (org == null) {
            return "";
        }

        if (organisaatioIsOfType(org, OrganisaatioTyyppi.OPPILAITOS)) {
            return org.getOppilaitosKoodi();
        }

        if (organisaatioIsOfType(org, OrganisaatioTyyppi.TOIMIPISTE)) {
            Organisaatio parentOppilaitos = findParentOppilaitos(org, parent);
            if (parentOppilaitos == null) {
                return null;
            }

            String opJarjNro = org.getOpetuspisteenJarjNro();
            if (isEmpty(opJarjNro)) {
                return "";
            }

            return parentOppilaitos.getOppilaitosKoodi() + opJarjNro;
        }
        return "";
    }

    /**
     * Check given organisation type.
     *
     * @param org                Organisaatio
     * @param organisaatioTyyppi Type to evaluate against
     * @return is organisaatio given type
     */
    private boolean organisaatioIsOfType(Organisaatio org, OrganisaatioTyyppi organisaatioTyyppi) {
        if (organisaatioTyyppi == null || org == null) {
            return false;
        }

        return (org.getTyypit() != null) && (org.getTyypit().contains(organisaatioTyyppi.koodiValue()));
    }

    private void generateOids(Organisaatio organisaatio) throws ExceptionMessage {
        if (organisaatio.getOid() == null) {
            if (OrganisaatioUtil.isRyhma(organisaatio)) {
                /// @TODO: Sitten kun Oid palveluss on ryhmä mukana --> korjaa
                //  organisaatio.setOid(oidService.newOid(NodeClassCode.RYHMA));

                organisaatio.setOid(oidService.newOidByClassValue("28"));
            } else {
                organisaatio.setOid(oidService.newOid(NodeClassCode.TOIMIPAIKAT));
            }
        }
        for (Yhteystieto curYt : organisaatio.getYhteystiedot()) {
            if (curYt.getYhteystietoOid() == null) {
                curYt.setYhteystietoOid(oidService.newOid(NodeClassCode.TEKN_5));
            }
        }
    }

    private void generateOidsMetadata(OrganisaatioMetaData omd) throws ExceptionMessage {
        if (omd != null) {
            for (Yhteystieto curYt : omd.getYhteystiedot()) {
                if (curYt != null && curYt.getYhteystietoOid() == null) {
                    curYt.setYhteystietoOid(oidService.newOid(NodeClassCode.TEKN_5));
                }
            }
        }
    }

    private String generateOpetuspisteenJarjNro(Organisaatio entity) {
        return generateOpetuspisteenJarjNro(entity, entity.getParent(), entity.getTyypit());
    }

    /*
     * Generoidaan opetuspisteen järjestysnumero toimipisteelle.
     * Opetuspisteen järjestysnumero on seuraava vapaa numero oppilaitoksen alla.
     */
    private String generateOpetuspisteenJarjNro(Organisaatio entity, Organisaatio parent, Set<String> tyypit) {
        // Opetuspisteen jarjestysnumero generoidaan vain toimipisteille,
        // mutta jos organisaatio on samalla oppilaitos, niin ei generoida
        if (tyypit.contains(OrganisaatioTyyppi.OPPILAITOS.koodiValue()) && !tyypit.contains(OrganisaatioTyyppi.TOIMIPISTE.koodiValue())) {
            log.debug("Organisaatio {} ei toimipiste -> ei tarvetta opetuspisteen järjestysnumerolle ({})", entity.getOid(), tyypit);
            return null;
        }

        // Haetaan parent oppilaitos
        Organisaatio parentOppilaitos = findParentOppilaitos(entity, parent);
        if (parentOppilaitos == null) {
            log.warn("Oppilaitos not found in parents");
            return null;
        }

        // Kokeillaan aina seuraavaa numeroa kunnes vapaa toimipistekoodi löytyy
        String jarjNro;
        int nextVal = parentOppilaitos.getChildCount(null) + 1;
        for (int i = nextVal; i < 100; i++) {
            jarjNro = (i < 10) ? String.format("%s%s", "0", i) : String.format("%s", i);
            if (checker.checkToimipistekoodiIsUniqueAndNotUsed(parentOppilaitos.getOppilaitosKoodi() + jarjNro)) {
                log.debug("Generoitu opetuspisteen järjestysnumero: {} / {}", parentOppilaitos.getOppilaitosKoodi(), jarjNro);
                return jarjNro;
            }
        }

        log.warn("Failed to generate opetuspisteenjarjnro (oppilaitoskoodi={})", parentOppilaitos.getOppilaitosKoodi());
        return null;

    }

    private void setParentPath(Organisaatio entity, String parentOid) {
        if (parentOid == null) {
            parentOid = rootOrganisaatioOid;
        }
        StringBuilder parentIdPath = new StringBuilder();
        List<Organisaatio> parents = organisaatioRepository.findParentsTo(parentOid);
        List<String> parentOids = new ArrayList<>();
        for (Organisaatio curParent : parents) {
            parentOids.add(curParent.getOid());
            parentIdPath.append(PARENT_SEPARATOR).append(curParent.getId());
        }
        Collections.reverse(parentOids);
        if (!parents.isEmpty()) {
            parentIdPath.append(PARENT_SEPARATOR);
        }
        entity.setParentOids(parentOids);
        entity.setParentIdPath(parentIdPath.toString());
    }

    private static Organisaatio findParentOppilaitos(Organisaatio organisaatio, Organisaatio parent) {
        Organisaatio currentParent = organisaatio.getParent();

        // Uuden organisaation luonnin yhteydessä ei parent-suhdetta ole välttämättä vielä
        // asetettu.
        if (currentParent == null) {
            currentParent = parent;
        }

        while (currentParent != null) {
            if (OrganisaatioUtil.isOppilaitos(currentParent)) {
                return currentParent;
            }

            currentParent = currentParent.getParent();
        }
        return null;
    }

    private boolean isAllowed(Organisaatio org, YhteystietojenTyyppi yad) {
        if (org.getOppilaitosTyyppi() != null && yad.getSovellettavatOppilaitostyyppis().contains(org.getOppilaitosTyyppi())) {
            return true;
        }
        for (String otype : org.getTyypit()) {
            if (yad.getSovellettavatOrganisaatioTyyppis().contains(otype)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEmpty(String val) {
        return val == null || val.isEmpty();
    }


    private void updateParentForChildrenRecursive(Organisaatio parent) {
        List<Organisaatio> children = organisaatioRepository.findChildren(parent.getId());
        if (children == null || children.isEmpty()) {
            return;
        }

        for (Organisaatio child : children) {
            // Create new parent id / oid paths for child.
            setParentPath(child, parent.getOid());
            Organisaatio updated = organisaatioRepository.saveAndFlush(child);
            updateParentForChildrenRecursive(updated);

        }
    }

    private void setLakkautusPvmForOppilaitosToimipistesRecursive(Organisaatio entity, Date lakkautusPvm) {
        if (!OrganisaatioUtil.isOppilaitos(entity) || lakkautusPvm == null) {
            return;
        }
        List<Organisaatio> children = organisaatioRepository.findChildren(entity.getId());
        for (Organisaatio child : children) {
            if (OrganisaatioUtil.isToimipiste(child)
                    && (child.getLakkautusPvm() == null || child.getLakkautusPvm().after(lakkautusPvm))) {
                child.setLakkautusPvm(lakkautusPvm);
                Organisaatio updated = organisaatioRepository.saveAndFlush(child);
                setLakkautusPvmForOppilaitosToimipistesRecursive(updated, lakkautusPvm);
            }
        }
    }

    @Override
    public List<OrganisaatioNimi> getOrganisaatioNimet(String oid) {
        List<OrganisaatioNimi> nimet = organisaatioNimiRepository.findNimet(oid);
        if (nimet.isEmpty()) {
            throw new OrganisaatioNotFoundException(oid);
        }
        return nimet;
    }

    @Override
    public OrganisaatioNimi newOrganisaatioNimi(String oid, OrganisaatioNimiDTO nimidto) {
        Organisaatio orgEntity = getOrganisaatio(oid);

        // Luodaan tallennettava entity objekti
        OrganisaatioNimi nimiEntity = organisaatioNimiModelMapper.map(nimidto, OrganisaatioNimi.class);

        // Asetetaan organisaatio
        nimiEntity.setOrganisaatio(orgEntity);

        // Insertoidaan kantaan
        nimiEntity = organisaatioNimiRepository.save(nimiEntity);

        // Jos nimi tulee nykyiseksi nimeksi, niin päivitetään se myös organisaatioon.
        if (OrganisaatioNimiUtil.isValidCurrentNimi(nimiEntity)) {
            // Asetetaan organisaation nimi ja nimihistorian nykyinen nimi
            // osoittamaan varmasti samaan monikieliseen tekstiin
            orgEntity.setNimi(nimiEntity.getNimi());
            orgEntity.setNimihaku(OrganisaatioNimiUtil.createNimihaku(orgEntity.getNimi()));
            organisaatioRepository.save(orgEntity);
        }

        return nimiEntity;
    }

    @Override
    public OrganisaatioNimi updateOrganisaatioNimi(String oid, OrganisaatioNimiUpdateDTO nimiUpdateDTO) {
        Organisaatio orgEntity = getOrganisaatio(oid);
        OrganisaatioNimiDTO currentNimiDTO = nimiUpdateDTO.getCurrentNimi();
        OrganisaatioNimiDTO updatedNimiDTO = nimiUpdateDTO.getUpdatedNimi();
        log.debug("Haetaan organisaation: {} nimeä alkupäivämäärällä: {}", oid, currentNimiDTO.getAlkuPvm());
        // Haetaan päivitettävä entity objecti
        OrganisaatioNimi nimiEntityOld = this.organisaatioNimiRepository.findNimi(orgEntity, currentNimiDTO);

        if (nimiEntityOld == null) {
            throw new OrganisaatioNimiNotFoundException(oid, currentNimiDTO.getAlkuPvm());
        }
        MonikielinenTeksti currentNimiMkt = Hibernate.unproxy(orgEntity.getActualNimi(), MonikielinenTeksti.class);
        boolean isUpdatedNameTheCurrentName = Objects.equals(currentNimiMkt.getId(), nimiEntityOld.getNimi().getId());

        // Luodaan tallennettava entity objekti
        OrganisaatioNimi nimiEntityNew = organisaatioNimiModelMapper.map(updatedNimiDTO, OrganisaatioNimi.class);

        // Asetetaan organisaatio
        nimiEntityNew.setOrganisaatio(orgEntity);

        // Päivitystapauksessa asetetaan id:t, ettei luoda uusia rivejä
        nimiEntityNew.setId(nimiEntityOld.getId());
        nimiEntityNew.getNimi().setId(nimiEntityOld.getNimi().getId());
        nimiEntityNew.getNimi().setVersion(nimiEntityOld.getNimi().getVersion());

        log.debug("updating nimi: {}", nimiEntityNew);

        // Päivitetään nimi
        organisaatioNimiRepository.save(nimiEntityNew);
        if (isUpdatedNameTheCurrentName) {
            log.info("Updated nimi is the current organization nimi; updating nimihaku");
            orgEntity.setNimihaku(OrganisaatioNimiUtil.createNimihaku(orgEntity.getNimi()));
            organisaatioRepository.save(orgEntity);
        }
        // Palautetaan päivitetty nini
        nimiEntityNew = organisaatioNimiRepository.findById(nimiEntityNew.getId()).orElseThrow();

        return nimiEntityNew;
    }

    @Override
    public void deleteOrganisaatioNimi(String oid, OrganisaatioNimiDTO nimidto) {
        Organisaatio orgEntity = getOrganisaatio(oid);

        // Haetaan poistettava entity objecti
        OrganisaatioNimi nimiEntity = this.organisaatioNimiRepository.findNimi(orgEntity, nimidto);

        // Tarkistetaan, että nimi ei ole nykyinen nimi
        OrganisaatioNimi currentNimiEntity = this.organisaatioNimiRepository.findCurrentNimi(orgEntity);

        if (nimiEntity == null) {
            throw new OrganisaatioNimiNotFoundException(oid, nimidto.getAlkuPvm());
        }

        // Tarkistetaan ettei poistettava nimi ole organisaation nykyinen nimi
        if (currentNimiEntity != null) {
            if (currentNimiEntity.getId().equals(nimiEntity.getId())) {
            throw new OrganisaatioNimiDeleteException();
        }
        }

        // Vain uusimman nimen, jonka voimassaolo ei ole alkanut saa poistaa
        if (nimidto.getAlkuPvm().before(new Date())) {
            throw new OrganisaatioNimiDeleteException();
        }

        log.debug("deleting {}", nimiEntity);

        // Poistetaan
        this.organisaatioNimiRepository.delete(nimiEntity);
    }


    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void batchValidatePvm(HashMap<String, OrganisaatioMuokkausTiedotDTO> givenData, HashMap<String, Organisaatio> organisaatioMap) {
        // näiden oidien vanhemmuussuhteet on jo löydetty
        Set<String> processed = new HashSet<>(givenData.size());
        List<Organisaatio> roots = new ArrayList<>(givenData.size());

        // etsitään juuriorganisaatiot, eli ne, joiden vanhempaa ei löydy annetuista oideista
        for (Organisaatio o : organisaatioMap.values()) {
            log.debug("bulkUpdatePvm(): käsitellään organisaatio:{},oid:{}", o, o.getOid());
            while (!processed.contains(o.getOid())) {
                processed.add(o.getOid());

                // jos vanhempaa ei löyty annetusta oidlistasta, tämä on juuriorganisaatio
                if (o.getParent() == null || !givenData.keySet().contains(o.getParent().getOid())) {
                    roots.add(o);
                    break;
                }
                o = o.getParent();
            }
        }
        log.debug("bulkUpdatePvm(): processed:{}", processed);

        String virheViesti = "";
        // tarkistetaan ettei minkään juuriorganisaatio alta löydy päivämääriä jotka rikkovat rajat
        for (Organisaatio o : roots) {
            virheViesti = checker.checkPvmConstraints(o, null, null, givenData);
            if (!virheViesti.equals("")) {
                log.error("bulkUpdatePvm() error: {}", virheViesti);
                throw new OrganisaatioDateException();
            }
        }
        for (Map.Entry<String, Organisaatio> entry : organisaatioMap.entrySet()) {
            String oid = entry.getKey();
            OrganisaatioMuokkausTiedotDTO tieto = givenData.get(oid);
            Organisaatio org = entry.getValue();

            if (tieto != null) {
                log.debug("bulkUpdatePvm(): testataan onko Organisaatiolla (oid {}) koulutuksia loppupäivämäärän {} jälkeen", org.getOid(), tieto.getLoppuPvm());
                if ((tieto.getLoppuPvm() != null) && !tieto.getLoppuPvm().equals(org.getLakkautusPvm()) && (organisaatioTarjonta.alkaviaKoulutuksia(oid, tieto.getLoppuPvm()))) {
                    log.error("Organisaatiolla (oid {}) koulutuksia jotka alkavat lakkautuspäivämäärän ({}) jälkeen", oid, tieto.getLoppuPvm());
                    throw new AliorganisaatioLakkautusKoulutuksiaException();
                }
            }
        }
    }

    @Override
    public Set<Organisaatio> processNewOrganisaatioSuhdeChanges() {
        Set<Organisaatio> results = new HashSet<>();
        List<OrganisaatioSuhde> suhdeList = organisaatioSuhdeRepository.findForDay(new Date());
        log.info("Found {} organisaatiosuhdee with alkupvm of today", suhdeList.size());
        for (OrganisaatioSuhde os : suhdeList) {
            log.info("Processing organisaatiosuhde {}", os);

            Organisaatio child = os.getChild();
            // Liitos ei muuta parenttia (kts. Organisaatio#getParent)
            if (!OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.LIITOS.equals(os.getSuhdeTyyppi())) {
                log.info("Setting org {} parent to {}", child.getOid(), os.getParent().getOid());
                setParentPath(child, os.getParent().getOid());
                organisaatioRepository.save(child); // TODO works?
            }

            updateParentForChildrenRecursive(child);

            results.add(child);
        }

        return results;
    }

    @Override
    public void mergeOrganisaatio(String organisaatio, String newParent, Optional<Date> inputDate, boolean merge) {
        Date date = inputDate.orElseGet(Date::new);
        Organisaatio child = getOrganisaatio(organisaatio);
        Organisaatio parent = getOrganisaatio(newParent);
        if (merge) mergeOrganisaatio(child, parent, date);
        else changeOrganisaatioParent(child, parent, date);
    }

    @Override
    public void mergeOrganisaatio(Organisaatio organisaatio, Organisaatio newParent, Date date) {
        // Organisaatiota ei saa liittää itseensä
        if (organisaatio.getOid().equals(newParent.getOid())) {
            throw new OrganisaatioMoveException("organisation.move.merge.self");
        }

        // Organisaatiota ei saa yhdistää eri organisaatiotasolla olevaan organisaatioon
        // Organisaatioista on löydyttävä ainakin 1 yhteinen tyyppi
        if (!CollectionUtils.containsAny(organisaatio.getTyypit(), newParent.getTyypit())) {
            throw new OrganisaatioMoveException("organisation.move.merge.level");
        }

        // Organisaatiota ei saa yhdistää lakkautettuun tai poistettuun organisaatioon
        if (newParent.isOrganisaatioPoistettu() || OrganisaatioUtil.isPassive(newParent)) {
            throw new OrganisaatioMoveException("organisation.move.merge.parent.invalid");
        }

        // Lakkautetaan yhdistyvä organisaatio
        Calendar previousDay = Calendar.getInstance();
        previousDay.setTime(date);
        previousDay.add(Calendar.DAY_OF_MONTH, -1);
        organisaatio.setLakkautusPvm(previousDay.getTime());
        organisaatioRepository.save(organisaatio); // TODO WOrkds

        // Lisätään uusi organisaatioiden liitos
        addLiitos(organisaatio, newParent, date);

        // Siirretään kaikki aktiiviset aliorganisaatiot uuden parentin alle
        final List<OrganisaatioSuhde> suhteet = organisaatioSuhdeRepository.findChildrenTo(organisaatio.getId(), date);
        for (OrganisaatioSuhde suhde : suhteet) {
            Organisaatio child = suhde.getChild();
            if (!OrganisaatioUtil.isPassive(child)) {
                changeOrganisaatioParent(child, newParent, date);
            }
        }

        // Päivitetään tiedot koodistoon.
        koodistoService.addKoodistoSyncByOid(organisaatio.getOid());
    }

    @Override
    public Timestamp updateTarkastusPvm(String oid) {
        Organisaatio org = getOrganisaatio(oid);
        org.setTarkastusPvm(new Date());
        return new Timestamp(org.getTarkastusPvm().getTime());
    }

    @Override
    public void changeOrganisaatioParent(Organisaatio organisaatio, Organisaatio newParent, Date date) {
        // Organisaatiota ei saa siirtää nykyisen parentin alle
        if (organisaatio.getParent().getOid().equals(newParent.getOid())) {
            throw new OrganisaatioMoveException("organisation.move.parent.invalid");
        }

        // Organisaatiota ei saa siirtää väärällä hierarkiatasolla olevaan organisaatioon
        checker.checkParentChildHierarchy(organisaatio, newParent);

        // Organisaatiota ei saa siirtää lakkautettuun tai poistettuun organisaatioon
        if (newParent.isOrganisaatioPoistettu() || OrganisaatioUtil.isPassive(newParent)) {
            throw new OrganisaatioMoveException("organisation.move.parent.invalid");
        }

        OrganisaatioSuhde currentParentRelationship = organisaatioSuhdeRepository.findParentTo(organisaatio.getId(), null);
        currentParentRelationship.setLoppuPvm(date);
        organisaatioSuhdeRepository.save(currentParentRelationship); // TODO Works?

        // Luodaan uusi suhde
        // HUOM! Tässä pitää laittaa organisaatiosuhde "organisaation kautta --> näin parent laskenta pysyy mukana"
        OrganisaatioSuhde parentRelation = new OrganisaatioSuhde();
        parentRelation.setSuhdeTyyppi(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.HISTORIA);
        parentRelation.setAlkuPvm(date);
        parentRelation.setLoppuPvm(null);
        parentRelation.setChild(organisaatio);
        parentRelation.setParent(newParent);

        // Asetetaan uusi parentsuhde
        organisaatio.getParentSuhteet().add(parentRelation);

        // Organisaatiolla on uusi parent, toimipisteen tapauksessa ollaan uuden oppilaitoksen alla.
        // Lasketaan toimipisteelle uusi opetuspisteen järjestysnumero ja asetetaan toimipistekoodi.
        // Mahdollinen vanha toimipistekoodi pitää lakkauttaa ja luoda uusi koodiston koodi.
        if (OrganisaatioUtil.isToimipiste(organisaatio)) {
            // Vanha toimipistekoodi tarvitaan pitää lakkauttaa
            String oldToimipistekoodi = organisaatio.getToimipisteKoodi();

            String opJarjNro = generateOpetuspisteenJarjNro(organisaatio);
            organisaatio.setOpetuspisteenJarjNro(opJarjNro);
            organisaatio.setToimipisteKoodi(calculateToimipisteKoodi(organisaatio, null));
            parentRelation.setOpetuspisteenJarjNro(opJarjNro);

            Calendar previousDay = Calendar.getInstance();
            previousDay.setTime(date);
            previousDay.add(Calendar.DAY_OF_MONTH, -1);

            // Lakkautetaan vanha opetuspistekoodi (opetuspiste pistetään lakkaamaan päivää ennen)
            organisaatioKoodisto.lakkautaKoodi(OrganisaatioKoodisto.KoodistoUri.TOIMIPISTE.uri(), oldToimipistekoodi, previousDay.getTime());

            // Päivitetään uusi opetuspistekoodi koodistoon.
            koodistoService.addKoodistoSyncByOid(organisaatio.getOid());

            // Jos toimipisteen nimi alkaa sen parent oppilaitoksen nimellä, niin siivotaan tuo osa pois toimipisteen nimestä
            MonikielinenTeksti updatedToimipisteNimi = getUpdatedToimipisteNimi(organisaatio, newParent);
            organisaatio.setNimi(updatedToimipisteNimi);
        }

        // Päivitetään suhteet ja indeksointi, jos uusi parent on jo voimassa (date == tänään / aiemmin)
        Date today = new Date();
        if (date.before(today) || DateUtils.isSameDay(date, today)) {
            setParentPath(organisaatio, newParent.getOid());
            organisaatioRepository.save(organisaatio); // TODO worrks?

            updateParentForChildrenRecursive(organisaatio);
        }
    }

    // Poistetaan parent oppilaitoksen nimeä vastaava prefix toimispisteen nimestä
    private MonikielinenTeksti getUpdatedToimipisteNimi(Organisaatio organisaatio, Organisaatio newParent) {
        Organisaatio oldParent = organisaatio.getParent();
        MonikielinenTeksti oldParentNimi = oldParent.getNimi();
        MonikielinenTeksti nimi = organisaatio.getNimi();
        MonikielinenTeksti newParentNimi = newParent.getNimi();
        Map<String, String> oldParentNimiMap = oldParentNimi.getValues();
        Map<String, String> toimipisteNimiMap = nimi.getValues();
        Map<String, String> newParentNimiMap = newParentNimi.getValues();

        updateNimiValues(oldParentNimiMap, toimipisteNimiMap, newParentNimiMap);
        nimi.setValues(toimipisteNimiMap);
        return nimi;
    }

    public void updateNimiValues(Map<String, String> oldParentNimiMap, Map<String, String> currentNimiMap, Map<String, String> newParentNimiMap) {
        oldParentNimiMap.forEach((oldParentNimikey, oldParentNimivalue) -> {
            String newParentNimi = newParentNimiMap.get(oldParentNimikey) != null ? newParentNimiMap.get(oldParentNimikey) : "";
            String currentNimi = currentNimiMap.get(oldParentNimikey);
            if (currentNimi != null && !newParentNimi.isEmpty()) {
                if (currentNimi.startsWith(oldParentNimivalue)) {
                    String changeName = currentNimi.replaceAll(oldParentNimivalue, newParentNimi);
                    currentNimiMap.put(oldParentNimikey, changeName);
                } else {
                    currentNimiMap.put(oldParentNimikey, newParentNimi + ", " + currentNimi);
                }
            }
        });
    }

    private Organisaatio updateCurrentNimiToOrganisaatio(Organisaatio organisaatio) {
        // Haetaan organisaation current nimi
        OrganisaatioNimi nimiEntity = this.organisaatioNimiRepository.findCurrentNimi(organisaatio);

        if (nimiEntity == null) {
            throw new OrganisaatioNimiNotFoundException(organisaatio.getOid());
        }

        // Päivitetään organisaation nimi
        organisaatio.setNimi(nimiEntity.getNimi());

        log.debug("updateCurrentNimiToOrganisaatio updating {}", organisaatio);
        try {
            // Päivitetään nimi
            organisaatioRepository.save(organisaatio);
        } catch (OptimisticLockException ole) {
            throw new OrganisaatioModifiedException(ole);
        }

        // Palautetaan päivitetty organisaatio
        return organisaatioRepository.findById(organisaatio.getId()).orElseThrow();
    }

    @Override
    public void updateCurrentOrganisaatioNimet() {
        // Haetaan organisaatiot, joiden nimi ei ole nimihistorian current nimi
        List<Organisaatio> organisaatiot = this.organisaatioNimiRepository.findNimiNotCurrentOrganisaatiot();

        if (organisaatiot.isEmpty()) {
            log.info("Orgnisaatioiden nimet kunnossa");
        }

        for (Organisaatio organisaatio : organisaatiot) {
            log.debug("Organisaation nimen update tarve: {}", organisaatio);

            // Päiviteään organisaatiolle nimihistorian current nimi
            organisaatio = this.updateCurrentNimiToOrganisaatio(organisaatio);
            organisaatio.setNimihaku(OrganisaatioNimiUtil.createNimihaku(organisaatio.getNimi()));


            // Päivitetään tiedot koodistoon.
            koodistoService.addKoodistoSyncByOid(organisaatio.getOid());
        }
    }

    private Organisaatio fetchParentOrg(String parentOid) {
        // Haetaan parent organisaatio
        return (parentOid != null && !parentOid.equalsIgnoreCase(rootOrganisaatioOid)) ? organisaatioRepository.findFirstByOid(parentOid) : null;

    }

    private boolean checkIfVarhaiskasvatuksenToimipaikka(Organisaatio entity) {
        return entity.getTyypit().stream().anyMatch(OrganisaatioTyyppi.VARHAISKASVATUKSEN_TOIMIPAIKKA.koodiValue()::equals);
    }


    private void checkDateConstraints(Organisaatio entity, Organisaatio parentOrg) {
        // OH-116
        if (parentOrg != null) {
            // Check if organization has parent and if it has, check that passivation dates match to parent
            OrganisationDateValidator dateValidator = new OrganisationDateValidator(true);
            if (!dateValidator.apply(Maps.immutableEntry(parentOrg, entity))) {
                throw new OrganisaatioDateException();
            }
        }
        // check min and max dates and validate against child organisations too
        checker.checkPvmConstraints(entity, null, null, new HashMap<>());
    }

    private void setYhteystietoArvot(Organisaatio entity, boolean updating) {
        // Asetetaan yhteystietoarvot
        entity.setYhteystietoArvos(mergeYhteystietoArvos(entity, entity.getYhteystietoArvos(), updating));
        // Kirjoitetaan yhteystiedot uusiksi (ei päivitetä vanhoja)
        for (Yhteystieto yhtTieto : entity.getYhteystiedot()) {
            yhtTieto.setOrganisaatio(entity);
        }
        for (OrganisaatioNimi nimi : entity.getNimet()) {
            nimi.setOrganisaatio(entity);
        }
        // Kirjoitetaan nimihistoria uusiksi (ei päivitetä vanhoja)
        // Nimihistoriaan liittyvät tarkistukset (HUOM! Ei koske Ryhmiä)
        if (!OrganisaatioUtil.isRyhma(entity)) {
            /** @TODO --> Tarkistetaan, ettei nimihistoriaa muuteta muuta kuin nykyisen tai uusimman nimen osalta */
            // Tarkistetaan, että nimen alkupäivämäärä ei ole NULL
            List<OrganisaatioNimi> nimet = entity.getNimet();
            checker.checkNimihistoriaAlkupvm(nimet);

            // Tarkistetaan, että nimihistoriassa on organisaatiolle validi nimi
            MonikielinenTeksti nimi = OrganisaatioNimiUtil.getNimi(nimet);
            if (nimi == null) {
                throw new OrganisaatioNameHistoryNotValidException();
            }

            // Tarkistetaan, että organisaatiolle asetettu nimi ei ole
            // ristiriidassa nimihistorian kanssa
            if (!nimi.getValues().equals(entity.getNimi().getValues())) {
                throw new OrganisaatioNameHistoryNotValidException();
            }
            // Asetetaan organisaatiolle sama nimi instanssi kuin nimihistoriassa
            entity.setNimi(nimi);
        }
    }

    public void generateToimipistekoodi(Organisaatio entity, Organisaatio oldOrg, Organisaatio parentOrg) {
        // Generate natural key, OVT-4954
        // "Jos kyseessä on koulutustoimija pitäisi palauttaa y-tunnus."
        // "Jos oppilaitos, palautetaan oppilaitosnumero."
        // "Jos toimipiste, palautetaan oppilaitosnro+toimipisteenjärjestysnumero(konkatenoituna)sekä yhkoulukoodi."
        if (oldOrg == null || isEmpty(oldOrg.getToimipisteKoodi())) {
            entity.setToimipisteKoodi(calculateToimipisteKoodi(entity, parentOrg));
        } else {
            entity.setToimipisteKoodi(oldOrg.getToimipisteKoodi());
        }
    }

    private Organisaatio saveParentSuhteet(Organisaatio entity, Organisaatio parentOrg, String opJarjNro) {
        if (parentOrg == null) {
            // Koulutustoimija in root level is stored under OPH
            Organisaatio rootOrganisation = organisaatioRepository.findFirstByOid(rootOrganisaatioOid);
            entity = saveParentSuhde(entity, rootOrganisation, opJarjNro);
        } else {
            entity = saveParentSuhde(entity, parentOrg, opJarjNro);
        }
        return entity;
    }

    protected void setPaivittajaData(Organisaatio entity) {
        // Asetetaan päivittäjä ja päivityksen aikaleima
        try {
            entity.setPaivittaja(getCurrentUser());
            entity.setPaivitysPvm(new Date());
        } catch (Exception t) {
            throw new OrganisaatioResourceException(HttpStatus.INTERNAL_SERVER_ERROR, t.getMessage(), "error.setting.updater");
        }
    }


    Organisaatio getOrganisaatio(String organisaatio) {
        Organisaatio child = this.organisaatioRepository.findFirstByOid(organisaatio);
        if (organisaatio == null) {
            throw new OrganisaatioNotFoundException(organisaatio);
        }
        return child;
    }

    private void addChild(Organisaatio parent, Organisaatio child, Date startingFrom, String opetuspisteenJarjNro) {
        if (startingFrom == null) {
            startingFrom = new Date();
        }

        OrganisaatioSuhde childRelation = new OrganisaatioSuhde();
        childRelation.setAlkuPvm(startingFrom);
        childRelation.setLoppuPvm(null);
        childRelation.setChild(child);
        childRelation.setParent(parent);
        childRelation.setOpetuspisteenJarjNro(opetuspisteenJarjNro);
        childRelation = organisaatioSuhdeRepository.save(childRelation);

        logRelation("  Created new child relation: ", childRelation);

    }

    private void addLiitos(Organisaatio organisaatio, Organisaatio kohde, Date startingFrom) {
        log.info("addLiitos({}, {}, {})", organisaatio, kohde, startingFrom);

        if (organisaatio == null || kohde == null) {
            throw new IllegalArgumentException();
        }
        if (startingFrom == null) {
            startingFrom = new Date();
        }

        //
        // Create the new relation
        //
        OrganisaatioSuhde liitosRelation = new OrganisaatioSuhde();
        liitosRelation.setAlkuPvm(startingFrom);
        liitosRelation.setLoppuPvm(null);
        liitosRelation.setChild(organisaatio);
        liitosRelation.setParent(kohde);
        liitosRelation.setSuhdeTyyppi(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.LIITOS);

        liitosRelation = organisaatioSuhdeRepository.save(liitosRelation);

        logRelation("  Created new liitos relation: ", liitosRelation);

    }

    private void logRelation(String message, OrganisaatioSuhde relation) {
        if (relation == null) {
            log.info("  {} - NULL", message);
        } else {
            log.info("  {} --> pId={}, cId={}, aPvm={}, lPvm={}",
                    message, relation.getParent().getId(), relation.getChild().getId(),
                    relation.getAlkuPvm(), relation.getLoppuPvm());
        }
    }
}
