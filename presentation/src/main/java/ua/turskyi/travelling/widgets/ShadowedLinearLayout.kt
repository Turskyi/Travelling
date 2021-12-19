
        val elevationValue = elevation.toInt()

        val outerRadius = floatArrayOf(
            0f, 0f, 0f,
            0f, 0f, 0f, 0f,
            0f
        )

        val shapeDrawablePadding = Rect()

        val shadowOffset: Float
        shapeDrawablePadding.right = (makeHorizontalOffset(elevation) + shadowMarginEnd).toInt()
        shapeDrawablePadding.left = (makeHorizontalOffset(elevation) + shadowMarginStart).toInt()

        shadowOffset = when (shadowGravity) {
            Gravity.CENTER -> 0f
            Gravity.TOP -> -1 * elevationValue / 3f
            Gravity.BOTTOM -> elevationValue / 1.5f
            else -> elevationValue / 1.5f
        }
        shapeDrawablePadding.top = (elevationValue * 2 + shadowMarginTop).toInt()
        shapeDrawablePadding.bottom = (elevationValue * 2 + shadowMarginBottom).toInt()
        val shapeDrawable = ShapeDrawable()

        shapeDrawable.setPadding(shapeDrawablePadding)
        shapeDrawable.paint.color = backgroundColor

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            setLayerType(View.LAYER_TYPE_HARDWARE, shapeDrawable.paint)
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, shapeDrawable.paint)
        }

        if (elevation > resources.getDimension(R.dimen.elevation_1) * 2) {
            shapeDrawable.paint.setShadowLayer(shadowRadius, elevation / 5, shadowOffset, shadowColor)
        }
        y = (-(elevationValue * 2)).toFloat()
        shapeDrawable.shape = RoundRectShape(outerRadius, null, null)
        val drawable = LayerDrawable(arrayOf<Drawable>(shapeDrawable))
        drawable.setLayerInset(
            0,
            (makeHorizontalOffset(elevation) + shadowMarginStart).toInt(),
            (elevationValue * 2 + shadowMarginTop).toInt(),
            (makeHorizontalOffset(elevation) + shadowMarginEnd).toInt(),
            (elevationValue * 2 + shadowMarginBottom).toInt()
        )
        return drawable
    }

    private fun makeHorizontalOffset(elevation: Float): Float {
        return if (scaleViewOnElevationChange) elevation / 2 else 0f
    }
}