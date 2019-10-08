package at.techikum_wien.fleischhacker.mocmagiccardlist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.loader.app.LoaderManager
import androidx.loader.content.AsyncTaskLoader
import androidx.loader.content.Loader
import at.techikum_wien.fleischhacker.mocmagiccardlist.utilities.NetworkUtils
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<String> {

    private val TAG = MainActivity::class.java.canonicalName

    /* A constant to save and restore the URL that is being displayed */
    private var PAGE_CONST = "query"

    private var stringyfiedList = ""
    private val TEST_LOADER = 22

    internal var magicCardList = ArrayList<MagicCard>()

    /* set page to zere because we increment at btnLoad */
    private var currentPage = 0

    fun getCurrentPage(): Int = currentPage

    private var mResultsTextView: TextView? = null
    private var mResultsListView: ListView? = null
    private var mErrorMessageDisplay: TextView? = null
    private var mLoadingIndicator: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var mBtnLoad: Button = findViewById(R.id.btn_load)

        mResultsTextView = findViewById(R.id.tv_results) as TextView
        //mResultsListView = findViewById(R.id.lv_results) as ListView

        mErrorMessageDisplay = findViewById(R.id.tv_error_message_display) as TextView
        mLoadingIndicator = findViewById(R.id.pb_loading_indicator) as ProgressBar

        if (savedInstanceState != null) {
            val queryUrl = savedInstanceState.getString(PAGE_CONST)
            Toast.makeText(this, "$queryUrl", Toast.LENGTH_SHORT).show()
        }

        /*
         * Initialize the loader
         */
        supportLoaderManager.initLoader(TEST_LOADER, null, this)

        mBtnLoad.setOnClickListener {
            mResultsTextView?.setText("")
            btn_load.setEnabled(false)

            currentPage += 1

            makeMagicCardsGetQuery(currentPage)
        }
    }

    private fun makeMagicCardsGetQuery(currentPage: Int) {

        val url = NetworkUtils.buildUrlMagicTheGathering(currentPage)

        Toast.makeText(this, "Page: $currentPage", Toast.LENGTH_SHORT).show()
        Toast.makeText(this, "checkURL: $url", Toast.LENGTH_SHORT).show()

        val queryBundle = Bundle()
        queryBundle.putString(PAGE_CONST, url.toString())

        // don't know why this is @deprecated
        val loaderManager = supportLoaderManager
        val mgSearchLoader = loaderManager.getLoader<Int>(TEST_LOADER)

        if (mgSearchLoader == null) {
            loaderManager.initLoader(TEST_LOADER, queryBundle, this)
        } else {
            loaderManager.restartLoader(TEST_LOADER, queryBundle, this)
        }
    }

    private fun showDataView() {
        // First, make sure the error is invisible
        mErrorMessageDisplay?.setVisibility(View.INVISIBLE)
        // Then, make sure the JSON data is visible
        mResultsListView?.setVisibility(View.VISIBLE)
    }

    private fun showErrorMessage() {
        // First, hide the currently visible data
        mResultsListView?.setVisibility(View.INVISIBLE)
        // Then, show the error
        mErrorMessageDisplay?.setVisibility(View.VISIBLE)
        Toast.makeText(this, "An error occurred. Please try again later.", Toast.LENGTH_LONG).show()
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<String> {
        return object : AsyncTaskLoader<String>(this) {

            var JSONResponse: String? = null

            override fun onStartLoading() {

                if (args == null) {
                    return
                }

                if (JSONResponse != null) {
                    deliverResult(JSONResponse!!)
                } else {
                    /*
                     * When we initially begin loading in the background, we want to display the
                     * loading indicator to the user
                     */
                    mLoadingIndicator?.setVisibility(View.VISIBLE)

                    forceLoad()
                }
            }

            override fun loadInBackground(): String? {

                /* Extract the page query from the args using our constant */
                val queryUrlString = args!!.getString(PAGE_CONST)

                if (TextUtils.isEmpty(queryUrlString)) {
                    return null
                }

                /* Parse the URL from the passed in String and perform the query */
                try {
                    val apiUrl = URL(queryUrlString)
                    val response = NetworkUtils.getResponseFromHttpUrl(apiUrl)

                    val rootJSONObject = JSONObject(response)
                    val cardsArray = rootJSONObject.getJSONArray("cards")

                    /* we parse the shizzle */
                    parseJSONArray(cardsArray)

                } catch (e: IOException) {
                    e.printStackTrace()
                    return null
                } catch (e: JSONException) {
                    e.printStackTrace()
                    return null
                }

                Log.e(TAG, "stringified: $stringyfiedList")
                /* make a it look like js */
                return stringify(magicCardList)

            }

            // COMPLETED (3) Override deliverResult and store the data in mGithubJson
            // COMPLETED (4) Call super.deliverResult after storing the data
            fun deliverResult(JSONresponse: String) {
                JSONResponse = JSONresponse
                super.deliverResult(JSONresponse)
            }
        }
    }

    fun parseJSONArray(cards: JSONArray): ArrayList<MagicCard> {
        for (i in 0 until cards.length()) {
            /* Get single JSON object node */
            val sObject = cards.get(i).toString()
            val mItemObject = JSONObject(sObject)

            /* Get String value from json object */
            val Name = mItemObject.getString("name")
            val Type = mItemObject.getString("type")
            val Rarity = mItemObject.getString("rarity")
            val colorsArr = JSONArray(mItemObject.getString("colors")).toString()

            val card = MagicCard(Name, Type, Rarity)

            card.addColor(colorsArr)
            magicCardList.add(card)
        }

        return magicCardList
    }

    fun parseToString(card: MagicCard): String {
        stringyfiedList += card.name.toString() + ": " + card.type.toString() + ", " + card.rarity.toString() + ", " + card.getColors().toString().substring(
            2,
            card.getColors().toString().length
        ).replace("\"", "").replace("]","") + "\n"
        return stringyfiedList
    }

    fun stringify(magicCardList: ArrayList<MagicCard>): String {
        for (card in magicCardList) {
            parseToString(card)
        }
        return stringyfiedList
    }

    override fun onLoadFinished(loader: Loader<String>, data: String?) {

        mLoadingIndicator?.setVisibility(View.INVISIBLE)

        if (null == data) {
            showErrorMessage()
        } else {
            mResultsTextView?.setText(data)
            btn_load.setEnabled(true)
            showDataView()
        }
    }

    override fun onLoaderReset(loader: Loader<String>) {
        /*
         * We aren't using this method in our example application, but we are required to Override
         * it to implement the LoaderCallbacks<String> interface
         */
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var currentPage = getCurrentPage()
        Toast.makeText(this, "currentPage", Toast.LENGTH_SHORT).show()
        outState.putString(PAGE_CONST, currentPage.toString())
    }
}
