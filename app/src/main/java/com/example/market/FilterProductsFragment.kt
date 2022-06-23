package com.example.market
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ArrayAdapter
//import com.example.market.binding.inflateBinding
//import com.example.market.databinding.FragmentProductFiltersLayoutBinding
//import com.google.android.material.bottomsheet.BottomSheetDialogFragment
//import com.google.firebase.firestore.Query
//
//class FilterProductsFragment(var filter: Filter,var doFilter: (filter: Filter) -> Unit) : BottomSheetDialogFragment() {
//    private var binding: FragmentProductFiltersLayoutBinding?=null
//
//    private var sortTypesArray = arrayListOf("Sold count","View count","New added","Cost")
//    private var listDirectionList = arrayListOf("Decrease","Increase")
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        binding= inflateBinding(container,R.layout.fragment_product_filters_layout)
//        binding?.apply {
//            sortTypeAutoCompleteTextView.apply {
//                setAdapter(ArrayAdapter(requireContext(),R.layout.auto_complete_item_view,sortTypesArray))
//                setOnItemClickListener { _, _, position, _ ->
//                    filter.sortBy = when(position) {
//                        0-> FILTER_SOLD_COUNT
//                        1-> FILTER_VIEWS_COUNT
//                        2-> FILTER_DATE
//                        3-> FILTER_NARXI
//                        else -> 0
//                    }
//                }
//                setText(sortTypesArray[filter.sortBy],false)
//            }
//            listDirectionAutoCompleteTextView.apply {
//                setAdapter(ArrayAdapter(requireContext(),R.layout.auto_complete_item_view,listDirectionList))
//                setOnItemClickListener { _, _, position, _ ->
//                    filter.direction = if (position == 0) Query.Direction.DESCENDING else Query.Direction.ASCENDING
//                }
//                setText(listDirectionList[if (filter.direction == Query.Direction.DESCENDING) 0 else 1],false)
//            }
//            filter.priceRange?.let {
//                minPriceEditText.text.append(it.minPrice.toString())
//                maxPriceEditText.text.append(it.maxPrice.toString())
//            }
//            showResults.setOnClickListener {
//                val minNarxi = minPriceEditText.text.toString()
//                val maxNarxi = maxPriceEditText.text.toString()
//                val freeShipping = checkBox.isChecked
//                filter.apply {
//                    val hasPriceRange = minNarxi.isNotEmpty() || maxNarxi.isNotEmpty()
//                    if (hasPriceRange) {
//                        priceRange = PriceRange().apply {
//                            minPrice = if (minNarxi.isNotEmpty()) minNarxi.toLong() else -1L
//                            maxPrice = if (maxNarxi.isNotEmpty()) maxNarxi.toLong() else -1L
//                        }
//                    }
//                    this.freeShipping = freeShipping
//                }
//                doFilter(filter)
//                dismiss()
//            }
//        }
//        return binding?.root
//    }
//}
