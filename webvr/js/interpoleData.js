/**
 * @author Alexej Gluschkow
 */


// interpolate the given data to get a better plane
function interpole(data, width, height) {
  var result = new Array();
  for (var j = height - 1; j >= 0; j--) {
    for (var i = 0; i < width; i++) {
      //use inverse distance weight interpolation to calculate all the points
      result.push(idwInterpol(data, i * 2, j * 2));
    }
  }
  return result;
}


// inverse distance weight interpolation;
function idwInterpol(data, x, y) {
  var weights = new Float32Array(data.length);
  var value = 0;
  var sumWeights = 0;
  // calculate the sum of w[i]*data[i].z and w[i]
  for (var i = 0; i < data.length; i++) {
    if(data[i].x == x && data[i].y==y){
      return data[i].z;
    }
    weights[i] = 1 / Math.pow(distance(data[i], x, y),2);
    sumWeights += weights[i];
    value += weights[i] * data[i].z;
  }
  // normalize the weighted sum by the weights of every node
  return value / sumWeights;
}

//returns the distance from a point p to some x and y cooridnates
function distance(p, x, y) {
  return Math.sqrt(Math.pow(p.x - x, 2) + Math.pow(p.y - y, 2));
}
