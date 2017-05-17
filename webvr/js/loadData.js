/**
 * @author Alexej Gluschkow
 */
 var eps = 0.05;

loadData = function(){
  //some test values
  var vertices = new Array();
  vertices[0] = new THREE.Vector3(0,0,-0.5);
  vertices[1] = new THREE.Vector3(4,0,-0.5);
  vertices[2] = new THREE.Vector3(4,5,-0.5);
  vertices[3] = new THREE.Vector3(0,5,-0.5);

  var sorted = vertices.sort(function (a, b) {  return a.x - b.x;  });
  var [first,next] = getFirstRow(sorted);
  var [second,next] = getNextRow(sorted,next);
  second = second.reverse();
  vertices = first.concat(second);
  return vertices;
};

function getNextRow(data,next){
  var i =0;
  var res = new Array();
  while( Math.abs(next - data[i].x) > eps){
    i= i+1;
  }
  var curr = data[i].x;
  while( Math.abs(curr - data[i].x) < eps){
    res = res.concat([data[i]]);
    i = i+1;
    if(i>data.length -1)
      break;
  }
  if(i>data.length -1){
    next = null;
  }else{
    next = data[i].x;
  }
  return [res,next]
}

function getFirstRow(data){
  var first = new Array();
  var curr = data[0].x;
  var i=0;
  while( Math.abs(curr - data[i].x) < eps){
    console.log(data[0]);
    first = first.concat([data[i]]);
    i = i+1;
  }
  return [first,data[i].x];
}
