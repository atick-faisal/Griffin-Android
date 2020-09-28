package ai.andromeda.griffin.device

import ai.andromeda.griffin.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_edit_name.view.*

class EditNameFragment : DialogFragment() {

    // private lateinit var editNameViewModel: EditNameViewModel
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_edit_name, container, false)

        val application = requireNotNull(this.activity).application

        val deviceId = EditNameFragmentArgs.fromBundle(requireArguments())
            .deviceId!!

        val position = EditNameFragmentArgs.fromBundle(requireArguments())
            .position


//        val editNameViewModelFactory = EditNameViewModelFactory(
//            application, deviceId, position
//        )

//        editNameViewModel = ViewModelProvider(this, editNameViewModelFactory)
//            .get(EditNameViewModel::class.java)

        val sharedViewModelFactory = SharedViewModelFactory(application)
        sharedViewModel = ViewModelProvider(requireActivity(), sharedViewModelFactory)
            .get(SharedViewModel::class.java)

        rootView.saveNameButton.setOnClickListener {
            val name = rootView.nameTextInput.text.toString()
            sharedViewModel.changeSensorName(deviceId, position, name)
            dismiss()
        }

        return rootView
    }

}