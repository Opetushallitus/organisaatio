<#import "pohja_fi.ftlh" as pohja>

<@pohja.sisalto otsikko>
    <p>
        YTJ-Tietojen haku ${time} ${status}
    </p>
    <#if virheet??>
    <p>
        <#list virheet as virhe>
        <div>
            <a href="https://${organisaatioUrl}/${virhe.oid}">${virhe.nimi}</a>
            <br />
            <ul>
                <#list virhe.viestit as viesti>
                <li>${viesti}</li>
                </#list>
            </ul>
            <br />
        </div>
        </#list>
    </p>
    </#if>
    <p>
        <a href="https://${ilmoitusUrl}">YTJ-päivitykset</a>
    </p>
</@pohja.sisalto>
