class Lines internal constructor(private val mMaxNum: Int) {
    private var mNum: Int = 0
    private val mX1: FloatArray
    private val mY1: FloatArray
    private val mX2: FloatArray
    private val mY2: FloatArray

    init {
        mX1 = FloatArray(mMaxNum)
        mY1 = FloatArray(mMaxNum)
        mX2 = FloatArray(mMaxNum)
        mY2 = FloatArray(mMaxNum)
    }

    fun init() {
        mNum = 0
    }

    fun add(x1: Float, y1: Float, x2: Float, y2: Float) {
        if (mNum < mMaxNum) {
            mX1[mNum] = x1
            mY1[mNum] = y1
            mX2[mNum] = x2
            mY2[mNum] = y2
            mNum++
        }
    }

    fun getX1(index: Int): Float {
        val ret: Float
        ret = if (index >= 0 && index < mNum) {
            mX1[index]
        } else {
            0f
        }
        return ret
    }

    fun getY1(index: Int): Float {
        val ret: Float
        ret = if (index >= 0 && index < mNum) {
            mY1[index]
        } else {
            0f
        }
        return ret
    }

    fun getX2(index: Int): Float {
        val ret: Float
        ret = if (index >= 0 && index < mNum) {
            mX2[index]
        } else {
            0f
        }
        return ret
    }

    fun getY2(index: Int): Float {
        val ret: Float
        ret = if (index >= 0 && index < mNum) {
            mY2[index]
        } else {
            0f
        }
        return ret
    }

    fun getNum(): Int {
        return mNum
    }
}