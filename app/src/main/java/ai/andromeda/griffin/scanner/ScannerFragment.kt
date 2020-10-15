package ai.andromeda.griffin.scanner

import ai.andromeda.griffin.R
import ai.andromeda.griffin.config.Config.LOG_TAG
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.android.synthetic.main.fragment_scanner.view.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias QrListener = (result: String?) -> Unit

class ScannerFragment : Fragment() {

    private lateinit var scanner: BarcodeScanner
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var scannerViewModel: ScannerViewModel
    private lateinit var rootView: View

    private var qrRecognized: Boolean = false

    //--------------- CONSTANTS -------------------//
    companion object {
        const val REQUEST_CODE_PERMISSIONS = 1001
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    //------------------ ON CREATE VIEW -----------------//
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_scanner, container, false)
        val application = requireActivity().application

        //------------------ VIEW MODEL SETUP ------------------//
        val scannerViewModelFactory = ScannerViewModelFactory(application)
        scannerViewModel = ViewModelProvider(this, scannerViewModelFactory)
            .get(ScannerViewModel::class.java)

        //---------------- LIVE DATA OBSERVERS ----------------//
        scannerViewModel.deviceName.observe(viewLifecycleOwner, Observer {
            it?.let {
                rootView.deviceNameText.text = it
                rootView.tryAgainButton.isEnabled = true
                if (it != "Device Not Recognized") {
                    rootView.addDeviceButton.isEnabled = true
                }
            }
        })

        //---------------- ON CLICK LISTENERS -------------------//
        rootView.addDeviceButton.setOnClickListener { addDevice() }
        rootView.tryAgainButton.setOnClickListener { tryAgain() }

        //--------------- CAMERA SETUP ---------------//
        // Scanner Options
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        // Initialize the scanner
        scanner = BarcodeScanning.getClient(options)

        // Check for Camera permission
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
        cameraExecutor = Executors.newSingleThreadExecutor()

        //---------------------- MENU -------------------//
        (context as AppCompatActivity).supportActionBar?.title =
            getString(R.string.qr_scanner)

        return rootView
    }

    //------------ ADD DEVICE ----------//
    private fun addDevice() {
        scannerViewModel.saveData()
        findNavController().navigate(
            ScannerFragmentDirections.actionScannerFragmentToHomeFragment()
        )
    }

    //-------------- RETRY -------------//
    private fun tryAgain() {
        rootView.tryAgainButton.isEnabled = false
        rootView.deviceNameText.text = getString(R.string.scanning)
        scannerViewModel.onTryAgain()
        restartCamera()
    }

    //-------------------- START CAMERA --------------------//
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(rootView.viewFinder.createSurfaceProvider())
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, CodeScanner { result ->
                        Log.i(LOG_TAG, "FINAL RESULT: $result")
                        result?.let {
                            scannerViewModel.parseData(result)
                        }
                    })
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )

            } catch (exc: Exception) {
                Log.e(LOG_TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    //------------------ ASK FOR CAMERA PERMISSION ----------------------//
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    requireNotNull(activity).application,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                activity?.finish()
            }
        }
    }

    //------------------- PROCESS QR CODE ------------------//
    inner class CodeScanner(private val listener: QrListener) :
        ImageAnalysis.Analyzer {
        @SuppressLint("UnsafeExperimentalUsageError")
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(
                    mediaImage, imageProxy.imageInfo.rotationDegrees
                )
                val task = scanner.process(image)
                    .addOnSuccessListener { barcode ->
                        if (barcode.isNotEmpty()) {
                            if (!qrRecognized) {
                                qrRecognized = true
                                listener(barcode[0].rawValue)
                            }
                        }
                    }
                task.addOnSuccessListener {
                    imageProxy.close()
                }
            }
        }
    }

    //------------ RESTART CAMERA ----------//
    private fun restartCamera() {
        qrRecognized = false
    }

    //----------- ON DESTROY ----------//
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}