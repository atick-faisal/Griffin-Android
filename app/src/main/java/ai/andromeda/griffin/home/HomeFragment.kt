package ai.andromeda.griffin.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.andromeda.griffin.R
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_home.view.*

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        rootView.fab.setOnClickListener { navigateToRegister() }
        return rootView
    }

    private fun navigateToRegister() {
        findNavController().navigate(R.id.action_homeFragment_to_registerFragment)
    }
}