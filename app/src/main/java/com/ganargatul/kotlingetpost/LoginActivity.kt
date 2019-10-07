package com.ganargatul.kotlingetpost

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        login.setOnClickListener{
            submitlogin()
        }
    }

    private fun submitlogin() {
        val URL = URL("https://reqres.in/api/login")
        val obj = JSONObject()
        obj.put("email",email.text.toString())
        obj.put("password",password.text.toString())
        Log.d("obj", obj.toString())
        networkHelper(URL,"POST",this::Reponse,obj)
        Toast.makeText(applicationContext,"Please Wait",Toast.LENGTH_SHORT).show()
    }

    private fun Reponse(string: String) {
        val obj = JSONObject(string)
        if (obj.getString("token").isNotEmpty()){
//            var intent = Intent(LoginActivity.this,MainActivity.class)
            startActivity(Intent(applicationContext,MainActivity::class.java))
        }
    }

    fun networkHelper(url: URL, method:String, onComplete: (String)->Unit, param: JSONObject = JSONObject()){
        val con = url.openConnection() as HttpURLConnection
        con.requestMethod = method
        con.setRequestProperty("Content-Type","Application/json")
        try {
            AsyncTask.execute {
                if (method != "GET"){
                    con.doOutput = true
                    val out = OutputStreamWriter(con.outputStream)
                    out.write(param.toString())
                    out.close()
                }
                val responseCode = con.responseCode
                if (responseCode in 200..299){
                    val result = streamToString(con.inputStream)

                    runOnUiThread {
                        onComplete(result)
                    }
                }else{
                    Log.d("responseCode", responseCode.toString())
                }
            }
        }catch (e: Throwable){
            e.printStackTrace()

        }finally {
            con.disconnect()
        }
    }

    private fun streamToString(inputStream: InputStream?): String {
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))

        var result= ""

        for (line in bufferedReader.readLines()){
            result += line
        }
        inputStream?.close()
        return result

    }
}
