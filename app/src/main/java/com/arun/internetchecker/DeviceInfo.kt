package com.arun.internetchecker

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics

/**
 * Util class to get decice info's.
 *
 * @author Arun Shankar
 * @since 31Jan2019 since v1.5
 */
class DeviceInfo {

    companion object {

        /**
         * Check whether it is Pie+ device.
         *
         * @return true - if Pie+ device, else false
         */
        fun isPiePlus() = Build.VERSION.SDK_INT >= /*Build.VERSION_CODES.P*/28

        /**
         * Check whether it is Nougat+ device.
         *
         * @return true - if Nougat+ device, else false
         */
        fun isNougatPlusDevice() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

        /**
         * Check whether it is Oreo+ device.
         *
         * @return true - if Oreo+ device, else false
         */
        fun isOreoPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

        /**
         * Check whether it is Marshmallow+ device.
         *
         * @return true - if Marshmallow+ device, else false
         */
        fun isMarshmallowPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

        /**
         * Check whether it is Lollipop+ device.
         *
         * @return true - if Lollipop+ device, else false
         */
        fun isLollipopPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

        /**
         * This method converts density independent pixels to device specific pixels.
         *
         * @param dp      A value in dp unit. Which we need to convert into px
         * @param context Context to get resources and device specific display metrics
         * @return A float value to represent px equivalent to dp value
         */
        fun convertDpToPixel(context: Context?, dp: Float): Float {
            if (context == null) return 0F
            val resources = context.resources
            val metrics = resources.displayMetrics
            return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }

        /**
         * This method converts device specific pixels to density independent pixels.
         *
         * @param px      A value in px (pixels) unit. Which we need to convert into db
         * @param context Context to get resources and device specific display metrics
         * @return A float value to represent dp equivalent to px value
         */
        fun convertPixelsToDp(context: Context?, px: Float): Float {
            if (context == null) return 0F
            val resources = context.resources
            val metrics = resources.displayMetrics
            return px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }
    }
}