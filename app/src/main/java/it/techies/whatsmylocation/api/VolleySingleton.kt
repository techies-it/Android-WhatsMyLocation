package it.techies.whatsmylocation.api

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class VolleySingleton(context: Context){
    companion object {
        @Volatile
        private var INSTANCE: VolleySingleton? = null

        fun getInstance(context: Context) =
                INSTANCE ?: synchronized(this){
                    INSTANCE ?: VolleySingleton(context).also {
                        INSTANCE = it
                    }
                }

        /**
         *  To stop from resend volley request
         */
        infix fun getUploadRetryStrat(boo:Boolean): DefaultRetryPolicy {
            return DefaultRetryPolicy(30000,0,0F)
        }

    }

    /**
     * initializes the volley new request queue
     */
    val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }

    /**
     * adds to the volley request queue
     */
    fun <T> addToRequestQueue(request: Request<T>){
        requestQueue.add(request)
    }
}