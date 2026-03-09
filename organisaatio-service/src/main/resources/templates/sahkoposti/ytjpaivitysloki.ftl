<#import "pohja_fi.ftlh" as pohja>

<@pohja.sisalto otsikko>
    <h1>${otsikko}</h1>
    <p>
        YTJ-päivityksessä (${time}) löydettiin ${virheet?size} lopetettua organisaatiota:<br/>
        <#list virheet as virhe>
            - <a href="https://${organisaatioUrl}/${virhe.oid}">${virhe.orgNimi}</a><br />
        </#list>
    </p>
    <p>
        <a href="https://${ilmoitusUrl}">YTJ-päivitykset</a>
    </p>
</@pohja.sisalto>
