package com.impostor.app.ui.components

import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.xmlpull.v1.XmlPullParser

/** Loads the project's path-only SVG artwork without AAPT's path-string size restriction. */
@Composable
fun rememberSvgPainter(@RawRes resource: Int): Painter {
    val resources = LocalContext.current.resources
    val vector = remember(resources, resource) {
        val parser = android.util.Xml.newPullParser()
        resources.openRawResource(resource).use { input ->
            parser.setInput(input, "UTF-8")
            var builder: ImageVector.Builder? = null
            var inheritedFill = "#000000"
            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == XmlPullParser.START_TAG) {
                    when (parser.name) {
                        "svg" -> {
                            inheritedFill = parser.getAttributeValue(null, "fill") ?: "#000000"
                            val bounds = requireNotNull(parser.getAttributeValue(null, "viewBox"))
                                .trim().split(Regex("[ ,]+" )).map(String::toFloat)
                            require(bounds.size == 4 && bounds[0] == 0f && bounds[1] == 0f)
                            builder = ImageVector.Builder(resources.getResourceEntryName(resource),
                                bounds[2].dp, bounds[3].dp, bounds[2], bounds[3])
                        }
                        "path" -> {
                            val fill = parser.getAttributeValue(null, "fill") ?: inheritedFill
                            val stroke = parser.getAttributeValue(null, "stroke")
                            requireNotNull(builder).addPath(
                                pathData = PathParser().parsePathString(requireNotNull(parser.getAttributeValue(null, "d"))).toNodes(),
                                pathFillType = if (parser.getAttributeValue(null, "fill-rule") == "evenodd")
                                    PathFillType.EvenOdd else PathFillType.NonZero,
                                fill = if (fill == "none") null else svgBrush(fill),
                                stroke = stroke?.takeUnless { it == "none" }?.let(::svgBrush),
                                strokeLineWidth = parser.getAttributeValue(null, "stroke-width")?.toFloat() ?: 0f,
                                strokeLineJoin = when (parser.getAttributeValue(null, "stroke-linejoin")) {
                                    "round" -> StrokeJoin.Round
                                    "bevel" -> StrokeJoin.Bevel
                                    else -> StrokeJoin.Miter
                                }
                            )
                        }
                        else -> error("Unsupported SVG element: ${parser.name}")
                    }
                }
                parser.next()
            }
            requireNotNull(builder).build()
        }
    }
    return rememberVectorPainter(vector)
}

private fun svgBrush(value: String) = SolidColor(Color(android.graphics.Color.parseColor(value)))
