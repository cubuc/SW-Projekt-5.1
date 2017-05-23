

var scene,camera,controls,effect,renderer;
var vrDisplay = null;
var lastRender;
var dollyCam;
init();

//setup the renderer, camera und loads the scene and data
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

  dollyCam = new THREE.PerspectiveCamera();
  dollyCam.add(camera);
  scene.add(dollyCam);
  // enable gamepad controlls

  // Apply VR stereo rendering to renderer.
  effect = new THREE.VREffect(renderer);
  effect.setSize(window.innerWidth, window.innerHeight);
  // load the whole scene
  createRoom(scene);
  //display the data
  var dataVis = displayData();
  scene.add(dataVis[0]);
  scene.add(dataVis[1]);

  moveCon = new moveCon(dollyCam,camera,dataVis[0],dataVis[1]);

  // Request animation frame loop function
  lastRender = 0;

  //the the vrDisplay and kick of render loop
  navigator.getVRDisplays().then(function(displays) {
    if (displays.length > 0) {
      vrDisplay = displays[0];
      // Kick off the render loop.
      vrDisplay.requestAnimationFrame(animate);
    }
  });
}


function animate(timestamp) {

  lastRender = timestamp;

  // Update VR headset position and apply to camera.
  controls.update();

  moveCon.update(vrDisplay);

  // Render the scene.
  effect.render(scene, camera);

  // Keep looping.
  vrDisplay.requestAnimationFrame(animate);
}

function onResize() {
  effect.setSize(window.innerWidth, window.innerHeight);
  camera.aspect = window.innerWidth / window.innerHeight;
  camera.updateProjectionMatrix();
}

function onVRDisplayPresentChange() {
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
