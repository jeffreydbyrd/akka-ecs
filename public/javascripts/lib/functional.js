/**
 * Returns a Array containing the results of applying f to each element in 1 or
 * more Arrays provided. 'f' must take the same number of parameters as the
 * number of Arrays provided. `map` only iterates as many times as the length of
 * the shortest Array provided. For example: a function `add` takes 2 params and
 * returns their sum (ie. add(3,5) == 8). Then map(add, [1,2,3], [1,2]) ==
 * [2,4].
 */
function map(f) {
	var arrs = [];
	for (i in arguments)
		if (i > 0)
			arrs.push(arguments[i]);
	var loop = function(accum, arrs) {
		var params = [];
		var newarrs = [];
		for (i in arrs) {
			if (arrs[i].length == 0)
				return accum;
			params.push(arrs[i][0]);
			newarrs.push(arrs[i].slice(1));
		}
		return loop(accum.concat([ f.apply(null, params) ]), newarrs);
	};
	return loop([], arrs);
}

function exists(p, arr) {
	for (i in arr)
		if (p(arr[i]))
			return true;
	return false;
}

function filter(p, arr) {
	var builder = [];
	for (i in arr)
		if (p(arr[i]))
			builder.push(arr[i]);
	return builder;
}

function reduce(f, arr) {
	if (arr.length == 0)
		return [];
	var accum = f(arr[0], arr[1]);
	for ( var i = 2; i < arr.length; i++)
		accum = f(accum, arr[i]);
	return accum;
}

/*******************************************************************************
 * Functional Linked List
 ******************************************************************************/

var Nil = (function() {
	function NIL() {
		this.isEmpty = true;
		this.length = 0;
		this.prepend = function(el) {
			return new NonEmptyList(el, this);
		};
		this.concat = function(lst) {
			return lst;
		};
		this.foreach = function(f) { /* noop */
		};
		this.map = function(f) {
			return this;
		};
		this.flatMap = this.map;
		this.reduce = this.map;
		this.filter = this.map;
		this.arr = function() {
			return [];
		};
	}
	return new NIL();
})();

function NonEmptyList(first, rest) {
	this.head = first;
	this.tail = rest;
	this.isEmpty = false;
	this.length = rest.length + 1;
	this.prepend = Nil.prepend;
	this.concat = function(lst) {
		if (lst.isEmpty)
			return this;
		return this.tail.concat(lst).prepend(this.head);
	};
	this.foreach = function(f) {
		f(this.head);
		this.tail.foreach(f);
	};
	this.map = function(f) { // f(x)
		return this.tail.map(f).prepend(f(this.head));
	};
	this.flatMap = function(f) { // f(x) returns a List
		return f(this.head).concat(this.tail.flatMap(f));
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
	this.filter = function(p) { // p(x) returns boolean
		var others = this.tail.filter(p);
		if (p(this.head))
			return others.prepend(this.head);
		return others;
	};
	this.arr = function() {
		return [ this.head ].concat(this.tail.arr());
	};
}

function List() {
	var lst = Nil;
	for (i in arguments)
		lst = lst.prepend(arguments[arguments.length - 1 - i]);
	return lst;
}

/*******************************************************************************
 * Option
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
	arr : []
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
	this.arr = [ ref ];
	this.get = ref;
}

function maybe(nullable) {
	if (nullable == null || typeof nullable == "undefined")
		return None;
	return new Some(nullable);
}
