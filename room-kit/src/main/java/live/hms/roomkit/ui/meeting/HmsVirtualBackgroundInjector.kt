package live.hms.roomkit.ui.meeting

import live.hms.video.plugin.video.virtualbackground.HmsVirtualBackgroundInterface
import live.hms.video.sdk.HMSSDK
import live.hms.video.virtualbackground.HMSVirtualBackground

class HmsVirtualBackgroundInjector(private val hmsSdk : HMSSDK) {
    val vbPlugin : HmsVirtualBackgroundInterface
    val isEnabled : Boolean
    init {
        // implementation "live.100ms:virtual-background:$hmsVersion"
        // without the above import being added, this will return false.
        val isVbImportAdded = try {
            // the class is not local or an anonymous object so this can't be null
            // and if it was turned into a local one the crash would happen every time and
            // be noticeable
            val qualifiedName = HMSVirtualBackground::class.qualifiedName!!
            Class.forName(qualifiedName)
            true
        } catch (ex : ClassNotFoundException) {
            false
        } catch (ex : NoClassDefFoundError) {
            false
        }
        isEnabled = isVbImportAdded
        vbPlugin = if(isVbImportAdded) HMSVirtualBackground(hmsSdk) else FakeVirtualBackground()

    }
}