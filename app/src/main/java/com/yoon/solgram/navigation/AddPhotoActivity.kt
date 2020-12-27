package com.yoon.solgram.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.yoon.solgram.R
import com.yoon.solgram.navigation.model.ContentDTO
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0
    var storage: FirebaseStorage? = null
    var photoUri: Uri? = null
    var auth : FirebaseAuth? = null //user 정보를 가져옴
    var firestore : FirebaseFirestore? =null //db 추가

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        // Initate
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        //Open the album(실행과 함께 바로)
        var photoPickerIntent = Intent(Intent.ACTION_PICK) //선택한 이미지 가져옴
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM) //onActivityResult에 전달

        //add image upload event
        addphoto_btn_upload.setOnClickListener {
            contentUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            if (resultCode == Activity.RESULT_OK) {
                //This is path to the selected image
                photoUri = data?.data
                addphoto_image.setImageURI(photoUri) //선택된 이미지를 addphoto_image에 보여줌
            } else {
                //Exit the addPhotoActivity if you leave the album without selecting it
                finish()
            }
        }
    } //선택한 이미지 받기

    fun contentUpload() {

        //Make filename
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()) //중복방지
        var imageFileName = "IMAGE_" + timestamp + "_.png"

        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

        //Promise method(구글에서 권장하는 방식)
        storageRef?.putFile(photoUri!!)?.continueWithTask{task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->
            var contentDTO = ContentDTO()

            //Insert downloadUrl of image
            contentDTO.imageUrl = uri.toString()

            //Insert uid of user
            contentDTO.uid = auth?.currentUser?.uid

            //Insert userId
            contentDTO.userId = auth?.currentUser?.email

            //Insert explain
            contentDTO.explain = addphoto_edit_explain.text.toString()

            //Insert timestamp
            contentDTO.timestamp = System.currentTimeMillis()

            firestore?.collection("images")?.document()?.set(contentDTO)

            setResult(Activity.RESULT_OK)

            finish()
        }

/*
        //Callback method
        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
            //Toast.makeText(this, getString(R.string.upload_success), Toast.LENGTH_LONG).show()
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                var contentDTO = ContentDTO() //데이터 저장을 위한 contentDTO 객체 생성

                //Insert downloadUrl of image
                contentDTO.imageUrl = uri.toString()

                //Insert uid of user
                contentDTO.uid = auth?.currentUser?.uid

                //Insert userId
                contentDTO.userId = auth?.currentUser?.email

                //Insert explain
                contentDTO.explain = addphoto_edit_explain.text.toString()

                //Insert timestamp
                contentDTO.timestamp = System.currentTimeMillis()

                firestore?.collection("images")?.document()?.set(contentDTO)

                setResult(Activity.RESULT_OK) //성공시 결과를 줌

                finish()
            }
        } //upload image
 */
    }
}