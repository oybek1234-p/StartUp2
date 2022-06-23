package com.example.market.home
//import android.graphics.Rect
//import android.view.View
//import androidx.recyclerview.widget.RecyclerView
//import androidx.recyclerview.widget.StaggeredGridLayoutManager
//import com.example.market.MyApplication
//import com.example.market.R
//
//object HomeListDecoration : RecyclerView.ItemDecoration() {
//    var edgeSpacing: Int = 0
//    var spacing: Int = 0
//
//    init {
//        edgeSpacing = MyApplication.appContext.resources.getDimension(R.dimen.edgeSpacing).toInt()
//        spacing = MyApplication.appContext.resources.getDimension(R.dimen.spacing).toInt()
//    }
//
//    override fun getItemOffsets(
//        outRect: Rect,
//        view: View,
//        parent: RecyclerView,
//        state: RecyclerView.State
//    ) {
//        super.getItemOffsets(outRect, view, parent, state)
//
//        val layoutParams = view.layoutParams as StaggeredGridLayoutManager.LayoutParams
//
//        if (!layoutParams.isFullSpan) {
//            val spanIndex =
//                (view.layoutParams as StaggeredGridLayoutManager.LayoutParams).spanIndex
//
//            if (spanIndex == StaggeredGridLayoutManager.LayoutParams.INVALID_SPAN_ID) {
//                return
//            }
//            if (spanIndex == 0) {
//                outRect.set(edgeSpacing, spacing, spacing, spacing)
//            } else {
//                outRect.set(spacing,spacing,edgeSpacing,spacing
//                )
//            }
//        }
//    }
//}