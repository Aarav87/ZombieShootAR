package com.example.zombieshooterar

import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class Gun (
    private val arFragment: ArFragment,
    private val scene: Scene
): MainFragment() {

    fun attachGunModelToCamera() {
        val gun = TransformableNode(arFragment.transformationSystem)

        // Disable movement in transformable node
        gun.translationController.isEnabled = false

        // Resize model
        gun.scaleController.minScale = 0.02f
        gun.scaleController.maxScale = 0.03f
        gun.localScale = Vector3(0.02f, 0.02f, 0.02f)

        // Set model position
        gun.localPosition = Vector3(0.3f, -0.35f, -0.8f)

        // Rotate model
        gun.localRotation = Quaternion.axisAngle(Vector3(0f, 1f, 0f), 180f)

        gun.renderable = gunRenderable
        scene.camera.addChild(gun)

        gunRenderableInstance = gun.renderableInstance!!
    }
}