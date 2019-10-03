package com.ganargatul.kotlingetpost


import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_blank.view.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class PostKotlin : Fragment() {
    lateinit var v: View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_blank, container, false)
        // Inflate the layout for this fragment
        setButton()
        return v
    }

    private fun setButton() {
        v.submit.setOnClickListener {
            register(v.name?.text.toString(),v.email?.text.toString(),v.password?.text.toString(),v.phone?.text.toString())
        }
    }

    private fun register(name: String, email: String,password: String, phone: String) {
        val url = URL("http://35.172.178.112:9000/api/users/v1/resetPassword")
        val obj= JSONObject()
        Log.d("postt",email)
//        obj.put("name",name)
        obj.put("email",email)
//        obj.put("password",password)
//        obj.put("phone",phone)
        Log.d("obj", obj.toString())
        networkHelper(url,"POST",this::onPostResponse,obj)


    }

    private fun onPostResponse(jsonString: String) {
        val obj = JSONObject(jsonString)
        Toast.makeText(context,"registered with ID ${obj.getString("code")}",Toast.LENGTH_SHORT).show()
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
    private fun showError(code: Int){
        Toast.makeText(context,"Http error code ${code}",Toast.LENGTH_SHORT).show()
    }
    private fun networkHelper( url: URL, method:String, onComplete: (String)-> Unit, param: JSONObject= JSONObject()){
        val con = url.openConnection() as HttpURLConnection
        con.requestMethod = method
        con.setRequestProperty("Content-Type","application/json")
        con.setRequestProperty("Authorization","Basic dGVmYS0yMDE5OnBkd25ncGlhLXI3NDc4dHEtNDdsdHNxNmstZmFsdHN0cDVrZg==")
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
