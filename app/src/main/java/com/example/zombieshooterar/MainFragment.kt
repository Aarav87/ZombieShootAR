package com.example.zombieshooterar

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class MainFragment : Fragment(R.layout.fragment_main) {

    // Initialize ArFragment
    lateinit var arFragment: ArFragment

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
        ModelRenderable.builder()
            .setSource(
                context,
                Uri.parse("https://github.com/Aarav87/ZombieShootAR/raw/master/app/sampledata/models/battle_rifle_animated/battle_rifle.glb")
            )
            .setIsFilamentGltf(true)
            .build()
            .thenAccept {
                attachModelToCamera(it)
            }
            .exceptionally {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                return@exceptionally null
            }
    }

    private fun attachModelToCamera(renderable: ModelRenderable?) {
        val gun = TransformableNode(arFragment.transformationSystem)

        // Resize model
        gun.scaleController.minScale = 0.02f
        gun.scaleController.maxScale = 0.03f
        gun.localScale = Vector3(0.02f, 0.02f, 0.02f)

        // Set model position
        gun.localPosition = Vector3(0.3f, -0.35f, -0.8f)

        // Rotate model
        gun.localRotation = Quaternion.axisAngle(Vector3(0f, 1f, 0f), 180f)

        gun.renderable = renderable
        arFragment.arSceneView.scene.camera.addChild(gun)
    }
}