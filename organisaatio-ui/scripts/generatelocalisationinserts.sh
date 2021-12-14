#!/bin/bash
oid=1.2.246.562.24.39768521067
outputfile=../build/lokalisaatio.sql
#echo "delete from localisation where xcategory ='organisaatio2';" > $outputfile
echo ""> $outputfile
for lang in 'fi' 'sv' 'en'
do
  upper=$(echo $lang | tr a-z A-Z)
  echo "generating inserts for $oid in language $upper/$lang"
  grep "translate('.*')" --exclude=*.test.ts -ohR ../src/ | sed "s/translate('\(.*\)'.*/\ insert into localisation (id,version,accessed,created,modified,createdby,modifiedby,xcategory,xkey,xvalue,xlanguage)values(nextval('hibernate_sequence') ,0,now(),now(),now(),'$oid','$oid','organisaatio2','\1','\1_$upper','$lang'); /" | sort | uniq >> $outputfile
done