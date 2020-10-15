package ai.andromeda.griffin.share

import ai.andromeda.griffin.R
import ai.andromeda.griffin.config.Config.LOG_TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_share.view.*

class ShareFragment : Fragment() {

    private lateinit var shareViewModel: ShareViewModel
    private lateinit var deviceId: String
    private lateinit var deviceName: String

    //------------------ ON_CREATE_VIEW() ------------------//
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_share, container, false)
        val application = requireActivity().application

        //--------------------- ARGUMENTS -----------------------//
        deviceId = ShareFragmentArgs.fromBundle(requireArguments()).deviceId ?: ""
        deviceName = ShareFragmentArgs.fromBundle(requireArguments()).deviceName
            ?: getString(R.string.unknown_device)

        //----------------------- VIEW MODEL SETUP -----------------------//
        val shareViewModelFactory = ShareViewModelFactory(application, deviceId)
        shareViewModel = ViewModelProvider(this, shareViewModelFactory)
            .get(ShareViewModel::class.java)

        //------------------------ LIVE DATA OBSERVERS ----------------------//
        shareViewModel.device.observe(viewLifecycleOwner, Observer {
            it?.let {
                Log.i(LOG_TAG, "SHARE_F: DEVICE FOUND")
                shareViewModel.generateBitmap(it)
            }
        })

        shareViewModel.qrBitmap.observe(viewLifecycleOwner, Observer {
            it?.let {
                Log.i(LOG_TAG, "SHARE_F: BITMAP GENERATED")
                rootView.qrCodeImage.setImageBitmap(it)
            }
        })

        //------------------- CLICK LISTENERS ----------------//
        rootView.doneSharingButton.setOnClickListener { navigateToDevice() }

        //-------------------- MENU ------------------//
        setHasOptionsMenu(true)
        (context as AppCompatActivity).supportActionBar?.title =
            getString(R.string.share)

        rootView.deviceNameText.text = deviceName
        return rootView
    }

    //---------------- NAVIGATE TO DEVICES -------------//
    private fun navigateToDevice() {
        findNavController().navigate(
            ShareFragmentDirections.actionShareFragmentToDeviceDetailsFragment(
                deviceId, deviceName
            )
        )
    }
}