# rekisterointi

`rekisterointi` on web-käyttöliittymä organisaatioiden rekisteröitymiseen. Sovelluksella on pieni backend tunnistautumisen ohjaamiseen (`cas-oppija`). Lisäksi se tarjoilee javascriptin ja yhdistyy `lokalisaatio`- yms. Opintopolku-palveluihin. Rekisteröinnit talletetaan `varda-rekisterointi`-sovellukseen.

## Paikallinen kehitys

Paikallisesti sovellus käynnistetään scriptillä:

```
./start-local-env.sh
```

Scriptin pitäisi kertoa, jos jotain puuttuu.

Paikallisessa kehityksessä `cas-oppija` ohitetaan ja sisään kirjaudutaan basic authilla (dev/dev). Organisaatiovaltuudet ovat asetettu staattisesti ja `varda-rekisterointi`-palvelun API on mockattu yhteen tmuxin ikkunoista.

## Testaus

Sovelluksen testit on toteutettu Playwrightillä hakemistoon `rekisterointi-ui/playwright`. Sovelluksen ollessa käynnissä ajo tapahtuu seuraavalla komennolla:

```
npm run playwright:test
```
