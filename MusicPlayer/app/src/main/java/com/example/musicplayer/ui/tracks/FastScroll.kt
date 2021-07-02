package com.example.musicplayer.ui.tracks

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FastScrollViewBinding
import com.example.musicplayer.utils.canScroll
import com.example.musicplayer.utils.inflater
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

class FastScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = FastScrollViewBinding.inflate(context.inflater, this, true)
    private val thumbAnim: SpringAnimation

    private var mRecycler: RecyclerView? = null
    private var mGetItem: ((Int) -> Char?)? = null

    private data class Indicator(val char: Char, val pos: Int)

    private var indicators = listOf<Indicator>()
    private val activeColor = ResourcesCompat.getColor(resources, R.color.accent, null)
    private val inactiveColor = ResourcesCompat.getColor(resources, R.color.textSecondary, null)

    private var hasPostedItemUpdate = false
    private var wasValidTouch = false
    private var lastPos = -1

    init {
        isFocusableInTouchMode = true
        isClickable = true

        thumbAnim = SpringAnimation(binding.scrollThumb, DynamicAnimation.TRANSLATION_Y).apply {
            spring = SpringForce().also {
                it.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
            }
        }

        binding.scrollThumb.visibility = View.GONE

    }

    fun setup(recycler: RecyclerView, getItem: (Int) -> Char?) {
        check(mRecycler == null) {}

        mRecycler = recycler
        mGetItem = getItem

        postIndicatorUpdate()
    }

    private fun postIndicatorUpdate() {
        if (!hasPostedItemUpdate) {
            hasPostedItemUpdate = true

            post {
                val recycler = requireNotNull(mRecycler)

                if (recycler.isAttachedToWindow && recycler.adapter != null) {
                    updateIndicators()
                    binding.scrollIndicatorText.requestLayout()
                }

                isVisible = recycler.canScroll()

                hasPostedItemUpdate = false
            }
        }
    }

    private fun updateIndicators() {
        val recycler = requireNotNull(mRecycler)
        val getItem = requireNotNull(mGetItem)

        indicators = 0.until(recycler.adapter!!.itemCount).mapNotNull { pos ->
            getItem(pos)?.let { Indicator(it, pos) }
        }.distinctBy { it.char }
    }

    @Suppress("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        performClick()

        val success = handleTouch(event.action, event.x.roundToInt(), event.y.roundToInt())

        binding.scrollThumb.isActivated = success
        binding.scrollThumb.visibility = View.VISIBLE
        binding.scrollIndicatorText.isPressed = success

        return success
    }

    private fun handleTouch(action: Int, touchX: Int, touchY: Int): Boolean {
        when (action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                binding.scrollIndicatorText.setTextColor(inactiveColor)
                wasValidTouch = false
                lastPos = -1

                return false
            }

            MotionEvent.ACTION_DOWN -> {
                wasValidTouch = binding.scrollIndicatorText.contains(touchX, touchY)
            }
        }

        if (binding.scrollIndicatorText.containsY(touchY) && wasValidTouch) {

            val indicatorTouchY = touchY - binding.scrollIndicatorText.top
            val textHeight = binding.scrollIndicatorText.height / indicators.size

            val index = min(indicatorTouchY / textHeight, indicators.lastIndex)

            val centerY = binding.scrollIndicatorText.y + (textHeight / 2) + (index * textHeight)

            selectIndicator(indicators[index], centerY)

            return true
        }

        return false
    }

    private fun selectIndicator(indicator: Indicator, centerY: Float) {
        if (indicator.pos != lastPos) {
            lastPos = indicator.pos
            binding.scrollIndicatorText.setTextColor(activeColor)

            mRecycler?.apply {
                stopScroll()
                (layoutManager as LinearLayoutManager).scrollToPositionWithOffset(indicator.pos, 0)
            }

            binding.scrollThumbText.text = indicator.char.toString()
        }
    }

    private fun View.contains(x: Int, y: Int): Boolean {
        return x in (left until right) && containsY(y)
    }

    private fun View.containsY(y: Int): Boolean {
        return y in (top until bottom)
    }
}