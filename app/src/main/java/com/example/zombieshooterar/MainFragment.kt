package com.example.zombieshooterar

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.ar.core.Anchor
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import java.util.*

class MainFragment : Fragment(R.layout.fragment_main) {

    // Initialize ArFragment
    lateinit var arFragment: ArFragment

    lateinit var frame: Frame
    lateinit var planes: Collection<Plane>
    lateinit var anchor: Anchor
    lateinit var anchorNode: AnchorNode

    private var zombieIsPlaced = false

    // 3D models
    lateinit var gunRenderable: ModelRenderable
    lateinit var zombieRenderable: ModelRenderable

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arFragment = childFragmentManager.findFragmentById(R.id.scene_form_fragment) as ArFragment

        // Hide plane discovery controller
        arFragment.instructionsController.isEnabled = false

        // Disable plane renderer
        arFragment.arSceneView.planeRenderer.isVisible = false

        loadModels()

    }

    private fun loadModels() {
        // Render gun model
        ModelRenderable.builder()
            .setSource(
                context,
                Uri.parse("https://github.com/Aarav87/ZombieShootAR/raw/master/app/models/guns/battle_rifle.glb")
            )
            .setIsFilamentGltf(true)
            .build()
            .thenAccept {
                gunRenderable = it
                attachModelToCamera()
            }
            .exceptionally {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                return@exceptionally null
            }

        // Render zombie model
        ModelRenderable.builder()
            .setSource(
                context,
                Uri.parse("https://github.com/Aarav87/ZombieShootAR/raw/master/app/models/zombies/zombie.glb")
            )
            .setIsFilamentGltf(true)
            .build()
            .thenAccept {
                zombieRenderable = it
                arFragment.arSceneView.scene.addOnUpdateListener(this::onUpdate)
            }
            .exceptionally {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                return@exceptionally null
            }
    }

    private fun onUpdate(frameTime: FrameTime) {
        frame = arFragment.arSceneView.arFrame!!
        planes = frame.getUpdatedTrackables(Plane::class.java)

        if (zombieIsPlaced) {
            return
        }

        for (plane in planes) {
            if (plane.trackingState == TrackingState.TRACKING) {
                anchor = plane.createAnchor(plane.centerPose)
                anchorNode = AnchorNode(anchor)

                anchorNode.localScale = Vector3(0.01f, 0.01f, 0.01f)

                anchorNode.setRenderable(zombieRenderable).animate(false).start()
                arFragment.arSceneView.scene.addChild(anchorNode)

                zombieIsPlaced = true
            } else {
                Toast.makeText(context, "Scan around to detect plane", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun attachModelToCamera() {
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
        arFragment.arSceneView.scene.camera.addChild(gun)
    }
}