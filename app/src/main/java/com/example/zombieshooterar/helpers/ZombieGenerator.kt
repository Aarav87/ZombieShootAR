package com.example.zombieshooterar

import android.os.SystemClock
import android.widget.Chronometer
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlin.random.Random

class ZombieGenerator(
    private val arFragment: ArFragment,
    private val planesDetected: MutableList<Plane>,
    private val scene: Scene,
    private val chronometer: Chronometer
): MainFragment() {

    fun generateZombies() {
        val timeSurvived = SystemClock.elapsedRealtime() - chronometer.base / 1000
        if (timeSurvived % 50 == 0L && planesDetected.size > 1) {
            val plane = planesDetected[Random.nextInt(planesDetected.size)]
            val anchor = plane.createAnchor(plane.centerPose)

            attachZombieToPlane(anchor)
        }
    }

    private fun attachZombieToPlane(anchor: Anchor?) {
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(scene)

        val zombie = TransformableNode(arFragment.transformationSystem)

        // Disable movement in transformable node
        zombie.translationController.isEnabled = false

        // Resize model
        zombie.scaleController.minScale = 0.01f
        zombie.scaleController.maxScale = 0.02f
        zombie.localScale = Vector3(0.01f, 0.01f, 0.01f)

        // Rotate model
        val cameraPosition = scene.camera.worldPosition
        val anchorNodePosition = zombie.worldPosition
        val direction = Vector3.subtract(cameraPosition, anchorNodePosition)
        zombie.localRotation = Quaternion.lookRotation(direction, Vector3.up())

        // Change node name
        zombie.name = "Zombie"

        zombie.renderable = zombieRenderable
        zombie.setParent(anchorNode)

        ModelAnimator.ofAnimationFrame(zombie.renderableInstance, "Zombie@Z_Walk_InPlace", 100).start()

        zombiesAlive++
    }

}