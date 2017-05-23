/**
* @author Alexej Gluschkow
*/


var lookDir = new THREE.Vector3();
var visibleOne = true;
moveCon = function(body,camera,dataOne,dataTwo){
  var pressed = false;

  var gamePad = navigator.getGamepads()[0];
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
    }else if(e.keyCode == 77) { //m
      visibleOne = !visibleOne;
      dataOne.traverse( function ( object ) { object.visible = visibleOne; } );
      dataTwo.traverse( function ( object ) { object.visible = !visibleOne; } );
    }
  };

  this.update = function(vrDisplay) {
    gamePad = navigator.getGamepads()[0];
    if(vrDisplay.isPresenting){
      updateLookDir();
    }else{
    updateLookDir();
    }
    if(gamePad != null){
      //lookDir.multiplyScalar(0.2)
      if(gamePad.axes[1] > 0.05) {
        body.translateZ(-lookDir.z);
        body.translateX(-lookDir.x);
      }else if(gamePad.axes[1] < -0.05){
        body.translateZ(lookDir.z);
        body.translateX(lookDir.x);
      }
      if(gamePad.buttons[0].value > 0 && !pressed) {
        pressed = true;
        visibleOne = !visibleOne;
        dataOne.traverse( function ( object ) { object.visible = visibleOne; } );
        dataTwo.traverse( function ( object ) { object.visible = !visibleOne; } );
      }
      else if(gamePad.buttons[0].value == 0 && pressed) {
        pressed = false;
      }


    }
  };


};
