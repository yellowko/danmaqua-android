package moe.feng.danmaqua.api.bili

import android.webkit.WebSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.feng.danmaqua.model.NavInfo
import moe.feng.danmaqua.model.SpaceInfo
import moe.feng.danmaqua.util.HttpUtils
import okhttp3.Request
import java.security.MessageDigest
import java.util.*

object UserApi {

    const val SPACE_INFO_URL = "https://api.bilibili.com/x/space/wbi/acc/info?"
    const val NAV_URL="https://api.bilibili.com/x/web-interface/nav"

    fun calculateMD5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { byte -> "%02x".format(byte) }
    }

    val mixinKeyEncTab:IntArray = intArrayOf(46, 47, 18, 2, 53, 8, 23, 32, 15,
        50, 10, 31, 58, 3, 45, 35, 27, 43, 5, 49, 33, 9, 42, 19, 29, 28, 14, 39,
        12, 38, 41, 13, 37, 48, 7, 16, 24, 55, 40, 61, 26, 17, 0, 1, 60, 51, 30,
        4, 22, 25, 54, 21, 56, 59, 6, 63, 57, 62, 11, 36, 20, 34, 44, 52)

    // 对 imgKey 和 subKey 进行字符顺序打乱编码
    fun getMixinKey(orig:String):String {
        var tempStr = ""
        for(n in mixinKeyEncTab){
            tempStr+=(orig[n])
        }
        return tempStr.slice(0..31)
    }

    suspend fun getNavInfo(id: Long): NavInfo = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(NAV_URL.format(id))
            .build()

        HttpUtils.requestAsJson<NavInfo>(request)
    }

    suspend fun getSpaceInfo(uid: Long): SpaceInfo = withContext(Dispatchers.IO) {
        val navInfo=getNavInfo(uid)

        val img_url = navInfo.data.wbi_img.img_url
        val sub_url = navInfo.data.wbi_img.sub_url
        val img_key = img_url.substring(img_url.lastIndexOf('/') + 1, img_url.length).split('.')[0]
        val sub_key = sub_url.substring(sub_url.lastIndexOf('/') + 1, sub_url.length).split('.')[0]

        val mixin_key = getMixinKey(img_key + sub_key)
        var currTime = (Date().time / 1000)
        var query = "mid=" + uid + "&wts=" + currTime
        val query_ = query + mixin_key
        val wbi_sign = calculateMD5(query_)

        query = SPACE_INFO_URL + query + "&w_rid=" + wbi_sign

        val request = Request.Builder()
            .url(query)
            .addHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
            .removeHeader("User-Agent")
            .addHeader("User-Agent", "Mozilla/5.0")
            .build()

        HttpUtils.requestAsJson<SpaceInfo>(request)
    }

}