#!/usr/bin/env python

from operator import itemgetter
from urllib import urlopen
import json


# Get the all of the localizations for organisaatio
url = urlopen('https://itest-virkailija.oph.ware.fi/lokalisointi/cxf/rest/v1/localisation?category=organisaatio')
locals = json.loads(url.read())

open('missing_locals.txt', 'w').close()
with open("missing_locals.txt", "a") as missing:
    
    missing.write("Incomplete localization found for these: \n")
       
    ind = 0
    count =0
    #matches = 0
   
    #Sort the JSON elements by "key"-element 1st so we can compare 2 consecutive elements (indices 0 and 1)
    
    locals = sorted(locals,key=itemgetter('key'))
      
    for i in locals:
        count = count+1
    
    while ind < count:
        #Get the 1st element
        a = locals[0]
        b = locals[1]
        
        if (a.get('key') == b.get('key')) :
            #print "match"
            # remove the 2 matching elements from comparison, aka. "pop-pop"
            locals.pop(0)
            locals.pop(0)
            ind = ind + 2
            #matches = matches + 1
            #print matches
        else:
            #print "no match"
            #print ind
            # No match found, save the JSON-element to file and pop it out from the 'locals'-list
            missing.write(json.dumps(a, sort_keys=False, indent=4))
            locals.pop(0)
            ind = ind + 1
            
            
            
    
   
