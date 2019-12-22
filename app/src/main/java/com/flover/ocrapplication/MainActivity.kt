package com.flover.ocrapplication

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.loopj.android.http.RequestParams
import com.loopj.android.http.SyncHttpClient
import com.loopj.android.http.TextHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONObject
import java.io.File


class MainActivity : AppCompatActivity() {
    private val localhost : String = "192.168.1.101"
    private lateinit var textView : TextView
    private lateinit var progressDialog : ProgressDialog
    private lateinit var path: String
    private lateinit var string: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        findViewById<ImageView>(R.id.uploaded_image).setOnClickListener {
            var galleryIntent = Intent()
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, 1)

        }

        findViewById<Button>(R.id.show_results_btn).setOnClickListener {
            var intent = Intent(this, ResultsActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.upload_result_btn).setOnClickListener {
            var client = SyncHttpClient()
            var params = RequestParams()
            params.put("image", File(path), path?.let { getContentType(it) })
            params.put("result", string)

            Thread{
                Looper.prepare()
                client.post("http://$localhost:3000/api/results", params, object : TextHttpResponseHandler(){
                    override fun onSuccess(
                        statusCode: Int,
                        headers: Array<out Header>?,
                        responseString: String?
                    ) {
                        println(responseString)
                    }

                    override fun onFailure(
                        statusCode: Int,
                        headers: Array<out Header>?,
                        responseString: String?,
                        throwable: Throwable?
                    ) {
                        println(responseString)
                    }
                })
                Looper.loop()
            }.start()
        }

        textView = findViewById(R.id.show_result)
        textView.text = ""
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("LOADING!")
        progressDialog.setMessage("WAITING FOR SERVER RESPONSE!")
        progressDialog.setCanceledOnTouchOutside(false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        progressDialog.show()
        if (requestCode==1&&resultCode== Activity.RESULT_OK&&data!=null){
            var uri: Uri? = data.data
            findViewById<ImageView>(R.id.uploaded_image).setImageURI(uri)

            var client = SyncHttpClient()
            var params = RequestParams()

            path = uri?.let { getPath(baseContext, it) }!!

            params.put("image", File(path), path?.let { getContentType(it) })
            // params.put("result", "GOOGLE!")
            client.connectTimeout = 60000
            client.responseTimeout = 120000

            Thread{
                Looper.prepare()
                client.post("http://$localhost:3000/api/result", params, object : TextHttpResponseHandler(){
                    override fun onSuccess(
                        statusCode: Int,
                        headers: Array<out Header>?,
                        responseString: String?
                    ) {
                        val json = JSONObject(responseString)
                        string = json["image_text"].toString()
                        this@MainActivity.runOnUiThread {
                            textView.text = string
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(
                        statusCode: Int,
                        headers: Array<out Header>?,
                        responseString: String?,
                        throwable: Throwable?
                    ) {
                        println(responseString)
                    }
                })
                Looper.loop()
            }.start()
        }
    }

    private fun getPath(context: Context?, uri: Uri): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) { // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )
                return context?.let { getDataColumn(it, contentUri, null, null) }
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return context?.let { getDataColumn(it, contentUri, selection, selectionArgs) }
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return context?.let { getDataColumn(it, uri, null, null) }
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    private fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex: Int = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun getContentType(path: String) : String{
        if (path.endsWith("png")){
            return "image/png"
        }else if(path.endsWith("jpg")){
            return "image/jpg"
        }
        return "err"
    }

}