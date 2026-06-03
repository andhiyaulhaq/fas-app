@file:Suppress("DEPRECATION")

package com.mv.livebodyexample

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.Rect
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.mv.livebodyexample.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import java.io.IOException

@ObsoleteCoroutinesApi
class MainActivity : AppCompatActivity(), SetThresholdDialogFragment.ThresholdDialogListener {

    private lateinit var binding: ActivityMainBinding

    private var enginePrepared: Boolean = false
    private lateinit var engineWrapper: EngineWrapper
    private var threshold: Float = defaultThreshold

    private var camera: Camera? = null
    private var cameraId: Int = Camera.CameraInfo.CAMERA_FACING_FRONT
    private var previewWidth: Int = 640
    private var previewHeight: Int = 480

    /**
     *    1       2       3       4        5          6          7            8
     * <p>
     * 888888  888888      88  88      8888888888  88                  88  8888888888
     * 88          88      88  88      88  88      88  88          88  88      88  88
     * 8888      8888    8888  8888    88          8888888888  8888888888          88
     * 88          88      88  88
     * 88          88  888888  888888
     */
    private val frameOrientation: Int = 7

    private var screenWidth: Int = 0
    private var screenHeight: Int = 0

    private var factorX: Float = 0F
    private var factorY: Float = 0F
    private var offsetX: Float = 0F
    private var offsetY: Float = 0F

    private val detectionContext = newSingleThreadContext("detection")
    private var working: Boolean = false

    private lateinit var scaleAnimator: ObjectAnimator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (hasPermissions()) {
            init()
        } else {
            requestPermission()
        }

