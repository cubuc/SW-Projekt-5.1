/**
* @author Alexej Gluschkow
*/

createRoom = function(scene){


  createGround();

  var skyBoxGeometry = new THREE.CubeGeometry( 10000, 10000, 10000 );
  var skyBoxMaterial = new THREE.MeshBasicMaterial( { color: 0x9999ff, side: THREE.BackSide } );
  var skyBox = new THREE.Mesh( skyBoxGeometry, skyBoxMaterial );

  createWalls();


  scene.add(skyBox);
};

function createWalls(){
  var wallColor = 0x8c8c8c;

  // left wall
   createWall(wallColor,0,Math.PI/2,-7.5,-.75,-12.5,35);
  //right wall 1
  createWall(wallColor,0,Math.PI/2,7.5,-0.75,-3.75,17.5);
  //back wall 1
  createWall(wallColor,0,0,0,-0.75,5,15);
  //side wall 1
  createWall(wallColor,0,Math.PI/4,12.8,-0.75,-17.8,15);
  // back wall 2
  createWall(wallColor,0,0,28.1,-0.75,-23.1,20);
  // front wall
  createWall(wallColor,0,0,15,-0.75,-30,45);
  //right wall 2
  createWall(wallColor,0,Math.PI/2,35,-0.75,-26.5,7.5);

}

function createGround(){
  var texture = new THREE.TextureLoader().load( "textures/ground.jpg" );
  texture.wrapS = THREE.RepeatWrapping;
  texture.wrapT = THREE.RepeatWrapping;

  texture.repeat.set( 10, 10 );

  var g1Geo = new THREE.PlaneGeometry( 100, 100 );
  var g1Mat = new THREE.MeshBasicMaterial( { map:texture } );
  var ground = new THREE.Mesh( g1Geo, g1Mat );
  ground.rotation.x = -Math.PI/2;
  ground.position.set(0,-2,-12.5);
  scene.add(ground);

}

function createWall(col,xRot,yRot,xPos,yPos,zPos,length){

  var wallGeo = new THREE.PlaneGeometry(length,2.5);
  var wallMat = new THREE.MeshBasicMaterial( { color: col, side: THREE.DoubleSide } );


  var wall = new THREE.Mesh( wallGeo, wallMat );
  wall.rotation.y = yRot;
  wall.rotation.x = xRot;
  wall.position.set(xPos,yPos,zPos);
  scene.add(wall);
}
