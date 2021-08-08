package com.example.zombieshooterar

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
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
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : Fragment(R.layout.fragment_main) {

    // Initialize ArFragment
    lateinit var arFragment: ArFragment

    // 3D models
    private lateinit var gunRenderable: ModelRenderable
    private lateinit var zombieRenderable: ModelRenderable

    private var zombiesAlive = 0
    private var timeSurvived = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arFragment = childFragmentManager.findFragmentById(R.id.scene_form_fragment) as ArFragment

        // Hide plane discovery controller
        arFragment.instructionsController.isEnabled = false

        // Disable plane renderer
        arFragment.arSceneView.planeRenderer.isVisible = false

        loadModels()
        addGunInfo()

    }

    private fun addGunInfo() {
        val imageView = ImageView(context)

        val imageParams = FrameLayout.LayoutParams(400, 200)
        imageParams.gravity = Gravity.TOP or Gravity.RIGHT
        imageParams.rightMargin = 100

        imageView.layoutParams = imageParams

        val imageResId = R.drawable.battlerifle

        imageView.setImageResource(imageResId)
        frameLayout.addView(imageView)
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
                arFragment.arSceneView.scene.addOnUpdateListener(this::zombieGenerator)
            }
            .exceptionally {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                return@exceptionally null
            }
    }

    private fun zombieGenerator(frameTime: FrameTime) {
        val frame: Frame = arFragment.arSceneView.arFrame!!
        val planes: Collection<Plane> = frame.getUpdatedTrackables(Plane::class.java)

        for (plane in planes) {
            if (plane.trackingState == TrackingState.TRACKING) {
                val savedPlane: Plane = plane
                if (timeSurvived % 10 == 0 && zombiesAlive < 50) {
                    val anchor: Anchor = savedPlane.createAnchor(savedPlane.centerPose)
                    attachZombieToPlane(anchor)
                }
            }
        }

        timeSurvived++
        Log.d("zombiesAlive", "$zombiesAlive")
    }

    private fun attachZombieToPlane(anchor: Anchor?) {
        val anchorNode = AnchorNode(anchor)

        // Resize model
        anchorNode.localScale = Vector3(0.01f, 0.01f, 0.01f)

        val cameraPosition = arFragment.arSceneView.scene.camera.worldPosition
        val anchorNodePosition = anchorNode.worldPosition
        val direction = Vector3.subtract(cameraPosition, anchorNodePosition)
        anchorNode.localRotation = Quaternion.lookRotation(direction, Vector3.up())

        anchorNode.renderable = zombieRenderable
        arFragment.arSceneView.scene.addChild(anchorNode)

        zombiesAlive++
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