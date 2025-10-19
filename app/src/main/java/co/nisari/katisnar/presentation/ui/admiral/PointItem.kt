package co.nisari.katisnar.presentation.ui.admiral

import android.graphics.Color
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import co.nisari.katisnar.R
import co.nisari.katisnar.presentation.util.DoubleRangeInputFilter
import com.google.android.material.card.MaterialCardView
import kotlin.math.roundToInt

data class PointItem(
    var lat: String = "",
    var lng: String = "",
    var location: String = ""
)

class PointAdapter(
    private val onLatChanged: (index: Int, value: String) -> Unit,
    private val onLngChanged: (index: Int, value: String) -> Unit,
    private val onLocationChanged: (index: Int, value: String) -> Unit,
    private val onSave: (index: Int) -> Unit = {},
    private val onRemove: (index: Int) -> Unit = {}
) : RecyclerView.Adapter<PointAdapter.VH>() {

    var validationActivated: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            notifyDataSetChanged()
        }

    private val items = mutableListOf<PointItem>()

    /** Обновляем все элементы из VM */
    fun submit(newItems: List<PointItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun snapshotItems(): List<PointItem> = items.map { it.copy() }


    /** Добавляем новую точку */
    fun addPoint(item: PointItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    /** Удаляем по индексу */
    fun removeAt(index: Int) {
        if (index in items.indices) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun showLocationErrors(indices: Set<Int>) {
        val filtered = indices.filter { it in items.indices }.toSet()
        val previous = locationErrorPositions
        locationErrorPositions = filtered
        val changed = (previous union filtered) - (previous intersect filtered)
        changed.forEach { index ->
            if (index in items.indices) notifyItemChanged(index)
        }
    }

    fun showCoordinateErrors(indices: Set<Int>) {
        val filtered = indices.filter { it in items.indices }.toSet()
        val previous = coordinateErrorPositions
        coordinateErrorPositions = filtered
        val changed = (previous union filtered) - (previous intersect filtered)
        changed.forEach { index ->
            if (index in items.indices) notifyItemChanged(index)
        }
    }


    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val etLat: EditText = view.findViewById(R.id.et_latitude1)
        private val etLng: EditText = view.findViewById(R.id.et_longitude1)
        private val etLocation: EditText = view.findViewById(R.id.et_location1)
        private val tvTitle: TextView = view.findViewById(R.id.tv_point_title)
        private val btnDelete: View = view.findViewById(R.id.btn_delete_point)
        private val cardCoordinates: MaterialCardView = view.findViewById(R.id.card_coordinates)
        private val cardLocation: MaterialCardView = view.findViewById(R.id.card_location)

        private val errorStrokeColor = ContextCompat.getColor(view.context, R.color.seg_border)
        private val errorStrokeWidth = view.resources.getDimensionPixelSize(R.dimen.stroke_2dp)
        private val normalStrokeColor = Color.parseColor("#B8FFFFFF")
        private val normalStrokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            1f,
            view.resources.displayMetrics
        ).roundToInt().coerceAtLeast(1)

        private var latWatcher: TextWatcher? = null
        private var lngWatcher: TextWatcher? = null
        private var locWatcher: TextWatcher? = null

        private fun coordEmpty(idx: Int): Boolean {
            if (idx !in items.indices) return false
            val point = items[idx]
            return point.lat.isBlank() || point.lng.isBlank()
        }

        private fun locEmpty(idx: Int): Boolean {
            if (idx !in items.indices) return false
            return items[idx].location.isBlank()
        }

        private fun applyCoordinateUI(idx: Int) {
            if (idx !in items.indices) return
            val showError = validationActivated && coordEmpty(idx)
            cardCoordinates.strokeWidth = if (showError) errorStrokeWidth else normalStrokeWidth
            cardCoordinates.strokeColor = if (showError) errorStrokeColor else normalStrokeColor
        }

        private fun applyLocationUI(idx: Int) {
            if (idx !in items.indices) return
            val showError = validationActivated && locEmpty(idx)
            cardLocation.strokeWidth = if (showError) errorStrokeWidth else normalStrokeWidth
            cardLocation.strokeColor = if (showError) errorStrokeColor else normalStrokeColor
            etLocation.error = if (showError) {
                itemView.context.getString(R.string.error_location_required)
            } else {
                null
            }
        }

        init {
            val context = itemView.context
            etLat.filters = arrayOf(
                DoubleRangeInputFilter(-90.0, 90.0) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.toast_latitude_range),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
            etLng.filters = arrayOf(
                DoubleRangeInputFilter(-180.0, 180.0) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.toast_longitude_range),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        fun bind(position: Int) {
            val item = items[position]

            tvTitle.text = itemView.context.getString(R.string.point_title_placeholder, position + 1)

            // --- LAT ---
            latWatcher?.let { etLat.removeTextChangedListener(it) }
            if (etLat.text.toString() != item.lat) etLat.setText(item.lat)
            latWatcher = etLat.doAfterTextChanged { text ->
                val v = text?.toString().orEmpty()
                val idx = bindingAdapterPosition
                if (idx in items.indices && items[idx].lat != v) {
                    items[idx].lat = v
                    onLatChanged(idx, v)
                }
                clearCoordinateErrorIfResolved(idx)
                applyCoordinateErrorState(idx)
            }

            // --- LNG ---
            lngWatcher?.let { etLng.removeTextChangedListener(it) }
            if (etLng.text.toString() != item.lng) etLng.setText(item.lng)
            lngWatcher = etLng.doAfterTextChanged { text ->
                val v = text?.toString().orEmpty()
                val idx = bindingAdapterPosition
                if (idx in items.indices && items[idx].lng != v) {
                    items[idx].lng = v
                    onLngChanged(idx, v)
                }
                clearCoordinateErrorIfResolved(idx)
                applyCoordinateErrorState(idx)
            }

            // --- LOCATION ---
            locWatcher?.let { etLocation.removeTextChangedListener(it) }
            if (etLocation.text.toString() != item.location) etLocation.setText(item.location)
            locWatcher = etLocation.doAfterTextChanged { text ->
                val v = text?.toString().orEmpty()
                val idx = bindingAdapterPosition
                if (idx in items.indices && items[idx].location != v) {
                    items[idx].location = v
                    onLocationChanged(idx, v)
                }
                if (locationErrorPositions.contains(idx)) {
                    etLocation.error = if (v.isBlank()) {
                        itemView.context.getString(R.string.error_location_required)
                    } else {
                        null
                    }
                }
                if (!locationErrorPositions.contains(idx)) {
                    etLocation.error = null
                }
                clearLocationErrorIfResolved(idx)
                applyLocationErrorState(idx)
            }

            btnDelete.visibility = View.VISIBLE
            btnDelete.setOnClickListener {
                val idx = bindingAdapterPosition
                if (idx in items.indices) {
                    onRemove(idx)
                }
            }

            etLocation.error = if (locationErrorPositions.contains(position)) {
                itemView.context.getString(R.string.error_location_required)
            } else {
                null
            }

            applyCoordinateErrorState(position)
            applyLocationErrorState(position)
        }

        private fun applyCoordinateErrorState(position: Int) {
            if (position == RecyclerView.NO_POSITION) return
            val showError = coordinateErrorPositions.contains(position)
            if (showError) {
                cardCoordinates.strokeWidth = errorStrokeWidth
                cardCoordinates.strokeColor = errorStrokeColor
            } else {
                cardCoordinates.strokeWidth = normalStrokeWidth
                cardCoordinates.strokeColor = normalStrokeColor
            }
        }

        private fun applyLocationErrorState(position: Int) {
            if (position == RecyclerView.NO_POSITION) return
            val showError = locationErrorPositions.contains(position)
            if (showError) {
                cardLocation.strokeWidth = errorStrokeWidth
                cardLocation.strokeColor = errorStrokeColor
            } else {
                cardLocation.strokeWidth = normalStrokeWidth
                cardLocation.strokeColor = normalStrokeColor
            }
        }

        private fun clearLocationErrorIfResolved(position: Int) {
            if (position == RecyclerView.NO_POSITION) return
            if (!locationErrorPositions.contains(position)) return
            val item = items.getOrNull(position) ?: return
            if (item.location.isNotBlank()) {
                locationErrorPositions = locationErrorPositions - position
            }
        }
    }
    private fun clearCoordinateErrorIfResolved(position: Int) {
        if (position == RecyclerView.NO_POSITION) return
        if (!coordinateErrorPositions.contains(position)) return
        val item = items.getOrNull(position) ?: return
        if (item.lat.isNotBlank() && item.lng.isNotBlank()) {
            coordinateErrorPositions = coordinateErrorPositions - position
        }
    }

    private fun clearLocationErrorIfResolved(position: Int) {
        if (position == RecyclerView.NO_POSITION) return
        if (!locationErrorPositions.contains(position)) return
        val item = items.getOrNull(position) ?: return
        if (item.location.isNotBlank()) {
            locationErrorPositions = locationErrorPositions - position
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_point, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(position)

    override fun getItemCount(): Int = items.size
}