package live.hms.android100ms.ui.meeting.pinnedvideo

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import live.hms.android100ms.databinding.ListItemVideoBinding
import live.hms.android100ms.ui.meeting.MeetingTrack
import live.hms.android100ms.util.NameUtils
import live.hms.android100ms.util.SurfaceViewRendererUtil
import live.hms.android100ms.util.crashlyticsLog
import org.webrtc.RendererCommon

class VideoListAdapter(
  private val onVideoItemClick: (item: MeetingTrack) -> Unit
) : RecyclerView.Adapter<VideoListAdapter.VideoItemViewHolder>() {

  companion object {
    private const val TAG = "VideoListAdapter"
  }

  override fun onViewAttachedToWindow(holder: VideoItemViewHolder) {
    super.onViewAttachedToWindow(holder)
    // TODO: Limit the maximum number of SurfaceView's occupying EglContext
    Log.d(TAG, "onViewAttachedToWindow($holder)")
    holder.bindSurfaceView()
  }

  override fun onViewDetachedFromWindow(holder: VideoItemViewHolder) {
    super.onViewDetachedFromWindow(holder)
    Log.d(TAG, "onViewDetachedFromWindow($holder)")
    holder.unbindSurfaceView()
  }


  inner class VideoItemViewHolder(
    val binding: ListItemVideoBinding
  ) : RecyclerView.ViewHolder(binding.root) {

    private var itemRef: VideoListItem? = null

    fun bind(item: VideoListItem) {
      binding.nameInitials.text = NameUtils.getInitials(item.track.peer.userName)
      binding.name.text = item.track.peer.userName

      binding.surfaceView.apply {
        setEnableHardwareScaler(true)
        setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_BALANCED)

        // Meanwhile until the video is not binded, hide the view.
        visibility = View.GONE

        // Update the reference such that when view is attached to window
        // surface view is initialized with correct [VideoTrack]
        itemRef = item
      }

      binding.root.setOnClickListener { onVideoItemClick(item.track) }
    }

    /**
     * [bindSurfaceView] relies on [onViewAttachedToWindow] called `after`
     * [onBindViewHolder] is called.
     *
     * [unbindSurfaceView] relied on [onViewDetachedFromWindow] called `before`
     * [onBindViewHolder] is called such that before binding another item we
     * always release context occupied by the previous view.
     */

    fun bindSurfaceView() {
      itemRef?.let { item ->
        SurfaceViewRendererUtil.bind(
          binding.surfaceView,
          item.track,
          "VideoItemViewHolder::bindSurfaceView"
        ).let { success ->
          if (success) binding.surfaceView.visibility = View.VISIBLE
        }
      }
    }

    fun unbindSurfaceView() {
      itemRef?.let { item ->
        SurfaceViewRendererUtil.unbind(
          binding.surfaceView,
          item.track,
          "VideoItemViewHolder::unbindSurfaceView"
        ).let { success ->
          if (success) binding.surfaceView.visibility = View.GONE
        }
      }
    }
  }

  private val items = ArrayList<VideoListItem>()

  /**
   * @param newItems: Complete list of video items which needs
   *  to be updated in the VideoGrid
   */
  @MainThread
  fun setItems(newItems: MutableList<MeetingTrack>) {
    val newVideoItems = newItems.mapIndexed { index, track -> VideoListItem(index.toLong(), track) }

    val callback = VideoListItemDiffUtil(items, newVideoItems)
    val diff = DiffUtil.calculateDiff(callback)
    items.clear()
    items.addAll(newVideoItems)
    diff.dispatchUpdatesTo(this)

    crashlyticsLog(TAG, "Updated video list: size=${items.size}")
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoItemViewHolder {
    val binding = ListItemVideoBinding.inflate(
      LayoutInflater.from(parent.context),
      parent,
      false
    )

    crashlyticsLog(TAG, "onCreateViewHolder(viewType=$viewType)")
    return VideoItemViewHolder(binding)
  }

  override fun onBindViewHolder(holder: VideoItemViewHolder, position: Int) {
    crashlyticsLog(TAG, "onBindViewHolder: ${items[position]}")
    holder.bind(items[position])
  }


  override fun getItemCount() = items.size
}