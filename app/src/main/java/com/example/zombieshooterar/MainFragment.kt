package com.example.zombieshooterar

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.ar.sceneform.ux.ArFragment

class MainFragment : Fragment(R.layout.fragment_main) {

    // Initialize ArFragment
    lateinit var arFragment: ArFragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arFragment = childFragmentManager.findFragmentById(R.id.scene_form_fragment) as ArFragment

        // Hide plane discovery controller
        arFragment.planeDiscoveryController.hide()
        arFragment.planeDiscoveryController.setInstructionView(null)

        // Disable plane renderer
        arFragment.arSceneView.planeRenderer.isEnabled = false
    }
}