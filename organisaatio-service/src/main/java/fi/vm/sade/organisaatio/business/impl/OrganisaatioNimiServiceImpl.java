package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.OrganisaatioNimiService;
import fi.vm.sade.organisaatio.dto.OrganisaatioNimiDTO;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioNimiModelMapper;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;
import fi.vm.sade.organisaatio.repository.OrganisaatioNimiRepository;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class OrganisaatioNimiServiceImpl implements OrganisaatioNimiService {

    private final OrganisaatioNimiModelMapper organisaatioNimiModelMapper;
    private final OrganisaatioRepository organisaatioRepository;
    private final OrganisaatioNimiRepository organisaatioNimiRepository;

    @Override
    public List<OrganisaatioNimiDTO> getNimet(String oid) {

        Organisaatio org = organisaatioRepository.findFirstByOid(oid);
        List<OrganisaatioNimiDTO> orgNimet = organisaatioNimiModelMapper.map(org.getNimet(), new TypeToken<List<OrganisaatioNimiDTO>>() {
        }.getType());
        return getOrganisaatioNimiDTOS(orgNimet, org).stream()
                .sorted(Comparator.comparing(OrganisaatioNimiDTO::getAlkuPvm)).collect(Collectors.toList());
    }

    private List<OrganisaatioNimiDTO> getOrganisaatioNimiDTOS(List<OrganisaatioNimiDTO> orgNimet, Organisaatio org) {
        return org.getTyypit().contains(OrganisaatioTyyppi.TOIMIPISTE.koodiValue()) ?
                decoreateToimipisteNimet(orgNimet, getOppilaitosNameIntervals(org)) :
                orgNimet;

    }

    List<Map.Entry<Map.Entry<Date, Optional<Date>>, List<OrganisaatioNimiDTO>>> getOppilaitosNameIntervals(Organisaatio org) {
        return sanitizeParentSuhteet(org.getParentSuhteet(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.HISTORIA), org).stream()
                .map(parentSuhde -> {
                    List<OrganisaatioNimiDTO> parentNimet = organisaatioNimiModelMapper.map(parentSuhde.getParent().getNimet(), new TypeToken<List<OrganisaatioNimiDTO>>() {
                    }.getType());
                    List<OrganisaatioNimiDTO> relevantParentNimet = getRelevantParentNimet(org, parentNimet);
                    return Map.<Map.Entry<Date, Optional<Date>>, List<OrganisaatioNimiDTO>>entry(Map.entry(parentSuhde.getAlkuPvm(), Optional.ofNullable(parentSuhde.getLoppuPvm())), relevantParentNimet);
                })
                .collect(Collectors.toList());
    }

    private List<OrganisaatioSuhde> sanitizeParentSuhteet(List<OrganisaatioSuhde> parentSuhteet, Organisaatio org) {
        List<OrganisaatioSuhde> sanitizedParentSuhteet = parentSuhteet.stream()
                .sorted(Comparator.comparing(OrganisaatioSuhde::getAlkuPvm))
                .collect(Collectors.toList());
        sanitizedParentSuhteet.get(0).setAlkuPvm(org.getAlkuPvm());
        return sanitizedParentSuhteet;
    }

    private static List<OrganisaatioNimiDTO> getRelevantParentNimet(Organisaatio org, List<OrganisaatioNimiDTO> parentNimet) {
        List<OrganisaatioNimiDTO> relevantParentNimet = parentNimet.stream().filter(parentNimi -> !parentNimi.getAlkuPvm().before(org.getAlkuPvm())).collect(Collectors.toList());
        return relevantParentNimet.isEmpty() ? List.of(parentNimet.get(parentNimet.size() - 1)) : relevantParentNimet;
    }

    List<OrganisaatioNimiDTO> decoreateToimipisteNimet(List<OrganisaatioNimiDTO> toimipisteNimet, List<Map.Entry<Map.Entry<Date, Optional<Date>>, List<OrganisaatioNimiDTO>>> oppilaitosHistoryNimet) {
        List<OrganisaatioNimiDTO> result = new ArrayList<>();
        List<OrganisaatioNimiDTO> oppilaitosNimet = evaluateParentNameHistory(oppilaitosHistoryNimet);
        IntStream.range(0, toimipisteNimet.size()).forEach(toimipisteIndex -> {
            OrganisaatioNimiDTO toimipiste1 = toimipisteNimet.get(toimipisteIndex);
            Optional<OrganisaatioNimiDTO> toimipiste2 = toimipisteIndex + 1 < toimipisteNimet.size() ? Optional.of(toimipisteNimet.get(toimipisteIndex + 1)) : Optional.empty();
            IntStream.range(0, oppilaitosNimet.size()).forEach(oppilaitosIndex -> {
                        OrganisaatioNimiDTO oppilaitos1 = oppilaitosNimet.get(oppilaitosIndex);
                        Optional<OrganisaatioNimiDTO> oppilaitos2 = oppilaitosIndex + 1 < oppilaitosNimet.size() ? Optional.of(oppilaitosNimet.get(oppilaitosIndex + 1)) : Optional.empty();
                        boolean firstToimipiste = toimipisteIndex == 0;
                        boolean lastToimipiste = toimipiste2.isEmpty();
                        boolean firtsOppilaitos = oppilaitosIndex == 0;
                        boolean lastOppilaitos = oppilaitos2.isEmpty();
                        boolean toimipisteInOppilaitosRange = (firtsOppilaitos || toimipiste1.getAlkuPvm().compareTo(oppilaitos1.getAlkuPvm()) >= 0) && (lastOppilaitos || toimipiste1.getAlkuPvm().compareTo(oppilaitos2.get().getAlkuPvm()) < 0);
                        boolean oppilaitosWithinToimipisteet = (firstToimipiste || oppilaitos1.getAlkuPvm().compareTo(toimipiste1.getAlkuPvm()) >= 0) && (lastToimipiste || oppilaitos1.getAlkuPvm().compareTo(toimipiste2.get().getAlkuPvm()) < 0);
                        if (toimipisteInOppilaitosRange) {
                            result.add(oppilaitosToimipisteNimi(toimipiste1, oppilaitos1, toimipiste1.getAlkuPvm()));
                        } else if (oppilaitosWithinToimipisteet)
                            result.add(oppilaitosToimipisteNimi(toimipiste1, oppilaitos1, oppilaitos1.getAlkuPvm()));
                    }
            );
        });
        result.sort(Comparator.comparing(OrganisaatioNimiDTO::getAlkuPvm));
        return result;
    }

    List<OrganisaatioNimiDTO> evaluateParentNameHistory(List<Map.Entry<Map.Entry<Date, Optional<Date>>, List<OrganisaatioNimiDTO>>> oppilaitosHistoryNimet) {
        List<OrganisaatioNimiDTO> result = new ArrayList<>();
        IntStream.range(0, oppilaitosHistoryNimet.size()).forEach(index -> {
            Map.Entry<Map.Entry<Date, Optional<Date>>, List<OrganisaatioNimiDTO>> currentOppilaitosNimet = oppilaitosHistoryNimet.get(index);
            Date startOfParentRange = currentOppilaitosNimet.getKey().getKey();
            Optional<Date> endOfParentRange = currentOppilaitosNimet.getKey().getValue();
            List<OrganisaatioNimiDTO> currentNames = new ArrayList<>();
            IntStream.range(0, currentOppilaitosNimet.getValue().size()).forEach(index2 -> {
                OrganisaatioNimiDTO currentOppilaitosNimi = currentOppilaitosNimet.getValue().get(index2);
                boolean lastOppilaitosNimi = (index == oppilaitosHistoryNimet.size() - 1 && index2 == currentOppilaitosNimet.getValue().size() - 1);
                boolean nimiInRange = (index == 0 || lastOppilaitosNimi || currentOppilaitosNimi.getAlkuPvm().compareTo(startOfParentRange) >= 0) && (endOfParentRange.isEmpty() || currentOppilaitosNimi.getAlkuPvm().compareTo(endOfParentRange.get()) < 0);
                if (nimiInRange) {
                    currentNames.addAll(getNames(index, index2, currentOppilaitosNimet, startOfParentRange, currentOppilaitosNimi, currentNames.isEmpty()));
                }
            });
            result.addAll(currentNames);
        });
        result.sort(Comparator.comparing(OrganisaatioNimiDTO::getAlkuPvm));
        return result;
    }

    private List<OrganisaatioNimiDTO> getNames(int index, int index2, Map.Entry<Map.Entry<Date, Optional<Date>>, List<OrganisaatioNimiDTO>> currentOppilaitosNimet, Date startOfParentRange, OrganisaatioNimiDTO currentOppilaitosNimi, boolean evaluatingFirstRange) {
        List<OrganisaatioNimiDTO> currentNames = new ArrayList<>();
        if (index > 0 && index2 > 0 && evaluatingFirstRange && currentOppilaitosNimi.getAlkuPvm().compareTo(startOfParentRange) > 0) {
            //add previous name with start from start of range
            currentNames.add(copyNimi(currentOppilaitosNimet.getValue().get(index2 - 1), startOfParentRange));
        }
        if (currentOppilaitosNimi.getAlkuPvm().compareTo(startOfParentRange) < 0) {
            // if oppilaitos is from before the start of the range, the startdate should still be according to the range
            currentNames.add(copyNimi(currentOppilaitosNimi, startOfParentRange));
        } else {
            currentNames.add(currentOppilaitosNimi);
        }
        return currentNames;
    }

    private OrganisaatioNimiDTO copyNimi(OrganisaatioNimiDTO toimipiste, Date alkuPvm) {
        OrganisaatioNimiDTO toimipisteNimi = new OrganisaatioNimiDTO();
        Map<String, String> thisNimi = toimipiste.getNimi().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        toimipisteNimi.setVersion(toimipiste.getVersion());
        toimipisteNimi.setAlkuPvm(alkuPvm);
        toimipisteNimi.setNimi(thisNimi);
        return toimipisteNimi;
    }

    OrganisaatioNimiDTO oppilaitosToimipisteNimi(OrganisaatioNimiDTO toimipiste, OrganisaatioNimiDTO oppilaitosNimi, Date alkuPvm) {
        OrganisaatioNimiDTO toimipisteNimi = copyNimi(toimipiste, alkuPvm);
        Map<String, String> nimi = toimipisteNimi.getNimi();
        nimi.keySet().forEach(kieli -> nimi.put(kieli,
                generateToimipisteNimi(oppilaitosNimi, nimi, kieli)));
        return toimipisteNimi;
    }

    private String generateToimipisteNimi(OrganisaatioNimiDTO oppilaitosNimi, Map<String, String> thisNimi, String kieli) {
        String nimiString = thisNimi.get(kieli);
        String oppilaitosNimiString = oppilaitosNimi.getNimi().get(kieli) != null ? oppilaitosNimi.getNimi().get(kieli) : oppilaitosNimi.getNimi().get("fi");
        if (nimiString.equals(oppilaitosNimiString))
            return nimiString;
        else
            return String.format("%s, %s", oppilaitosNimiString, nimiString);
    }
}
