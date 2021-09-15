package com.example.zombieshooterar

import android.graphics.Point
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.RenderableInstance
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import java.util.*
import kotlinx.android.synthetic.main.fragment_main.*
import kotlin.concurrent.thread
import kotlin.random.Random

class MainFragment : Fragment(R.layout.fragment_main) {

    private lateinit var arFragment: ArFragment
    private lateinit var scene: Scene

    // Model Renderable
    private lateinit var gunRenderable: ModelRenderable
    private lateinit var zombieRenderable: ModelRenderable

    // Renderable Instance
    private var gunRenderableInstance: RenderableInstance? = null

    // Detected Planes
    private var planesDetected: MutableList<Plane> = mutableListOf()

    private var zombiesAlive = 0
    private var zombiesKilled = 0
    private var timeSurvived: Long = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get height & width of users screen
        val display = activity?.windowManager?.defaultDisplay
        val point = Point()
        display?.getRealSize(point)

        // Initialize ArFragment
        arFragment = childFragmentManager.findFragmentById(R.id.scene_form_fragment) as ArFragment
        scene = arFragment.arSceneView.scene

        // Hide plane discovery controller
        arFragment.instructionsController.isEnabled = false

        // Disable plane renderer
        arFragment.arSceneView.planeRenderer.isVisible = false

        loadModels()
        addGunInfo()

        scene.addOnUpdateListener(this::zombieGenerator)

        shoot.setOnClickListener {
            onShoot(point)
        }
    }

    private fun onShoot(point: Point) {
        // Start gun shooting animation
        val shootAnimation = ModelAnimator.ofAnimationTime(gunRenderableInstance, "CINEMA_4D_Main", 10F)
        shootAnimation.start()

        // Play gun shooting sound effect
        val gunSound = MediaPlayer.create(context, R.raw.battle_riffle_sound).start()

        val ray = scene.camera.screenPointToRay(point.x / 2f, point.y / 2f)

        thread {
            activity?.runOnUiThread {
                val nodeInContact = scene.hitTest(ray, false)

                if (nodeInContact.node!!.name == "Zombie") {
                    scene.removeChild(nodeInContact.node)

                    zombiesAlive --
                    zombiesKilled ++
                    kills.text = zombiesKilled.toString()
                }
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            shootAnimation.cancel()
        }, 100)
    }

    private fun addGunInfo() {
        val imageView = ImageView(context)

        val imageParams = FrameLayout.LayoutParams(400, 200)
        imageParams.gravity = Gravity.BOTTOM or Gravity.LEFT
        imageParams.leftMargin = 100

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
            }
            .exceptionally {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                return@exceptionally null
            }
    }

    private fun zombieGenerator(frameTime: FrameTime) {
        val frame = arFragment.arSceneView.arFrame!!
        val planes = frame.getUpdatedTrackables(Plane::class.java)

        while (planesDetected.size < 50) {
            for (plane in planes) {
                if (plane.trackingState == TrackingState.TRACKING) {
                    planesDetected.add(plane)
                }
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val chronometer = Chronometer(context)
            chronometer.base = SystemClock.elapsedRealtime()

            chronometer.start()
            timeSurvived = SystemClock.elapsedRealtime() - chronometer.base / 1000

            if (timeSurvived % 50 == 0L) {
                val plane = planesDetected[Random.nextInt(planesDetected.size)]
                val anchor = plane.createAnchor(plane.centerPose)

                attachZombieToPlane(anchor)
            }
        }, 10000)
    }

    private fun attachZombieToPlane(anchor: Anchor?) {
        val zombie = AnchorNode(anchor)

        // Resize model
        zombie.localScale = Vector3(0.01f, 0.01f, 0.01f)

        val cameraPosition = scene.camera.worldPosition
        val anchorNodePosition = zombie.worldPosition
        val direction = Vector3.subtract(cameraPosition, anchorNodePosition)
        zombie.localRotation = Quaternion.lookRotation(direction, Vector3.up())

        // Change node name
        zombie.name = "Zombie"

        zombie.renderable = zombieRenderable
        scene.addChild(zombie)

        ModelAnimator.ofAnimationFrame(zombie.renderableInstance, "Zombie@Z_Walk_InPlace", 100).start()

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
        scene.camera.addChild(gun)

        gunRenderableInstance = gun.renderableInstance!!
    }
}