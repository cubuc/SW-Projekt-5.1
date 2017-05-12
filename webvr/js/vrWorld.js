/**
* @author Alexej Gluschkow
*/

createRoom = function(scene){
  // Create 3D objects.
  var geometry = new THREE.BoxGeometry(0.5, 0.5, 0.5);
  var material = new THREE.MeshNormalMaterial();
  var cube = new THREE.Mesh(geometry, material);
  // Position cube mesh
  cube.position.set(0,-1,-5);


  // Add cube mesh to your three.js scene
  scene.add(cube);

  var groundGeo = new THREE.PlaneGeometry( 25, 25 );
  var groundMat = new THREE.MeshBasicMaterial( { color: 0x979797 } );
  //groundMat.color.setHSL(  .74, .64, .59 );
  var ground = new THREE.Mesh( groundGeo, groundMat );
  ground.rotation.x = -Math.PI/2;

  ground.position.y=-2;
  //ground.receiveShadow = true;

  var skyBoxGeometry = new THREE.CubeGeometry( 10000, 10000, 10000 );
  var skyBoxMaterial = new THREE.MeshBasicMaterial( { color: 0x9999ff, side: THREE.BackSide } );
  var skyBox = new THREE.Mesh( skyBoxGeometry, skyBoxMaterial );

  var wallColor = 0x8c8c8c;
  // right wall
  createWall(wallColor,0,Math.PI/2,-12.5,1,0);
  //left wall
  createWall(wallColor,0,Math.PI/2,12.5,1,0);
  //back wall
  createWall(wallColor,0,0,0,1,10);
  //front wall
  createWall(wallColor,0,0,0,1,-10);


  scene.add(skyBox);
  scene.add(ground);
};

function createWall(col,xRot,yRot,xPos,yPos,zPos){
  var wallGeo = new THREE.PlaneGeometry(25,8);
  var wallMat = new THREE.MeshBasicMaterial( { color: col, side: THREE.DoubleSide } );


  var wall = new THREE.Mesh( wallGeo, wallMat );
  wall.rotation.y = yRot;
  wall.rotation.x = xRot;
  wall.position.set(xPos,yPos,zPos);
  scene.add(wall);
}
