/**
 * Returns a list containing the results of applying f to each element in 1 or
 * more Arrays provided. 'f' must take the same number of parameters as the
 * number of Arrays provided. `map` only iterates as many times as the length of
 * the shortest Array provided. For example: a function `add` takes 2 params and
 * returns their sum (ie. add(3,5) == 8). Then map(add, [1,2,3], [1,2]) == [2,4].
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