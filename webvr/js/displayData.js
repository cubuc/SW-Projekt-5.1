/**
* @author Alexej Gluschkow
*/

function loadData(loadedData){
  var dataVis = new Array();

  var verticies = new Array();
  for (var i = 0; i < loadedData.length; i++) {
    var vert = new THREE.Vector3(Number(loadedData[i].x),Number(loadedData[i].z),Number(loadedData[i].y));
    verticies[i] = vert;
  }
  var data = new Array();
  data[0] = verticies;
  data = sortData(data);
  dataVis[0] = makePlanes(data);

  var geo = new THREE.Geometry();
  geo.vertices = data[0]
  var material = new THREE.LineBasicMaterial( { color: 0xffffff, opacity: 0.5 } );
  var dataVisTwo = new THREE.Line( geo, material);
  dataVisTwo.rotateX( - Math.PI / 2 );
  dataVisTwo.traverse( function ( object ) { object.visible = false; } );
  dataVis[1] = dataVisTwo;
  return dataVis;
}

function makePlanes(data){
  var plane = new THREE.Object3D();
  for (var i = 1; i < data.length; i++) {
    //create the plane by going over all vertex lists
    var planeGeometry = new THREE.Geometry();
    var planeMat = new THREE.MeshBasicMaterial({side:THREE.DoubleSide,transparent: true});
    planeMat.opacity = 0.5;
    var triangles = THREE.ShapeUtils.triangulateShape(data[i],[]);
    planeGeometry.vertices = data[i];
    for( var j = 0; j < triangles.length; j++ ){
      planeGeometry.faces.push(new THREE.Face3(triangles[j][0], triangles[j][1], triangles[j][2]));
    }
    //planeGeometry.rotateX( - Math.PI / 2 );
    plane.add(new THREE.Mesh(planeGeometry, planeMat));
  }
  plane.rotateX( - Math.PI / 2 );
  return plane;
}
