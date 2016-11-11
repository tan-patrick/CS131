/* Name: Patrick Tan

   UID: 204158646

   Others With Whom I Discussed Things:

   Other Resources I Consulted: Piazza Q&A (CS 131)
   
*/

// import lists and other data structures from the Java standard library
import java.util.*;

// a type for arithmetic expressions
interface AExp {
    double eval(); 	                       // Problem 1a
    List<AInstr> compile(); 	               // Problem 1c
}

class Num implements AExp {
    protected double val;

    Num(double x){
    	this.val = x;
    }

    public double eval(){
    	return val;
    }

    public List<AInstr> compile(){
    	List<AInstr> myList = new LinkedList<AInstr>();
    	myList.add(new Push(val));
    	return myList;
    }
}

class BinOp implements AExp {
    protected AExp left, right;
    protected Op op;

    BinOp(AExp l, Op operator, AExp r){
    	this.left = l;
    	this.op = operator;
    	this.right = r;
    }

    public double eval(){
    	//calculate left and right sides, then put together using correct operator+calculate

    	double l = left.eval();
    	double r = right.eval();
    	return op.calculate(l,r);
    }

    public List<AInstr> compile(){
    	//use addAll as an append (appends all elements in list to the new list)
    	List<AInstr> myList = new LinkedList<AInstr>();
    	myList.addAll(left.compile());
    	myList.addAll(right.compile());
    	myList.add(new Calculate(op));
    	return myList;
    }
}

// a representation of four arithmetic operators
enum Op {
    PLUS { public double calculate(double a1, double a2) { return a1 + a2; } },
    MINUS { public double calculate(double a1, double a2) { return a1 - a2; } },
    TIMES { public double calculate(double a1, double a2) { return a1 * a2; } },
    DIVIDE { public double calculate(double a1, double a2) { return a1 / a2; } };

    abstract double calculate(double a1, double a2);
}

// a type for arithmetic instructions
interface AInstr {
	void eval(Stack<Double> myArr);
}

class Push implements AInstr {
    protected double val;

    Push(double x){
    	this.val = x;
    }

    public void eval(Stack<Double> myArr){
    	myArr.push(val);
    }

    public String toString(){
    	return "Push " + val;
    }
}

class Calculate implements AInstr {
    protected Op op;

    Calculate(Op oper){
    	this.op = oper;
    }

    public void eval(Stack<Double> myArr){
    	//top of the stack is the second number
    	double r = myArr.pop();
    	double l = myArr.pop();
    	double result = op.calculate(l,r);
    	//push back onto stack
    	myArr.push(result);
    }

    public String toString(){
    	return "Calculate " + op;
    }
}

class Instrs {
    protected List<AInstr> instrs;
    private Stack<Double> arr;

    public Instrs(List<AInstr> instrs) { 
    	this.instrs = instrs; 
    	arr = new Stack<Double>();
    }

 	// Problem 1b
    public double eval() {
    	for(AInstr n : this.instrs){ //call eval for each instruction (numbers will add to array, operators will do operation to top to on array)
    		n.eval(arr);
    	}
    	return arr.pop();
    } 
}


class CalcTest {
    public static void main(String[] args) {
	    // a test for Problem 1a
	AExp aexp =
	    new BinOp(new BinOp(new Num(1.0), Op.PLUS, new Num(2.0)),
		      Op.TIMES,
		      new Num(3.0));
	System.out.println("aexp evaluates to " + aexp.eval()); // aexp evaluates to 9.0

	// a test for Problem 1b
	List<AInstr> is = new LinkedList<AInstr>();
	is.add(new Push(1.0));
	is.add(new Push(2.0));
	is.add(new Calculate(Op.PLUS));
	is.add(new Push(3.0));
	is.add(new Calculate(Op.TIMES));
	is.add(new Push(9.0));
	is.add(new Calculate(Op.DIVIDE)); //check left -> right popping
	Instrs instrs = new Instrs(is);
	System.out.println("instrs evaluates to " + instrs.eval());  // instrs evaluates to 1.0

	// a test for Problem 1c
	System.out.println("aexp converts to " + aexp.compile());

    }
}

// a type for dictionaries mapping keys of type K to values of type V
interface Dict<K,V> {
    void put(K k, V v);
    V get(K k) throws NotFoundException;
}

class NotFoundException extends Exception {}


// Problem 2a
class DictImpl2<K,V> implements Dict<K,V> {
    protected Node<K,V> root;

    DictImpl2() { root = new Empty<K,V>(); }

    public void put(K k, V v) { root = root.put(k,v); }

    public V get(K k) throws NotFoundException { 
    	try{ 
    		return root.get(k); 
    	}
    	catch(Exception e){
    		throw new NotFoundException();
    	}
    }
}

interface Node<K,V> {
	public Node<K,V> put(K k, V v);
	public V get(K k) throws NotFoundException;
}

class Empty<K,V> implements Node<K,V> {
    Empty() {}
    public Node<K,V> put(K k, V v){
    	return new Entry<K,V>(k, v, this); //creates an Entry with just one key-value pair and an empty node at the end
    }

    public V get(K k) throws NotFoundException{
    	//empty dictionary so it can't find anything
    	throw new NotFoundException();
    }
}

