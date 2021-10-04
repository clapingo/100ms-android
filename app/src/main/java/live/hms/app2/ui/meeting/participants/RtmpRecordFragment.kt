package live.hms.app2.ui.meeting.participants

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import live.hms.app2.databinding.LayoutRtmpRecordingBinding
import live.hms.app2.ui.meeting.MeetingViewModel
import live.hms.app2.ui.settings.SettingsStore
import live.hms.app2.util.viewLifecycle
import java.net.URI
import java.net.URISyntaxException

class RtmpRecordFragment : Fragment() {

    private var binding by viewLifecycle<LayoutRtmpRecordingBinding>()
    private lateinit var settings: SettingsStore
    private val rtmpUrladapter: RtmpRecordAdapter = RtmpRecordAdapter(::removeItem)


    private val meetingViewModel: MeetingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = LayoutRtmpRecordingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settings = SettingsStore(requireContext())
        // Get a listener on the page for the urls.

        binding.addRtmpUrlButton.setOnClickListener { addItem() }
        rtmpUrladapter.submitList(settings.rtmpUrlsList.toList())
        binding.rtmpUrls.layoutManager = LinearLayoutManager(context)
        binding.rtmpUrls.adapter = rtmpUrladapter
        binding.startButton.setOnClickListener { startClicked() }

        // Load the role for the bot from preferences.
    }

    private fun addItem() {
        val urlToAdd = binding.newRtmpUrl.editableText.toString()
        when {
            urlToAdd.isEmpty() -> {
                Toast.makeText(context, "Invalid url", Toast.LENGTH_LONG).show()
            }
            checkValidUrl(urlToAdd) -> {
                Toast.makeText(context, "Invalid url, must start with RTMP", Toast.LENGTH_LONG)
                    .show()
            }
            else -> {
                // URI is a valid rtmp url
                rtmpUrladapter.submitList(rtmpUrladapter.currentList.plus(urlToAdd))
                settings.rtmpUrlsList = rtmpUrladapter.currentList.toSet()

            }
        }
    }

    private fun checkValidUrl(url: String): Boolean = try {
        val uri = URI(url)
        uri.host == "rtmp"
    } catch (e: URISyntaxException) {
        false
    }

    private fun startClicked() {
        // Create a config and start
        val rtmpStreamingUrls = settings.rtmpUrlsList
        val isRecording = binding.shouldRecord.isChecked
        meetingViewModel.recordMeeting(isRecording, settings.rtmpUrlsList.toList())
        findNavController().popBackStack()
    }

    private fun removeItem(url: String) {
        Log.d("rtmprecord", "clicked $url")
        // Remove this item from the adapter.
        rtmpUrladapter.submitList(rtmpUrladapter.currentList.minus(url))
        // Actually remove it from preferences
        settings.rtmpUrlsList = rtmpUrladapter.currentList.toSet()
    }
}