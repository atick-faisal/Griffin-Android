package ai.andromeda.griffin.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.andromeda.griffin.R
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_sign_up.view.*

class SignUpFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_sign_up, container, false)
        rootView.signUpButton.setOnClickListener { navigateToHome() }

        setHasOptionsMenu(true)

        (context as AppCompatActivity).supportActionBar?.title =
            getString(R.string.sign_up)

        return rootView
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.action_signUpFragment_to_homeFragment)
    }
}