class Entry<K,V> implements Node<K,V> {
    protected K k;
    protected V v;
    protected Node<K,V> next;

    Entry(K k, V v, Node<K,V> next) {
	this.k = k;
	this.v = v;
	this.next = next;
    }

    public Node<K,V> put(K k, V v){
    	if(k.equals(this.k)){ //if this if the same key, change the value and return the node
    		this.v = v;
    		return this;
    	}
    	else{	//otherwise, try to put into the next node in the list
    		next = next.put(k,v);
    		return this;
    	}
    }

    public V get(K k) throws NotFoundException{
    	if(k.equals(this.k))
    		return v;
    	else
    		return next.get(k);
    }
}


interface DictFun<A,R> {
    R invoke(A a) throws NotFoundException;
}

// Problem 2b
class DictImpl3<K,V> implements Dict<K,V> {
    protected DictFun<K,V> dFun;

    DictImpl3() { dFun =
    	new DictFun<K,V>() {
	    	public V invoke (K a) throws NotFoundException{
    			throw new NotFoundException(); //simulates empty list
	    	}
	    };
	}

    public void put(K k, V v) { 
    	final DictFun<K,V> currentFun = dFun; //added finals to avoid warnings while testing on older java versions
    	final K key = k;
    	final V value = v;
    	dFun =
    	new DictFun<K,V>() {
	    	public V invoke (K a) throws NotFoundException{
	    	    if(a.equals(key)) //checks if a is the current key
	    	    	return value;
	    	    else //otherwise, calls the old function
	    	    	return currentFun.invoke(a);
	    	}
	    };
    }

    public V get(K k) throws NotFoundException { return dFun.invoke(k); }
}


class Pair<A,B> {
    protected A fst;
    protected B snd;

    Pair(A fst, B snd) { this.fst = fst; this.snd = snd; }

    A fst() { return fst; }
    B snd() { return snd; }
}

// Problem 2c
interface FancyDict<K,V> extends Dict<K,V> {
    void clear();
    boolean containsKey(K k);
    void putAll(List<Pair<K,V>> entries);
}

class FancyDictImpl3<K,V> extends DictImpl3<K,V> implements FancyDict<K,V> {

    //construction, put, and get all use the DictImpl3 super implementations

	public void clear(){ //creates a new empty dictionary (clears the dictionary)
		dFun = 
    	new DictFun<K,V>() {
	    	public V invoke (K a) throws NotFoundException{
    			throw new NotFoundException(); //simulates empty list
	    	}
	    };
	}

	public boolean containsKey(K k){ //tries to get k, if we encounter an error, that means there was no key k
		try{
			get(k);
			return true;
		}
		catch(Exception e){
			return false;
		}
	}

	public void putAll(List<Pair<K,V>> entries){ //go through each pair and put into dictionary
		for(Pair<K,V> p : entries){
			put(p.fst(), p.snd());
		}
	}
}


class DictTest {
    public static void main(String[] args) {

	// a test for Problem 2a
	Dict<String,Integer> dict1 = new DictImpl2<String,Integer>();
	dict1.put("hello", 23);
	dict1.put("bye", 45);
	dict1.put("hello", 500);
	try {
	    System.out.println("bye maps to " + dict1.get("bye")); // prints 45
   	    System.out.println("hello maps to " + dict1.get("hello"));  // prints 500
	    System.out.println("hi maps to " + dict1.get("hi"));  // throws an exception
	} catch(NotFoundException e) {
	    System.out.println("not found!");  // prints "not found!"
	}

	// a test for Problem 2b
	Dict<String,Integer> dict2 = new DictImpl3<String,Integer>();
	dict2.put("hello", 23);
	dict2.put("bye", 45);
	dict2.put("hello", 500);
	try {
	    System.out.println("bye maps to " + dict2.get("bye"));  // prints 45
	    System.out.println("hello maps to " + dict2.get("hello"));   // prints 500
	    System.out.println("hi maps to " + dict2.get("hi"));   // throws an exception
	} catch(NotFoundException e) {
	    System.out.println("not found!");  // prints "not found!"
	}

	// a test for Problem 2c
	FancyDict<String,Integer> dict3 = new FancyDictImpl3<String,Integer>();
	dict3.put("hello", 23);
	dict3.put("bye", 45);
	dict3.put("hello", 500);
	List<Pair<String, Integer>> is = new LinkedList<Pair<String, Integer>>();
	is.add(new Pair<String,Integer>("a", 1));
	is.add(new Pair<String,Integer>("b", 2));
	is.add(new Pair<String,Integer>("c", 3));	
	is.add(new Pair<String,Integer>("d", 4));
	is.add(new Pair<String,Integer>("a", 5));
	dict3.putAll(is);
	try{
		System.out.println("bye maps to " + dict3.get("bye"));  // prints 45
	    System.out.println("hello maps to " + dict3.get("hello"));   // prints 500
	    System.out.println("a maps to " + dict3.get("a"));   // prints 5
		System.out.println(dict3.containsKey("bye")); // prints true
		System.out.println(dict3.containsKey("b")); // prints true		
		System.out.println(dict3.containsKey("f")); // prints false
		dict3.clear();
		System.out.println(dict3.containsKey("bye")); // prints false
	}
	catch(NotFoundException e){
		System.out.println("not found!");  // prints "not found!"
	}

    }
}
