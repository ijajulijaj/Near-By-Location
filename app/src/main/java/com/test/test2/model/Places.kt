package com.test.test2.model

import com.google.gson.annotations.SerializedName

class Places {
    @SerializedName("places" )
    var places : ArrayList<Results> = arrayListOf()
}