# batchmoveorganizations

Skripti child-organisaatioiden massasiirtoihin parentista toiseen

## Asennus

`npm install`

## Käyttö

 * Aseta ympäristömuuttuja(t)
   * DEV_HOST
   * QA_HOST
   * PROD_HOST
 * Määrittele ehdot `./conditions.js` -tiedostoon
   * Ehtojen perusteella valitaan ne child-organisaatioot, jotka halutaan 
     siirtää
   * Tyhjä ehtolista valitsee kaikki child-organisaatiot
 * Aja `node move` nähdäksesi komennon käyttö
