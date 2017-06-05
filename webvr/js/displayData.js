/**
 * @author Alexej Gluschkow
 */

function loadData(loadedData) {
  var dataVis = new Array();

  var verticies = new Array();
  for (var i = 0; i < loadedData.length; i++) {
    var vert = new THREE.Vector3(Number(loadedData[i].x), Number(loadedData[i].y), Number(loadedData[i].z));
    verticies[i] = vert;
  }
  var data = new Array();
  data[0] = verticies;
  data = sortData(data);
  dataVis[0] = makePlanes(data);
  dataVis[1] = makeBalls(data[0]);
  return dataVis;
}

function makeBalls(data) {
  var text = new THREE.Object3D();
  var balls = new THREE.Object3D();
  for (var i = 0; i < data.length; i++) {
    // the spheres
    var geometry = new THREE.SphereGeometry(0.05, 10, 10);
    var material = new THREE.MeshBasicMaterial({
      color: 0xffff00
    });
    var sphere = new THREE.Mesh(geometry, material);
    sphere.translateX(data[i].x);
    sphere.translateZ(-data[i].y);
    sphere.translateY(-0.5)
    balls.add(sphere);
    // the text as a 2D sprite which is nicer then just a 3D text
    var canvas = document.createElement('canvas');
    var context = canvas.getContext('2d');
    var size = 64;
    canvas.width = size;
    canvas.height = size;

    context.font = "20px Serif";
    context.textAlign = "left";
    context.textBaseline = "top";
    context.fillStyle = 'white';
    context.strokeStyle = 'black';
    context.fillText(data[i].z, 0, 0);

    var texture = new THREE.Texture(canvas);
    texture.needsUpdate = true;

    var material = new THREE.SpriteMaterial({
      map: texture,
      transparent: false
    });
    var sprite = new THREE.Sprite(material);
    sprite.position.x = data[i].x;
    sprite.position.z = -data[i].y;
    sprite.position.y = -0.5;

    text.add(sprite);

  }
  text.traverse(function(object) {
    object.visible = false;
  });

  balls.traverse(function(object) {
    object.visible = false;
  });
  var dataVis = new THREE.Object3D();
  dataVis.add(balls);
  dataVis.add(text);
  return dataVis;
}

function makePlanes(data) {
  var plane = new THREE.Object3D();
  for (var i = 1; i < data.length; i++) {
    //create the plane by going over all vertex lists
    var planeGeometry = new THREE.Geometry();
    var planeMat = new THREE.MeshBasicMaterial({
      side: THREE.DoubleSide,
      wireframe: false,
      transparent: true
    });
    planeMat.opacity = 0.5;
    var triangles = THREE.ShapeUtils.triangulateShape(data[i], []);
    planeGeometry.vertices = data[i];
    for (var j = 0; j < triangles.length; j++) {
      planeGeometry.faces.push(new THREE.Face3(triangles[j][0], triangles[j][1], triangles[j][2]));
    }
    //planeGeometry.rotateX( - Math.PI / 2 );
    plane.add(new THREE.Mesh(planeGeometry, planeMat));
  }
  plane.rotateX(-Math.PI / 2);
  return plane;
}
