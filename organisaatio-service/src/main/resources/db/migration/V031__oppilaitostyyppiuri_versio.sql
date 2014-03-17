update organisaatio set oppilaitostyyppi = concat(oppilaitostyyppi, '#1') where oppilaitostyyppi like 'oppilaitostyyppi_%' and oppilaitostyyppi not like 'oppilaitostyyppi_%#1';

