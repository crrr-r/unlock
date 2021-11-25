package swu.cr.unlock

import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.core.graphics.contains
import swu.cr.unlock.databinding.ActivityMainBinding
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    //被点亮的View
    private var alreadyViews = mutableListOf<View>()
    private var lastDot:View? = null
    private lateinit var binding: ActivityMainBinding
    //记录输入的密码
    private val pwd = StringBuilder()
    //保存点9个
    private val dotsArray:Array<ImageView> by lazy {

        arrayOf(binding.dot1,binding.dot2,binding.dot3,
            binding.dot4,binding.dot5,binding.dot6,
            binding.dot7,binding.dot8,binding.dot9)

    }
    //保存所有的线段的tag值
    private val lineTagsArray = arrayOf(
        12,23,45,56,78,79,//横线
        14,25,36,47,58,69,//竖线
        24,35,57,68,15,26,48,59//斜线
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        //判断事件的类型
        when(event?.action){
            MotionEvent.ACTION_DOWN ->{
                val dotView = checkTouchView(event)
                if (dotView != null){
                    //点亮这个点
                    dotView.visibility = View.VISIBLE
                    //记录这个点
                    lastDot = dotView
                    //添加到被点亮的视图
                    alreadyViews.add(dotView)
                    //记录密码
                    pwd.append(dotView.tag)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val dotView = checkTouchView(event)
                if (dotView != null) {
                    if (dotView.visibility == View.INVISIBLE) {
                        if (lastDot == null) {
                            //点亮这个点
                            dotView.visibility = View.VISIBLE
                            //记录这个点
                            lastDot = dotView
                            //记录密码
                            pwd.append(dotView.tag)
                        } else {
                            //获取上一个点的tag值
                            val perTag = (lastDot?.tag as String).toInt()
                            val currentTag = (dotView?.tag as String).toInt()
                            val lineTag = if (perTag < currentTag) {
                                (perTag * 10 + currentTag).toString()
                            } else {
                                (currentTag*10 + perTag).toString()
                            }
                            //判断
                            val lineTagABS = abs(perTag - currentTag)
                            if (lineTagABS <= 4) {
                                //有路径则获取tag对应的视图
                                val lineView = binding.dotContainer.findViewWithTag<ImageView>(
                                    lineTag
                                )
                                //点亮线
                                lineView.visibility = View.VISIBLE
                                //点亮点
                                dotView.visibility = View.VISIBLE
                                lastDot = dotView
                                //记录密码
                                pwd.append(dotView.tag)
                                //添加到被点亮的视图
                                alreadyViews.add(dotView)
                                alreadyViews.add(lineView)
                            }
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP ->{
                //隐藏
                alreadyViews.forEach {
                    it.visibility = View.INVISIBLE
                }
                alreadyViews.clear()
                //SharedPreference
                lastDot = null
                Log.v("pharaoh","$pwd")
            }
        }
        return true
    }

    //判断触摸点是否在某个dot区域内
    private fun checkTouchView(event: MotionEvent):View?{
        //获取触摸点在容器中的坐标
        val point = getTouchPoint(event)
        //循环遍历保存九个点的数组
        dotsArray.forEach {
            //获取当前视图的Rect
            val dotRect = toRect(it)
            //判断触摸点是否在这个矩形区域内
            val result = dotRect.contains(point)

            if (result){
                return it
            }
        }
        return null
    }

    //将某个视图的坐标转换为Rect
    private fun toRect(dot: ImageView):Rect{
        return Rect(
            dot.x.toInt(),
            dot.y.toInt(),
            (dot.x + dot.width).toInt(),
            (dot.y + dot.height).toInt())
    }

    //计算触摸点相对于父容器的坐标
    private fun getTouchPoint(event: MotionEvent):Point{
        //相对于屏幕的高度 - 顶部bar的高度 - 容器和内容区域顶部的高度
        val height = event.y - getBarHeight() - binding.dotContainer.y

        val touchPoint = Point().apply {
            x = event.x.toInt()
            y = height.toInt()
        }

        return touchPoint
    }

    //获取顶部bar的高度
    private fun getBarHeight():Int{
        super.onRestart()
        //计算屏幕高度

        var allHeight = 0
        //判断minSdkVersion的版本
        allHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds.height()
        }else {
            val mMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(mMetrics)
            mMetrics.heightPixels
        }
        //内容的高度
        val rect = Rect()
        val content = window.findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT)
        content.getDrawingRect(rect)
        //底部的高度
        //val resourceId = resources.getIdentifier("navigation_bar_height","dimen","android")
        //val navBarHeight = resources.getDimensionPixelSize(resourceId)

        val barHeight = allHeight - rect.height()

        return barHeight
    }
}