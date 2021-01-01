package com.yoon.solgram.navigation.model

data class  ContentDTO(var explain : String? = null,
                       var imageUrl : String? = null,
                       var uid :String? =null,
                       var userId : String? =null,
                       var timestamp : Long? = null,
                       var favoriteCount : Int = 0,
                       var favorites : MutableMap<String,Boolean> = HashMap()){ //좋아요 중복 방지
    data class Comment( var uid :String? =null,
                        var userId : String? =null,
                        var comment : String? = null,
                        var timestamp : Long? = null)
}