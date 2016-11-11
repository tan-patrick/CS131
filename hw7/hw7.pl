/* Name: Patrick Tan

   UID: 204158646

   Others With Whom I Discussed Things:

   Other Resources I Consulted: CS 131 Piazza
   
*/

/*
1. Duplist

Base case: empty list -> empty list
Otherwise, first element of list 1 is the same as the first 2 elements of list 2, then run on rest of list 1 and list2

*/

duplist([],[]).
duplist([H|T],[H1,H2|T1]):- H = H1, H = H2, duplist(T, T1).

/*
2. Dictionary
*/

/* Put checks if D1 is empty (base case). If so, adds new key value pair to end of dictionary.
Otherwise, checks if K = K1. If it does, replace the old key value pair. Otherwise, checks the next key value in current dictionary.*/

put(K,V,[],D2) :- D2 = [[K,V]].
put(K,V,[[K1, _] | Rest], D2) :- K = K1, D2 = [[K, V] | Rest].
put(K,V,[[K1, V1] | Rest], D2) :- D2 = [[K1, V1] | CheckRest], K \= K1, put(K, V, Rest, CheckRest).

/* Checks if current key is equal to current key of dictionary. Value to return is set to value in key-value pair.
If not equal, then check the next key-value pair in dictionary. */

get(K,[[K1, V1] | _],V) :- K = K1, V = V1.
get(K,[[_, _] | Rest],V) :- get(K, Rest, V).

/*
3. Interpreter
*/

/*intconst -> intval and boolconst -> boolval */
eval(intconst(I), _, intval(I)).
eval(boolconst(B), _, boolval(B)).
/*Variables are in the environment. Check using member (i.e. member(X, [1,2,3,4,5,6,7,8]) from NQueens)*/
eval(var(X), Env, Val):- member([var(X), Val], Env).
/*Evaluate E1 and E2 to intvals, then compare them. Return boolval of true or false*/
eval(geq(E1,E2), Env, boolval(true)):- eval(E1, Env, intval(I1)), eval(E2, Env, intval(I2)), I1 >= I2.
eval(geq(E1,E2), Env, boolval(false)):- eval(E1, Env, intval(I1)), eval(E2, Env, intval(I2)), I1 < I2.
/*Evaluate E1 to a boolval. If true, return the result as eval of E2. If false, eval E3. */
eval(if(E1,E2,_), Env, Result):- eval(E1, Env, boolval(true)), eval(E2, Env, Result).
eval(if(E1,_,E3), Env, Result):- eval(E1, Env, boolval(false)), eval(E3, Env, Result).
/*Function maps to funval (Similar to intconst and boolconst). Set the Function's environment to the current environment. */
eval(function(X,E), Env, funval(X,E,Env)).
/*Eval funcall to funval. Evaluate E2 to V2 with the current environment. Add this mapping to FunEnv (environment of the function when it was created). Then, evaluate the function with new environment.*/
eval(funcall(E1, E2), Env, V):- eval(E1, Env, funval(X,E,FunEnv)), eval(E2, Env, V2), eval(E, [[var(X), V2] | FunEnv], V).

/*
4. Blocks world
*/

/*moveblock for pickup checks if the stack to be picked up is empty and that the robot is not holding anything. If checks pass, remove top element from stack and put it in robot hand */
moveblock(world([H|T], Stack2, Stack3, none), pickup(stack1), world(T, Stack2, Stack3, H)).
moveblock(world(Stack1, [H|T], Stack3, none), pickup(stack2), world(Stack1, T, Stack3, H)).
moveblock(world(Stack1, Stack2, [H|T], none), pickup(stack3), world(Stack1, Stack2, T, H)).
/*moveblock for putdown checks that the robot has a block in hand. If so, then it prepends it the the stack given. (It puts it on top of the stack) */
moveblock(world(Stack1, Stack2, Stack3, Block), putdown(stack1), world([Block | Stack1], Stack2, Stack3, none)).
moveblock(world(Stack1, Stack2, Stack3, Block), putdown(stack2), world(Stack1, [Block | Stack2], Stack3, none)).
moveblock(world(Stack1, Stack2, Stack3, Block), putdown(stack3), world(Stack1, Stack2, [Block | Stack3], none)).

/*Base case, the start state is equal to the end state. */
blocksworld(Start, [], Start).
/* Otherwise, try a possible move and call blocksworld on the new state. Need to put length(Actions, L) before, otherwise goes into infinite loop (since it is DFS)*/
blocksworld(Start, [A|As], End):- moveblock(Start,A,Mid), blocksworld(Mid,As,End).