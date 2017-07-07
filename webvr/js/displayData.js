/**
 * @author Alexej Gluschkow
 */



//get the data from the main file
function loadData(loadedData) {
  // the two different visualisations
  var dataVis = new Array();

  var verticies = new Array();
  // get the data from the file and store it in an array
  for (var i = 0; i < loadedData.length; i++) {
    var vert = new THREE.Vector3(Number(loadedData[i].x), Number(loadedData[i].y), Number(loadedData[i].z));
    verticies[i] = vert;
  }

  dataVis[1] = makeBalls(verticies, loadedData);
  //interpolate the given data to get a smooth plane
  var data = interpole(verticies, 26, 16);
  var planes = new THREE.Object3D();
  planes.add(formPlane(data));
  planes.add(makeText(verticies, loadedData));
  dataVis[0] = planes
  // make the spheres and the data points
  return dataVis;
}


// make the balls to display the data in a second way
function makeBalls(data, loadedData) {
  // make the text and balls seperate
  var text = new THREE.Object3D();
  var balls = new THREE.Object3D();
  var color = calcColor(loadedData);

  //first make the balls in the position of the verticies, but disregard the
  // height so all balls are in the same plane
  for (var i = 0; i < data.length; i++) {
    // the spheres
    var geometry = new THREE.SphereGeometry(0.05, 10, 10);
    var material = new THREE.MeshBasicMaterial({
      color: color[i]
    });
    var sphere = new THREE.Mesh(geometry, material);
    sphere.translateX(data[i].x);
    sphere.translateZ(-data[i].y);
    sphere.translateY(data[i].z);
    balls.add(sphere);
    // the text as a 2D sprite which is nicer then just a 3D text
    text.add(makeSprite(data[i], loadedData[i].data));
  }
  // hide the second visualisation
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


// puts all the sprites into one object
function makeText(data, loadedData) {
  var text = new THREE.Object3D();
  for (var i = 0; i < data.length; i++) {
    text.add(makeSprite(data[i], loadedData[i].data));
  }
  return text;
}

// constructs the value sptrites used to show the real recored data
function makeSprite(pos, data) {
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
  context.fillText(data, 0, 0);

  var texture = new THREE.Texture(canvas);
  texture.needsUpdate = true;

  var material = new THREE.SpriteMaterial({
    map: texture,
    transparent: false,
  });
  var sprite = new THREE.Sprite(material);
  sprite.position.x = pos.x;
  sprite.position.z = -pos.y;
  sprite.position.y = pos.z;

  return sprite;
}

// calculatess a color value depending on the given data array
function calcColor(data) {
  var col = [];
  var max = Number(data[0].data);
  var min = Number(data[0].data);
  for (var i = 0; i < data.length; i++) {
    if (Number(data[i].data) > max) {
      max = Number(data[i].data);
    } else if (Number(data[i].data) < min) {
      min = Number(data[i].data);
    }
  }

  for (var i = 0; i < data.length; i++) {
    var tmp = (Number(data[i].data) - min) / (max - min);
    tmp = 50 - 50 * tmp;
    // colors range form yellow to red
    col[i] = new THREE.Color("hsl(" + tmp + ", 70%, 50%)");
  }
  return col
}

// creates a plane with the given interpolated data
function formPlane(data) {
  // create a plane with 25 width segments and 15 height segments
  var geometry = new THREE.PlaneBufferGeometry(50, 30, 25, 15);
  // rotate and move the plane into position
  geometry.rotateX(-Math.PI / 2);
  geometry.translate(25, 0, -15);
  // now set the color and height of every vertex
  var vertices = geometry.getAttribute('position').array;
  var color = new THREE.Color();
  var colors = new Float32Array(vertices.length);
  for (var j = 0; j < vertices.length; j += 3) {
    // set the height of every vertex depending on its height
    vertices[j + 1] = data[j / 3];
    var tmp = 50 - (data[j / 3] + 1.5) * 50;
    // create a hsl color for better transitions
    var col = new THREE.Color("hsl(" + tmp + ", 70%, 50%)");
    // set the color of a vertex
    colors[j] = col.r;
    colors[j + 1] = col.g;
    colors[j + 2] = col.b;

  }
  // add the color to the geometry
  geometry.addAttribute('color', new THREE.BufferAttribute(colors, 3));
  var planeMat = new THREE.MeshBasicMaterial({
    side: THREE.DoubleSide,
    wireframe: false,
    transparent: true,
    vertexColors: THREE.VertexColors
  });
  geometry.setZ = -0.5
  planeMat.opacity = 0.8;
  return new THREE.Mesh(geometry, planeMat);
}

//old unused stuff
/*

function makePlanes(data) {
  var plane = new THREE.Object3D();
  for (var i = 1; i < data.length; i++) {
    //create the plane by going over all vertex lists
    var planeGeometry = new THREE.Geometry();
    var planeMat = new THREE.MeshBasicMaterial({
      side: THREE.DoubleSide,
      wireframe: false,
      transparent: true,
      shading: THREE.SmoothShading
    });
    planeMat.opacity = 0.5;
    var triangles = THREE.ShapeUtils.triangulateShape(data[i], []);
    planeGeometry.vertices = data[i];
    for (var j = 0; j < triangles.length; j++) {
      planeGeometry.faces.push(new THREE.Face3(triangles[j][0], triangles[j][1], triangles[j][2]));
    }
    planeGeometry.computeVertexNormals();

    plane.add(new THREE.Mesh(planeGeometry, planeMat));
  }
  plane.rotateX(-Math.PI / 2);
  return plane;
}*/
