/*global $:false */
/*jshint devel:true */
$(function () {
    "use strict";
    var MAX_ORGS = 20;

    var ROOT_URL = '/';

    var proceed = false;

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
    var error = function(message) {
        rawLog('<span class="error message">' + new Date() +':'+ message + '</span>');
    };

    var showGo = function () {
        $('#go').show();
        $('#stop').hide();
    };

    var errorHandler = function (jxhr, code, reason) {
        logSelector = '#error';
        logAppend = true;
        error([JSON.stringify(jxhr), code, reason].join(','));
        showGo();
    };

    var orgsHash = {};

    var getMissingOrgData = function (oids) {
        var query = function () {
            var oid = oids.shift();

            if (!proceed || !oid) {
                logSelector = '#puuttuvat-edistys';
                logAppend = false;
                log('');
                showGo();
                return;
            }

            $.ajax(ROOT_URL + 'organisaatio-service/rest/organisaatio/' + oid)
                .then(function (org, textStatus, jqXHR) {
                    logSelector = '#puuttuvat';
                    logAppend = true;

                    var oid = org.oid;
                    var parentOid = org.parentOid;
                    var tyypit = org.tyypit.join(',');
                    var nimet = $.map(org.nimi, function (propertyOfObject, key) {
                        return key + ': ' + propertyOfObject;
                    });
                    var alkuPvm = org.alkuPvm;
                    var lakkautusPvm = org.lakkautusPvm;
                    warn('<br><strong>oid:</strong>' + oid + '<br>' +
                        '<strong>parentOid:</strong>' + parentOid + '<br>' +
                        '<strong>tyypit:</strong>' + tyypit + '<br>' +
                        '<strong>nimet:</strong>' + nimet + '<br>' +
                        '<strong>alkuPvm:</strong>' + alkuPvm + '<br>' +
                        '<strong>lakkautusPvm:</strong>' + lakkautusPvm);
                    query();
                }, errorHandler);

            logSelector = '#puuttuvat-edistys';
            logAppend = false;
            log('puuttuvan organisaation tietojen kysely lähetetty, ' + oids.length + ' jäljellä');
        };
        query();
    };

    var go = function () {
        $('#go').hide();
        $('#stop').show();
        $('.output').empty();
        ROOT_URL = $('#root-url').val();
        logSelector = '#vaihe1';
        logAppend = true;
        proceed = true;

        $.ajax(ROOT_URL + 'organisaatio-service/rest/organisaatio/').then(function (data) {
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
                delete orgsHash['1.2.246.562.10.00000000001']; // root oid

                $.each(orgsHash, function (index, oid) {
                    if (!allSolrOrganisations[index]) {
                        notFoundOids.push(index);
                    }
                });
                logSelector='#tulos';
                logAppend = true;

                if(notFoundOids.length) {
                    warn('Seuraavia oideja ei löydy Solrista:' + notFoundOids.join('<br>'));
                    getMissingOrgData(notFoundOids);
                } else {
                    log('Kaikki oidit löytyivät Solrista');
                    showGo();
                }
            };

            var query = function () {
                var orgs = allOrganisations.shift();

                if (!proceed) {
                    showGo();
                    return;
                }
                if (!orgs) {
                    checkOrganisations();
                    return;
                }

                $.ajax(ROOT_URL + 'organisaatio-service/rest/organisaatio/v2/hae?aktiiviset=true&lakkautetut=true&suunnitellut=true&skipParents=true&oidRestrictionList="' +
                    orgs.join('","') +
                    '"').then(function (data, textStatus, jqXHR ) {
                    $.each(data.organisaatiot, function (index, org) {
                        allSolrOrganisations[org.oid] = true;
                    });

                    logAppend = false;
                    log(Object.keys(allSolrOrganisations).length + ' Solr-organisaatiota vastaanotettu');
                    logAppend = true;
                    query();
                }, errorHandler);
                log('kysely lähetetty, ' + allOrganisations.length + ' jäljellä');
            };
            query();
        }, errorHandler);
        log('organisaatio-kysely lähetetty');
    };

    $('#go').on('click', go);
    $('#stop').on('click', function () {
        proceed = false;
    });
});
