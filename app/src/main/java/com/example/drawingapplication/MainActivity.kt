package com.example.drawingapplication

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.drawingapplication.databinding.ActivityMainBinding
import com.example.drawingapplication.databinding.DialogBrushSizeBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var mSelectedImageButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mSelectedImageButton = binding.ibBlack
        mSelectedImageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_selected))

        binding.drawingView.setSizeForBrush(20.toFloat())

        binding.ibBrush.setOnClickListener {
            showBrushSizeChooserDialog()
        }

        binding.ibGallery.setOnClickListener {
            if (isReadStorageAllowed()){

                val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickPhotoIntent, GALLERY)
            } else{
                requestStoragePermission()
            }
        }

        binding.ibUndo.setOnClickListener { binding.drawingView.onClickUndo() }

        binding.ibRedo.setOnClickListener { binding.drawingView.onClickRedo() }

        binding.ibCircle.setOnClickListener { chooseCircleDialogue() }

        binding.ibRectangle.setOnClickListener { chooseRectangleDialogue() }

        binding.ibCancel.setOnClickListener {
            binding.apply {
                drawingView.radius = ""
                drawingView.length = ""
                drawingView.breadth = ""
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == GALLERY){
                try {
                    if (data!!.data != null){
                        binding.ivBackground.visibility = View.VISIBLE
                        binding.ivBackground.setImageURI(data.data)
                    } else{
                        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }
    }

    private fun showBrushSizeChooserDialog(){
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush size: ")
        val smallBtn = brushDialog.findViewById<ImageButton>(R.id.ib_small_brush)
        smallBtn.setOnClickListener {
            binding.drawingView.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
        }
        val mediumBtn = brushDialog.findViewById<ImageButton>(R.id.ib_medium_brush)
        mediumBtn.setOnClickListener {
            binding.drawingView.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
        }
        val largeBtn = brushDialog.findViewById<ImageButton>(R.id.ib_large_brush)
        largeBtn.setOnClickListener {
            binding.drawingView.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.show()
    }

    //in the view ImageButton is passed
    fun paintClicked(view: View){
        if (view !== mSelectedImageButton){
            //checks if the view is image button
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()

            binding.drawingView.setColor(colorTag)
            imageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_selected))
            mSelectedImageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_normal))

            mSelectedImageButton = imageButton
        }
    }

    private fun requestStoragePermission(){
        //checks if permission is needed
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE).toString())){
            Toast.makeText(this, "Need permission", Toast.LENGTH_SHORT).show()
        }
        //request permission
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
    }

    //what happens when request is accepted or denied
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
            } else{
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //if user allows permission from settings, the app won't know, so
    private fun isReadStorageAllowed(): Boolean{
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    companion object{
        private const val STORAGE_PERMISSION_CODE = 1
        private const val GALLERY = 2
    }

    private fun chooseCircleDialogue(){
        val circleDialog = Dialog(this)
        circleDialog.setContentView(R.layout.circle_dialogue)
        circleDialog.setTitle("Circle radius: ")

        circleDialog.findViewById<Button>(R.id.button_Enter).setOnClickListener {
            if (circleDialog.findViewById<EditText>(R.id.et_Radius).text.isNotEmpty()) {
                binding.drawingView.radius =
                    circleDialog.findViewById<EditText>(R.id.et_Radius).text.toString()
                //if before this rectangle was selected then, rectangle needs to be cancelled if not then along with circle rectangle is also created
                binding.drawingView.length = ""
                binding.drawingView.breadth = ""
            }
            circleDialog.dismiss()
        }

        circleDialog.show()
    }

    private fun chooseRectangleDialogue(){
        val rectDialog = Dialog(this)
        rectDialog.setContentView(R.layout.rectangle_dialog)
        rectDialog.setTitle("Rectangle l & b: ")

        rectDialog.findViewById<Button>(R.id.button_Enter).setOnClickListener {
            if (rectDialog.findViewById<EditText>(R.id.et_Length).text.isNotEmpty() && rectDialog.findViewById<EditText>(R.id.et_Breadth).text.isNotEmpty()) {
                binding.drawingView.length =
                    rectDialog.findViewById<EditText>(R.id.et_Length).text.toString()
                binding.drawingView.breadth =
                    rectDialog.findViewById<EditText>(R.id.et_Breadth).text.toString()
                //if before this circle was selected then, circle needs to be cancelled if not then along with rectangle circle is also created
                binding.drawingView.radius = ""
            }
            rectDialog.dismiss()
        }

        rectDialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.menu_refresh -> {
                binding.ivBackground.setImageDrawable(null)
                binding.drawingView.refresh()
                true
            }
            R.id.menu_help->{
                val intent = Intent(this, HelpActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}