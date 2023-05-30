package moe.feng.danmaqua.model

import com.google.gson.annotations.SerializedName

data class NavInfo(
    val code: Int,
    val message: String,
    val ttl: Int,
    val data: Data
) {

    data class Data(
        @SerializedName("mid") val uid: Long,
        val isLogin: Boolean,
        val wbi_img: Wbi_img
    )

    data class Wbi_img(
        val img_url: String,
        val sub_url: String
    )

}