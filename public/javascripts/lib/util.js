function contains(arr, obj) {
  for (var i = 0; i < arr.length; i++) {
    if (arr[i] === obj) {
      return true;
    }
  }
  return false;
}

/**
 * Returns a Array containing the results of applying f to each element in 1 or
 * more Arrays provided. 'f' must take the same number of parameters as the
 * number of Arrays provided. `map` only iterates as many times as the length of
 * the shortest Array provided. For example: a function `add` takes 2 params and
 * returns their sum (ie. add(3,5) == 8). Then map(add, [1,2,3], [1,2]) ==
 * [2,4].
 *
 * NOTE: when referencing 'this', know that the owner of 'f' is 'window'
 */
function map(f) {
	var arrs = [];
	for (i in arguments) {
		if (i > 0) {
			arrs.push(arguments[i]);
    }
  }

	var loop = function(accum, arrs) {
		var params = [];
		var newarrs = [];
		for (i in arrs) {
			if (arrs[i].length == 0) {
				return accum;
      }
			params.push(arrs[i][0]);
			newarrs.push(arrs[i].slice(1));
		}
		return loop(accum.concat([ f.apply(window, params) ]), newarrs);
	};

	return loop([], arrs);
}

function exists(arr, p) {
	for (i in arr) {
		if (p(arr[i])) {
			return true;
    }
  }
	return false;
}

function filter(arr, p) {
	var builder = [];
	for (i in arr) {
		if (p(arr[i])) {
			builder.push(arr[i]);
    }
  }
	return builder;
}

function reduce(arr, f) {
  if (arr.length == 0) {
    throw new TypeError("Reduce of empty array with no initial value");
  }
	if (arr.length == 1) {
		return arr[0];
  }
	var accum = f(arr[0], arr[1]);
	for ( var i = 2; i < arr.length; i++) {
		accum = f(accum, arr[i]);
  }
	return accum;
}
