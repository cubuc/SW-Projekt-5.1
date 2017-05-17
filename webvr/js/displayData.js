/**
* @author Alexej Gluschkow
*/

displayData = function(){

  //load the data
  var vert = loadData();
  //the plane is created
  var geometry = new THREE.Geometry();
  var matOne = new THREE.MeshBasicMaterial({side:THREE.DoubleSide ,transparent: true});
  matOne.opacity = 0.5;
  var triangles = THREE.ShapeUtils.triangulateShape(vert,[]);
  geometry.vertices = vert;
  for( var i = 0; i < triangles.length; i++ ){
    geometry.faces.push(new THREE.Face3(triangles[i][0], triangles[i][1], triangles[i][2]));
  }
  geometry.rotateX( - Math.PI / 2 );
  //the lines just for testing
  var geo = new THREE.Geometry();
  geo.vertices = vert
  var material = new THREE.LineBasicMaterial( { color: 0xffffff, opacity: 0.5 } );
  var dataVisOne = new THREE.Mesh(geometry, matOne);
  var dataVisTwo = new THREE.Line( geo, material);
  dataVisTwo.traverse( function ( object ) { object.visible = false; } );

  var dataVis = new Array();
  dataVis[0] = dataVisOne;
  dataVis[1] = dataVisTwo;
  return dataVis;
};
