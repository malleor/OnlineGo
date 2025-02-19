package io.zenandroid.onlinego.ui.screens.stats

import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM
import android.view.MotionEvent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.LineDataSet.Mode.LINEAR
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.ChartTouchListener.ChartGesture
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.EntryXComparator
import io.zenandroid.onlinego.R
import io.zenandroid.onlinego.R.color
import io.zenandroid.onlinego.ui.screens.stats.StatsViewModel.Filter
import io.zenandroid.onlinego.ui.screens.stats.StatsViewModel.Filter.ALL
import io.zenandroid.onlinego.ui.screens.stats.StatsViewModel.Filter.FIVE_YEARS
import io.zenandroid.onlinego.ui.screens.stats.StatsViewModel.Filter.ONE_MONTH
import io.zenandroid.onlinego.ui.screens.stats.StatsViewModel.Filter.ONE_YEAR
import io.zenandroid.onlinego.ui.screens.stats.StatsViewModel.Filter.THREE_MONTHS
import io.zenandroid.onlinego.utils.egfToRank
import io.zenandroid.onlinego.utils.formatRank
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ChartWrapper(
  chartData: List<Entry>,
  filter: Filter,
  onFilterChanged: (Filter) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier) {
    var text by remember { mutableStateOf(AnnotatedString("")) }
    Text(
      text = text,
      modifier = Modifier.padding(start = 8.dp)
    )
    AndroidView(
      factory = { context ->
        LineChart(context).apply {
          val chart = this
          xAxis.apply {
            position = BOTTOM
            valueFormatter = DayAxisValueFormatter(chart)
            setDrawAxisLine(false)
            setDrawLabels(false)
            textColor = ResourcesCompat.getColor(resources, color.colorText, context.theme)
            setDrawGridLines(false)
          }

          axisLeft.apply {
            setDrawGridLines(false)
            textColor = ResourcesCompat.getColor(resources, color.colorText, context.theme)
            setDrawLabels(false)
            setDrawAxisLine(false)
          }

          axisRight.apply {
            setDrawGridLines(false)
            setDrawLabels(false)
            setDrawAxisLine(false)
            valueFormatter = object : ValueFormatter() {
              override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return formatRank(egfToRank(value.toDouble()))
              }
            }
          }

          ChartValueSelectedListener.chart = this
          ChartValueSelectedListener.onTextChanged = { text = it }

          val onChartValueSelectedListener = ChartValueSelectedListener
          ChartValueSelectedListener.onNothingSelected()

          setOnChartValueSelectedListener(onChartValueSelectedListener)

          onChartGestureListener = object : OnChartGestureListener {
            override fun onChartGestureStart(
              me: MotionEvent,
              lastPerformedGesture: ChartGesture
            ) {
              data?.setDrawValues(false)
            }

            override fun onChartGestureEnd(
              me: MotionEvent,
              lastPerformedGesture: ChartGesture
            ) {
              data?.setDrawValues(false)

              onChartValueSelectedListener.onNothingSelected()
              highlightValues(null)
            }

            override fun onChartLongPressed(me: MotionEvent) {}
            override fun onChartDoubleTapped(me: MotionEvent) {}
            override fun onChartSingleTapped(me: MotionEvent) {}
            override fun onChartFling(
              me1: MotionEvent,
              me2: MotionEvent,
              velocityX: Float,
              velocityY: Float
            ) {
            }

            override fun onChartScale(me: MotionEvent, scaleX: Float, scaleY: Float) {}
            override fun onChartTranslate(me: MotionEvent, dX: Float, dY: Float) {}
          }

          setViewPortOffsets(0f, 0f, 0f, 0f)

          description.isEnabled = false
          setDrawMarkers(false)
          setNoDataText("No ranked games on record")
          setNoDataTextColor(
            ResourcesCompat.getColor(
              resources,
              color.colorActionableText,
              context.theme
            )
          )
          legend.isEnabled = false
          isDoubleTapToZoomEnabled = false

          animateY(250)
          invalidate()
          notifyDataSetChanged()
        }
      },
      update = {
        val entries = chartData.sortedWith(EntryXComparator())
        val rankDataSet = LineDataSet(entries, "Games").apply {
          setDrawIcons(false)
          lineWidth = 1.3f
          highLightColor = android.graphics.Color.GRAY
          highlightLineWidth = 0.7f
          enableDashedHighlightLine(7f, 2f, 0f)
          setDrawCircles(false)
          setDrawValues(false)
          color =
            ResourcesCompat.getColor(it.resources, R.color.rankGraphLine, it.context?.theme)
          mode = LINEAR
          setDrawFilled(true)
          fillDrawable = GradientDrawable(
            TOP_BOTTOM,
            arrayOf(
              ResourcesCompat.getColor(
                it.resources,
                R.color.rankGraphLine,
                it.context?.theme
              ),
              android.graphics.Color.TRANSPARENT
            ).toIntArray()
          )
        }
        if (entries.isNotEmpty()) {
          it.axisLeft.axisMinimum = entries.minOf { it.y } * .85f
          it.axisRight.axisMinimum = entries.minOf { it.y } * .85f
        }
        it.data = LineData(rankDataSet)
        if(!ChartValueSelectedListener.isDragging) {
          ChartValueSelectedListener.onNothingSelected()
        }
        it.invalidate()
      },
      modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
    )
    TimeRangeTabs(filter = filter, onFilterChanged = onFilterChanged)
  }
}

