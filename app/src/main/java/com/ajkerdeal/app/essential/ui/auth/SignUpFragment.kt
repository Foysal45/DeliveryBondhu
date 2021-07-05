package com.ajkerdeal.app.essential.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.api.models.district.LocationData
import com.ajkerdeal.app.essential.databinding.FragmentSignUpBinding
import com.ajkerdeal.app.essential.ui.dialog.LocationSelectionDialog
import com.ajkerdeal.app.essential.utils.ViewState
import com.ajkerdeal.app.essential.utils.hideKeyboard
import com.ajkerdeal.app.essential.utils.toast
import org.koin.android.ext.android.inject
import java.util.*

class SignUpFragment : Fragment() {

    private var binding: FragmentSignUpBinding? = null

    private val viewModel: AuthViewModel by inject()

    private var districtId: Int = 0
    private var thanaId: Int = 0
    private var districtName: String = ""
    private var thanaName: String = ""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return inflater.inflate(R.layout.fragment_sign_up, container, false)
        return FragmentSignUpBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding?.lifecycleOwner = viewLifecycleOwner
        binding?.viewModel = viewModel

        viewModel.viewState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is ViewState.ShowMessage -> {
                    requireContext().toast(state.message)
                }
                is ViewState.KeyboardState -> {
                    hideKeyboard()
                }
                is ViewState.NextState -> {

                    findNavController().navigate(R.id.action_signUp_login)
                }
            }
        })

        binding?.registration?.setOnClickListener {
            findNavController().navigate(R.id.action_signUp_login)
        }

        binding?.districtET?.setOnClickListener {

            binding?.districtET?.isEnabled = false
            binding?.thanaProgress?.visibility = View.VISIBLE
            viewModel.loadLocationList().observe(viewLifecycleOwner, Observer { response ->

                val districtModelList = response.districtInfo
                binding?.thanaProgress?.visibility = View.GONE

                val locationList: MutableList<LocationData> = mutableListOf()
                districtModelList.forEach { model ->
                    locationList.add(LocationData(
                        model.districtId,
                        model.districtBng,
                        model.district,
                        "0",
                        model.district.lowercase(Locale.US) ?: "",
                        false
                    ))
                }

                val dialog = LocationSelectionDialog.newInstance(locationList)
                dialog.show(childFragmentManager, LocationSelectionDialog.tag)
                dialog.onLocationPicked = { model ->
                    //context?.toast(value)
                    districtName = model.displayNameBangla ?: ""
                    binding?.districtET?.text = model.displayNameBangla ?: ""
                    binding?.thanaET?.text = ""
                    districtId = model.id
                    viewModel.districtId.value = districtId
                    viewModel.address.value = districtName
                    viewModel.districtName.value = districtName

                    binding?.thanaLayout?.visibility = View.VISIBLE
                    viewModel.thanaName.value = ""
                    viewModel.thanaId.value = 0

                    thanaName = ""
                    thanaId = 0

                }

                binding?.districtET?.isEnabled = true
            })
        }

        binding?.thanaET?.setOnClickListener {

            binding?.thanaET?.isEnabled = false
            binding?.thanaProgress?.visibility = View.VISIBLE
            viewModel.loadLocationList(districtId).observe(viewLifecycleOwner, Observer { response ->

                val thanaModelList = response.districtInfo[0].thanaHome
                binding?.thanaProgress?.visibility = View.GONE

                val locationList: MutableList<LocationData> = mutableListOf()
                thanaModelList.forEach { model ->
                    locationList.add(LocationData(
                        model.thanaId,
                        model.thanaBng,
                        model.thana,
                        model.postalCode,
                        model.thana.lowercase(Locale.US) ?: "",
                        false
                    ))
                }

                val dialog = LocationSelectionDialog.newInstance(locationList)
                dialog.show(childFragmentManager, LocationSelectionDialog.tag)
                dialog.onLocationPicked = { model ->
                    //context?.toast(value)
                    thanaName = model.displayNameBangla ?: ""
                    binding?.thanaET?.text = model.displayNameBangla ?: ""

                    thanaId = model.id
                    viewModel.thanaId.value = thanaId
                    viewModel.postCode.value = model.displayPostalCode?.toIntOrNull() ?: 0
                    viewModel.address.value = "$thanaName, $districtName"
                    viewModel.thanaName.value = thanaName
                }

                binding?.thanaET?.isEnabled = true
            })
        }
    }

    override fun onDestroyView() {
        viewModel.clearSignUp()
        super.onDestroyView()
        binding?.unbind()
        binding = null
    }
}
