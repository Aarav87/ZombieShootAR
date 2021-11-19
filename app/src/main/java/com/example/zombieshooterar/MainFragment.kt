package com.example.zombieshooterar

import android.graphics.Point
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.ar.core.*
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.RenderableInstance
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.fragment_main.*
import kotlin.concurrent.thread

open class MainFragment: Fragment(R.layout.fragment_main) {

    private lateinit var arFragment: ArFragment
    private lateinit var scene: Scene
    private lateinit var chronometer: Chronometer

    // Detected Planes
    private var planesDetected: MutableList<Plane> = mutableListOf()

    var zombiesAlive = 0
    var zombiesKilled = 0

    companion object {
        // Model Renderable
        lateinit var gunRenderable: ModelRenderable
        lateinit var zombieRenderable: ModelRenderable

        // Renderable Instance
        var gunRenderableInstance: RenderableInstance? = null

        // Ammo
        var ammoInClip = 30
        var ammoTotal = 300
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get height & width of users screen
        val display = activity?.windowManager?. defaultDisplay
        val point = Point()
        display?.getRealSize(point)

        chronometer = Chronometer(context)
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()

        // Initialize ArFragment
        arFragment = childFragmentManager.findFragmentById(R.id.scene_form_fragment) as ArFragment
        scene = arFragment.arSceneView.scene

        // Hide plane discovery controller
        arFragment.instructionsController.isEnabled = false

        // Disable plane renderer
        arFragment.arSceneView.planeRenderer.isVisible = false

        RenderModels(arFragment, scene, context).loadModels()
        addGunInfo()

        shoot.setOnTouchListener(RepeatListener(400, 150) {
            if (ammoTotal == 0 && ammoInClip == 0) {
                noAmmo()
            } else if (ammoInClip == 0) {
                onReload()
            } else {
                onShoot(point)
            }

        })

        scene.addOnUpdateListener(this::detectPlanes)
    }

    private fun noAmmo() {
        // Play empty gun sound effect
        val emptyGunSound = MediaPlayer.create(context, R.raw.empty_gun_sound).start()

        // Change color of ammo text
        clipAmmo.setTextColor(resources.getColor(R.color.red))
        totalAmmo.setTextColor(resources.getColor(R.color.red))
    }

    private fun addGunInfo() {
        clipAmmo.text = "$ammoInClip"
        totalAmmo.text = "$ammoTotal"
        gunIcon.setImageResource(R.drawable.battlerifle)
    }

    private fun onReload() {
        // Disable shoot button
        shoot.isEnabled = false

        // Start gun reload animation
        val reloadAnimation = ModelAnimator.ofAnimationTime(gunRenderableInstance, "CINEMA_4D_Main", 5F)
        reloadAnimation.start()

        // Play gun reloading sound effect
        val reloadSound = MediaPlayer.create(context, R.raw.reload_sound).start()

        // Reset ammo in clip
        ammoInClip = 30
        clipAmmo.text = "$ammoInClip"

        // Reduce total ammo
        ammoTotal -= 30
        totalAmmo.text = "$ammoTotal"

        // Stop reload animation
        Handler(Looper.getMainLooper()).postDelayed({
            reloadAnimation.cancel()
            shoot.isEnabled = true
        }, 5000)
    }

    private fun onShoot(point: Point) {
        // Start gun shooting animation
        val shootAnimation = ModelAnimator.ofAnimationTime(gunRenderableInstance, "CINEMA_4D_Main", 10F)
        shootAnimation.start()

        // Play gun shooting sound effect
        val shootSound = MediaPlayer.create(context, R.raw.shoot_sound).start()

        val ray = scene.camera.screenPointToRay(point.x / 2f, point.y / 2f)

        ammoInClip --
        clipAmmo.text = "$ammoInClip"

        thread {
            activity?.runOnUiThread {
                val nodeInContact = scene.hitTest(ray, true)

                if (nodeInContact.node?.name ?: String == "Zombie") {
                    scene.removeChild(nodeInContact.node?.parent)

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

    private fun detectPlanes(frameTime: FrameTime) {
        val frame = arFragment.arSceneView.arFrame!!
        val planes = frame.getUpdatedTrackables(Plane::class.java)

        for (plane in planes) {
            if (plane.trackingState == TrackingState.TRACKING) {
                planesDetected.add(plane)
            }
        }

        ZombieGenerator(arFragment, planesDetected, scene, chronometer).generateZombies()

    }
}