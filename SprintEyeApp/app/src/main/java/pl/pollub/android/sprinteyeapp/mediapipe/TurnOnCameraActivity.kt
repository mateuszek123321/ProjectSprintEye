package pl.pollub.android.sprinteyeapp.mediapipe

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import pl.pollub.android.sprinteyeapp.databinding.ActivityAdvancedSessionBinding
import pl.pollub.android.sprinteyeapp.databinding.ActivityBasicSessionBinding


class TurnOnCameraActivity : AppCompatActivity() {
    companion object{
        const val EXTRA_SESSION_MODE = "extra_session_mode"
        const val SESSION_MODE_BASIC = "basic"
        const val SESSION_MODE_ADVANCED = "advanced"
    }

    private val requiredPermissions = buildList{
        add(android.Manifest.permission.CAMERA)
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){
            add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }.toTypedArray()

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val permissionGranted = result.all { (_, isGranted) -> isGranted }
            if (permissionGranted) {
                openAdjustCamera()
            } else {
                Toast.makeText(this, "Brak pozwoleń", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermission()
    }

    private fun checkPermission(){
        if(allPermissionsGranted()){
            openAdjustCamera()
        }else{
            permissionLauncher.launch(requiredPermissions)
        }
    }

    private fun allPermissionsGranted(): Boolean {
        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun openAdjustCamera(){
        val sessionModeName = intent.getStringExtra(EXTRA_SESSION_MODE)
        val adjustIntent = Intent(this, AdjustCameraActivity::class.java).apply {
            sessionModeName?.let { putExtra(EXTRA_SESSION_MODE, it) }
        }
        startActivity(adjustIntent)
        finish()
    }
}