/**
* @author Alexej Gluschkow
*/
var eps = 0.05;

function sortData(data){
  var sorted = data[0].sort(function (a, b) {  return a.x - b.x;  });
  var [first,next] = getFirstRow(sorted);
  var second;
  while(next != null){
    [second,next] = getNextRow(sorted,next);
    first = first.sort(function (a, b) {  return a.y - b.y;  });
    second = second.sort(function (a, b) {  return b.y - a.y  });
    var vertices = first.concat(second);
    data.push(vertices);
    first = second.reverse();
  }
  return data;
}

function getNextRow(vert,next){
  var i =0;
  var res = new Array();
  while( Math.abs(next - vert[i].x) > eps){
    i= i+1;
  }
  var curr = vert[i].x;
  while( Math.abs(curr - vert[i].x) < eps){
    res = res.concat([vert[i]]);
    i = i+1;
    if(i>vert.length -1)
    break;
  }
  if(i>vert.length -1){
    next = null;
  }else{
    next = vert[i].x;
  }
  return [res,next]
}

function getFirstRow(vert){
  var first = new Array();
  var curr = vert[0].x;
  var i = 0;
  while( Math.abs(curr - vert[i].x) < eps){
    if(i>=vert.length-1)
    break;
    first = first.concat([vert[i]]);
    i = i+1;
  }
  return [first,vert[i].x];
}
