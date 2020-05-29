package it.techies.whatsmylocation.api

import com.android.volley.Request
import com.android.volley.VolleyError

/**
 * Main class for adding requests and errors to an interface
 */
interface RequestAndErrorInit {

    /**
     *  Call when success of volley request
     *  @param address - address of location
     *  after reverse geocode
     */
    fun onSuccess(address:String?)

    /**
     * Called when an error occurs during a volley request
     * @param error the error which has occured in1 a volley request
     */
    fun onError(error: VolleyError)

    /**
     * Called when the reqeust has to be added to the queue
     * @param req the volley request to be added to our queue
     */
    fun <T> addRequest(req: Request<T>)
}