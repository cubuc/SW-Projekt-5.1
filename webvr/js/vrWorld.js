/**
 * @author Alexej Gluschkow
 */

//create the scene we want to explore the data in
createRoom = function(scene) {
  //createGround();
  // a skybox so if we run out of the room its nicer
  var skyBoxGeometry = new THREE.CubeGeometry(10000, 10000, 10000);
  var skyBoxMaterial = new THREE.MeshBasicMaterial({
    color: 0x9999ff,
    side: THREE.BackSide
  });
  var skyBox = new THREE.Mesh(skyBoxGeometry, skyBoxMaterial);

  //createWalls();
  var light = new THREE.HemisphereLight( 0xffffff, 0x080820, 1 );
  scene.add( light );

  var ambLight = new THREE.AmbientLight( 0x404040 ); // soft white light
  scene.add( ambLight );



  var mtlLoader = new THREE.MTLLoader();
  mtlLoader.setPath('files/');
  mtlLoader.load('sporthalle4.mtl', function(materials) {
    materials.preload();
    var objLoader = new THREE.OBJLoader();
    objLoader.setMaterials(materials);
    objLoader.setPath('files/');
    objLoader.load('sporthalle4.obj', function(object) {
      object.position.y = -2;
      scene.add(object);
    }, onProgress, onError);
  });
/*
  var objLoader = new THREE.OBJLoader();
  var mat = new THREE.MeshBasicMaterial({
    color: 0x8c8c8c,
    wireframe: true,

    side: THREE.DoubleSide
  });
  objLoader.load('files/sporthalle.obj', function(obj) {
    obj.traverse(function(child) {
      if (child instanceof THREE.Mesh) {
        child.material = mat;
      }

    });
    obj.position.y = -2;
    scene.add(obj);
  });*/


  scene.add(skyBox);
};

var onProgress = function ( xhr ) {
  if ( xhr.lengthComputable ) {
    var percentComplete = xhr.loaded / xhr.total * 100;
    console.log( Math.round(percentComplete, 2) + '% downloaded' );
  }
};
var onError = function ( xhr ) { };


//create all the walls
function createWalls() {
  var wallColor = 0x8c8c8c;

  // left wall
  createWall(wallColor, 0, Math.PI / 2, -7.5, -.75, -12.5, 35);
  //right wall 1
  createWall(wallColor, 0, Math.PI / 2, 7.5, -0.75, -3.75, 17.5);
  //back wall 1
  createWall(wallColor, 0, 0, 0, -0.75, 5, 15);
  //side wall 1
  createWall(wallColor, 0, Math.PI / 4, 12.8, -0.75, -17.8, 15);
  // back wall 2
  createWall(wallColor, 0, 0, 28.1, -0.75, -23.1, 20);
  // front wall
  createWall(wallColor, 0, 0, 15, -0.75, -30, 45);
  //right wall 2
  createWall(wallColor, 0, Math.PI / 2, 35, -0.75, -26.5, 7.5);

}

//create the gound as a very big plane and add a texture to it
function createGround() {
  var texture = new THREE.TextureLoader().load("textures/ground.jpg");
  texture.wrapS = THREE.RepeatWrapping;
  texture.wrapT = THREE.RepeatWrapping;

  texture.repeat.set(10, 10);

  var g1Geo = new THREE.PlaneGeometry(100, 100);
  var g1Mat = new THREE.MeshBasicMaterial({
    map: texture
  });
  var ground = new THREE.Mesh(g1Geo, g1Mat);
  ground.rotation.x = -Math.PI / 2;
  ground.position.set(0, -2, -12.5);
  scene.add(ground);

}

// create a wall at position xPos,yPos,zPos with lenght and the rotation xRot,yRot to move it
// to the right place
function createWall(col, xRot, yRot, xPos, yPos, zPos, length) {

  var wallGeo = new THREE.PlaneGeometry(length, 2.5);
  var wallMat = new THREE.MeshBasicMaterial({
    color: col,
    side: THREE.DoubleSide
  });

  var wall = new THREE.Mesh(wallGeo, wallMat);
  wall.rotation.y = yRot;
  wall.rotation.x = xRot;
  wall.position.set(xPos, yPos, zPos);
  scene.add(wall);
}
