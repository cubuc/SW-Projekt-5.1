/**
* @author Alexej Gluschkow
*/


var lookDir = new THREE.Vector3();

moveCon = function(body,camera){

  function updateLookDir(){
    camera.getWorldDirection(lookDir);
    lookDir.y=0;
    lookDir.normalize();
    lookDir.multiplyScalar(0.1);
  }

  document.onkeydown = function(e){
    if (e.keyCode == 87) { // w
      updateLookDir();
      body.translateZ(lookDir.z);
      body.translateX(lookDir.x);
    }
    else if (e.keyCode == 83) { //s
      updateLookDir();
      body.translateZ(-lookDir.z);
      body.translateX(-lookDir.x);
    }

  };
  this.update = function() {
    var gamePad = navigator.getGamepads()[0];
    if(gamePad != null){
      updateLookDir();
      lookDir.multiplyScalar(0.2)
      if(gamePad.axes[1] > 0.05) {
        body.translateZ(-lookDir.z);
        body.translateX(-lookDir.x);
      }else if(gamePad.axes[1] < -0.05){
        body.translateZ(lookDir.z);
        body.translateX(lookDir.x);

      }

    }
  };


};
