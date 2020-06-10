<#macro sisalto otsikko>
<!doctype html>
<html lang="sv">
<head>
    <meta charset="utf-8">
    <title>${otsikko}</title>
    <style>
        body {
            background: #F6F4F0;
        }
        .box {
            background: #FFFFFF;
            padding: 1em 2em;
            margin: 2em 4em;
        }
    </style>
</head>
<body>
<div class="box">
    <#nested/>
</div>
<div class="box" style="text-align: right;">
    <img src="http://www.oph.fi/instancedata/prime_product_julkaisu/oph/pics/opetushallitus2.gif" alt="Utbildningsstyrelsen" />
</div>
</body>
</html>
</#macro>
