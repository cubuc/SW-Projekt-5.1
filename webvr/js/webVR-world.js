// Setup three.js WebGL renderer. Note: Antialiasing is a big performance hit.
// Only enable it if you actually need to.


var scene,camera,controls,effect,renderer;
var vrDisplay = null;
var lastRender;

init();

function init(){

  renderer = new THREE.WebGLRenderer({antialias: false});
  renderer.setPixelRatio(Math.floor(window.devicePixelRatio));

  // Append the canvas element created by the renderer to document body element.
  document.body.appendChild(renderer.domElement);

  // Create a three.js scene.
  scene = new THREE.Scene();

  // Create a three.js camera.
  camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 10000);

  // Apply VR headset positional data to camera.
  controls = new THREE.VRControls(camera);


  // Apply VR stereo rendering to renderer.
  effect = new THREE.VREffect(renderer);
  effect.setSize(window.innerWidth, window.innerHeight);

  // Get the VRDisplay and save it for later.

  navigator.getVRDisplays().then(function(displays) {
    if (displays.length > 0) {
      vrDisplay = displays[0];

      // Kick off the render loop.
      vrDisplay.requestAnimationFrame(animate);
    }
  });
  createRoom();
  // Create 3D objects.
  var geometry = new THREE.BoxGeometry(0.5, 0.5, 0.5);
  var material = new THREE.MeshNormalMaterial();
  var cube = new THREE.Mesh(geometry, material);
 cube.castShadow= true;
 cube.receiveShadow = true;

  // Position cube mesh
  cube.position.set(0,-1,-5);


  // Add cube mesh to your three.js scene
  scene.add(cube);

  renderer.shadowMap.enabled = true;
  renderer.shadowMap.renderReverseSided = false;


  // Request animation frame loop function
  lastRender = 0;

}
function createRoom(){

// White directional light at 0.8 intensity shining from the right top.
  var directionalLight = new THREE.DirectionalLight( 0xffffff, 0.8 );
  directionalLight.position.set( -1, 1.75, 1 );

  scene.add( directionalLight );
  directionalLight.castShadow = true;


  var groundGeo = new THREE.PlaneBufferGeometry( 25, 25 );
  var groundMat = new THREE.MeshPhongMaterial( { color: 0x000099, specular: 0x000099 } );
  groundMat.color.setHSL(  .74, .64, .59 );
  var ground = new THREE.Mesh( groundGeo, groundMat );
  ground.rotation.x = -Math.PI/2;

  ground.position.y=-2;
  ground.receiveShadow = true;

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
}

function createWall(col,xRot,yRot,xPos,yPos,zPos){
  var wallGeo = new THREE.PlaneBufferGeometry( 25, 7 );
  var wallMat = new THREE.MeshStandardMaterial( { color: col, side: THREE.DoubleSide } );
  wallMat.color.setHSL(  .74, .64, .59 );

  var wall = new THREE.Mesh( wallGeo, wallMat );
  wall.rotation.y = yRot;
  wall.rotation.x = xRot;
  wall.position.set(xPos,yPos,zPos);
  scene.add(wall);
}

function animate(timestamp) {

  lastRender = timestamp;

  // Update VR headset position and apply to camera.
  controls.update();

  // Render the scene.
  effect.render(scene, camera);

  // Keep looping.
  vrDisplay.requestAnimationFrame(animate);
}

function onResize() {
  console.log('Resizing to %s x %s.', window.innerWidth, window.innerHeight);
  effect.setSize(window.innerWidth, window.innerHeight);
  camera.aspect = window.innerWidth / window.innerHeight;
  camera.updateProjectionMatrix();
}

function onVRDisplayPresentChange() {
  console.log('onVRDisplayPresentChange');
  onResize();
}

// Resize the WebGL canvas when we resize and also when we change modes.
window.addEventListener('resize', onResize);
window.addEventListener('vrdisplaypresentchange', onVRDisplayPresentChange);

// Button click handlers.
document.querySelector('button#fullscreen').addEventListener('click', function() {
  enterFullscreen(renderer.domElement);
});
document.querySelector('button#vr').addEventListener('click', function() {
  vrDisplay.requestPresent([{ source: renderer.domElement }]);
});
document.querySelector('button#reset').addEventListener('click', function() {
  vrDisplay.resetPose();
});

function enterFullscreen (el) {
  if (el.requestFullscreen) {
    el.requestFullscreen();
  } else if (el.mozRequestFullScreen) {
    el.mozRequestFullScreen();
  } else if (el.webkitRequestFullscreen) {
    el.webkitRequestFullscreen();
  } else if (el.msRequestFullscreen) {
    el.msRequestFullscreen();
  }
}
