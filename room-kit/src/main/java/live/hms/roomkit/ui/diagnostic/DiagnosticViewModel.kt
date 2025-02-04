package live.hms.roomkit.ui.diagnostic

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import live.hms.video.audio.HMSAudioManager
import live.hms.video.diagnostics.HMSAudioDeviceCheckListener
import live.hms.video.diagnostics.HMSCameraCheckListener
import live.hms.video.diagnostics.HMSDiagnostics
import live.hms.video.diagnostics.models.ConnectivityCheckResult
import live.hms.video.diagnostics.models.ConnectivityState
import live.hms.video.error.HMSException
import live.hms.video.media.settings.HMSVideoTrackSettings
import live.hms.video.media.tracks.HMSVideoTrack
import live.hms.video.sdk.HMSAudioListener
import live.hms.video.sdk.HMSSDK
import live.hms.video.sdk.models.HMSSpeaker
import java.util.UUID

class DiagnosticViewModel(application: Application) : AndroidViewModel(application) {
    // First create a new sdk instance
    private var regionCode = "in"
    private val diagnosticProvider = DiagnosticProvider(application)
    private val diagnosticSDK  : HMSDiagnostics
        get() {return diagnosticProvider.getDiagnosticSdk()}
    private val hmsSDK : HMSSDK
        get() { return diagnosticProvider.getSdk()}
    val cameraTrackLiveData = MutableLiveData<HMSVideoTrack?>()
    fun cameraPermssionGranted() {
        diagnosticSDK.startCameraCheck(
            HMSVideoTrackSettings.CameraFacing.FRONT,
            object : HMSCameraCheckListener {
                override fun onError(error: HMSException) {

                }

                override fun onVideoTrack(localVideoTrack: HMSVideoTrack) {
                    cameraTrackLiveData.postValue(localVideoTrack)
                }
            })
    }

    fun getRegionList() = listOf(Pair("in", "India"), Pair("eu", "Europe"), Pair("us", "US"))
    fun setRegionPreference(regionName: String) {
        // Set the region preference
        getRegionList().forEach {
            if (it.second == regionName) {
                regionCode = it.first
            }
        }
    }

    fun stopCameraCheck() {
        diagnosticSDK.stopCameraCheck()
        cameraTrackLiveData.postValue(null)
    }

    fun initSDK() {

    }

    fun stopMicCheck() {
        isRecording = false
        kotlin.runCatching { diagnosticSDK.stopMicCheck() }
    }

    var isRecording = false
    val audioLevelLiveData = MutableLiveData<Int>()
    fun startMicRecording() {
        isRecording = true
        diagnosticSDK.startMicCheck(getApplication<Application>(), object :
            HMSAudioDeviceCheckListener {
            override fun onError(error: HMSException) {

            }

            override fun onSuccess() {

            }

            override fun onAudioLevelChanged(decibel: Int) {
                super.onAudioLevelChanged(decibel)
                audioLevelLiveData.postValue(decibel)

            }

        })

    }


    fun stopRecording() {
        isRecording = false
        kotlin.runCatching {
            diagnosticSDK.stopMicCheck()
            diagnosticSDK.stopSpeakerCheck()
        }
    }

    fun switchAudioOutput(audioDevice: HMSAudioManager.AudioDevice) {
        hmsSDK.switchAudioOutput(audioDevice)
    }

    fun getAudioDevicesInfoList() = hmsSDK.getAudioDevicesInfoList()
    fun getAudioOutputRouteType(): HMSAudioManager.AudioDevice {
        return hmsSDK.getAudioOutputRouteType()
    }

    val connectivityLiveData = MutableLiveData<ConnectivityCheckResult?>(null)
    val connectivityStateLiveData = MutableLiveData<ConnectivityState?>(null)

    var isMediaCaptured : Boolean = false
    var isMediaPublished : Boolean = false
    fun startConnectivityTest() {
        diagnosticProvider.disposeOfDiagnostic()
        isMediaPublished= false
        isMediaCaptured = false
        connectivityLiveData.postValue(null)
        connectivityStateLiveData.postValue(null)
        diagnosticSDK.startConnectivityCheck(regionCode,
            object : live.hms.video.diagnostics.ConnectivityCheckListener {
                override fun onCompleted(result: ConnectivityCheckResult) {
                    connectivityLiveData.postValue(result)
                }
                override fun onConnectivityStateChanged(state: ConnectivityState) {
                    connectivityStateLiveData.postValue(state)
                    if (state == ConnectivityState.MEDIA_CAPTURED) {
                        isMediaCaptured = true
                    }
                    if (state == ConnectivityState.MEDIA_PUBLISHED) {
                        isMediaPublished = true
                    }
                }
            })
    }

    fun stopConnectivityTest() {
        diagnosticSDK.stopConnectivityCheck()
        connectivityLiveData.postValue(null)
        connectivityStateLiveData.postValue(null)
    }

    fun startSpeakerTest() {
       kotlin.runCatching {  diagnosticSDK.startSpeakerCheck() }
    }

   

}