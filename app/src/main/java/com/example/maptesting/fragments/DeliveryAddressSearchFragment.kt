package com.example.maptesting.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maptesting.R
import com.example.maptesting.adapters.RouteSelectionAdapter
import com.example.maptesting.data.building_route.Duration
import com.example.maptesting.databinding.FragmentDeliveryAddressSearchBinding
import com.example.maptesting.view_modal.DeliveryAddressSearchViewModal

class DeliveryAddressSearchFragment : Fragment() {

    private var _binding:  FragmentDeliveryAddressSearchBinding? = null
    private val binding get() = _binding!!

    val animals : ArrayList<Duration> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(DeliveryAddressSearchViewModal::class.java)

        _binding = FragmentDeliveryAddressSearchBinding.inflate(inflater, container, false)
        val root: View = binding.root


        val rv_animal_list = binding.rvAdapterSearchAdress

        binding.buttonBlack.setOnClickListener {
            findNavController().popBackStack()
        }
        addAnimals()

        rv_animal_list.layoutManager = LinearLayoutManager(root.context)
        rv_animal_list.adapter = RouteSelectionAdapter(R.layout.route_list_item ,animals)



        return root
    }

    fun addAnimals() {
        animals.add(Duration("Anjing" , 1))
        animals.add(Duration("Ayam", 2))
        animals.add(Duration("Bebek", 3))
        animals.add(Duration("Ular", 4))
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}