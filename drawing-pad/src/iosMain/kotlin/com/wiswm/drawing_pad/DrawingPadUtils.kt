package com.wiswm.drawing_pad

import kotlinx.cinterop.BooleanVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreGraphics.CGLineCap
import platform.CoreGraphics.CGLineJoin
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSDate
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToFile
import platform.UIKit.UIBezierPath
import platform.UIKit.UIColor
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImagePNGRepresentation
import platform.UIKit.UIRectFill
import platform.UIKit.UIScreen
import kotlin.text.toDouble

actual class DrawingPadUtils {
    actual fun saveStrokesToPng(
        strokeList: List<DrawingPadViewModel.Stroke>,
        width: Int,
        height: Int
    ): String? {
        return save(strokeList, width, height)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun save(strokeList: List<DrawingPadViewModel.Stroke>, width: Int, height: Int): String? {
        return autoreleasepool {
            try {
                val size = CGSizeMake(width.toDouble(), height.toDouble())
                UIGraphicsBeginImageContextWithOptions(size, false, UIScreen.mainScreen.scale)

                UIColor.whiteColor.setFill()
                val rect = CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble())
                UIRectFill(rect)

                UIColor.blackColor.setStroke()

                strokeList.forEach { stroke ->
                    if (stroke.points.isNotEmpty()) {
                        val path = UIBezierPath.bezierPath()
                        path.lineWidth = stroke.strokeWidth.toDouble()
                        path.lineCapStyle = CGLineCap.kCGLineCapRound
                        path.lineJoinStyle = CGLineJoin.kCGLineJoinRound

                        val firstPoint = stroke.points.first()
                        path.moveToPoint(CGPointMake(firstPoint.x.toDouble(), firstPoint.y.toDouble()))

                        stroke.points.drop(1).forEach { point ->
                            path.addLineToPoint(CGPointMake(point.x.toDouble(), point.y.toDouble()))
                        }

                        path.stroke()
                    }
                }

                val image = UIGraphicsGetImageFromCurrentImageContext()
                UIGraphicsEndImageContext()

                image?.let {
                    val filePath = getDirectory()
                    filePath?.let {
                        val imageData = UIImagePNGRepresentation(image)
                        imageData?.writeToFile(filePath, true)

                        return@autoreleasepool filePath
                    }
                    return@autoreleasepool null
                }
                return@autoreleasepool null
            } catch (e: Exception) {
                return@autoreleasepool null
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun getDirectory(): String?{
        val dir = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory, NSUserDomainMask, true

        ).firstOrNull() as? String ?: return null
        val photosDir = "$dir/signatures"
        val fileManager = NSFileManager.defaultManager
        val timestamp = (NSDate().timeIntervalSince1970 * 1000).toLong()

        memScoped {
            val isDir = alloc<BooleanVar>()
            val exists = fileManager.fileExistsAtPath(photosDir, isDir.ptr)
            if (!exists || !isDir.value) {
                fileManager.createDirectoryAtPath(
                    path = photosDir,
                    withIntermediateDirectories = true,
                    attributes = null,
                    error = null
                )
            }
        }
        return "$photosDir/$timestamp.jpg"
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun deleteFolder(): Boolean {
        val dir = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        ).firstOrNull() as? String ?: return false

        val photosDir = "$dir/signatures"
        val fileManager = NSFileManager.defaultManager

        memScoped {
            val isDir = alloc<BooleanVar>()
            val exists = fileManager.fileExistsAtPath(photosDir, isDir.ptr)

            if (!exists || !isDir.value) return false
        }

        val errorPtr = memScoped { alloc<ObjCObjectVar<NSError?>>() }

        val success = fileManager.removeItemAtPath(
            path = photosDir,
            error = errorPtr.ptr
        )

        if (!success) {
            val err = errorPtr.value
            println("Failed to delete folder: ${err?.localizedDescription}")
        }

        return success
    }
}