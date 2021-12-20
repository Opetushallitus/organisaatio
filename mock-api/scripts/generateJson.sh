#!/bin/bash
ymp='untuvaopintopolku'
folder='../src/api'
for koodisto in 'kayttoryhmat' 'kunta' 'maatjavaltiot1' 'oppilaitoksenopetuskieli' 'organisaatiotyyppi' 'posti' 'ryhmantila' 'ryhmatyypit' 'kieli' 'vuosiluokat' 'oppilaitostyyppi' 'varhaiskasvatuksenToimipaikkaTiedot' 'vardatoimintamuoto' 'vardakasvatusopillinenjarjestelma' 'vardatoiminnallinenpainotus' 'vardajarjestamismuoto'
do
  url="https://virkailija.$ymp.fi/koodisto-service/rest/json/$koodisto/koodi?onlyValidKoodis=true"
  echo fetching $koodisto from $url
  rm -rf $folder/koodisto-service/rest/json/$koodisto
  mkdir -p $folder/koodisto-service/rest/json/$koodisto/koodi
  curl -X GET $url -H "accept: application/json;charset=UTF-8" -H "Caller-Id: local-dev" | python -m json.tool > $folder/koodisto-service/rest/json/$koodisto/koodi/GET.json
done


url="https://virkailija.$ymp.fi/lokalisointi/cxf/rest/v1/localisation?category=organisaatio2"
curl -X GET $url -H "accept: application/json;charset=UTF-8" -H "Caller-Id: local-dev" | python -m json.tool > $folder/lokalisointi/cxf/rest/v1/localisation/GET.json
