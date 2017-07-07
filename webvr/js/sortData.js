/**
 * @author Alexej Gluschkow
 */


 //Old file no longer used at all

//used to see if the variance in one coordinate is very small
var eps = 0.05;

//sort the data we got from the app
function sortData(data) {
  // first sort by the x value to create the lanes we later need to triangulate
  var sorted = data[0].sort(function(a, b) {
    return a.x - b.x;
  });
  //get the first row and save the x value of the next row
  var [first, next] = getFirstRow(sorted);
  var second;
  //now create a new lane for every x there is the lanes used to triangulate will
  // will look like this
  //  x_01  x_11  x_11  x_21
  //  x_02  x_12  x_12  x_22
  //  x_03  x_13  x_13  x_23
  //  so we doulbe every lane except the first and the last one to get a goot mesh
  while (next != null) {
    //get the second row and save the next x to be considert
    [second, next] = getNextRow(sorted, next);
    //sort the first row
    first = first.sort(function(a, b) {
      return a.y - b.y;
    });
    //sort the second row in reverse ordering to triangulate easier later on
    second = second.sort(function(a, b) {
      return b.y - a.y
    });
    //store the first and second row together to triangulate them later
    var vertices = first.concat(second);
    data.push(vertices);
    //now duplicate the second row and safe it as the first
    first = second.reverse();
  }
  return data;
}

// get the row inside vert with the value next, save it in res and
// store the next bigger index of x
function getNextRow(vert, next) {
  var i = 0;
  var res = new Array();
  //find the right index (not very efficent but the data array is not so big to use a better search)
  while (Math.abs(next - vert[i].x) > eps) {
    i = i + 1;
  }
  var curr = vert[i].x;
  //while the x value is not too big add them to a list
  while (Math.abs(curr - vert[i].x) < eps) {
    res = res.concat([vert[i]]);
    i = i + 1;
    if (i > vert.length - 1)
      break;
  }
  //store the next x value of the next lane
  if (i > vert.length - 1) {
    next = null;
  } else {
    next = vert[i].x;
  }
  return [res, next]
}
//the first row is done seperatly since if the x values are negative we dont know where the first row is
function getFirstRow(vert) {
  var first = new Array();
  var curr = vert[0].x;
  var i = 0;
  while (Math.abs(curr - vert[i].x) < eps) {
    if (i >= vert.length - 1)
      break;
    first = first.concat([vert[i]]);
    i = i + 1;
  }
  return [first, vert[i].x];
}
