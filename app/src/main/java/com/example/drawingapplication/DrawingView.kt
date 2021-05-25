package com.example.drawingapplication

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet): View(context, attrs) {

    private var mDrawPath: CustomPath? = null
    //Everything that is drawn in android is a Bitmap
    private var mCanvasBitmap: Bitmap? = null
    /**
     * Paint class holds the style and color info about hot to draw
     * geometries, text and bitmaps.
     */
    private var mDrawPaint: Paint? = null
    private var mCanvasPaint: Paint? = null
    //brush size
    private var mBrushSize: Float = 0.toFloat()
    //color for drawing
    private var color = Color.BLACK
    //background for drawing
    private var canvas: Canvas? = null
    //used to save path drawn
    private val mPaths = ArrayList<CustomPath>()
    //stores undo path
    private val mUndoPath = ArrayList<CustomPath>()

    //circle radius
    var radius = ""
    //rectangle length
    var length = ""
    //rectangle breadth
    var breadth = ""

    init {
        setUpDrawing()
    }

    fun onClickUndo(){
        if (mPaths.size > 0){
            mUndoPath.add(mPaths.removeAt(mPaths.size-1))
            //invalidate will force the view to redraw, so calls onDraw again
            invalidate()
        }
    }

    fun onClickRedo(){
        if (mUndoPath.isNotEmpty()){
            mPaths.add(mUndoPath.removeAt(mUndoPath.size-1))
            invalidate()
        }
    }


    private fun setUpDrawing() {
        mDrawPath = CustomPath(color,mBrushSize)

        mDrawPaint = Paint()
        mDrawPaint!!.color = color
        mDrawPaint!!.style = Paint.Style.STROKE
        //how the beginning and end of stroke should be, rounded or not rounded
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND

        mCanvasPaint = Paint(Paint.DITHER_FLAG)

//        mBrushSize = 20.toFloat()

    }

    //everytime the size of screen is changed
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    //when we want to write or draw
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!,0f,0f, mCanvasPaint)

        for (path in mPaths){
            mDrawPaint!!.strokeWidth = path.brushThickness
            mDrawPaint!!.color = path.color
            canvas.drawPath(path, mDrawPaint!!)
        }
        //if is used cuz mDrawPath is customPath so for when we haven't drawn anything
        if (!mDrawPath!!.isEmpty){
            //telling how thick the paint should be
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.color

            canvas.drawPath(mDrawPath!!,mDrawPaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        //variable which store x and y value where it was touched
        val touchX = event?.x
        val touchY = event?.y

        when(event?.action){
            //when we get our finger on the screen
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.color = color
                mDrawPath!!.brushThickness = mBrushSize
                mDrawPath!!.reset()
                mDrawPath!!.moveTo(touchX!!,touchY!!)
            }
            //once we drag our finger across the screen
            MotionEvent.ACTION_MOVE ->{
                mDrawPath!!.lineTo(touchX!!, touchY!!)
            }
            //when we lift our finger from screen
            MotionEvent.ACTION_UP ->{
                mPaths.add(mDrawPath!!)
                mDrawPath = CustomPath(color, mBrushSize)
            }
            else -> return false
        }
        //invalidate the whole view
        invalidate()
        //draws circle
        if (touchX != null && touchY != null && radius.isNotEmpty()) {
            canvas!!.drawCircle(touchX, touchY, radius.toFloat(), mDrawPaint!!)
        }
        //draws rectangle
        if (touchX != null && touchY != null && length.isNotEmpty() && breadth.isNotEmpty()) {
            canvas!!.drawRect(touchX, touchY, touchX+length.toFloat(), touchY+breadth.toFloat(),mDrawPaint!!)
        }
        return true
    }

    fun setSizeForBrush(newSize: Float){
        //used to have same paint size in different screen size, eg 20 pixel will look different on one screen than other
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
        newSize, resources.displayMetrics)
        mDrawPaint!!.strokeWidth = mBrushSize
    }

    fun setColor(newColor: String){
        color = Color.parseColor(newColor)
        mDrawPaint!!.color = color
    }

    internal inner class CustomPath(var color: Int,
                                    var brushThickness: Float) : Path(){

    }

    fun refresh(){
        mPaths.clear()
        mUndoPath.clear()
        radius = ""
        length = ""
        breadth = ""
        invalidate()
        canvas!!.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)


    }
}