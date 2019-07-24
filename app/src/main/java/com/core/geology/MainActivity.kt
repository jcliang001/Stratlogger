package com.core.geology


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.annotation.TargetApi
import android.os.Build



class MainActivity : AppCompatActivity() {
    private var fileName = ""
    private val REQUEST_EXTERNAL_STORAGE = 1
    private val REQUEST_CODE_DATA_COLLECTION = 2
    private val PERMISSIONS_STORAGE =
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(LOG_TAG, "onCreated() called")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (shouldAskPermissions()) {
            askPermissions();
        }
        btn_submit.setOnClickListener {

            fileName = id_input_text.getText().toString()
            if(TextUtils.isEmpty(fileName)){
                id_input_text.error = "Enter file name"
                return@setOnClickListener
            }
            val i = DataCollection.createIntent(this)
            i.putExtra(FILE_NAME_EXTRA, fileName)
            startActivityForResult(i, REQUEST_CODE_DATA_COLLECTION)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_CODE_DATA_COLLECTION){
            if(resultCode == Activity.RESULT_CANCELED){
                id_input_text.setText("")
            }
        }
    }

    companion object{
        private const val LOG_TAG = "Geo.MainActivity"
        public val FILE_NAME_EXTRA = "FILE_NAME_EXTRA"
    }

    protected fun shouldAskPermissions(): Boolean {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1
    }

    @TargetApi(23)
    protected fun askPermissions() {
        val permissions =
            arrayOf("android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE")
        val requestCode = 200
        requestPermissions(permissions, requestCode)
    }

    fun verifyStoragePermissions(activity: Activity) {
        // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                activity,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
            )
        }
    }


}
