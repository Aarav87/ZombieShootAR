package com.example.zombieshooterar

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment

class RenderModels (
    private val arFragment: ArFragment,
    private val scene: Scene,
    private val contxt: Context?
): MainFragment() {

    fun loadModels() {
        // Render gun model
        ModelRenderable.builder()
            .setSource(
                contxt,
                Uri.parse("models/guns/battle_rifle.glb")
            )
            .setIsFilamentGltf(true)
            .build()
            .thenAccept {
                gunRenderable = it
                Gun(arFragment, scene).attachGunModelToCamera()
            }
            .exceptionally {
                Toast.makeText(contxt, "Error", Toast.LENGTH_SHORT).show()
                return@exceptionally null
            }

        // Render zombie model
        ModelRenderable.builder()
            .setSource(
                contxt,
                Uri.parse("models/zombies/zombie.glb")
            )
            .setIsFilamentGltf(true)
            .build()
            .thenAccept {
                zombieRenderable = it
            }
            .exceptionally {
                Toast.makeText(contxt, "Error", Toast.LENGTH_SHORT).show()
                return@exceptionally null
            }
    }
}