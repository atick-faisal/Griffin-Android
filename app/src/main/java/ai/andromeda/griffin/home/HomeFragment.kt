package ai.andromeda.griffin.home

import ai.andromeda.griffin.R
import ai.andromeda.griffin.database.DeviceEntity
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_home.view.*

class HomeFragment : Fragment() {

    private lateinit var deviceListAdapter: DeviceAdapter
    private lateinit var homeViewModel: HomeViewModel

    //-------------- ON_CREATE_VIEW() ------------------//
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        val application = requireNotNull(this.activity).application

        //----------------- VIEW MODEL SETUP -------------------//
        val viewModelFactory = HomeViewModelFactory(application)
        homeViewModel = ViewModelProvider(this, viewModelFactory)
            .get(HomeViewModel::class.java)

        //------------------ RECYCLER VIEW SETUP ---------------------//
        deviceListAdapter = DeviceAdapter { navigateToDetails(it) }
        rootView.deviceList.adapter = deviceListAdapter
        rootView.deviceList.layoutManager = GridLayoutManager(activity, 2)

        //------------------- LIVE DATA OBSERVERS ------------------------//
        homeViewModel.deviceList.observe(viewLifecycleOwner, Observer {
            it?.let {
                deviceListAdapter.deviceList = it
            }
        })

        //------------------------- ON CLICK LISTENERS -----------------------//
        rootView.fab.setOnClickListener { navigateToRegister() }

        //--------------------- MENU -----------------//
        setHasOptionsMenu(true)
        (context as AppCompatActivity).supportActionBar?.title = getString(R.string.dashboard)

        return rootView
    }

    //----------------- NAVIGATE TO REGISTER -------------------//
    private fun navigateToRegister() {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToRegisterFragment()
        )
    }

    //------------------ NAVIGATE TO SCANNER -----------------//
    private fun navigateToScanner() {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToScannerFragment()
        )
    }

    //------------------- NAVIGATE TO DETAILS ---------------//
    private fun navigateToDetails(device: DeviceEntity) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToDeviceDetailsFragment(
                deviceId = device.deviceId,
                deviceName = device.deviceName
            )
        )
    }

    //------------------- MENU LISTENER --------------------//
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard_menu, menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.scannerFragment -> {
                navigateToScanner()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}