/**
 * Returns a list containing the results of applying f to each element in 1 or
 * more Arrays provided. 'f' must take the same number of parameters as the
 * number of Arrays provided. `map` only iterates as many times as the length of
 * the shortest Array provided. For example: a function `add` takes 2 params and
 * returns their sum (ie. add(3,5) == 8). Then map(add, [1,2,3], [1,2]) ==
 * [2,4].
 */
function map(f) {
	var lsts = [];
	for (i in arguments)
		if (i > 0)
			lsts.push(arguments[i]);
	var loop = function(accum, lsts) {
		var params = [];
		var newlsts = [];
		for (i in lsts) {
			if (lsts[i].length == 0)
				return accum;
			params.push(lsts[i][0]);
			newlsts.push(lsts[i].slice(1));
		}
		return loop(accum.concat([ f.apply(null, params) ]), newlsts);
	};
	return loop([], lsts);
}

function exists(p, lst) {
	for (i in lst)
		if (p(lst[i]))
			return true;
	return false;
}

function filter(p, lst) {
	var builder = [];
	for (i in lst)
		if (p(lst[i]))
			builder.push(list[i])
	return builder;
}

function reduce(f, lst) {
	if (lst.length == 0)
		return [];
	var accum = f(lst[0], lst[1]);
	for ( var i = 2; i < lst.length; i++)
		accum = f(accum, lst[i]);
	return accum;
}

/*******************************************************************************
 * A Functional Linked List
 ******************************************************************************/

var Nil = {
	isEmpty : true,
	length : 0,
	cons : function(el) {
		return new NonEmptyList(el, this);
	},
	foreach : function(f) { /* noop */
	},
	map : function(f) {
		return this;
	},
	flatMap : this.map,
	reduce : this.map
};

function NonEmptyList(first, rest) {
	this.head = first;
	this.tail = rest;
	this.isEmpty = false;
	this.length = rest.length + 1;
	this.cons = Nil.cons;
	this.foreach = function(f) {
		f(this.head);
		this.tail.foreach(f);
	};
	this.map = function(f) { // f(x)
		return this.tail.map(f).cons(f(this.head));
	};
	this.flatMap = function(f) { // f(x) returns list
		val lst = f(this.head);
		
	};
	this.reduce = function(f) { // f(acc, x)
		var acc = this.head;
		var these = this.tail;
		while (!these.isEmpty) {
			acc = f(acc, these.head);
			these = these.tail;
		}
		return acc;
	};
}

function list() {
	var lst = Nil;
	for (i in arguments)
		lst = lst.cons(arguments[arguments.length - 1 - i]);
	return lst;
}

/*******************************************************************************
 * Monadic Option
 ******************************************************************************/

var None = {
	isDefined : false,
	map : function(f) { // f returns a new value
		return this;
	},
	flatMap : function(f) { // f returns Some or None
		return this;
	},
	filter : function(f) {
		return this;
	},
	asArray : []
};

function Some(ref) {
	this.isDefined = true;
	this.map = function(f) { // f returns a new value
		return new Some(f(ref));
	};
	this.flatMap = function(f) { // f returns Some or None
		return f(ref);
	};
	this.filter = function(p) { // p is a predicate
		if (p(ref))
			return this;
		return None;
	}
	this.asArray = [ ref ];
	this.get = ref;
}

function maybe(nullable) {
	if (nullable == null)
		return None;
	return new Some(nullable);
}

function test() {

}