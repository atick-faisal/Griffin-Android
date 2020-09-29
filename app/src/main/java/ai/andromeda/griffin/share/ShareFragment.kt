package ai.andromeda.griffin.share

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.andromeda.griffin.R
import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.database.DeviceDatabase
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_share.view.*

class ShareFragment : Fragment() {

    private lateinit var deviceDatabase: DeviceDatabase
    private lateinit var shareViewModel: ShareViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_share, container, false)

        val deviceId = ShareFragmentArgs.fromBundle(requireArguments())
            .deviceId ?: ""

        val deviceName = ShareFragmentArgs.fromBundle(requireArguments())
            .deviceName ?: getString(R.string.unknown_device)

        val application = requireNotNull(this.activity).application
        deviceDatabase = DeviceDatabase.getInstance(application)

        val shareViewModelFactory = ShareViewModelFactory(
            deviceDatabase,
            application,
            deviceId
        )
        shareViewModel = ViewModelProvider(this, shareViewModelFactory)
            .get(ShareViewModel::class.java)

        shareViewModel.device.observe(viewLifecycleOwner, Observer {
            it?.let {
                Log.i(LOG_TAG, "DEVICE FOUND")
                shareViewModel.generateBitmap(it)
            }
        })

        shareViewModel.qrBitmap.observe(viewLifecycleOwner, Observer {
            it?.let {
                Log.i(LOG_TAG, "BITMAP GENERATED")
                rootView.qrCodeImage.setImageBitmap(it)
            }
        })

        rootView.doneSharingButton.setOnClickListener {
            navigateToDevice()
        }

        setHasOptionsMenu(true)
        (context as AppCompatActivity).supportActionBar?.title =
            getString(R.string.share)

        rootView.deviceNameText.text = deviceName

        return rootView
    }

    private fun navigateToDevice() {
        findNavController().navigate(
            ShareFragmentDirections.actionShareFragmentToDeviceDetailsFragment()
        )
    }
}