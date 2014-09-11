/*global $:false */
/*jshint devel:true */
$(function () {
    "use strict";
    var MAX_ORGS = 20;

    var logSelector = '#vaihe1';
    var logAppend = true;

    var rawLog = function (data) {
        var logElement = $(logSelector);
        if (logAppend) {
            logElement.append(data);
        } else {
            logElement.html(data);
        }
    };

    var log = function(message) {
        rawLog('<span class="log message">' + new Date() + ':' + message + '</span>');
    };
    var warn = function(message) {
        rawLog('<span class="warn message">' + new Date() +':'+ message + '</span>');
    };

    var orgsHash = {};

    var go = function () {
        $('#go').hide();
        $.ajax('/organisaatio-service/rest/organisaatio/').then(function (data) {
            log('organisaatiot haettu');

            var allOrganisations = [];

            var currentOrganisations = [];

            var allSolrOrganisations = {};

            $.each(data, function (index, entry) {
                currentOrganisations.push(entry);
                orgsHash[entry] = true;

                if (currentOrganisations.length >= MAX_ORGS) {
                    allOrganisations.push(currentOrganisations);
                    currentOrganisations = [];
                }
            });
            allOrganisations.push(currentOrganisations);
            log(Object.keys(orgsHash).length + ' organisaatiota käsitelty');

            logSelector='#solr-valmistui';
            logAppend = false;

            var checkOrganisations = function () {
                var notFoundOids = [];

                $.each(orgsHash, function (index, oid) {
                    if (!allSolrOrganisations[index]) {
                        notFoundOids.push(index);
                    }
                });
                logSelector='#tulos';
                logAppend = true;

                if(notFoundOids.length) {
                    warn('Seuraavia oideja ei löydy Solrista:<br>' + notFoundOids.join('<br>'));
                } else {
                    log('Kaikki oidit löytyivät Solrista');
                }
            };

            var query = function () {
                var orgs = allOrganisations.shift();

                if (!orgs) {
                    checkOrganisations();
                    return;
                }

                $.ajax('/organisaatio-service/rest/organisaatio/v2/hae?aktiiviset=true&lakkautetut=true&suunnitellut=true&skipParents=true&oidRestrictionList="' +
                    orgs.join('","') +
                    '"').then(function (data, textStatus, jqXHR ) {
                    $.each(data.organisaatiot, function (index, org) {
                        allSolrOrganisations[org.oid] = true;
                    });

                    logAppend = false;
                    log(Object.keys(allSolrOrganisations).length + ' Solr-organisaatiota vastaanotettu');
                    logAppend = true;
                    query();
                });
                log('kysely lähetetty, ' + allOrganisations.length + ' jäljellä');
            };
            query();
     });
        log('organisaatio-kysely lähetetty');
    };

    $('#go').on('click', go);
});