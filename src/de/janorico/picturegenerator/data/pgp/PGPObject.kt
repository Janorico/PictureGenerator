package de.janorico.picturegenerator.data.pgp

class PGPObject(var name: String, var version: String, var data: Array<Array<Boolean>>) {
    companion object {
        fun empty(name: String, version: String, width: Int, height: Int): PGPObject = PGPObject(name, version, emptyArray(width, height))

        fun emptyArray(width: Int, height: Int): Array<Array<Boolean>> = Array(height) {
            return@Array Array(width) SubArray@{ return@SubArray false }
        }
    }

    fun setPixel(x: Int, y: Int, value: Boolean) {
        data[y][x] = value
    }

    fun getPixel(x: Int, y: Int): Boolean = data[y][x]

    fun getWidth(): Int = data[0].size

    fun getHeight(): Int = data.size

    override fun toString(): String {
        return "$name $version"
    }
}
