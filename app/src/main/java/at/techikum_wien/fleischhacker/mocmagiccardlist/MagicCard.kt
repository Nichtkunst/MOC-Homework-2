package at.techikum_wien.fleischhacker.mocmagiccardlist

import androidx.annotation.NonNull
import java.util.*

class MagicCard(var name: String?, var type: String?, var rarity: String?) : Comparable<MagicCard> {
    private val colors: MutableList<String>

    init {
        this.colors = LinkedList()
    }

    fun getColors(): List<String> {
        return colors
    }

    fun addColor(color: String) {
        this.colors.add(color)
    }

    override fun compareTo(@NonNull second: MagicCard): Int {
        return name!!.compareTo(second.name!!)
    }
}


