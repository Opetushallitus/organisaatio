# Organisaatio UI:n lokaalidevaus

Perustuu nginx välityspalvelimeen. Ajatus on että osa resursseista haetaan
lokaalista tiedostojärjestelmästä ja loput halutusta pilviympäristöstä.

## Vaatimukset

* docker
* docker compose

## Resurssit

Välityspalvelinta ajetaan portissa **8080**

## Ohjeet

1. Käynnistä välityspalvelin: `docker compose up`
2. Autentikoidu navigoimalle selaimella: http://localhost:8080/cas -> pitäisi päätyä virkailijan työpöydälle.
3. Navigoi organisaatiopalveluun joko menun kautta tai suoraan: http://localhost:8080/organisaatio-ui

## Ongelmatilanteet

### Haluan käyttää eri kehitysympäristöä

1. Muokkaa [nginx.conf](nginx.conf) tiedostoa ja vaihda kehitysympäristö haluttuun.
2. Tyhjennä evästeet
3. Katso ohjeet yllä

### Muokkasin lähdekoodia mutten näe muutoksia

Muutokset eivät päivity automaattisesti vaan manuaalinen sivunlataus vaaditaan.

