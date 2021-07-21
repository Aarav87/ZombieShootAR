package com.example.zombieshooterar

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.ar.core.Anchor
import com.google.ar.core.Frame
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class MainFragment : Fragment(R.layout.fragment_main) {

    // Initialize ArFragment
    lateinit var arFragment: ArFragment

    lateinit var gun: Renderable
    lateinit var frame: Frame
    lateinit var pose: Pose
    lateinit var anchor: Anchor
    lateinit var anchorNode: AnchorNode

    private var placed = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arFragment = childFragmentManager.findFragmentById(R.id.scene_form_fragment) as ArFragment

        // Hide plane discovery controller
        arFragment.instructionsController.isEnabled = false

        // Disable plane renderer
        arFragment.arSceneView.planeRenderer.isEnabled = false

        loadModels()

        arFragment.arSceneView.scene.addOnUpdateListener(this::onUpdateFrame)

    }

    private fun loadModels() {
        ModelRenderable.builder()
            .setSource(
                context,
                Uri.parse("https://github.com/Aarav87/ZombieShootAR/raw/master/app/sampledata/models/battle_rifle_animated/battle_rifle.glb")
            )
            .setIsFilamentGltf(true)
            .build()
            .thenAccept {
                gun = it
            }
            .exceptionally {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                return@exceptionally null
            }
    }

    private fun onUpdateFrame(frameTime: FrameTime) {
        frame = arFragment.arSceneView.arFrame!!

        if (frame.camera.trackingState == TrackingState.TRACKING) {
            if (placed) {
                anchor.detach()
            }

            pose = frame.camera.pose.compose(Pose.makeTranslation(0.3f, -0.35f, -0.8f))

            anchor = arFragment.arSceneView.session!!.createAnchor(pose)
            anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment.arSceneView.scene.camera)

            val gun = TransformableNode(arFragment.transformationSystem)

            // Resize model
            gun.scaleController.minScale = 0.02f
            gun.scaleController.maxScale = 0.03f
            gun.localScale = Vector3(0.02f, 0.02f, 0.02f)

            // Rotate model
            gun.localRotation = Quaternion.axisAngle(Vector3(0f, 1f, 0f), 180f)

            gun.setParent(anchorNode)
            gun.renderable = this.gun
            gun.select()

            placed = true
        }
    }
}