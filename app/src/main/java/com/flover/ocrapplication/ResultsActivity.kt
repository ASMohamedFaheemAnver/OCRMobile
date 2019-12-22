package com.flover.ocrapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flover.ocrapplication.adapter.RecyclerViewAdapter
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import org.json.JSONArray

class ResultsActivity : AppCompatActivity() {

    private val localhost : String = "192.168.1.101"

    private var imageUrl  : ArrayList<String> = ArrayList()
    private var imageResult  : ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        getHttp()


        findViewById<Button>(R.id.take_pic_btn).setOnClickListener {
            finish()
        }
    }

    private fun getHttp(){
        val httpAsync = "http://$localhost:3000/api/image_results"
            .httpGet().responseString{_, _, result ->
                when(result){
                    is Result.Failure -> {
                        val ex = result.getException()
                        println(ex)
                    }
                    is Result.Success -> {
                        val data = result.get()
                        // println(data)


                        val json = JSONArray(data)

                        for(i in 0 until json.length()){
                            val result = json.getJSONObject(i)
                            imageUrl.add(result.getString("image_path").replace("localhost", localhost))
                            imageResult.add(result.getString("image_text"))
                        }

                        var recyclerView = findViewById<RecyclerView>(R.id.results_recycler_view)
                        var adapter = RecyclerViewAdapter(imageUrl, imageResult, this)
                        recyclerView.adapter = adapter
                        recyclerView.layoutManager = LinearLayoutManager(this)
                    }
                }
            }
        httpAsync.join()
    }
}