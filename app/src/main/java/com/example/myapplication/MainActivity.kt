package com.example.myapplication

import android.graphics.Paint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.gray
import com.example.myapplication.ui.theme.green
import com.example.myapplication.ui.theme.redOrange
import com.example.myapplication.ui.theme.white
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val calendarInputList by remember {
                mutableStateOf(createCalendarList())
            }
            var clickedCalendarElem by remember {
                mutableStateOf<CalendarInput?>(null)
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gray),
                contentAlignment = Alignment.TopCenter
            ){
                Calendar(
                    calendarInput = calendarInputList,
                    onDayClick = { day->
                        clickedCalendarElem = calendarInputList.first { it.day == day }
                    },
                    month = "September 2024",
                    modifier = Modifier
//                        .padding(10.dp)
                        .padding(10.dp,10.dp,10.dp,10.dp)

                        .fillMaxWidth()
                        .aspectRatio(1.0f)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp,70.dp,10.dp,10.dp)
//                        .padding(10.dp)

                        .align(Alignment.Center)
                ){
                    clickedCalendarElem?.toDos?.forEach {
                        Text(
                            if(it.contains("day")) it else "- $it",
                            color = white,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = if(it.contains("day")) 25.sp else 18.sp
                        )
                    }
                }
            }
        }
    }

    private fun createCalendarList(): List<CalendarInput> {
        val calendarInputs = mutableListOf<CalendarInput>()

        for (i in 1..30) {
            val toDos = when (i) {
                1 -> listOf("$i September: Sunday",)
                2 -> listOf("$i September: Monday")
                3 -> listOf("3 September: Tuesday")
                4 -> listOf("4 September: Wednesday")
                5 -> listOf("5 September: Thursday")
                6 -> listOf("6 September: Friday")
                7 -> listOf("7 September: Saturday", "Ganesh Chaturthi/Vinayaka Chaturthi")
                8 -> listOf("8 September: Sunday")
                9 -> listOf("9 September: Monday")
                10 -> listOf("10 September: Tuesday")
                11 -> listOf("11 September: Wednesday")
                12 -> listOf("12 September: Thursday")
                13 -> listOf("13 September: Friday")
                14 -> listOf("14 September: Saturday")
                15 -> listOf("15 September: Sunday", "Onam")
                16 -> listOf("16 September: Monday", "Milad un-Nabi/Id-e-Milad")
                17 -> listOf("17 September: Tuesday")
                18 -> listOf("18 September: Wednesday")
                19 -> listOf("19 September: Thursday")
                20 -> listOf("20 September: Friday")
                21 -> listOf("21 September: Saturday")
                22 -> listOf("22 September: Sunday")
                23 -> listOf("23 September: Monday")
                24 -> listOf("24 September: Tuesday")
                25 -> listOf("25 September: Wednesday")
                26 -> listOf("26 September: Thursday")
                27 -> listOf("27 September: Friday")
                28 -> listOf("28 September: Saturday")
                29 -> listOf("29 September: Sunday")
                30 -> listOf("30 September: Monday")
                else -> listOf("$i September:")
            }

            calendarInputs.add(
                CalendarInput(
                    i,
                    toDos = toDos
                )
            )
        }

        return calendarInputs
    }
}

private const val CALENDAR_ROWS = 5
private const val CALENDAR_COLUMNS = 7

@Composable
fun Calendar(
    modifier: Modifier = Modifier,
    calendarInput: List<CalendarInput>,
    onDayClick:(Int)->Unit,
    strokeWidth:Float = 15f,
    month:String
) {
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    var canvasSize by remember {
        mutableStateOf(Size.Zero)
    }
    var clickAnimationOffset by remember {
        mutableStateOf(Offset.Zero)
    }

    var animationRadius by remember {
        mutableStateOf(0f)
    }

    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Display the month at the top
        Text(
            text = month,
            fontWeight = FontWeight.SemiBold,
            color = white,
            fontSize = 40.sp
        )

        // Row for the days of the week
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    fontWeight = FontWeight.Bold,
                    color = white,
                    fontSize = 20.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(true) {
                    detectTapGestures(
                        onTap = { offset ->
                            val column =
                                (offset.x / canvasSize.width * CALENDAR_COLUMNS).toInt() + 1
                            val row = (offset.y / canvasSize.height * CALENDAR_ROWS).toInt() + 1
                            val day = column + (row - 1) * CALENDAR_COLUMNS
                            if (day <= calendarInput.size) {
                                onDayClick(day)
                                clickAnimationOffset = offset
                                scope.launch {
                                    animate(0f, 225f, animationSpec = tween(300)) { value, _ ->
                                        animationRadius = value
                                    }
                                }
                            }
                        }
                    )
                }
        ){
            val canvasHeight = size.height
            val canvasWidth = size.width
            canvasSize = Size(canvasWidth,canvasHeight)
            val ySteps = canvasHeight / CALENDAR_ROWS
            val xSteps = canvasWidth / CALENDAR_COLUMNS

            val column = (clickAnimationOffset.x / canvasSize.width * CALENDAR_COLUMNS).toInt() + 1
            val row = (clickAnimationOffset.y / canvasSize.height * CALENDAR_ROWS).toInt() + 1

            val path = Path().apply {
                moveTo((column-1) * xSteps, (row-1) * ySteps)
                lineTo(column * xSteps, (row-1) * ySteps)
                lineTo(column * xSteps, row * ySteps)
                lineTo((column-1) * xSteps, row * ySteps)
                close()
            }

            clipPath(path) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(green.copy(0.8f), redOrange.copy(0.2f)),
                        center = clickAnimationOffset,
                        radius = animationRadius + 0.1f
                    ),
                    radius = animationRadius + 0.1f,
                    center = clickAnimationOffset
                )
            }

            drawRoundRect(
                redOrange,
                cornerRadius = CornerRadius(25f, 25f),
                style = Stroke(
                    width = strokeWidth
                )
            )

            for (i in 1 until CALENDAR_ROWS) {
                drawLine(
                    color = redOrange,
                    start = Offset(0f, ySteps * i),
                    end = Offset(canvasWidth, ySteps * i),
                    strokeWidth = strokeWidth
                )
            }
            for (i in 1 until CALENDAR_COLUMNS) {
                drawLine(
                    color = redOrange,
                    start = Offset(xSteps * i, 0f),
                    end = Offset(xSteps * i, canvasHeight),
                    strokeWidth = strokeWidth
                )
            }
            val textHeight = 17.dp.toPx()
            for (i in calendarInput.indices) {
                val textPositionX = xSteps * (i % CALENDAR_COLUMNS) + strokeWidth
                val textPositionY = (i / CALENDAR_COLUMNS) * ySteps + textHeight + strokeWidth / 2
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        "${i + 1}",
                        textPositionX,
                        textPositionY,
                        Paint().apply {
                            textSize = textHeight
                            color = white.toArgb()
                            isFakeBoldText = true
                        }
                    )
                }
            }
        }
    }
}


