/**
* @author Alexej Gluschkow
*/


// the main funciton to control the character
moveCon = function(body,camera,dataOne,dataTwo){
  //checks if the button is pressed or not
  var pressed = false;
  //used to store the current viewVector
  var viewVector = new THREE.Vector3();
  // used to store which visualisation is shown
  var visibleOne = true;

  //get the connected gamepads
  var gamePad = navigator.getGamepads()[0];
  // update the viewVector to know where to move
  function updateViewVector(){
    camera.getWorldDirection(viewVector);
    viewVector.y=0;
    // normalize it so the length is uniform
    viewVector.normalize();
    // scale it so we dont move so fast
    viewVector.multiplyScalar(0.1);
  }
  //the keyboard controls for testing stuff in the browser
  document.onkeydown = function(e){
    if (e.keyCode == 87) { // w
      updateViewVector();
      body.translateZ(viewVector.z);
      body.translateX(viewVector.x);
    }
    else if (e.keyCode == 83) { //s
      updateViewVector();
      body.translateZ(-viewVector.z);
      body.translateX(-viewVector.x);
    }else if(e.keyCode == 77) { //m
      visibleOne = !visibleOne;
      dataOne.traverse( function ( object ) { object.visible = visibleOne; } );
      dataTwo.traverse( function ( object ) { object.visible = !visibleOne; } );
    }
  };

  //the gamepad controls which is run inside the render loop
  this.update = function() {
    //again get the gamepads to update the current gamepad state
    gamePad = navigator.getGamepads()[0];
    //update the view Vector to know where to move
    updateViewVector();
    // a gamepad is found
    if(gamePad != null){
      //scale it again to move slower
      viewVector.multiplyScalar(0.2)
      //pushing the stick backwards
      if(gamePad.axes[1] > 0.05) {
        body.translateZ(-viewVector.z);
        body.translateX(-viewVector.x);
        //pushing the stick forwards
      }else if(gamePad.axes[1] < -0.05){
        body.translateZ(viewVector.z);
        body.translateX(viewVector.x);
      }
      // if a specific button is pressed change the visualisation
      if(gamePad.buttons[0].value > 0 && !pressed) {
        //look out that the button is pressed only once
        pressed = true;
        visibleOne = !visibleOne;
        dataOne.traverse( function ( object ) { object.visible = visibleOne; } );
        dataTwo.traverse( function ( object ) { object.visible = !visibleOne; } );
      }
      //if the button is released the user can press it agin to change the views
      else if(gamePad.buttons[0].value == 0 && pressed) {
        pressed = false;
      }
    }
  };
};
