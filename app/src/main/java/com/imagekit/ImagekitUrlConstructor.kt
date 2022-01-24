package com.imagekit

import android.content.Context
import com.imagekit.ImageKit.Companion.IK_VERSION_KEY
import com.imagekit.android.entity.*
import com.imagekit.android.injection.module.ContextModule
import com.imagekit.android.util.TransformationMapping
import com.imagekit.entity.CropMode
import com.imagekit.injection.component.DaggerUtilComponent
import java.lang.Math.abs
import java.net.URI
import java.net.URLEncoder
import java.util.*

class ImagekitUrlConstructor constructor(
    private val context: Context,
    private var source: String,
    private var transformationPosition: TransformationPosition
) {
    private val transformationList: MutableList<String> = ArrayList()
    private val transformationMap = HashMap<String, Any>()
    private var path: String? = null
    private var isSource: Boolean = true
    private var queryParams: HashMap<String, String> =
        hashMapOf(IK_VERSION_KEY to "")

    constructor(
        context: Context,
        urlEndpoint: String,
        path: String,
        transformationPosition: TransformationPosition
    ) : this(context, urlEndpoint, transformationPosition) {
        this.path = path
        isSource = false
    }

    init {
        ImageKit.getInstance()
        val appComponent = DaggerUtilComponent.builder()
            .contextModule(ContextModule(context))
            .build()

        appComponent
            .inject(this)
    }

    /**
     * Method to specify the height of the output image.
     * @param height Accepts integer value greater than 1 and if a value between 0 and 1 is specified, then it acts as a percentage height.
     * For eg, 0.1 means 10% of the original height, 0.2 means 20% of the original height.
     * @return the current ImagekitUrlConstructor object.
     */
    fun height(height: Int): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.height] = height
        transformationList.add(String.format("%s-%d", TransformationMapping.height, height))
        return this
    }

    /**
     * Method to specify the width of the output image.
     * @param width Accepts integer value greater than 1 and if a value between 0 and 1 is specified, then it acts as a percentage width.
     * For eg, 0.1 means 10% of the original width, 0.2 means 20% of the original width.
     * @return the current ImagekitUrlConstructor object.
     */
    fun width(width: Int): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.width] = width
        transformationList.add(String.format("%s-%d", TransformationMapping.width, width))
        return this
    }

    /**
     * Method to specify the aspect ratio of the output image or the ratio of width to height of the output image.
     * This transform must be used along with either the height or the width transform.
     * @param width Accepts integer value greater than equal to 1
     * @param height Accepts integer value greater than equal to 1
     * @return the current ImagekitUrlConstructor object.
     */
    fun aspectRatio(width: Int, height: Int): ImagekitUrlConstructor {
        val s = String.format("%s-%d-%d", TransformationMapping.aspectRatio, width, height)
        transformationMap[TransformationMapping.aspectRatio] = s
        transformationList.add(s)
        return this
    }

    /**
     * Method to specify the output quality of the lossy formats like JPG and WebP. A higher quality number means a
     * larger size of the output image with high quality. A smaller number means low quality image at a smaller size.
     * @param quality Accepts integer value between 1 and 100.
     * Default value is picked from the dashboard settings. It is set to 80.
     * @return the current ImagekitUrlConstructor object.
     */
    fun quality(quality: Int): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.quality] = quality
        transformationList.add(String.format("%s-%d", TransformationMapping.quality, quality))
        return this
    }

    /**
     * Method to decide the final value of height and width of the output image based on the aspect ratio of the input
     * image and the requested transform.
     * @param cropType Accepts value of type CropType. Possible values include maintain_ratio, force, at_least and at_max.
     * Default value - maintain_ratio
     * @see CropType
     * @return the current ImagekitUrlConstructor object.
     */
    fun crop(cropType: CropType): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.crop] = cropType
        transformationList.add(String.format("%s-%s", TransformationMapping.crop, cropType.value))
        return this
    }

    /**
     * Method used to specify the strategy of how the input image is used for cropping to get the output image.
     * @param cropMode Accepts value of type CropMode. Possible values include resize, extract, pad_extract and pad_resize.
     * Default value - resize
     * @see CropMode
     * @return the current ImagekitUrlConstructor object.
     */
    fun cropMode(cropMode: CropMode): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.cropMode] = cropMode
        transformationList.add(
            String.format(
                "%s-%s",
                TransformationMapping.cropMode,
                cropMode.value
            )
        )
        return this
    }

    /**
     * Method used to specify the focus using cropped image coordinates
     * @param x Accepts value of x coordinate for focus.
     * @param y Accepts value of y coordinate for focus.
     * @return the current ImagekitUrlConstructor object.
     */
    fun focus(x: Int, y: Int): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.x] = x
        transformationMap[TransformationMapping.y] = y
        transformationList.add(String.format("%s-%d", TransformationMapping.x, x))
        transformationList.add(String.format("%s-%d", TransformationMapping.y, y))
        return this
    }

    /**
     * Method used to specify the focus which is coupled with the extract type of crop mode (crop mode is not needed
     * if you are using auto focus) to get the area of the input image that should be focussed on to get the output image.
     * @param focusType Accepts value of type FocusType. Possible values include center, top, left, bottom, right,
     * top_left, top_right, bottom_left, bottom_right and auto.
     * Default value - center
     * @see FocusType
     * @return the current ImagekitUrlConstructor object.
     */
    fun focus(focusType: FocusType): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.focus] = focusType
        transformationList.add(String.format("%s-%s", TransformationMapping.focus, focusType.value))
        return this
    }

    /**
     * Method used to specify the format of the output image. If no output format is specified and
     * the “Dynamic image format selection” option is selected in your dashboard settings, then the output format is
     * decided on the basis of the user’s device capabilities and input image format. If dynamic image format selction
     * is switched off, and no output format is specified then the format of the output image is same as that of the input image.
     * @param format Accepts value of type FocusType. Possible values include auto, webp, jpg, jpeg and pnt.
     * Default value - auto
     * @see Format
     * @return the current ImagekitUrlConstructor object.
     */
    fun format(format: Format): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.format] = format
        transformationList.add(String.format("%s-%s", TransformationMapping.format, format.value))
        return this
    }

    /**
     * Method used to specify the radius to be used to get a rounded corner image.
     * This option is applied after resizing of the image, if any.
     * @param radius Possible values include positive integer.
     * @return the current ImagekitUrlConstructor object.
     */
    fun radius(radius: Int): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.radius] = radius
        transformationList.add(String.format("%s-%d", TransformationMapping.radius, radius))
        return this
    }

    /**
     * Method used to get a perfectly rounded image.
     * This option is applied after resizing of the image, if any.
     * @return the current ImagekitUrlConstructor object.
     */
    fun round(): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.radius] = "max"
        transformationList.add(String.format("%s-%s", TransformationMapping.radius, "max"))
        return this
    }

    /**
     * Method used to specify the background color as RGB hex code (e.g. FF0000) or an RGBA code (e.g. FFAABB50)
     * to be used for the image.
     * @param backgroundColor Default value - Black 000000
     * @return the current ImagekitUrlConstructor object.
     */
    fun background(backgroundColor: String): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.background] =
            backgroundColor.toUpperCase(Locale.getDefault())
        transformationList.add(
            String.format(
                "%s-%s",
                TransformationMapping.background,
                backgroundColor.toUpperCase(Locale.getDefault())
            )
        )
        return this
    }

    /**
     * Method used to specify the width and color of the border that is added around the image.
     * The width is a positive integer that specifies the border width in pixels.
     * The border color is specified as a standard RGB hex code e.g b-{width}_{color}
     * @param borderWidth width of the border
     * @param borderColor color of the border as RGB hex code
     * @return the current ImagekitUrlConstructor object.
     */
    fun border(borderWidth: Int, borderColor: String): ImagekitUrlConstructor {

        val s = String.format(
            "%s-%d_%s",
            TransformationMapping.border,
            borderWidth,
            borderColor.toUpperCase(Locale.getDefault())
        )
        transformationMap[TransformationMapping.border] = s
        transformationList.add(s)
        return this
    }

    /**
     * Method used to specify the degrees by which the output image has to be rotated or specifies the use of
     * EXIF Orientation tag for the rotation of the image using auto.
     * @param rotation Possible values include 0, 90, 180, 270, 360 and auto.
     * Default value - center
     * @see Rotation
     * @return the current ImagekitUrlConstructor object.
     */
    fun rotation(rotation: Rotation): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.rotation] = rotation
        transformationList.add(
            String.format(
                "%s-%s",
                TransformationMapping.rotation,
                rotation.value
            )
        )
        return this
    }

    /**
     * Method to specify the Gaussian blur that has to be applied to the image. The value of blur decides the radius of
     * the Gaussian blur that is applied. Higher the value, higher is the radius of Gaussian blur.
     * @param blur Accepts integer value between 1 and 100.
     * @return the current ImagekitUrlConstructor object.
     */
    fun blur(blur: Int): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.blur] = blur
        transformationList.add(String.format("%s-%d", TransformationMapping.blur, blur))
        return this
    }

    /**
     * Method to specify the Named transformations which is an alias for the entire transformation string.
     * E.g we can create a named transform media_library_thumbnail for transformation string tr:w-150,h-150,f-center,c-at_max
     * @param namedTransformation
     * @return the current ImagekitUrlConstructor object.
     */
    fun named(namedTransformation: String): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.named] = namedTransformation
        transformationList.add(
            String.format(
                "%s-%s",
                TransformationMapping.named,
                namedTransformation
            )
        )
        return this
    }

    /**
     * Method used to provide more granular control over the positioning of the overlay image on the input image.
     * The top left corner of the input image is considered as (0,0) and the bottom right corner is considered as (w, h)
     * where w is the width and h is the height of the input image.
     * Negative values are supported with a leading capital N in front of the value provided. The value provided
     * is subtracted from the original dimension of the image & positioned accordingly.
     * @param overlayX Possible values include all integers.
     * @return the current ImagekitUrlConstructor object.
     * or the overlay focus has already been applied
     */
    fun overlayX(overlayX: Int): ImagekitUrlConstructor {
        val s = if (overlayX < 0)
            String.format("%s-N%s", TransformationMapping.overlayX, abs(overlayX))
        else
            String.format("%s-%d", TransformationMapping.overlayX, overlayX)

        transformationMap[TransformationMapping.overlayX] = s
        transformationList.add(s)
        return this
    }

    /**
     * Method used to provide more granular control over the positioning of the overlay image on the input image.
     * The top left corner of the input image is considered as (0,0) and the bottom right corner is considered as (w, h)
     * where w is the width and h is the height of the input image.
     * Negative values are supported with a leading capital N in front of the value provided. The value provided
     * is subtracted from the original dimension of the image & positioned accordingly.
     * @param overlayY Possible values include all integers.
     * @return the current ImagekitUrlConstructor object.
     * or the overlay focus has already been applied
     */
    fun overlayY(overlayY: Int): ImagekitUrlConstructor {
        val s = if (overlayY < 0)
            String.format("%s-N%s", TransformationMapping.overlayY, abs(overlayY))
        else
            String.format("%s-%d", TransformationMapping.overlayY, overlayY)

        transformationMap[TransformationMapping.overlayY] = s
        transformationList.add(s)
        return this
    }

    /**
     * Method used to specify the position of the overlaid image relative to the input image.
     * @param overlayFocus Possible values include center, top, left, bottom, right, top_left, top_right, bottom_left and bottom_right.
     * Default value - center
     * @see OverlayFocusType
     * @return the current ImagekitUrlConstructor object.
     * using either overlayPosX() or overlayPosY()
     */
    fun overlayFocus(overlayFocus: OverlayFocusType): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.overlayFocus] = overlayFocus
        transformationList.add(
            String.format(
                "%s-%s",
                TransformationMapping.overlayFocus,
                overlayFocus.value
            )
        )
        return this
    }

    /**
     * Method used to specify the height of the overlaid image.
     * @param overlayHeight
     * @return the current ImagekitUrlConstructor object.
     */
    fun overlayHeight(overlayHeight: Int): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.overlayHeight] = overlayHeight
        transformationList.add(
            String.format(
                "%s-%d",
                TransformationMapping.overlayHeight,
                overlayHeight
            )
        )
        return this
    }

    /**
     * Method used to specify the width of the overlaid image.
     * @param overlayWidth
     * @return the current ImagekitUrlConstructor object.
     */
    fun overlayWidth(overlayWidth: Int): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.overlayWidth] = overlayWidth
        transformationList.add(
            String.format(
                "%s-%d",
                TransformationMapping.overlayWidth,
                overlayWidth
            )
        )
        return this
    }

    /**
     * Method to specify an image to overlay over another image. This will help you generate watermarked images if needed.
     * @param overlayImage
     * @return the current ImagekitUrlConstructor object.
     */
    fun overlayImage(overlayImage: String): ImagekitUrlConstructor {
        val formattedOverlayImage = overlayImage.replace("/", "@@")
        transformationMap[TransformationMapping.overlayImage] = formattedOverlayImage
        transformationList.add(
            String.format(
                "%s-%s",
                TransformationMapping.overlayImage,
                formattedOverlayImage
            )
        )
        return this
    }


    /**
     * Method to trims the overlay image before overlaying it on the base.
     * @param overlayImage
     * @return the current ImagekitUrlConstructor object.
     */
    fun overlayImageTrim(overlayImageTrim: Boolean): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.overlayImageTrim] = overlayImageTrim.toString()
        transformationList.add(
            String.format(
                "%s-%s",
                TransformationMapping.overlayImageTrim,
                overlayImageTrim.toString()
            )
        )
        return this
    }

    /**
     * Method to specify the aspect ratio of the output image or the ratio of width to height of the output image.
     * This transform must be used along with either the height or the width transform.
     * @param width Accepts integer value greater than equal to 1
     * @param height Accepts integer value greater than equal to 1
     * @return the current ImagekitUrlConstructor object.
     */
    fun overlayImageAspectRatio(width: Int, height: Int): ImagekitUrlConstructor {
        val s =
            String.format("%s-%d-%d", TransformationMapping.overlayImageAspectRatio, width, height)
        transformationMap[TransformationMapping.overlayImageAspectRatio] = s
        transformationList.add(s)
        return this
    }

    /**
     * Method used to specify the colour of background canvas to be overlaid. Possible values include a valid RGB Hex code.
     * @param overlayBackground
     * @return the current ImagekitUrlConstructor object.
     */
    fun overlayImageBackground(overlayImageBackground: String): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.overlayImageBackground] =
            overlayImageBackground.toUpperCase(Locale.getDefault())
        transformationList.add(
            String.format(
                "%s-%s",
                TransformationMapping.overlayImageBackground,
                overlayImageBackground.toUpperCase(Locale.getDefault())
            )
        )
        return this
    }

    /**
     * Method used to specify the width and color of the border that is added around the overlaid image.
     * The width is a positive integer that specifies the border width in pixels.
     * The border color is specified as a standard RGB hex code e.g b-{width}_{color}
     * @param borderWidth width of the border
     * @param borderColor color of the border as RGB hex code
     * @return the current ImagekitUrlConstructor object.
     */
    fun overlayImageBorder(borderWidth: Int, borderColor: String): ImagekitUrlConstructor {
        val s = String.format(
            "%s-%d_%s",
            TransformationMapping.overlayImageBorder,
            borderWidth,
            borderColor.toUpperCase(Locale.getDefault())
        )
        transformationMap[TransformationMapping.border] = s
        transformationList.add(s)
        return this
    }

    /**
     * Method to specify the device pixel ratio to be used to calculate the dimension of the overlaid image. It is useful
     * when creating image transformations for devices with high density screens (DPR greater than 1) like the iPhone.
     * The DPR option works only when either the height or the width or both are specified for resizing the image
     * If the resulting height or width after considering the specified DPR value is less than 1px or more than 5000px
     * then the value of DPR is not considered and the normal height or width specified in the transformation string is used.
     * @param dpr Possible values: 0.1 to 5.0
     * @return the current ImagekitUrlConstructor object.
     */
    fun overlayImageDPR(dpr: Float): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.overlayImageDPR] = dpr
        transformationList.add(String.format("%s-%.2f", TransformationMapping.overlayImageDPR, dpr))
        return this
    }

    /**
     * Method to specify the output quality of the lossy formats like JPG and WebP of Overlaid Image. A higher quality number means a
     * larger size of the output image with high quality. A smaller number means low quality image at a smaller size.
     * @param quality Accepts integer value between 1 and 100.
     * Default value is picked from the dashboard settings. It is set to 80.
     * @return the current ImagekitUrlConstructor object.
     */
    fun overlayImageQuality(quality: Int): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.overlayImageQuality] = quality
        transformationList.add(
            String.format(
                "%s-%d",
                TransformationMapping.overlayImageQuality,
                quality
            )
        )
        return this
    }


    /**
     * Method used to specify the strategy of how the overlay image is used for cropping to get the output image.
     * @param cropMode Accepts value of type CropMode. Possible values include resize, extract, pad_extract and pad_resize.
     * Default value - resize
     * @see CropMode
     * @return the current ImagekitUrlConstructor object.
     */
    fun overlayImageCropping(cropMode: CropMode): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.overlayImageCropping] = cropMode
        transformationList.add(
            String.format(
                "%s-%s",
                TransformationMapping.overlayImageCropping,
                cropMode.value
            )
        )
        return this
    }


    /**
     * Method used to overlay text over an image. Our current support is limited to alphanumberic & special characters _ & - only.
     * @param overlayText
     * @return the current ImagekitUrlConstructor object.
     */
    fun overlayText(overlayText: String): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.overlayText] =
            URLEncoder.encode(overlayText, "UTF-8")
        transformationList.add(
            String.format(
                "%s-%s",
                TransformationMapping.overlayText,
                URLEncoder.encode(overlayText, "UTF-8")
            )
        )
        return this
    }

    /**
     * Method used to specify the size of the overlaid text.
     * @param overlayTextFontSize Possible values include any integer. Default value - 14px
     * @return the current ImagekitUrlConstructor object.
     */
    fun overlayTextFontSize(overlayTextFontSize: Int): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.overlayTextFontSize] = overlayTextFontSize
        transformationList.add(
            String.format(
                "%s-%d",
                TransformationMapping.overlayTextFontSize,
                overlayTextFontSize
            )
        )
        return this
    }

    /**
     * Method used to specify the font family for the overlaid text.
     * @param overlayTextFontFamily
     * @return the current ImagekitUrlConstructor object.
     * @see overlayTextFontFamily
     */
    fun overlayTextFontFamily(overlayTextFontFamily: OverlayTextFont): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.overlayTextFontFamily] = overlayTextFontFamily
        transformationList.add(
            String.format(
                "%s-%s",
                TransformationMapping.overlayTextFontFamily,
                overlayTextFontFamily.value
            )
        )
        return this
    }

    /**
     * Method used to specify the color of the overlaid text on the image.
     * @param overlayTextColor Possible value is a valid valid RGB Hex Code
     * @return the current ImagekitUrlConstructor object.
     */
    fun overlayTextColor(overlayTextColor: String): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.overlayTextColor] =
            overlayTextColor.toUpperCase(Locale.getDefault())
        transformationList.add(
            String.format(
                "%s-%s",
                TransformationMapping.overlayTextColor,
                overlayTextColor.toUpperCase(Locale.getDefault())
            )
        )
        return this
    }

    /**
     * Method used to specify the transparency leve of the overlaid text on the image.
     * Note Overlay transparency is currently supported for overlay texts & backgrounds only.
     * @param overlayAlpha Possible values include integer from 1 to 9.
     * @return the current ImagekitUrlConstructor object.
     */
    fun overlayTextTransparency(overlayTextTransparency: Int): ImagekitUrlConstructor {
        return this.overlayAlpha(overlayTextTransparency)
    }

    /**
     * Method used to specify the transparency level for the overlaid image.
     * Note Overlay transparency is currently supported for overlay texts & backgrounds only.
     * @param overlayAlpha Possible values include integer from 1 to 9.
     * @return the current ImagekitUrlConstructor object.
     */
    fun overlayAlpha(overlayAlpha: Int): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.overlayAlpha] = overlayAlpha
        transformationList.add(
            String.format(
                "%s-%d",
                TransformationMapping.overlayAlpha,
                overlayAlpha
            )
        )
        return this
    }

    /**
     * Method used to specify the typography of the font family used for the overlaid text. Possible values include bold b and italics i.
     * Note Bold & Italics are not supported for all provided fonts.
     * @param overlayTextTypography
     * @return the current ImagekitUrlConstructor object
     * @see OverlayTextTypography
     */
    fun overlayTextTypography(overlayTextTypography: OverlayTextTypography): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.overlayTextTypography] = overlayTextTypography
        transformationList.add(
            String.format(
                "%s-%s",
                TransformationMapping.overlayTextTypography,
                overlayTextTypography.value
            )
        )
        return this
    }

    /**
     * Method used to specify the colour of background canvas to be overlaid. Possible values include a valid RGB Hex code.
     * @param overlayBackground
     * @return the current ImagekitUrlConstructor object.
     */
    fun overlayBackground(overlayBackground: String): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.overlayBackground] =
            overlayBackground.toUpperCase(Locale.getDefault())
        transformationList.add(
            String.format(
                "%s-%s",
                TransformationMapping.overlayBackground,
                overlayBackground.toUpperCase(Locale.getDefault())
            )
        )
        return this
    }

    /**
     * Method used to base64 encded overlay text over an image.
     * @param overlayTextEncoded
     * @return the current ImagekitUrlConstructor object.
     */
    fun overlayTextEncoded(overlayTextEncoded: String): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.overlayTextEncoded] =
            overlayTextEncoded.replace("=", "%3D")
        transformationList.add(
            String.format(
                "%s-%s",
                TransformationMapping.overlayTextEncoded,
                overlayTextEncoded.replace("=", "%3D")
            )
        )
        return this
    }

    /**
     * Method to specify the width of the overlaid text on the output image.
     * @param width Accepts integer value greater than 1.
     * @return the current ImagekitUrlConstructor object.
     */
    fun overlayTextWidth(width: Int): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.overlayTextWidth] = width
        transformationList.add(
            String.format(
                "%s-%d",
                TransformationMapping.overlayTextWidth,
                width
            )
        )
        return this
    }

    /**
     * Method used to specify the background color of the overlaid text on the image.
     * @param overlayTextColor
     * @return the current ImagekitUrlConstructor object.
     */
    fun overlayTextBackground(overlayTextColor: String): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.overlayTextBackground] =
            overlayTextColor.toUpperCase(Locale.getDefault())
        transformationList.add(
            String.format(
                "%s-%s",
                TransformationMapping.overlayTextBackground,
                overlayTextColor.toUpperCase(Locale.getDefault())
            )
        )
        return this
    }

    /**
     * Method used to specify the padding of the overlaid text on the image.
     * @param overlayTextPadding
     * @return the current ImagekitUrlConstructor object.
     */
    fun overlayTextPadding(overlayTextPadding: String): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.overlayTextPadding] = overlayTextPadding
        transformationList.add(
            String.format(
                "%s-%s",
                TransformationMapping.overlayTextPadding,
                overlayTextPadding
            )
        )
        return this
    }

    /**
     * Method used to specify the padding of the overlaid text on the image.
     * @param overlayTextPadding
     * @return the current ImagekitUrlConstructor object.
     */
    fun overlayTextPadding(overlayTextPadding: Int): ImagekitUrlConstructor {
        return this.overlayTextPadding(String.format("%d", overlayTextPadding))
    }

    /**
     * Method used to specify the padding of the overlaid text on the image.
     * @param verticalPadding
     * @param horizontalPadding
     * @return the current ImagekitUrlConstructor object.
     */
    fun overlayTextPadding(verticalPadding: Int, horizontalPadding: Int): ImagekitUrlConstructor {
        return this.overlayTextPadding(String.format("%d_%d", verticalPadding, horizontalPadding))
    }

    /**
     * Method used to specify the padding of the overlaid text on the image.
     * @param topPadding
     * @param horizontalPadding
     * @param bottomPadding
     * @return the current ImagekitUrlConstructor object.
     */
    fun overlayTextPadding(
        topPadding: Int,
        horizontalPadding: Int,
        bottomPadding: Int
    ): ImagekitUrlConstructor {
        return this.overlayTextPadding(
            String.format(
                "%d_%d_%d",
                topPadding,
                horizontalPadding,
                bottomPadding
            )
        )
    }

    /**
     * Method used to specify the padding of the overlaid text on the image.
     * @param topPadding
     * @param rightPadding
     * @param bottomPadding
     * @param leftPadding
     * @return the current ImagekitUrlConstructor object.
     */
    fun overlayTextPadding(
        topPadding: Int,
        rightPadding: Int,
        bottomPadding: Int,
        leftPadding: Int
    ): ImagekitUrlConstructor {
        return this.overlayTextPadding(
            String.format(
                "%d_%d_%d_%d",
                topPadding,
                rightPadding,
                bottomPadding,
                leftPadding
            )
        )
    }

    /**
     * Method used to specify the alignment of the overlaid text on the image.
     * @param overlayTextInnerAlignment
     * @return the current ImagekitUrlConstructor object.
     */
    fun overlayTextInnerAlignment(overlayTextInnerAlignment: OverlayTextInnerAlignment): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.overlayTextInnerAlignment] =
            overlayTextInnerAlignment.value
        transformationList.add(
            String.format(
                "%s-%s",
                TransformationMapping.overlayTextInnerAlignment,
                overlayTextInnerAlignment.value
            )
        )
        return this
    }

    /**
     * Method used to specify the radius to be used to get a rounded corner overlay.
     * @param radius Possible values include positive integer.
     * @return the current ImagekitUrlConstructor object.
     */
    fun overlayRadius(radius: Int): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.overlayRadius] = radius
        transformationList.add(String.format("%s-%d", TransformationMapping.overlayRadius, radius))
        return this
    }

    /**
     * Method to specify if the output JPEG image should be rendered progressively. In progressive rendering,
     * the client instead of downloading the image row-wise (baseline loading), renders a low-quality pixelated
     * full image and then gradually keeps on adding more pixels and information to the image. It gives faster-perceived load times.
     * @param flag Possible values include true and false. Default value - false
     * @return the current ImagekitUrlConstructor object.
     */
    fun progressive(flag: Boolean): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.progressive] = flag
        transformationList.add(String.format("%s-%b", TransformationMapping.progressive, flag))
        return this
    }

    /**
     * Method to specify if the output image (if in PNG or WebP format) should be compressed losslessly.
     * In lossless compression, the output file size would be larger than the regular lossy compression but at the same time,
     * the perceived quality can be better in certain cases, especially for computer generated graphics.
     * Using lossless compression is not recommended for photographs.
     * @param flag Possible values include true and false. Default value - false
     * @return the current ImagekitUrlConstructor object.
     */
    fun lossless(flag: Boolean): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.lossless] = flag
        transformationList.add(String.format("%s-%b", TransformationMapping.lossless, flag))
        return this
    }

    /**
     * Method to specify if the redundant pixels of the original image need to be removed. It uses a default logic
     * to identify the redundant surrounding region and removes it. This transformation is useful for images that have
     * a solid / nearly-solid background and the object in the center. This transformation will trim
     * the background from the edges, leaving only the central object in the picture.
     * @param flag Possible values include true and false.
     * @return the current ImagekitUrlConstructor object.
     * @see trim
     */
    fun trim(flag: Boolean): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.trim] = flag
        transformationList.add(String.format("%s-%b", TransformationMapping.trim, flag))
        return this
    }

    /**
     * Method to specify the number of redundant pixels of the original image that need to be removed.
     * This transformation is useful for images that have a solid / nearly-solid background and the object in the center.
     * This transformation will trim the background from the edges, leaving only the central object in the picture.
     * @param value Number of pixels from the edge that need to be removed across all four sides.
     * @return the current ImagekitUrlConstructor object.
     * @see trim
     */
    fun trim(value: Int): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.trim] = value
        transformationList.add(String.format("%s-%d", TransformationMapping.trim, value))
        return this
    }

    /**
     * Method used to specify if the output image should contain all the metadata that is initially available from
     * the original image. Enabling this is not recommended because this metadata is not relevant for rendering on the
     * web and mobile apps. The only reason where you should enable the metadata option is when you have knowingly wanted
     * the additional data like camera information, lens information and other image profiles attached to your image.
     * Possible values include true and false.
     * @param flag Default value - false
     * @return the current ImagekitUrlConstructor object.
     */
    fun metadata(flag: Boolean): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.metadata] = flag
        transformationList.add(String.format("%s-%b", TransformationMapping.metadata, flag))
        return this
    }

    /**
     * Method used to specify if the output image should contain the color profile that is initially available
     * from the original image. It is recommended to remove the color profile before serving the image on web and apps.
     * However, in cases where you feel that the image looks faded or washed-out after using ImageKit and want to preserve
     * the colors in your image, then you should set this option to true. Possible values include true and false.
     * @param flag Default value - false
     * @return the current ImagekitUrlConstructor object.
     */
    fun colorProfile(flag: Boolean): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.colorProfile] = flag
        transformationList.add(String.format("%s-%b", TransformationMapping.colorProfile, flag))
        return this
    }

    /**
     * Method to specify the default image which is delivered in case the image that is requested using ImageKit does not exist.
     * @param defaultImage
     * @return the current ImagekitUrlConstructor object.
     */
    fun defaultImage(defaultImage: String): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.defaultImage] = defaultImage
        transformationList.add(
            String.format(
                "%s-%s",
                TransformationMapping.defaultImage,
                defaultImage
            )
        )
        return this
    }

    /**
     * Method to specify the device pixel ratio to be used to calculate the dimension of the output image. It is useful
     * when creating image transformations for devices with high density screens (DPR greater than 1) like the iPhone.
     * The DPR option works only when either the height or the width or both are specified for resizing the image
     * If the resulting height or width after considering the specified DPR value is less than 1px or more than 5000px
     * then the value of DPR is not considered and the normal height or width specified in the transformation string is used.
     * @param dpr Possible values: 0.1 to 5.0
     * @return the current ImagekitUrlConstructor object.
     */
    fun dpr(dpr: Float): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.dpr] = dpr
        transformationList.add(String.format("%s-%.2f", TransformationMapping.dpr, dpr))
        return this
    }

    /**
     * Method sharpens the input image. It is useful to highlight the edges and finer details in the image.
     * If just e-sharpen is used, then a default sharpening is performed on the image. This behavior can be controlled
     * by specifying a number that controls the extent of sharpening performed - higher the number,
     * more the sharpening
     * @param value
     * @return the current ImagekitUrlConstructor object.
     */
    fun effectSharpen(value: Int = 0): ImagekitUrlConstructor {
        if (value == 0) {
            transformationMap[TransformationMapping.effectSharpen] = true
            transformationList.add(String.format("%s", TransformationMapping.effectSharpen))
        } else {
            transformationMap[TransformationMapping.effectSharpen] = value
            transformationList.add(
                String.format(
                    "%s-%d",
                    TransformationMapping.effectSharpen,
                    value
                )
            )
        }
        return this
    }

    /**
     * Unsharp masking (USM) is an image sharpening technique.
     * Method allows you to apply and control unsharp mask on your images. The amount of sharpening can be controlled
     * by varying the 4 parameters - radius, sigma, amount and threshold. This results in perceptually better images
     * compared to just e-sharpen.
     * @param radius Possible values include positive floating point values.
     * @param sigma Possible values include positive floating point values.
     * @param amount Possible values include positive floating point values.
     * @param threshold Possible values include positive floating point values.
     * @return the current ImagekitUrlConstructor object.
     */
    fun effectUSM(
        radius: Float,
        sigma: Float,
        amount: Float,
        threshold: Float
    ): ImagekitUrlConstructor {
        val s = String.format(
            "%s-%.2f-%.2f-%.2f-%.2f",
            TransformationMapping.effectUSM,
            radius,
            sigma,
            amount,
            threshold
        )
        transformationMap[TransformationMapping.effectUSM] = s
        transformationList.add(s)
        return this
    }

    /**
     * Method used to automatically enhance the contrast of the image by using the full range of intensity values
     * that a particular image format allows. This basically means that the lighter portions of the image would become
     * even lighter and the darker portions of the image would become even brighter, thereby enhancing the contrast.
     * @param flag
     * @return the current ImagekitUrlConstructor object.
     */
    fun effectContrast(flag: Boolean): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.effectContrast] = flag

        if (flag)
            transformationList.add(String.format("%s", TransformationMapping.effectContrast))

        return this
    }

    /**
     * Method to turn an image into its grayscale version.
     * @param flag Accepts boolean value of either true or false. Default value is false.
     * @return the current ImagekitUrlConstructor object.
     */
    fun effectGray(flag: Boolean): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.effectGray] = flag

        if (flag)
            transformationList.add(String.format("%s", TransformationMapping.effectGray))

        return this
    }

    /**
     * Method to retrieve the original image
     * @return the current ImagekitUrlConstructor
     */
    fun original(flag: Boolean): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.original] = flag
        if (flag)
            transformationList.add(String.format("%s-true", TransformationMapping.original))

        return this
    }

    /**
     * Some transformations are dependent on the sequence in which they are carried out.
     * Method used to add sequence dependent steps in a transform chain to obtain predictable results.
     * @return the current ImagekitUrlConstructor object.
     */
    fun chainTransformation(): ImagekitUrlConstructor {
        transformationMap[TransformationMapping.transformationStep] = ":"
        transformationList.add(":")

        return this
    }

    /**
     * Method allows adding custom transformations to the image.
     * @return the current ImagekitUrlConstructor object.
     */
    fun addCustomTransformation(key: String, value: String): ImagekitUrlConstructor {
        transformationMap[key] = value
        transformationList.add(String.format("%s-%s", key, value))
        return this
    }

    /**
     * Method allows adding custom Query Parameter to the image.
     * @return the current ImagekitUrlConstructor object.
     */
    fun addCustomQueryParameter(key: String, value: String): ImagekitUrlConstructor {
        queryParams[key] = value
        return this
    }

    /**
     * Method allows adding custom Query Parameters to the image.
     * @return the current ImagekitUrlConstructor object.
     */
    fun addCustomQueryParameter(params: HashMap<String, String>): ImagekitUrlConstructor {
        params.forEach { (key, value) -> queryParams[key] = value }
        return this
    }

    /**
     * Used to create the url using the transformations specified before invoking this method.
     * @return the Url used to fetch an image after applying the specified transformations.
     */
    fun create(): String {
        var url = source.trim('/')
        path = path?.trim('/')

        if (url.isEmpty()) {
            return ""
        }

        if (transformationList.isNotEmpty()) {
            var transforms =
                transformationList.map { transformation -> transformation }.joinToString(",")
                    .replace(",:,", ":")
            if (isSource) {
                transformationPosition = TransformationPosition.QUERY
                if (url.contains("?tr=")) {
                    url = url.substring(0, url.indexOf("?tr="))
                }
                if (url.contains("/tr:")) {
                    url = url.replace(
                        url.substring(url.indexOf("/tr:"), url.lastIndexOf("/")),
                        ""
                    )
                }
                queryParams["tr"] = transforms
            } else {
                if (transformationPosition == TransformationPosition.PATH) {
                    url = String.format("%s/%s", addPathParams(url), path)
                }
                if (transformationPosition == TransformationPosition.QUERY) {
                    url = String.format("%s/%s", url, path)
                    queryParams["tr"] = transforms
                }
            }
        } else {
            if (!isSource) {
                url = String.format("%s/%s", url, path)
            }
        }

        var u = URI(url)
        val sb = StringBuilder(
            if (u.query == null)
                ""
            else
                u.query
        )

        if (sb.isNotEmpty())
            sb.append('&')

        sb.append(queryParams.map { (key, value) ->
            String.format(
                "%s=%s",
                key,
                value
            )
        }.joinToString("&"))

        return u.scheme + "://" + u.authority + u.path.replace("=", "%3D") + "?" + sb.toString()

    }

    private fun addPathParams(endpoint: String): String {

        var url = String.format("%s/tr:", endpoint)
        for (t in 0 until transformationList.size) {
            url = when {
                transformationList[t].contentEquals(":") -> String.format(
                    "%s%s",
                    url,
                    transformationList[t]
                )
                url.endsWith(":") -> String.format("%s%s", url, transformationList[t])
                else -> String.format("%s,%s", url, transformationList[t])
            }
        }

        return url
    }
}