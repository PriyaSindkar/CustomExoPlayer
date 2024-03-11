package com.priyasindkar.customexoplayer.utils

import android.content.Context
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.priyasindkar.customexoplayer.R
import com.priyasindkar.customexoplayer.ui.theme.BlurTextFieldBg
import com.priyasindkar.customexoplayer.ui.theme.GreyButtonColor

@Composable
@ReadOnlyComposable
fun fontDimensionResource(@DimenRes id: Int) = dimensionResource(id = id).value.sp


@Composable
fun ButtonWithText(
    context: Context,
    title: String,
    contentDescription: String,
    @DrawableRes icon: Int?,
    modifier: Modifier = Modifier,
    aContentColor: Color = GreyButtonColor,
    aContainerColor: Color = Color.Transparent,
    textSize: TextUnit = fontDimensionResource(id = R.dimen._20sp),
    textPadding: Modifier = Modifier
        .padding(end = dimensionResource(id = R.dimen._4dp)),
    onClick: () -> Unit,
) {
    TextButton(
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            contentColor = aContentColor,
            containerColor = aContainerColor
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.wrapContentSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = contentDescription
                )
            }
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen._4dp)))
            Text(
                modifier = textPadding,
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = textSize,
                    color = aContentColor
                )
            )
        }
    }
}

@Composable
fun PlayPauseButton(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    progress: Float = 0f,
    onClick: () -> Unit,
    content: @Composable() () -> Unit
) {
    Box(
        modifier = modifier
            .width(150.dp)
            .height(150.dp)
            .background(shape = CircleShape, color = BlurTextFieldBg)
            .clickable {
                onClick.invoke()
            }
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(150.dp)
                    .height(150.dp)
                    .align(Alignment.Center),
                strokeWidth = dimensionResource(id = R.dimen._4dp),
                color = Color.White,
                trackColor = BlurTextFieldBg,
                strokeCap = StrokeCap.Square
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(150.dp)
                    .height(150.dp)
                    .align(Alignment.Center),
                strokeWidth = dimensionResource(id = R.dimen._4dp),
                color = Color.White,
                trackColor = BlurTextFieldBg,
                progress = progress,
                strokeCap = StrokeCap.Square
            )
        }
        Row(modifier = Modifier.align(Alignment.Center)) {
            content()
        }
    }
}
@Composable
fun LoadingIndicator(color: Color = MaterialTheme.colorScheme.secondary, trackColor: Color = MaterialTheme.colorScheme.primary) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            color = color,
            trackColor = trackColor
        )
    }
}

@Composable
fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }

@Composable
fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }