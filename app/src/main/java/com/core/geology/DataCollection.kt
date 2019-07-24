package com.core.geology

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.data_collection.*
import java.io.*

class DataCollection : AppCompatActivity() {
    private var file_name = ""
    private var section_name = ""
    private var type = ""
    private var bed_id = ""
    private var type_num = 0
    private var note = ""
    private var thickness = ""
    private var picture_name = ""
    private val inputFileHeader = "SectionName,BedNum,lith,lithName,thickness,BedID,Note,PictureName" + "\n"
    private var myExternalFile: File?=null
    private val filepath = "MyFileStorage"
    private var isFileExist = false
    private var index = 1
    val REQUEST_IMAGE_CAPTURE = 1
    private val PERMISSION_REQUEST_CODE: Int = 101
    private var mCurrentPhotoPath: String? = null;
    private var takePic = false
    private var skipSave = false
    var mutableListRun  = arrayListOf<Run>()

    companion object {
        private const val LOG_TAG = "Geo.DataCollection"
        fun createIntent(ctx: Context?) = Intent(ctx, DataCollection::class.java)
        //vector
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(LOG_TAG, "onCreate called")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.data_collection)

        file_name = intent.getStringExtra(MainActivity.FILE_NAME_EXTRA)
        file_name = file_name + ".csv"
        id_file_display.text = file_name
        myExternalFile = File(getExternalFilesDir(filepath), file_name)
        val file = File(getExternalFilesDir(filepath).toString() + "/" + file_name)
        if(file.exists() && mutableListRun.size == 0){
            isFileExist = true
            var fileInputStream: FileInputStream? = null
            fileInputStream = FileInputStream(myExternalFile)
            var inputStreamReader: InputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader: BufferedReader = BufferedReader(inputStreamReader)
            var text: String? = null
            var arrIndex = 0
            while ({ text = bufferedReader.readLine(); text }() != null) {
                if(arrIndex==0){
                    arrIndex++
                    continue
                }
                val arr = text.toString().split(",")
                val runObject = Run(arr[0], arr[1].toInt(),arr[2],arr[3],arr[4], arr[5],arr[6],arr[7])
                mutableListRun.add(runObject)
            }
        }

        index = mutableListRun.size + 1
        id_layer_count.text = index.toString()

        //sand button
        btn_sand.setOnClickListener {
            btn_mud.setBackgroundColor(Color.WHITE)
            btn_sand.setBackgroundColor(Color.YELLOW)
            btn_other.setBackgroundColor(Color.WHITE)
            type = "sand"
            type_num = 1

        }
        //mud button
        btn_mud.setOnClickListener {
            btn_sand.setBackgroundColor(Color.WHITE)
            btn_mud.setBackgroundColor(Color.GRAY)
            btn_other.setBackgroundColor(Color.WHITE)
            type = "mud"
            type_num = 0


        }
        //other button
        btn_other.setOnClickListener {
            btn_sand.setBackgroundColor(Color.WHITE)
            btn_mud.setBackgroundColor(Color.WHITE)
            btn_other.setBackgroundColor(Color.RED)
            type = "other"
            type_num = 2

        }

        btn_camera.setOnClickListener(View.OnClickListener {
            if (checkPersmission()) takePicture() else requestPermission()
        })



        //save button
        btn_save.setOnClickListener {
            thickness = id_thickness.getText().toString()
            val sandColorDrawable = btn_sand.background as ColorDrawable
            var sandColor = sandColorDrawable.color
            val mudColorDrawable = btn_mud.background as ColorDrawable
            var mudColor = mudColorDrawable.color
            val otherColorDrawable = btn_other.background as ColorDrawable
            var otherColor = otherColorDrawable.color
            if(!thickness.isEmpty()){
                id_thickness.setError(null)
            }else{
                id_thickness.error = "Enter the thickness"
                return@setOnClickListener

            }

            if(sandColor == Color.WHITE && mudColor == Color.WHITE && otherColor == Color.WHITE){
                Toast.makeText(this, "Select Sand, Other or Mud", Toast.LENGTH_SHORT).show()
                return@setOnClickListener

            } else {
                index = mutableListRun.size + 1
                bed_id = id_bed.getText().toString()
                note = id_note.getText().toString()
                if(takePic) {
                    picture_name = file_name + "_" + index.toString()
                }else{
                    picture_name = ""
                }
                val run_object =
                    Run(file_name, index, type, type_num.toString(), thickness, bed_id, note, picture_name)
                mutableListRun.add(run_object)
                resetData()
                checkInputFile(run_object)
                Toast.makeText(this, "Data have been saved.", Toast.LENGTH_SHORT).show()

            }
        }

        //end of run button
        btn_end.setOnClickListener {
            thickness = id_thickness.getText().toString()
            val sandColorDrawable = btn_sand.background as ColorDrawable
            var sandColor = sandColorDrawable.color
            val mudColorDrawable = btn_mud.background as ColorDrawable
            var mudColor = mudColorDrawable.color
            val otherColorDrawable = btn_other.background as ColorDrawable
            var otherColor = otherColorDrawable.color
            if((thickness.isNotEmpty() || sandColor != Color.WHITE || mudColor != Color.WHITE || otherColor != Color.WHITE) && !skipSave){
                Toast.makeText(this, "Are you going to end the run without saving your data? Pressing End of Run again will navigate you to main page.", Toast.LENGTH_SHORT).show()
                skipSave = true
                return@setOnClickListener
            }
            Toast.makeText(this, "End of run.", Toast.LENGTH_SHORT).show()
            val returnIntent = Intent()
            setResult(Activity.RESULT_CANCELED, returnIntent)
            this.finish()
        }

        if (!isExternalStorageAvailable || isExternalStorageReadOnly) {
            Toast.makeText(application, "Your memory is almost full. Please clean up your memory then store your files.", Toast.LENGTH_SHORT).show()
            btn_save.isEnabled = false
        }

    }

    private fun resetData(){
        btn_sand.setBackgroundColor(Color.WHITE)
        btn_mud.setBackgroundColor(Color.WHITE)
        btn_other.setBackgroundColor(Color.WHITE)
        id_note.setText("")
        id_bed.setText("")
        id_thickness.setText("")
        takePic = false
        val idx = mutableListRun.size + 1
        id_layer_count.text = idx.toString()
    }

    private fun checkInputFile(run_object : Run){
        myExternalFile = File(getExternalFilesDir(filepath), run_object.file_name)
        val file = File(getExternalFilesDir(filepath).toString() + "/" + run_object.file_name)
        val exist = file.exists()
        try {
            val str = run_object.FiletoString() + "\n"
            val fileOutPutStream = FileOutputStream(myExternalFile, exist)
            if(!exist){
                fileOutPutStream.write(inputFileHeader.toByteArray())
            }

            fileOutPutStream.write(str.toByteArray())
            fileOutPutStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        //Toast.makeText(applicationContext,"data save",Toast.LENGTH_SHORT).show()
        Log.d("fileLocator", getExternalFilesDir(filepath).toString())
    }



    private val isExternalStorageReadOnly: Boolean get() {
        val extStorageState = Environment.getExternalStorageState()
        return if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            true
        } else {
            false
        }
    }

    private val isExternalStorageAvailable: Boolean get() {
        val extStorageState = Environment.getExternalStorageState()
        return if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            true
        } else{
            false
        }
    }

    //camera section:
    private fun takePicture() {
        Log.d(LOG_TAG, "takePicture() is called")
        takePic = true
        val intent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file: File = createFile()

        val uri: Uri = FileProvider.getUriForFile(
            this,
            "com.example.android.fileprovider",
            file
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)

    }

    @Throws(IOException::class)
    private fun createFile(): File {
        Log.d(LOG_TAG, "createFile() is called")
        var fileName = file_name + "_" + (mutableListRun.size+1).toString() + ".jpg"
        // Create an image file name
        val timeStamp: String = fileName
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        var F = File(storageDir, timeStamp)
        return F.apply {
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = absolutePath
            Log.d(LOG_TAG, mCurrentPhotoPath.toString())
        }
    }

    //check permission:
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d(LOG_TAG, "onRequestPermissionsResult() is called")
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {

                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    takePicture()
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }

            else -> {

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(LOG_TAG, "onActivityResult() is called")
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            //To get the File for further usage
            val auxFile = File(mCurrentPhotoPath)
            var bitmap: Bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath)

        }
    }

    private fun checkPersmission(): Boolean {
        Log.d(LOG_TAG, "requestPermission() is called")
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermission() {
        Log.d(LOG_TAG, "requestPermission() is called")
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ), PERMISSION_REQUEST_CODE)
    }



    override fun onStop(){
        Log.d(LOG_TAG, "onStrop() called")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d(LOG_TAG, "onDestroy() called")
        super.onDestroy()
    }
}