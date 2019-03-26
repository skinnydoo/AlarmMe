// ItemDivider.java
// Class that defines dividers displayed between the RecyclerView items;
// based on Google's sample implementation at bit.ly/DividerItemDecoration
package ca.poly.inf8405.alarmme.utils


import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView

class ItemDivider(context: Context) : RecyclerView.ItemDecoration() {
    private var mDivider: Drawable? = null

    init {
        val attrs = intArrayOf(android.R.attr.listDivider)
        val a = context.obtainStyledAttributes(attrs)
        mDivider = a.getDrawable(0)
        if (mDivider == null) {
            LogWrapper.e("@android:attr/listDivider was not set in the theme used for this " +
                    "ItemDivider. Please set that attribute all call setDrawable()")
        }
        a.recycle()
    }

    /**
     * Sets the [Drawable] for this divider.
     *
     * @param drawable Drawable that should be used as a divider.
     */
    fun setDrawable(drawable: Drawable) { mDivider = drawable }

    // draws the list item dividers onto the RecyclerView
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        // calculate left/right x-coordinates for all dividers
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        // for every item but the last, draw a line below it
        for (i in 0 until parent.childCount - 1) {
            val item = parent.getChildAt(i) // get ith list item

            // calculate top/bottom y-coordinates for current mDivider
            val top = item.bottom + (item.layoutParams as RecyclerView.LayoutParams).bottomMargin
            val bottom = top + mDivider!!.intrinsicHeight

            // draw the mDivider with the calculated bounds
            mDivider!!.setBounds(left, top, right, bottom)
            mDivider!!.draw(c)
        }
    }
}
