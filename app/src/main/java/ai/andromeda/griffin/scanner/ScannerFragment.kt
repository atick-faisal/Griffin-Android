package ai.andromeda.griffin.scanner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.andromeda.griffin.R
import ai.andromeda.griffin.config.Config.LOG_TAG
import ai.andromeda.griffin.database.DeviceDatabase
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    private lateinit var rootView: View
    private lateinit var deviceDatabase: DeviceDatabase
    private lateinit var scannerViewModel: ScannerViewModel

    private var qrRecognized: Boolean = false

    companion object {
        const val REQUEST_CODE_PERMISSIONS = 1001
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_scanner, container, false)

        val application = requireActivity().application
        deviceDatabase = DeviceDatabase.getInstance(application)

        val scannerViewModelFactory = ScannerViewModelFactory(application, deviceDatabase)
        scannerViewModel = ViewModelProvider(this, scannerViewModelFactory)
            .get(ScannerViewModel::class.java)

        (context as AppCompatActivity).supportActionBar?.title =
            getString(R.string.qr_scanner)

        scannerViewModel.deviceName.observe(viewLifecycleOwner, Observer {
            it?.let {
                rootView.deviceNameText.text = it
                rootView.tryAgainButton.isEnabled = true
                if (it != "Device Not Recognized") {
                    rootView.addDeviceButton.isEnabled = true
                }
            }
        })

        rootView.addDeviceButton.setOnClickListener {
            scannerViewModel.saveData()
            findNavController().navigate(
                ScannerFragmentDirections.actionScannerFragmentToHomeFragment()
            )
        }

        rootView.tryAgainButton.setOnClickListener {
            rootView.tryAgainButton.isEnabled = false
            rootView.deviceNameText.text = getString(R.string.scanning)
            scannerViewModel.onTryAgain()
            restartCamera()
        }

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

        return rootView
    }

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
                    this, cameraSelector, preview, imageAnalyzer)

            } catch(exc: Exception) {
                Log.e(LOG_TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    //////////////////////////////////////////////////////////////////
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }
    //////////////////////////////////////////////////////////////////
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    requireNotNull(activity).application,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                activity?.finish()
            }
        }
    }

    /////////////////////////////////////////////////////////////////
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

    private fun restartCamera() {
        qrRecognized = false
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}