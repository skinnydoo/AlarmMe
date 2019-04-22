package ca.poly.inf8405.alarmme.ui.alarmscreen


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import ca.poly.inf8405.alarmme.R
import ca.poly.inf8405.alarmme.ui.BaseFragment
import ca.poly.inf8405.alarmme.utils.LogWrapper
import ca.poly.inf8405.alarmme.viewmodel.CheckPointViewModel
import ca.poly.inf8405.alarmme.vo.CheckPoint
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_alarm_list.*
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 *
 */
class AlarmListFragment : BaseFragment(), AlarmListAdapter.OnAlarmListItemClickListener {
  private lateinit var listAdapter: AlarmListAdapter
  
  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory
  
  private lateinit var checkPointViewModel: CheckPointViewModel
  
  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_alarm_list, container, false)
  }
  
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    checkPointViewModel = ViewModelProviders.of(this, viewModelFactory)[CheckPointViewModel::class.java]
    initView()
    subscribeToModel()
  }
  
  
  override fun onResume() {
    super.onResume()
    listener?.onFragmentDisplayed(hideSearchBar = true, hideCurrentLocationFab = true)
  }
  
  
  private fun initView() {
    
    // TODO: add a No-Alarms placeholder recycler
    listAdapter = AlarmListAdapter(requireContext(), this )
    val linearLayout = LinearLayoutManager(requireContext())
    alarms_recycler?.apply {
      layoutManager = linearLayout
      
      adapter = listAdapter
      
      setHasFixedSize(true)
    }
  }
  
  override fun onAlarmSwithClicked(checkPoint: CheckPoint) {
    LogWrapper.d("Checkpoint ${checkPoint.name} active? -> ${checkPoint.active}")
    if (checkPoint.active) {
      val geofence = buildGeofence(
        checkPoint.id,
        LatLng(checkPoint.latitude, checkPoint.longitude),
        checkPoint.radius.toFloat()
      )
      addGeofence(geofence)
    } else {
      removeGeofence(checkPoint.id)
    }
    checkPointViewModel.updateCheckPoint(checkPoint)
  }
  
  override fun onItemClicked(checkPoint: CheckPoint) {
    LogWrapper.d("Starting Detail fragment...")
    findNavController().navigate(AlarmListFragmentDirections.nextAction(checkPoint))
  }
  
  private fun subscribeToModel() {
    checkPointViewModel.allCheckPoints.observe(viewLifecycleOwner, Observer {
      LogWrapper.d("Updating cached copy of the adapter -> ${it.joinToString(prefix = "[",
        postfix = "]")}")
      listAdapter.submitList(it)
    })
  }
  
  
}
