package com.ganargatul.kotlingetpost


import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.dialoguser.*
import kotlinx.android.synthetic.main.fragment_kotlin_get.view.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class KotlinGet : Fragment() {
    val userlist = arrayListOf(hashMapOf<String,String>())
    lateinit var v: View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_kotlin_get, container, false)
        // Inflate the layout for this fragment
        prepareData()
        setFab()
        return v
    }

    private fun setFab() {
        v.fab_add_data.setOnClickListener {
            showDialog()
        }
    }

    private fun showDialog() {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialoguser)
        dialog.btn_submit.setOnClickListener {
            submitUser(dialog.input_name.editText?.text.toString(),dialog.input_job.editText?.text.toString())
        }
        dialog.show()
    }

    private fun submitUser(toString: String, toString1: String) {
        val url = URL("https://reqres.in/api/users")
        val obj = JSONObject()
        obj.put("name",toString)
        obj.put("job",toString1)
        networkhelepr(url,"POST",this::onPostResponse,obj)
    }

    private fun onPostResponse(jsonstring: String) {
        val obj = JSONObject(jsonstring)
        Toast.makeText(context,"your id ${obj.getString("id")}",Toast.LENGTH_SHORT).show()
    }

    private fun prepareData() {
        val url = URL("https://reqres.in/api/users")
        networkhelepr(url,"GET",this::onGetListResponse)
    }

    private fun onGetListResponse(JsonString: String) {
        repopulateListData(JSONObject(JsonString))
        renderListData()
    }

    private fun renderListData() {
        val adapter = SimpleAdapter(
            context,
            userlist,
            android.R.layout.simple_list_item_2,
            arrayOf("first_name","email"),
            intArrayOf(android.R.id.text1,android.R.id.text2)
        )
        v.list_view.adapter = adapter
    }

    private fun repopulateListData(jsonObject: JSONObject) {
        userlist.clear()
        Log.d("jsonobject", jsonObject.toString());
        val data = jsonObject.getJSONArray("data")
        for (i in 0 until  data.length()){
            val obj = data.getJSONObject(i)
            val f_name = obj.getString("first_name")
            val email = obj.getString("email")
            userlist.add(hashMapOf("first_name" to f_name, "email" to email))
        }
    }

    private fun showError(code: Int){
        Toast.makeText(context,"Http error code ${code}",Toast.LENGTH_SHORT).show()
    }

    private fun streamToString(stream: InputStream): String{
        val bufferedReader = BufferedReader(InputStreamReader(stream))
        var result = ""
        for (line in bufferedReader.readLines()){
            result += line
        }
        stream.close()
        return result
    }

    private fun networkhelepr(
        url:URL, method:String, onComplete: (String) -> Unit, param: JSONObject = JSONObject()){
        val con = url.openConnection()  as HttpURLConnection
        con.requestMethod = method
        con.setRequestProperty("Content-Type","application/json")
        try {
            AsyncTask.execute { 
                if (method != "GET"){
                    con.doOutput = true
                    val out = OutputStreamWriter(con.outputStream)
                    out.write(param.toString())
                    out.close()
                }
                val responsecode =con.responseCode
                if (responsecode in 200..299){
                    val result = streamToString(con.inputStream)
                    activity?.runOnUiThread {
                        onComplete(result)
                    }
                }else{
                    activity?.runOnUiThread {
                        showError(responsecode)
                    }
                }
            }
        }catch (e: Throwable){
            e.printStackTrace()
        }finally {
            con.disconnect()
        }
    }



}

