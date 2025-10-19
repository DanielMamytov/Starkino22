package co.nisari.katisnar.presentation.ui.admiral

import android.graphics.Color
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import co.nisari.katisnar.R
import co.nisari.katisnar.presentation.util.DoubleRangeInputFilter
import com.google.android.material.card.MaterialCardView

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

    private val items = mutableListOf<PointItem>()
    private var locationErrorPositions: Set<Int> = emptySet()

    /** Обновляем все элементы из VM */
    fun submit(newItems: List<PointItem>) {
        items.clear()
        items.addAll(newItems)
        locationErrorPositions = locationErrorPositions.filter { it < newItems.size }.toSet()
        notifyDataSetChanged()
    }

    /** Добавляем новую точку */
    fun addPoint(item: PointItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    /** Удаляем по индексу */
    fun removeAt(index: Int) {
        if (index in items.indices) {
            items.removeAt(index)
            locationErrorPositions = locationErrorPositions
                .filter { it != index }
                .map { if (it > index) it - 1 else it }
                .toSet()
            notifyItemRemoved(index)
        }
    }

    fun showLocationErrors(indices: Set<Int>) {
        locationErrorPositions = indices
        notifyDataSetChanged()
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

        private var latWatcher: TextWatcher? = null
        private var lngWatcher: TextWatcher? = null
        private var locWatcher: TextWatcher? = null

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
                if (items.getOrNull(bindingAdapterPosition)?.lat != v) {
                    items[bindingAdapterPosition].lat = v
                    onLatChanged(bindingAdapterPosition, v)
                }
                applyCoordinateErrorState(bindingAdapterPosition)
            }

            // --- LNG ---
            lngWatcher?.let { etLng.removeTextChangedListener(it) }
            if (etLng.text.toString() != item.lng) etLng.setText(item.lng)
            lngWatcher = etLng.doAfterTextChanged { text ->
                val v = text?.toString().orEmpty()
                if (items.getOrNull(bindingAdapterPosition)?.lng != v) {
                    items[bindingAdapterPosition].lng = v
                    onLngChanged(bindingAdapterPosition, v)
                }
                applyCoordinateErrorState(bindingAdapterPosition)
            }

            // --- LOCATION ---
            locWatcher?.let { etLocation.removeTextChangedListener(it) }
            if (etLocation.text.toString() != item.location) etLocation.setText(item.location)
            locWatcher = etLocation.doAfterTextChanged { text ->
                val v = text?.toString().orEmpty()
                if (items.getOrNull(bindingAdapterPosition)?.location != v) {
                    items[bindingAdapterPosition].location = v
                    onLocationChanged(bindingAdapterPosition, v)
                }
                if (locationErrorPositions.contains(bindingAdapterPosition)) {
                    etLocation.error = if (v.isBlank()) {
                        itemView.context.getString(R.string.error_location_required)
                    } else {
                        null
                    }
                }
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
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_point, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(position)

    override fun getItemCount(): Int = items.size
}
