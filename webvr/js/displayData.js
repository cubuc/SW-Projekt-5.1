/**
 * @author Alexej Gluschkow
 */

 displayData = function(){
    //some tests for the conrtols
    var dataVisOne = new THREE.Mesh();

     var geometry = new THREE.BoxGeometry( 2, 2, 2 );
     var material = new THREE.MeshBasicMaterial( { color: 0x00ff00 , side:THREE.DoubleSide} );
     var cube = new THREE.Mesh( geometry, material );
     cube.position.z = -5;
     cube.position.x = -2;

     var dataVisTwo = new THREE.Mesh();

     var geometrys = new THREE.SphereGeometry( 0.2, 64, 64 );
     var materials = new THREE.MeshBasicMaterial( { color: 0xffffff , side:THREE.DoubleSide} );
     var sphere = new THREE.Mesh( geometrys, materials );
     sphere.position.z= -5;

     var gs2 = new THREE.SphereGeometry( 0.2, 64, 64 );
     var ms2 = new THREE.MeshBasicMaterial( { color: 0xfff000 , side:THREE.DoubleSide} );
     var s2 = new THREE.Mesh( gs2, ms2 );
     s2.position.z= -5;
     s2.position.x = 2;

     dataVisTwo.add(sphere);
     dataVisTwo.add(s2);
     dataVisOne.add(cube);
     dataVisTwo.traverse( function ( object ) { object.visible = false; } );

     var dataVis = new Array();
     dataVis[0] = dataVisOne;
     dataVis[1] = dataVisTwo;

     return dataVis;
 };
