(* Name: Patrick Tan

   UID: 204 158 646

   Others With Whom I Discussed Things:

   Other Resources I Consulted: www.piazza.com (CS 131 Q & A)
   
*)

(* EXCEPTIONS *)

(* This is a marker for places in the code that you have to fill in.
   Your completed assignment should never raise this exception. *)
exception ImplementMe of string

(* This exception is thrown when a type error occurs during evaluation
   (e.g., attempting to invoke something that's not a function).
*)
exception DynamicTypeError

(* This exception is thrown when pattern matching fails during evaluation. *)  
exception MatchFailure  

(* This exception is used for nested pattern matches*)
exception NestedFailure

(* EVALUATION *)

(* See if a value matches a given pattern.  If there is a match, return
   an environment for any name bindings in the pattern.  If there is not
   a match, raise the MatchFailure exception.

   type mopat =
    IntPat of int
  | BoolPat of bool
  | WildcardPat
  | VarPat of string
  | NilPat
  | ConsPat of mopat * mopat

type molistval =
    NilVal
  | ConsVal of movalue * molistval
and movalue =
    IntVal of int
  | BoolVal of bool
      (* A function value carries its lexical environment with it! *)
      (* If the function is recursive it also carries around its own name. *)
  | FunctionVal of string option * mopat * moexpr * moenv
  | ListVal of molistval
*)
let rec patMatch (pat:mopat) (value:movalue) : moenv =
  match (pat, value) with
      (* an integer pattern matches an integer only when they are the same constant;
	 no variables are declared in the pattern so the returned environment is empty *)
      (IntPat(i), IntVal(j)) when i=j -> Env.empty_env()
    | (BoolPat(b), BoolVal(j)) when b=j -> Env.empty_env()
    | (WildcardPat, _) -> Env.empty_env()
    | (VarPat(s), _) -> Env.add_binding s value (Env.empty_env())
    | (NilPat, ListVal(NilVal)) -> Env.empty_env()
    | (ConsPat(pat1, pat2), ListVal(ConsVal(first, rest))) -> (Env.combine_envs (patMatch pat1 first) (patMatch pat2 (ListVal(rest)))) (* We combine the first element and "recursively" call a pattern match on the second element (the list) *)
    | _ -> raise MatchFailure

    
(* Evaluate an expression in the given environment and return the
   associated value.  Raise a MatchFailure if pattern matching fails.
   Raise a DynamicTypeError if any other kind of error occurs (e.g.,
   trying to add a boolean to an integer) which prevents evaluation
   from continuing.
*)
(*
  | BoolConst of bool
  | Nil
  | Var of string
  | BinOp of moexpr * moop * moexpr
  | Negate of moexpr 
  | If of moexpr * moexpr * moexpr
  | Function of mopat * moexpr
  | FunctionCall of moexpr * moexpr
  | Match of moexpr * (mopat * moexpr) list ;;

type molistval =
    NilVal
  | ConsVal of movalue * molistval
and movalue =
    IntVal of int
  | BoolVal of bool
      (* A function value carries its lexical environment with it! *)
      (* If the function is recursive it also carries around its own name. *)
  | FunctionVal of string option * mopat * moexpr * moenv
  | ListVal of molistval   

  type moop = Plus | Minus | Times | Eq | Gt | Cons
*)

(* Used to deal with nested matches. We return a different failure to signify that we should go to the next tuple in list. *)
let getMatch (test:mopat) (matched:movalue) : moenv =
  (try patMatch test matched with
  MatchFailure -> raise NestedFailure)

let rec evalExpr (e:moexpr) (env:moenv) : movalue =
  match e with
      (* an integer constant evaluates to itself *)
      IntConst(i) -> IntVal(i)
    | BoolConst(b) -> BoolVal(b)
    | Nil -> ListVal (NilVal)
    | Var(s) -> (try 
                  Env.lookup s env with
                    Env.NotBound -> raise DynamicTypeError)
    | BinOp (exp1, oper, exp2) -> (* Check which of the binary operations is matches with and use that one*)
        let res1 = evalExpr exp1 env in
          let res2 = evalExpr exp2 env in
            (match (res1, oper, res2) with
              (IntVal(val1), Plus, IntVal(val2)) -> IntVal (val1 + val2)
             |(IntVal(val1), Minus, IntVal(val2)) -> IntVal (val1 - val2)
             |(IntVal(val1), Times, IntVal(val2)) -> IntVal (val1 * val2)
             |(IntVal(val1), Eq, IntVal(val2)) -> BoolVal (val1 = val2)
             |(IntVal(val1), Gt, IntVal(val2)) -> BoolVal (val1 > val2)
             |(val1, Cons, ListVal(val2)) -> ListVal(ConsVal(val1, val2))
             |_ -> raise DynamicTypeError)
    | Negate (exp) ->
        let res = evalExpr exp env in
          (match res with
            IntVal(result) -> IntVal (result * -1)
           |_ -> raise DynamicTypeError)
    | If(exp1, exp2, exp3) -> 
        let test = evalExpr exp1 env in
          (match test with
            BoolVal(true) -> evalExpr exp2 env
           |BoolVal(false) -> evalExpr exp3 env
           |_ -> raise DynamicTypeError)
    | Function (pattern, expression) -> FunctionVal(None, pattern, expression, env)
    | FunctionCall (useFunc, argument) -> (* first exp is function, second are parameters *)
        let func = evalExpr useFunc env in
          let argu = evalExpr argument env in
            (match func with
              FunctionVal(s, args, expr, envir) -> 
                (match s with 
                  None -> evalExpr expr (Env.combine_envs envir (patMatch args argu))
                 |Some(funcName) -> evalExpr expr (Env.combine_envs (Env.add_binding funcName func (envir)) (patMatch args argu))) 
                (* shadow the function name with the current environment. Then, shadow that with the arguments. 
                It needs to be in this order in case the function name and an argument name are equivalent. *)
             |_ -> raise DynamicTypeError)
    | Match (matchExpr, matchList) -> 
        let matchWith = evalExpr matchExpr env in
          (match matchList with
            [] -> raise MatchFailure
           |(testMatch, expr)::rest -> 
              (try
                (let matched = getMatch testMatch matchWith in 
                  (* We run a helper function that returns Nested Failure if there is no match. 
                  We do this in order to do pattern matching for nested pattern matches. If we did it with MatchFailure, 
                it would propagate back up and search through the higher nested match if the lower nested match didn't find anything. *)
                  evalExpr expr (Env.combine_envs env matched))
              with
                NestedFailure -> evalExpr (Match(matchExpr, rest)) env ))
    | _ -> raise (ImplementMe "expression evaluation not implemented")


(* Evaluate a declaration in the given environment.  Evaluation
   returns the name of the variable declared (if any) by the
   declaration along with the value of the declaration's expression.

   type modecl =
  | Expr of moexpr
  | Let of string * moexpr
  | LetRec of string * mopat * moexpr

  type moresult = string option * movalue
*)
let rec evalDecl (d:modecl) (env:moenv) : moresult =
  match d with
      Expr(e) -> (None, evalExpr e env)
    | Let (s, e) -> (Some s, evalExpr e env)
    | LetRec(s, pat, expr) -> (Some s, (FunctionVal(Some s, pat, expr, env))) (* Store the function name so we can later shadow the running environment when a recursive function is called. *)
    | _ -> raise (ImplementMe "let and let rec not implemented")

