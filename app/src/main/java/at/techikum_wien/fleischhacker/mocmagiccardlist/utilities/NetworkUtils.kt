package at.techikum_wien.fleischhacker.mocmagiccardlist.utilities

import android.net.Uri
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*

/**
 * These utilities will be used to communicate with the network.
 */
object NetworkUtils {

    /*
     * Example
     * https://api.magicthegathering.io/v1/cards?pageSize=50&orderBy=alphabetically
     * https://api.magicthegathering.io/v1/cards?page=1&5&sort=name
     */

    internal val MTG_BASE_URL = "https://api.magicthegathering.io/v1/cards"

    internal val PARAM_PAGE_SIZE = "pageSize"
    internal val count = "50"

    internal val PARAM_PAGE = "size"

    internal val CURRENT_PAGE = "page"

    /*
     * The sort field.
     */
    internal val PARAM_SORT = "sort"
    internal val sortBy = "name"

    /**
     * Builds the URL used to query Magic: The Gathering API!.
     *
     * @param mtgQuery The keyword that will be queried for.
     * @return The URL to use to query the Magic: The Gathering API! server.
     */
    fun buildUrlMagicTheGathering(page: Int): URL? {

        // @TODO query pageSize for size == 0
        val builtUri = Uri.parse(MTG_BASE_URL).buildUpon()
            .appendQueryParameter(CURRENT_PAGE, page.toString())
            //.appendQueryParameter(PARAM_PAGE_SIZE, count)
            .appendQueryParameter(PARAM_SORT, sortBy)
            .build()

        var url: URL? = null
        try {
            url = URL(builtUri.toString())
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }

        return url
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    @Throws(IOException::class)
    fun getResponseFromHttpUrl(url: URL): String? {
        val urlConnection = url.openConnection() as HttpURLConnection
        urlConnection.requestMethod = "GET"

        try {
            val `in` = urlConnection.inputStream

            val scanner = Scanner(`in`)
            scanner.useDelimiter("\\A")

            val hasInput = scanner.hasNext()
            return if (hasInput) {
                scanner.next()
            } else {
                null
            }
        } finally {
            urlConnection.disconnect()
        }
    }
}