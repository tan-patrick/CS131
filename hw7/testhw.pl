consult("hw7.pl").
findall(Y, duplist([1,2,3], Y), L), L = [[1,1,2,2,3,3]].
findall(Y, duplist(Y, [1,1,5,5,2,2,7,7]), L), L = [[1,5,2,7]].
findall(D, put(1,hello,[],D), L), L = [[[1,hello]]].
findall(D, put(1,hello,[[2,two]],D), L), L = [[[2,two],[1,hello]]].
findall(D, put(1,hello,D,[[2,two],[1,hello]]), L), L = [[[2,two]],[[2,two],[1,_]]].
findall(V, get(1,[[2,two],[1,hello]],V), L), L = [hello].
findall(K, get(K,[[2,hello],[1,hello]],hello), L), L = [2,1].
findall([K,V], get(K,[[2,two],[1,hello]],V), L), L = [[2,two],[1,hello]].

put(1, hello, [[2, two],[1,fire]], D). D = [[2,two],[1,hello]] ? ;
findall(D, put(1,hello,[[1,one],[2,two]],D), L), L = [[[1,hello],[2,two]]].

findall(D, put(1,hello,D,[[2,two],[1,hello]]), L), L = [[[2,two]],[[2,two],[1,_]]].