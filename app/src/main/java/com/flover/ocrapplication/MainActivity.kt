package com.flover.ocrapplication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.net.toFile
import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.loopj.android.http.SyncHttpClient
import com.loopj.android.http.TextHttpResponseHandler
import cz.msebera.android.httpclient.Header
import java.io.File
import java.net.URI
import java.net.URL

class MainActivity : AppCompatActivity() {
    private val localhost : String = "192.168.1.102"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        findViewById<ImageView>(R.id.add_image).setOnClickListener {
            var galleryIntent = Intent()
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, 1)

        }

        findViewById<Button>(R.id.show_results_btn).setOnClickListener {
            var intent = Intent(this, ResultsActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode==1&&resultCode== Activity.RESULT_OK&&data!=null){
            var uri: Uri? = data.data
            findViewById<ImageView>(R.id.add_image).setImageURI(uri)

            var client = SyncHttpClient()
            var params = RequestParams()

//            var path = Environment.getExternalStorageDirectory().toString() + uri?.path
//            var file = File(path)
//            println(path)


            params.put("image", "SOMETHING")
            params.put("result", "GOOGLE!")

            Thread{
                Looper.prepare()
                client.post("http://$localhost:3000/api/results", params, object : AsyncHttpResponseHandler(){
                    override fun onSuccess(
                        statusCode: Int,
                        headers: Array<out Header>?,
                        responseBody: ByteArray?
                    ) {

                    }

                    override fun onFailure(
                        statusCode: Int,
                        headers: Array<out Header>?,
                        responseBody: ByteArray?,
                        error: Throwable?
                    ) {

                    }

                })
                Looper.loop()
            }.start()
        }
    }
}