        hideSystemUI()
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }
    }

    private fun hasPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun requestPermission() = requestPermissions(permissions, permissionReqCode)

    private fun init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.result = DetectionResult()

        calculateSize()

        binding.surface.holder.let {
            it.addCallback(object : SurfaceHolder.Callback, Camera.PreviewCallback {
                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {
                    if (holder.surface == null) return

                    if (camera == null) return

                    try {
                        camera?.stopPreview()
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }

                    val parameters = camera?.parameters
                    val optimalSize = getOptimalPreviewSize(parameters?.supportedPreviewSizes, screenWidth, screenHeight)
                    
                    optimalSize?.let {
                        previewWidth = it.width
                        previewHeight = it.height
                        parameters?.setPreviewSize(previewWidth, previewHeight)
                    }
                    
                    parameters?.previewFormat = ImageFormat.NV21

                    // Important: In portrait, the camera frame is rotated. 
                    // So we use Height as Width and Width as Height for ratio math.
                    val cameraRatio = previewHeight.toFloat() / previewWidth.toFloat()
                    val screenRatio = screenWidth.toFloat() / screenHeight.toFloat()

                    var finalWidth: Int
                    var finalHeight: Int

                    // Center Crop Strategy: Ensure the preview fills the screen without stretching
                    if (cameraRatio > screenRatio) {
                        finalHeight = screenHeight
                        finalWidth = (screenHeight * cameraRatio).toInt()
                    } else {
                        finalWidth = screenWidth
                        finalHeight = (screenWidth / cameraRatio).toInt()
                    }

                    // Apply dimensions using a post to ensure layout pass completion
                    binding.surface.post {
                        val params = binding.surface.layoutParams as FrameLayout.LayoutParams
                        params.width = finalWidth
                        params.height = finalHeight
                        params.gravity = android.view.Gravity.CENTER
                        binding.surface.layoutParams = params
                        
                        // Sync the hardware buffer
                        holder.setFixedSize(finalWidth, finalHeight)
                    }

                    // Update factors for coordinate mapping
                    factorX = finalWidth / previewHeight.toFloat()
                    factorY = finalHeight / previewWidth.toFloat()
                    offsetX = (finalWidth - screenWidth) / 2f
                    offsetY = (finalHeight - screenHeight) / 2f

                    camera?.parameters = parameters
                    camera?.startPreview()
                    camera?.setPreviewCallback(this)

                    setCameraDisplayOrientation()
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    camera?.setPreviewCallback(null)
                    camera?.release()
                    camera = null
                }

                override fun surfaceCreated(holder: SurfaceHolder) {
                    try {
                        camera = Camera.open(cameraId)
                    } catch (e: Exception) {
                        cameraId = Camera.CameraInfo.CAMERA_FACING_BACK
                        camera = Camera.open(cameraId)
                    }

                    try {
                        camera!!.setPreviewDisplay(binding.surface.holder)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }

                override fun onPreviewFrame(data: ByteArray, camera: Camera) {
                    if (enginePrepared) {
                        if (!working) {
                            GlobalScope.launch(detectionContext) {
                                working = true
                                val result = engineWrapper.detect(
                                    data,
                                    previewWidth,
                                    previewHeight,
                                    frameOrientation
                                )
                                result.threshold = threshold

                                val rect = calculateBoxLocationOnScreen(
                                    result.left,
                                    result.top,
                                    result.right,
                                    result.bottom
                                )

                                binding.result = result.updateLocation(rect)

                                Log.d(
                                    tag,
                                    "threshold:${result.threshold}, confidence: ${result.confidence}"
                                )

                                binding.rectView.postInvalidate()
                                working = false
                            }
                        }
                    }
                }
            })
        }

        scaleAnimator = ObjectAnimator.ofFloat(binding.scan, View.SCALE_Y, 1F, -1F, 1F).apply {
            this.duration = 3000
            this.repeatCount = ValueAnimator.INFINITE
            this.repeatMode = ValueAnimator.REVERSE
            this.interpolator = LinearInterpolator()
            this.start()
        }

    }

    private fun calculateSize() {
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(dm)
        screenWidth = dm.widthPixels
        screenHeight = dm.heightPixels
    }

    private fun calculateBoxLocationOnScreen(left: Int, top: Int, right: Int, bottom: Int): Rect =
        Rect(
            (left * factorX - offsetX).toInt(),
            (top * factorY - offsetY).toInt(),
            (right * factorX - offsetX).toInt(),
            (bottom * factorY - offsetY).toInt()
        )

    private fun getOptimalPreviewSize(sizes: List<Camera.Size>?, w: Int, h: Int): Camera.Size? {
        if (sizes == null) return null
        
        val targetRatio = h.toDouble() / w.toDouble() // Portrait ratio
        var optimalSize: Camera.Size? = null
        var minDiff = Double.MAX_VALUE

        // Sort by area to prefer moderate resolutions (prevents 4K lag)
        val sortedSizes = sizes.sortedByDescending { it.width * it.height }

        for (size in sortedSizes) {
            // Prefer sizes that aren't excessively large (e.g. max height 1280)
            if (size.width > 1280 && sortedSizes.size > 5) continue
            
            val ratio = size.width.toDouble() / size.height.toDouble()
            if (Math.abs(ratio - targetRatio) < minDiff) {
                optimalSize = size
                minDiff = Math.abs(ratio - targetRatio)
            }
        }
        return optimalSize ?: sizes[0]
    }

    private fun setCameraDisplayOrientation() {
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(cameraId, info)
        val rotation = windowManager.defaultDisplay.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        var result: Int
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360 // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360
        }
        camera!!.setDisplayOrientation(result)
    }

    fun setting(@Suppress("UNUSED_PARAMETER") view: View) =
        SetThresholdDialogFragment().show(supportFragmentManager, "dialog")

    override fun onDialogPositiveClick(t: Float) {
        threshold = t
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionReqCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                init()
            } else {
                Toast.makeText(this, "Please grant camera permission", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        hideSystemUI()
        engineWrapper = EngineWrapper(assets)
        enginePrepared = engineWrapper.init()

        if (!enginePrepared) {
            Toast.makeText(this, "Engine init failed.", Toast.LENGTH_LONG).show()
        }

        super.onResume()
    }

    override fun onDestroy() {
        engineWrapper.destroy()
        scaleAnimator.cancel()
        super.onDestroy()
    }

    companion object {
        const val tag = "MainActivity"
        const val defaultThreshold = 0.915F

        val permissions: Array<String> = arrayOf(Manifest.permission.CAMERA)
        const val permissionReqCode = 1
    }

}
