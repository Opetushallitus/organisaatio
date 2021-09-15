#!/bin/bash
ymp='untuvaopintopolku'
folder='../src/api'
for koodisto in 'kayttoryhmat' 'kunta' 'maatjavaltiot1' 'oppilaitoksenopetuskieli' 'organisaatiotyyppi' 'posti' 'ryhmantila' 'ryhmatyypit' 'kieli'
do
  url="https://virkailija.$ymp.fi/koodisto-service/rest/json/$koodisto/koodi?onlyValidKoodis=true"
  echo fetching $koodisto from $url
  rm -rf $folder/koodisto-service/rest/json/$koodisto
  mkdir -p $folder/koodisto-service/rest/json/$koodisto/koodi
  curl -X GET $url -H "accept: application/json;charset=UTF-8" -H "Caller-Id: local-dev" | python -m json.tool > $folder/koodisto-service/rest/json/$koodisto/koodi/GET.json
done


#for endpoint in 'opetuspisteet_0128130' 'koulutustoimija_25658335' 'koulutustoimija_09961676'
#do
#  url="https://virkailija.$ymp.fi/koodisto-service/rest/codeelement/$endpoint/1"
#  echo fetching $endpoint from $url
#  rm -rf koodisto-service/rest/codeelement/$endpoint
#  mkdir -p koodisto-service/rest/codeelement/$endpoint/1
#  curl -X GET $url -H "accept: application/json;charset=UTF-8" -H "Caller-Id: local-dev" > koodisto-service/rest/codeelement/$endpoint/1/GET.json
#  curl -X GET $url -H "accept: application/json;charset=UTF-8" -H "Caller-Id: local-dev" > koodisto-service/rest/codeelement/$endpoint/POST.json
#done

url="https://virkailija.$ymp.fi/lokalisointi/cxf/rest/v1/localisation?category=organisaatio2"
curl -X GET $url -H "accept: application/json;charset=UTF-8" -H "Caller-Id: local-dev" | python -m json.tool > $folder/lokalisointi/cxf/rest/v1/localisation/GET.json