private val shortFormat = DateTimeFormatter.ofPattern("d MMM uuuu")

private fun formatDate(secondsSinceEpoch: Long): String {
  return shortFormat.format(
    LocalDateTime.ofEpochSecond(
      secondsSinceEpoch,
      0,
      OffsetDateTime.now().offset
    )
  )
}

@Composable
private fun TimeRangeTabs(
  filter: Filter,
  onFilterChanged: (Filter) -> Unit
) {
  TabRow(
    selectedTabIndex = Filter.values().indexOf(filter),
    backgroundColor = Color.Transparent,
    contentColor = MaterialTheme.colors.primary,
  ) {
    Filter.values().forEach { range ->
      Tab(
        selected = range == filter,
        onClick = { onFilterChanged(range) },
        text = { Text(text = range.toPrettyName()) },
        selectedContentColor = MaterialTheme.colors.primary,
        unselectedContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
      )
    }
  }
}

private fun Filter.toPrettyName() =
  when (this) {
    ONE_MONTH -> "1M"
    THREE_MONTHS -> "3M"
    ONE_YEAR -> "1Y"
    FIVE_YEARS -> "5Y"
    ALL -> "All"
  }

private object ChartValueSelectedListener: OnChartValueSelectedListener {
  var chart: LineChart? = null
  var onTextChanged: (AnnotatedString) -> Unit = {}
  var isDragging = false
    private set

  override fun onValueSelected(e: Entry?, h: Highlight?) {
    if (e == null) {
      onNothingSelected()
    } else {
      isDragging = true
      onTextChanged(
        buildAnnotatedString {
          withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(
              "${e.y.toInt()} ELO (${
                formatRank(
                  egfToRank(e.y.toDouble()),
                  longFormat = true
                )
              })"
            )
          }
          append(" on ${formatDate(e.x.toLong())}")
        }
      )
    }
  }

  override fun onNothingSelected() {
    isDragging = false
    if ((chart?.data?.entryCount ?: 0) != 0) {
      val entries = (chart?.data as LineData).dataSets[0]
      val last = entries.getEntryForXValue(entries.xMax, 0f)
      val first = entries.getEntryForXValue(entries.xMin, 0f)
      val delta = (last.y - first.y).toInt()
      val color = if (delta < 0) Color(0xFFC63A38) else Color(0xFF90b06e)
      val arrow = if (delta < 0) "⬇" else "⬆"
      onTextChanged(
        buildAnnotatedString {
          withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold)) {
            append("$arrow $delta ELO")
          }
          append(" since ${formatDate(first.x.toLong())}")
        }
      )
    }
  }
}



